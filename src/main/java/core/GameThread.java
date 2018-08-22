package core;

import events.GameListener;
import players.Player;
import players.human.HumanPlayer;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for running a Gomoku game from start to finish, given a
 * GameSettings instance determining the players, timing, etc.
 * Emits game updates to any listeners passed in.
 * @see core.GameSettings
 * @see events.GameListener
 */
public class GameThread extends Thread {

    private static final Logger LOGGER = Logger.getGlobal();

    private final GameSettings settings;
    private final GameState state;
    private final List<GameListener> listeners;
    private final Player[] players;
    private final long[] times;
    private Future<Move> pendingMove;

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
        while(state.terminal() == 0) {
            try {
                listeners.forEach(listener -> listener.turnStarted(
                        state.getCurrentIndex()));

                long startTime = System.currentTimeMillis();
                Move move = requestMove(state.getCurrentIndex());
                long elapsedTime = System.currentTimeMillis() - startTime;

                LOGGER.log(Level.INFO,
                        MessageFormat.format(Strings.MOVE_MESSAGE,
                                state.getCurrentIndex(),
                                move.getAlgebraicString(state.getSize())));

                times[state.getCurrentIndex() - 1] -= elapsedTime;
                listeners.forEach(listener -> listener.moveAdded(
                        state.getCurrentIndex(), move));
                state.makeMove(move);

            } catch (InterruptedException ex) {
                // An interrupt
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                break;
            } catch (ExecutionException ex) {
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                LOGGER.log(Level.SEVERE, MessageFormat.format(Strings.FAILED_MOVE,
                                state.getCurrentIndex()), ex);
                break;
            } catch (TimeoutException ex) {
                if(!pendingMove.isDone()) {
                    pendingMove.cancel(true);
                }
                LOGGER.log(Level.INFO, MessageFormat.format(
                        Strings.TIMEOUT_MESSAGE, state.getCurrentIndex()));
                break;
            }
        }
        listeners.forEach(listener -> listener.gameFinished());
        if(state.terminal() != 0) {
            LOGGER.log(Level.INFO, MessageFormat.format(Strings.WINNER_MESSAGE,
                            state.terminal()));
        }
    }

    /**
     * Request a move from a player.
     * @return Players move
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private Move requestMove(int playerIndex) throws
            InterruptedException, ExecutionException, TimeoutException {
        Player player = players[playerIndex - 1];
        ExecutorService executor = Executors.newSingleThreadExecutor();
        this.pendingMove = executor.submit(() -> player.getMove(state.clone()));
        if(player instanceof HumanPlayer) {
            listeners.forEach(listener -> listener.userMoveRequested
                    (playerIndex));
        }

        long timeout = calculateTimeoutMillis(playerIndex);

        if (timeout > 0) {
            try {
                return pendingMove.get(timeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException ex) {
                pendingMove.cancel(true);
                throw(ex);
            }
        } else {
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
