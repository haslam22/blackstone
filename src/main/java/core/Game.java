package core;

import events.GameListener;
import events.SettingsListener;
import players.Player;
import players.human.HumanPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game loop responsible for running the game from start to finish.
 */
public class Game {

    private static Logger LOGGER = Logger.getGlobal();

    private final List<GameListener> listeners;
    private final GameSettings settings;
    private final ExecutorService executor;
    private final Player[] players;
    private final long[] gameTimes;
    private final Timer timer;
    private Future<Move> futureMove;
    private Thread gameThread;
    private TimerTask timeUpdateSender;
    private GameState state;
    private GameState loadedState;

    /**
     * Create a new game instance.
     */
    public Game(GameSettings settings) {
        this.settings = settings;
        this.state = new GameState(settings.getSize());
        this.gameTimes = new long[2];
        this.players = new Player[2];
        this.executor = Executors.newSingleThreadExecutor();
        this.listeners = new ArrayList<>();
        this.gameThread = new Thread(getRunnable());
        this.timer = new Timer();
        this.settings.addListener(new SettingsListener() {
            @Override
            public void settingsChanged() {
                // State is no longer valid if settings change
                // TODO: Only invalidate state if size changes
                Game.this.state = new GameState(settings.getSize());
            }
        });
    }

    /**
     * Start the game. Reads the game settings and launches a new game thread.
     * Has no effect if the game thread is already running.
     */
    public void start() {
        if(!this.gameThread.isAlive()) {
            listeners.forEach(listener -> listener.gameStarted());
            if(this.loadedState != null) {
                this.state = loadedState.clone();
                listeners.forEach(listener -> listener.positionLoaded(
                        loadedState.getMovesMade()));
            } else {
                this.state = new GameState(settings.getSize());
            }
            players[0] = settings.getPlayer1();
            players[1] = settings.getPlayer2();
            gameTimes[0] = settings.getGameTimeMillis();
            gameTimes[1] = settings.getGameTimeMillis();
            this.gameThread = new Thread(getRunnable());
            this.gameThread.start();
        }
    }

    /**
     * Stop the game. Safely interrupts the thread and cancels any pending
     * moves and calls join() to wait for the thread to resolve. Has no
     * effect if the game thread is not running.
     */
    public void stop() {
        if(this.gameThread.isAlive()) {
            this.gameThread.interrupt();
            try {
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!futureMove.isDone()) {
                futureMove.cancel(true);
            }
            timeUpdateSender.cancel();
        }
    }

    /**
     * Undo the last two moves. This stops the game, removes the last two
     * moves from the game state and emits an event to let the board update.
     * The game is restarted immediately after.
     */
    public void undo() {
        if(state.getMovesMade().size() > 0) {
            stop();
            for (int i = 0; i < 2; i++) {
                Move move = state.undo();
                if (move != null) {
                    listeners.forEach(listener -> listener.moveRemoved(move));
                }
            }
            this.gameThread = new Thread(getRunnable());
            this.gameThread.start();
            listeners.forEach(listener -> listener.gameResumed());
        }
    }

