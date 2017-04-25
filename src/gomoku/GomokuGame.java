package gomoku;

import gui.GomokuApplication;
import gui.GomokuBoardPanel;
import static gui.GomokuBoardPanel.StoneColor.BLACK;
import static gui.GomokuBoardPanel.StoneColor.WHITE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private MouseAdapter listener;
    private HumanPlayer listeningPlayer;
    private boolean interrupted;
    
    /**
     * Create a new game between two players.
     * @param app App instance
     * @param intersections Game intersections
     * @param player1 Player 1 string identifier
     * @param player2 Player 2 string identifier
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
    
    /**
     * Start the game loop, which will continue to request moves until the
     * game is over or stop() is called.
     * @param app
     */
    public void start(GomokuApplication app) {
        gameThread.start();
    }
    
    /**
     * Safely interrupt the game, notifies all players to stop calculating
     * moves and removes all listeners.
     * @param app
     */
    public void stop(GomokuApplication app) {
        // Set flag for the game thread to stop after the next move is returned
        interrupted = true;
        // Set flags to inform players to stop
        players[0].interrupted = true;
        players[1].interrupted = true;
        // Disable listener
        if(listener != null) {
            synchronized(listeningPlayer) {
                listeningPlayer.notify();
            }
        }
    }
    
    /**
     * Create a player corresponding to a string identifier, assigning it an
     * index (black or white, 1/2).
     * @param name Name of the player
     * @param playerIndex Index the player (1/2)
     * @param opponentIndex Index of the opponent (1/2)
     * @return
     */
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
    
    /**
     * Add a new line to the game log on the interface.
     * @param text String to append to the log as a new line
     */
    public void writeLog(String text) {
        app.writeLog(text);
    }
    
    /**
     * Add a board listener for a Human player, which will return a move when
     * a valid click is detected on the board panel.
     * @param player HumanPlayer instance
     */
    public void addBoardListener(HumanPlayer player) {
        board.enableStonePicker(player.getPlayerIndex() == 1? BLACK : WHITE);
        this.listener = new MouseAdapter() {
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
                    listeningPlayer = null;
                    listener = null;
                }
            }
        };
        this.listeningPlayer = player;
        board.addMouseListener(listener);
    }
    
    /**
     * Get the board size (number of intersections) for this game.
     * @return
     */
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
    
    /**
     * Convert a board row to its board representation (15, 14, 13, 12..)
     * @param row
     * @return
     */
    private int convertRow(int row) {
        return this.intersections - row;
    }
    
    /**
     * Convert a board column to its board representation (A, B, C, D...)
     * @param col
     * @return
     */
    private String convertCol(int col) {
        return String.valueOf((char)((col + 1) + 'A' - 1));
    }
    
    private Thread createGame() {
        return new Thread(new Runnable(){
            @Override
            public void run(){
                board.reset();
                app.clearLog();

                // Start the game loop, keep requesting moves if the game is alive
                while(state.terminal() == 0) {
                    app.updateStatus("Waiting for move from player " + 
                            state.getCurrentIndex() + "...");
                    try {
                        // Pass a copy of the state and request a move
                        GomokuMove move = players[state.getCurrentIndex() - 1]
                                .getMove(state);
                        if(interrupted) break;
                        draw(move.row, move.col, state.getCurrentIndex());
                        writeLog("Player " + state.getCurrentIndex() + " move: "
                        + convertRow(move.row) + convertCol(move.col));
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
