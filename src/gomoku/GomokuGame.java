package gomoku;

import gui.GomokuApplication;
import gui.GomokuBoardPanel;
import static gui.GomokuBoardPanel.StoneColor.BLACK;
import static gui.GomokuBoardPanel.StoneColor.WHITE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import players.GomokuPlayer;
import players.human.HumanPlayer;
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
    private final int time;
    
    // Status strings used to display the game status on the GUI
    private static final String STATUS_INTERRUPTED = "Status: Game interrupted"
            + " by user.";
    private static final String STATUS_EXECUTION_ERROR = "Player %d (%s) "
            + "failed to return a move due to an error, exiting.";
    private static final String STATUS_CURRENT_PLAYER = "Status: Waiting for "
            + "move from player %d ...";
    private static final String STATUS_WINNER = "Status: Game over. Player %d "
            + "wins.";
    
    /**
     * Create a new game between two players.
     * @param app App instance
     * @param intersections Game intersections
     * @param player1 Player 1 string identifier
     * @param player2 Player 2 string identifier
     * @param time Time limit for AI
     */
    public GomokuGame(GomokuApplication app, int intersections,
            String player1, String player2, int time) {
        this.time = time;
        this.app = app;
        this.board = app.getBoardPanel();
        this.state = new GomokuState(intersections);
        this.intersections = intersections;
        this.players = new GomokuPlayer[] { 
            createPlayer(player1, 1, 2), 
            createPlayer(player2, 2, 1) 
        };
        this.gameThread = createGameThread();
    }
    
    /**
     * Start the game loop, which will continue to request moves until the
     * game is over or stop() is called.
     * @param app
     */
    public void start(GomokuApplication app) {
        board.reset();
        gameThread.start();
    }
    
    /**
     * Safely interrupt the game.
     * @param app
     */
    public void stop(GomokuApplication app) {
        gameThread.interrupt();
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
            case "AI":
                return new MinimaxPlayer(this, playerIndex, opponentIndex,
                        time);
            default:
                return null;
        }
    }
    
    /**
     * Get the board size (number of intersections) for this game.
     * @return
     */
    public int getIntersections() {
        return intersections;
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
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the nearest intersection to where the user clicked
                int row = board.getNearestRow(e.getY());
                int col = board.getNearestCol(e.getX());
                if(row >= 0 && col >= 0) {
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
            }
        };
        board.addMouseListener(listener);
    }
    
    /**
     * Draw a stone for a player on the board panel at a given row/col location
     * @param row Intersection row
     * @param col Intersection col
     * @param index Index of the player
     */
    private void drawMove(GomokuMove move, int index) {
        switch(index) {
            case 1:
                board.addStone(BLACK, move.row, move.col);
                break;
            case 2:
                board.addStone(WHITE, move.row, move.col);
                break;
        }
    }
    
    /**
     * Write a move to the log.
     * @param move Move to write
     * @param index Player who made the move
     */
    private void writeMove(GomokuMove move, int index) {
        String moveStr = this.intersections - move.row + String.valueOf((char)
                ((move.col + 1) + 'A' - 1));
        writeLog("Player " + index + " move: " + moveStr);
    }
    
    /**
     * Asynchronously grab a move from a player instance for the current state.
     * @param player
     * @return Move returned by the player
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private GomokuMove getMove(GomokuPlayer player) throws InterruptedException, 
            ExecutionException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<GomokuMove> callable = new Callable<GomokuMove>() {
            @Override
            public GomokuMove call() {
                return player.getMove(state);
            }
        };
        Future<GomokuMove> future = executor.submit(callable);
        executor.shutdown();
        
        try {
            return future.get();
        } catch(InterruptedException | ExecutionException ex) {
            future.cancel(true);
            throw(ex);
        }
    }
    
    private Thread createGameThread() {
        return new Thread() {
            @Override
            public void run() {
                app.clearLog();
                while(state.terminal() == 0) {
                    GomokuMove move;
                    try {
                        app.updateStatus(String.format(STATUS_CURRENT_PLAYER,
                                state.getCurrentIndex()));
                        move = getMove(players[state.getCurrentIndex() - 1]);
                        drawMove(move, state.getCurrentIndex());
                        writeMove(move, state.getCurrentIndex());
                        state.makeMove(move);
                    } catch (ExecutionException ex) {
                        board.disableStonePicker();
                        ex.printStackTrace();
                        writeLog(String.format(STATUS_EXECUTION_ERROR, 
                                state.getCurrentIndex(),
                                players[state.getCurrentIndex() - 1]));
                        break;
                    } catch (InterruptedException ex) {
                        board.disableStonePicker();
                        app.updateStatus(STATUS_INTERRUPTED);
                        break;
                    }
                }
                int terminal = state.terminal();
                switch(terminal) {
                    case 1:
                        app.updateStatus(String.format(STATUS_WINNER, 1));
                        break;
                    case 2:
                        app.updateStatus(String.format(STATUS_WINNER, 2));
                        break;
                }
                app.forfeit();
            }
        };
    }
}