    /**
     * Get the game settings.
     * @retun GameSettings instance
     */
    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Register a listener with this game instance.
     * @param listener GameListener to register
     */
    public void addListener(GameListener listener) {
        this.listeners.add(listener);
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
        this.futureMove = executor.submit(() -> player.getMove(state.clone()));
        if(player instanceof HumanPlayer) {
            listeners.forEach(listener -> listener.userMoveRequested
                    (playerIndex));
        }

        long timeout = calculateTimeoutMillis(playerIndex);

        if (timeout > 0) {
            try {
                return futureMove.get(timeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException ex) {
                futureMove.cancel(true);
                throw(ex);
            }
        } else {
            return futureMove.get();
        }
    }

    /**
     * Called by the GUI to set a user's move for the game.
     * @param move Move from the user
     * @return True if the move was accepted
     */
    public boolean setUserMove(Move move) {
        Player currentPlayer = players[state.getCurrentIndex() - 1];
        if(currentPlayer instanceof HumanPlayer) {
            if(!state.getMovesMade().contains(move)) {
                synchronized(currentPlayer) {
                    ((HumanPlayer) currentPlayer).setMove(move);
                    players[state.getCurrentIndex() - 1].notify();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Load in a state to use for this game.
     * @param state GameState object to use
     */
    public void setLoadedState(GameState state) {
        if(this.gameThread.isAlive()) {
            this.stop();
        }
        this.loadedState = state;
        settings.setSize(loadedState.getSize());
        listeners.forEach(listener -> listener.positionLoaded(
                state.getMovesMade()));
    }

    /**
     * Clear the loaded state.
     */
    public void clearLoadedState() {
        this.loadedState = null;
        this.state = new GameState(settings.getSize());
        listeners.forEach(listener -> listener.positionLoaded(
                state.getMovesMade()));
    }

    /**
     * @return Current game state
     */
    public GameState getState() {
        return this.state.clone();
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
            return Math.min(settings.getMoveTimeMillis(), gameTimes[player - 1]);
        } else if(settings.gameTimingEnabled()) {
            // Only game timing is enabled
            return gameTimes[player - 1];
        } else if(settings.moveTimingEnabled()) {
            // Only move timing is enabled
            return settings.getMoveTimeMillis();
        } else {
            // No timing is enabled
            return 0;
        }
    }

    /**
     * Get the runnable game loop for this game.
     */
    private Runnable getRunnable() {
        return () -> {
            while(state.terminal() == 0) {
                try {
                    listeners.forEach(listener -> listener.turnStarted(
                            state.getCurrentIndex()));

                    sendTimeUpdates(state.getCurrentIndex());
                    long startTime = System.currentTimeMillis();

                    Move move = requestMove(state.getCurrentIndex());

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    stopTimeUpdates();

                    LOGGER.log(Level.INFO, getMoveMessage(move, state
                            .getCurrentIndex(), settings.getSize()));

                    gameTimes[state.getCurrentIndex() - 1] -= elapsedTime;
                    listeners.forEach(listener -> listener.moveAdded(
                            state.getCurrentIndex(), move));
                    state.makeMove(move);


                } catch (InterruptedException ex) {
                    stopTimeUpdates();
                    break;
                } catch (ExecutionException ex) {
                    stopTimeUpdates();
                    ex.printStackTrace();
                    break;
                } catch (TimeoutException ex) {
                    stopTimeUpdates();
                    LOGGER.log(Level.INFO,
                            getTimeoutMessage(state.getCurrentIndex()));
                    break;
                }
            }
            listeners.forEach(listener -> listener.gameFinished());
            if(state.terminal() != 0) {
                LOGGER.log(Level.INFO, getGameWinnerMessage(state.terminal()));
            }
        };
    }

    /**
     * Start sending time events to listeners.
     * @param playerIndex Player index to send times for
     */
    private void sendTimeUpdates(int playerIndex) {
        this.timeUpdateSender = new TimerTask() {
            long startTime = System.currentTimeMillis();
            long moveTime = settings.getMoveTimeMillis();
            long gameTime = gameTimes[playerIndex - 1];
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                gameTime -= elapsed;
                moveTime -= elapsed;
                // Broadcast the elapsed times since the last TimerTask
                if(settings.gameTimingEnabled()) {
                    listeners.forEach(listener -> listener.gameTimeChanged
                            (playerIndex, gameTime));
                }
                if(settings.moveTimingEnabled()) {
                    listeners.forEach(listener -> listener.moveTimeChanged
                            (playerIndex, moveTime));
                }
                startTime = System.currentTimeMillis();
            }
        };
        timer.scheduleAtFixedRate(timeUpdateSender, 0, 100);
    }

    /**
     * Stop sending time updates.
     */
    private void stopTimeUpdates() {
        timeUpdateSender.cancel();
    }

    private static String getGameWinnerMessage(int index) {
        return String.format("Game over, winner: Player %d.", index);
    }

    private static String getTimeoutMessage(int index) {
        return String.format("Player %d ran out of time.", index);
    }

    private static String getMoveMessage(Move move, int index, int boardSize) {
        return String.format("Player %d move: %s", index,
                move.getAlgebraicString(boardSize));
    }
}
