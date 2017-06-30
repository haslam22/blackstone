package core;

import gui.BoardPane;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import players.Player;
import players.human.HumanPlayer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game loop for the program, asynchronously requests moves from each
 * player and draws moves to the board.
 */
public class Game implements Runnable {

    private final Player[] players;
    private final GameState state;
    private final ExecutorService executor;
    private final BoardPane board;
    private final GameManager manager;
    private final GameTimer[] gameTimers;
    private final boolean moveTimingEnabled;
    private final boolean gameTimingEnabled;
    private Future<Move> futureMove;
    private EventHandler<MouseEvent> moveListener;
    private Timer updateSender;

    /**
     * Create a new game instance
     * @param manager Manager object to control this game
     * @param board Board pane
     * @param player1 Player 1 (Black)
     * @param player2 Player 2 (White)
     * @param intersections Board size (in intersections)
     * @param moveTime Time per move (ms)
     * @param gameTime Time per game (ms)
     * @param gameTimingEnabled Game timing enabled
     * @param moveTimingEnabled Move timing enabled
     */
    protected Game(GameManager manager, BoardPane board, Player player1, Player
            player2, int intersections, int moveTime, int gameTime, boolean
            moveTimingEnabled, boolean gameTimingEnabled) {
        this.board = board;
        this.manager = manager;
        this.players = new Player[] { player1, player2 };
        this.state = new GameState(intersections);
        this.executor = Executors.newSingleThreadExecutor();
        this.moveTimingEnabled = moveTimingEnabled;
        this.gameTimingEnabled = gameTimingEnabled;
        this.gameTimers = new GameTimer[] {
                new GameTimer(gameTime, moveTime, gameTimingEnabled,
                        moveTimingEnabled),
                new GameTimer(gameTime, moveTime, gameTimingEnabled,
                        moveTimingEnabled)
        };
        this.manager.addListener(new GameEventAdapter(){
            @Override
            public void undo() {
                if(futureMove != null) {
                    futureMove.cancel(true);
                }
            }
        });
    }

