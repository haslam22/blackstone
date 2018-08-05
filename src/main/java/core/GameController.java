package core;

import events.GameListener;
import players.Player;
import players.human.HumanPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Responsible for facilitating interaction between the game and the GUI. An
 * instance of this is sent down to all GUI controllers so that they can
 * perform certain actions on a game e.g. start/pause/undo.
 */
public class GameController {

    private static Logger LOGGER = Logger.getGlobal();

    private final List<GameListener> listeners;
    private final GameSettings settings;
    private GameThread gameThread;
    private GameState loadedState;
    private GameState currentState;

    /**
     * Create a new GameController.
     * @param settings Game settings
     */
    public GameController(GameSettings settings) {
        this.settings = settings;
        this.listeners = new ArrayList<>();
    }

    /**
     * Start the game. Reads the game settings and launches a new game thread.
     * Has no effect if the game thread is already running.
     */
    public void start() {
        if(this.gameThread == null || !this.gameThread.isAlive()) {
            listeners.forEach(listener -> listener.gameStarted());
            this.currentState = loadedState != null ? loadedState.clone() :
                    new GameState(settings.getSize());
            if(this.loadedState != null) {
                listeners.forEach(listener -> listener.positionLoaded(
                        loadedState.getMovesMade()));
            }
            this.gameThread = new GameThread(currentState, settings, listeners);
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
        }
    }

    /**
     * Undo the last two moves. This stops the game, removes the last two
     * moves from the game state and emits an event to let the board update.
     * The game is restarted immediately after.
     */
    public void undo() {
        if(this.currentState.getMovesMade().size() > 0) {
            this.stop();
            for (int i = 0; i < 2; i++) {
                Move move = currentState.undo();
                if (move != null) {
                    listeners.forEach(listener -> listener.moveRemoved(move));
                }
            }
            listeners.forEach(listener -> listener.gameResumed());
            // Create a new game thread, but pass in the times from the previous
            // instance to stop resetting of times.
            this.gameThread = new GameThread(currentState, settings,
                    listeners, new long[] {
                            gameThread.getGameTime(1),
                            gameThread.getGameTime(2)
            });
            gameThread.start();
        }
    }

    /**
     * Get the game settings.
     * @return GameSettings instance
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
     * Called by the GUI to set a user's move for the game.
     * @param move Move from the user
     * @return True if the move was accepted
     */
    public boolean setUserMove(Move move) {
        Player currentPlayer = gameThread.getCurrentPlayer();
        if(currentPlayer instanceof HumanPlayer) {
            if(!currentState.getMovesMade().contains(move)) {
                synchronized(currentPlayer) {
                    ((HumanPlayer) currentPlayer).setMove(move);
                    currentPlayer.notify();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Load in a state to use for this game.
     * @param state GameState object to use (can be null)
     */
    public void setLoadedState(GameState state) {
        if(this.gameThread.isAlive()) {
            this.stop();
        }
        this.loadedState = state;
        if(state != null) {
            settings.setSize(loadedState.getSize());
            listeners.forEach(listener -> listener.positionLoaded(
                    state.getMovesMade()));
        } else {
            listeners.forEach(listener -> listener.positionCleared());
        }
    }

    /**
     * @return Copy of the current game state.
     */
    public GameState getState() {
        return this.currentState.clone();
    }

    /**
     * Get the game time remaining for a player.
     * @param playerIndex Player identifier (1/2)
     * @return Game time left, in milliseconds
     */
    public long getGameTime(int playerIndex) {
        return this.gameThread.getGameTime(playerIndex);
    }
}
