package gomoku;

import gui.GomokuApplication;
import gui.GomokuBoardPanel;
import static gui.GomokuBoardPanel.StoneColor.BLACK;
import static gui.GomokuBoardPanel.StoneColor.WHITE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import players.GomokuPlayer;
import players.HumanPlayer;
import players.minimax.MinimaxPlayer;

/**
 * This class manages a game between two players, calling getMove() on
 * each player until the game ends.
 * @author Hassan
 */
public class GomokuGame {
    
    private final GomokuState state;
    private final GomokuPlayer[] players;
    private final GomokuBoardPanel board;
    private final GomokuApplication app;
    private final int intersections;
    private final Thread gameThread;
    
    /**
     * Create a new game between two players.
     * @param app App instance
     * @param intersections Game intersections
     * @param player1 Player 1
     * @param player2 Player 2
     */
    public GomokuGame(GomokuApplication app, int intersections,
            String player1, String player2) {
        this.app = app;
        this.board = app.getBoardPanel();
        this.state = new GomokuState(intersections);
        this.intersections = intersections;
        this.players = new GomokuPlayer[] { 
            createPlayer(player1, 1, 2), 
            createPlayer(player2, 2, 1) 
        };
        this.gameThread = createGame();
    }
    
    public void start(GomokuApplication app) {
        gameThread.start();
    }
    
    public void stop(GomokuApplication app) {
        gameThread.interrupt();
    }
    
    private GomokuPlayer createPlayer(String name, 
            int playerIndex, int opponentIndex) {
        switch(name) {
            case "Human":
                return new HumanPlayer(this, playerIndex, opponentIndex);
            case "Minimax":
                return new MinimaxPlayer(this, playerIndex, opponentIndex);
            default:
                return null;
        }
    }
    
    public void writeLog(String text) {
        app.writeLog(text);
    }
    
    public void addBoardListener(HumanPlayer player) {
        board.enableStonePicker(player.getPlayerIndex() == 1? BLACK : WHITE);
        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the nearest intersection to where the user clicked
                int row = board.getNearestRow(e.getY());
                int col = board.getNearestCol(e.getX());
                if(state.getIntersectionIndex(row, col) == 0) {
                    synchronized(player) {
                        // User clicked on a valid move, wake the thread up
                        player.move = new GomokuMove(row, col);
                        player.notify();
                    }
                    board.removeMouseListener(this);
                    board.disableStonePicker();
                }
            }
        });
    }
    
    public int getIntersections() {
        return intersections;
    }
    
    /**
     * Draw a stone for a player on the board panel at a given row/col location
     * @param row Intersection row
     * @param col Intersection col
     * @param index Index of the player
     */
    private void draw(int row, int col, int index) {
        switch(index) {
            case 1:
                board.addStone(BLACK, row, col);
                break;
            case 2:
                board.addStone(WHITE, row, col);
                break;
        }
    }
    
    private Thread createGame() {
        return new Thread(new Runnable(){
            @Override
            public void run(){
                board.reset();

                // Start the game loop, keep requesting moves if the game is alive
                while(state.terminal() == 0 && !Thread.interrupted()) {
                    app.updateStatus("Waiting for move from player " + 
                            state.getCurrentIndex() + "...");
                    try {
                        // Pass a copy of the state and request a move
                        GomokuMove move = players[state.getCurrentIndex() - 1]
                                .getMove(state);
                        draw(move.row, move.col, state.getCurrentIndex());
                        state.makeMove(move);
                    } catch (NullPointerException e) {
                        // A move wasn't returned, exit
                        break;
                    }
                }

                int terminal = state.terminal();

                switch (terminal) {
                    case 1:
                    case 2:
                        app.updateStatus("Game over. Winner: Player " + terminal);
                        break;
                    case 3:
                        app.updateStatus("Game over. Winner: N/A (Draw)");
                        break;
                    case 0:
                        app.updateStatus("Game over. Winner: N/A (Interrupted)");
                        break;
                }

                // Game has finished, cleanup
                board.repaint();
                board.disableStonePicker();
                app.forfeit();
            }
        });
    }
}