    /**
     * Schedule a game time update task to fire a gameTimeChanged() update to
     * any listening components
     * @param player Player who is currently moving
     */
    private void sendGameTimeUpdates(int player) {
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                manager.gameTimeChanged(player, gameTimers[player - 1]
                        .getRemainingGameTime());
            }
        };
        updateSender.scheduleAtFixedRate(updateTask, 0, 100);
    }

    /**
     * Schedule a move time update task to fire a moveTimeChanged() update to
     * any listening components
     * @param player Player who is currently moving
     */
    private void sendMoveTimeUpdates(int player) {
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                manager.moveTimeChanged(player, gameTimers[player - 1]
                        .getRemainingMoveTime());
            }
        };
        updateSender.scheduleAtFixedRate(updateTask, 0, 100);
    }

    /**
     * Add a mouse click listener to the board if we need to request a move
     * from the user, set and call notify() on the HumanPlayer instance upon
     * completion
     * @param player HumanPlayer instance
     */
    private void addMoveListener(HumanPlayer player) {
        this.moveListener = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int closestRow = board.getClosestRow(event.getY());
                int closestCol = board.getClosestCol(event.getX());
                Move closestMove = new Move(closestRow, closestCol);
                if(state.isLegalMove(closestMove)) {
                    synchronized(player) {
                        player.setMove(closestMove);
                        player.notify();
                    }
                    board.removeEventHandler(MouseEvent.MOUSE_CLICKED,
                            this);
                    board.disableStonePicker();
                }
            }
        };
        board.enableStonePicker(state.getCurrentIndex());
        board.addEventHandler(MouseEvent.MOUSE_CLICKED, moveListener);
    }

    /**
     * Request a move from a player asynchronously
     * @param player Player to request from
     * @return Players move
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private Move requestMove(Player player) throws ExecutionException,
            InterruptedException, TimeoutException, CancellationException {
        // Setup the mouse listener if we're requesting a move from a non-AI
        // player
        if(player instanceof HumanPlayer) {
            addMoveListener((HumanPlayer) player);
        }

        int index = state.getCurrentIndex();
        long timeout = gameTimers[index - 1].getTimeout();
        this.futureMove = executor.submit(() -> player.getMove(state));

        if(timeout > 0) {
            return futureMove.get(timeout, TimeUnit.MILLISECONDS);
        } else {
            return futureMove.get();
        }
    }

    /**
     * Draw a move on the board pane for the current player
     * @param move Move to draw
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void drawStone(Move move) throws InterruptedException,
            ExecutionException {
        FutureTask<Void> drawStoneTask = new FutureTask<>(() -> {
            board.addStone(state.getCurrentIndex(), move.getRow(),
                    move.getCol(), false);
        }, null);
        Platform.runLater(drawStoneTask);
        drawStoneTask.get();
    }

    /**
     * Remove a stone from the board
     * @param move Move to remove
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void removeStone(Move move) throws InterruptedException,
            ExecutionException {
        FutureTask<Void> removeStoneTask = new FutureTask<>(() -> {
            board.removeStone(move.getRow(), move.getCol());
        }, null);
        Platform.runLater(removeStoneTask);
        removeStoneTask.get();
    }

    /**
     * Clear the board completely, removing all stones.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void clearBoard() throws ExecutionException, InterruptedException {
        FutureTask<Void> clearBoardTask = new FutureTask<>(() -> {
            board.clear();
        }, null);
        Platform.runLater(clearBoardTask);
        clearBoardTask.get();
    }

    /**
     * Disable the stone picker and remove the mouse listener from the board.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void disableListener() throws ExecutionException,
            InterruptedException {
        FutureTask<Void> removeListenerTask = new FutureTask<>(() -> {
            board.disableStonePicker();
        }, null);
        Platform.runLater(removeListenerTask);
        removeListenerTask.get();
        if(moveListener != null) {
            board.removeEventHandler(MouseEvent.MOUSE_CLICKED, moveListener);
            this.moveListener = null;
        }
    }

    /**
     * Perform an undo on the current game state, removing stones from the
     * board and updating the state
     */
    private void undo() {
        Move move1 = state.undo();
        Move move2 = state.undo();
        try {
            if(move1 != null) removeStone(move1);
            if(move2 != null) removeStone(move2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            clearBoard();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // Continue to request moves until the state is terminal
        while(state.terminal() == 0) try {
            // Send a turn changed event
            manager.turn(state.getCurrentIndex());

            // Start the timer for the current player
            gameTimers[state.getCurrentIndex() - 1].startTimer();

            // Send time events to the GUI
            this.updateSender = new Timer();
            if(gameTimingEnabled) sendGameTimeUpdates(state.getCurrentIndex());
            if(moveTimingEnabled) sendMoveTimeUpdates(state.getCurrentIndex());

            // Request the next move asynchronously and wait
            Move nextMove = requestMove(players[state.getCurrentIndex() - 1]);

            // Stop the timer and cancel any event updates
            gameTimers[state.getCurrentIndex() - 1].stopTimer();
            this.updateSender.cancel();

            // Draw the stone, update the internal states and output the move to
            // the log
            drawStone(nextMove);
            Logger.getGlobal().log(Level.INFO, MessageConstants
                    .playerMoved(state.getCurrentIndex(), nextMove));
            state.makeMove(nextMove);

        } catch (ExecutionException e) {
            // Execution exception should not occur. Means a player instance
            // has thrown an unhandled exception somewhere
            gameTimers[state.getCurrentIndex() - 1].stopTimer();
            this.updateSender.cancel();
            e.printStackTrace();
            break;
        } catch (InterruptedException e) {
            // Game was stopped by the user
            gameTimers[state.getCurrentIndex() - 1].stopTimer();
            this.updateSender.cancel();
            Logger.getGlobal().log(Level.INFO, MessageConstants
                    .gameInterrupted());
            break;
        } catch (TimeoutException e) {
            // Player ran out of time, async requestMove() wasn't returned
            gameTimers[state.getCurrentIndex() - 1].stopTimer();
            this.updateSender.cancel();
            Logger.getGlobal().log(Level.INFO, MessageConstants
                    .timeout(state.getCurrentIndex()));
            break;
        } catch (CancellationException e) {
            // Player requested an undo
            gameTimers[state.getCurrentIndex() - 1].stopTimer();
            this.updateSender.cancel();
            try {
                undo();
                disableListener();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        // Output the winner to the log
        if(state.terminal() == 1 || state.terminal() == 2) {
            Logger.getGlobal().log(Level.INFO, MessageConstants.gameOver
                    (state.terminal()));
        }

        // Cleanup and shutdown
        try {
            disableListener();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
        manager.stopGame();
    }
}
