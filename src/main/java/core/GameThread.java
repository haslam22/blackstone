package core;

import events.GameListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import players.Player;
import players.human.HumanPlayer;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Responsible for running a Gomoku game from start to finish.
 *
 * @see core.GameSettings
 * @see events.GameListener
 */
public class GameThread extends Thread {

    private static final Logger LOGGER =
            LogManager.getLogger(GameThread.class.getName());

    private final GameSettings settings;
    private final GameState state;
    private final List<GameListener> listeners;
    private final Player[] players;
    private final long[] times;
    private Future<Move> pendingMove;
    private boolean[] sendBoard;

    /**
     * Initialize a new game thread.
     * @param state State to use
     * @param settings Game settings
     * @param listeners Listeners to receive game events
     * @param times Game timeouts for each player
     */
    public GameThread(GameState state, GameSettings settings, Player player1,
                      Player player2, List<GameListener> listeners,
                      long[] times) {
        this.state = state;
        this.settings = settings;
        this.listeners = listeners;
        this.players = new Player[]{ player1, player2 };
        this.times = times;
    }

    /**
     * Initialize a new game thread, setting game timeouts to the values
     * specified in settings.
     * @param state State to use
     * @param settings Game settings
     * @param listeners Listeners to receive game events
     */
    public GameThread(GameState state, GameSettings settings, Player player1,
                      Player player2, List<GameListener> listeners) {
        this(state, settings, player1, player2, listeners, new long[] {
                settings.getGameTimeMillis(),
                settings.getGameTimeMillis()
        });
    }

    /**
     * Get the current player instance making a move.
     * @return Player instance
     */
    public Player getCurrentPlayer() {
        return players[state.getCurrentIndex() - 1];
    }

    /**
     * Get the up to date game time for a player.
     * @param playerIndex Player identifier (1/2)
     * @return Game time in milliseconds
     */
    public long getGameTime(int playerIndex) {
        return times[playerIndex - 1];
    }

    @Override
    public void run() {
        // Send settings to players.
        players[0].setupGame(1, settings.getSize(),
                settings.getMoveTimeMillis(), settings.getGameTimeMillis());
        players[1].setupGame(2, settings.getSize(),
                settings.getMoveTimeMillis(), settings.getGameTimeMillis());

        // We've started this thread from a non-empty state. Set this so we
        // know when to send the board to the players.
        this.sendBoard = new boolean[] { !state.isEmpty(), !state.isEmpty() };
        while(state.terminal() == 0) {
            try {
                // Notify listeners that a turn has started.
                listeners.forEach(listener -> listener.turnStarted(
                        state.getCurrentIndex()));

                // Get the move from the current player and time execution.
                long startTime = System.currentTimeMillis();
                Move move = requestMove(state.getCurrentIndex());
                long elapsedTime = System.currentTimeMillis() - startTime;

                // Check for an invalid move.
                if(state.getMovesMade().contains(move)) {
                    LOGGER.error(
                            MessageFormat.format(Strings.INVALID_MOVE,
                            state.getCurrentIndex(),
                            move.getAlgebraicString(state.getSize())));
                    return;
                }

                LOGGER.info(
                        MessageFormat.format(Strings.MOVE_MESSAGE,
                                state.getCurrentIndex(),
                                move.getAlgebraicString(state.getSize()),
                                move.row,
                                move.col,
                                elapsedTime));

                // Subtract elapsed time from the current player.
                times[state.getCurrentIndex() - 1] -= elapsedTime;
                // Notify listeners of the new move.
                listeners.forEach(listener -> listener.moveAdded(
                        state.getCurrentIndex(), move));
                // Update our internal state of the game with the new move.
                state.makeMove(move);

            } catch (InterruptedException ex) {
                // An interrupt from the user.
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                break;
            } catch (ExecutionException ex) {
                // Failed execution from the player.
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                LOGGER.error(MessageFormat.format(Strings.FAILED_MOVE,
                                state.getCurrentIndex()), ex);
                break;
            } catch (TimeoutException ex) {
                // Player ran out of time.
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                LOGGER.error(MessageFormat.format(
                        Strings.TIMEOUT_MESSAGE, state.getCurrentIndex()));
                break;
            }
        }
        listeners.forEach(listener -> listener.gameFinished());
        if(state.terminal() != 0) {
            LOGGER.error(MessageFormat.format(Strings.WINNER_MESSAGE,
                            state.terminal()));
        }
        players[0].cleanup();
        players[1].cleanup();
    }

    /**
     * Request a move from a player.
     * @return Players move
     * @throws ExecutionException Player execution failed.
     * @throws InterruptedException Game interrupted by the user.
     * @throws TimeoutException Player timed out.
     */
    private Move requestMove(int playerIndex) throws
            InterruptedException, ExecutionException, TimeoutException {
        Player player = players[playerIndex - 1];
        // Run the execution of getMove() on a new thread so we have more
        // control over the operation.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Request the move from the player. If no moves exist, ask the
        // player to open the game.
        if(!state.getMovesMade().isEmpty()) {
            if(sendBoard[playerIndex - 1]) {
                this.pendingMove = executor.submit(() -> player.loadBoard(
                        state.getMovesMade(), times[playerIndex - 1]));
                // Don't send the board again.
                sendBoard[playerIndex - 1] = false;
            } else {
                this.pendingMove = executor.submit(() -> player.getMove(
                        state.getLastMove(), times[playerIndex - 1]));
            }
        } else {
            this.pendingMove = executor.submit(() -> player.beginGame(times[playerIndex - 1]));
        }
        if(player instanceof HumanPlayer) {
            listeners.forEach(listener -> listener.userMoveRequested
                    (playerIndex));
        }

        // Allow 100ms breathing room on top of the regular timeout.
        long timeout = calculateTimeoutMillis(playerIndex) + 100;

        if (timeout > 0) {
            try {
                // We've submitted the job, now get the result with a timeout.
                return pendingMove.get(timeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException ex) {
                pendingMove.cancel(true);
                throw(ex);
            }
        } else {
            // No timing enabled.
            return pendingMove.get();
        }
    }

    /**
     * Calculate the timeout value for a player or return 0 if timing is not
     * enabled for this game.
     * @param player Player index
     * @return Timeout value in milliseconds
     */
    private long calculateTimeoutMillis(int player) {
        if(settings.moveTimingEnabled() && settings.gameTimingEnabled()) {
            // Both move timing and game timing are enabled
            return Math.min(settings.getMoveTimeMillis(), times[player - 1]);
        } else if(settings.gameTimingEnabled()) {
            // Only game timing is enabled
            return times[player - 1];
        } else if(settings.moveTimingEnabled()) {
            // Only move timing is enabled
            return settings.getMoveTimeMillis();
        } else {
            // No timing is enabled
            return 0;
        }
    }
}
