package haslam.blackstone.core;

import haslam.blackstone.events.GameListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import haslam.blackstone.players.Player;
import haslam.blackstone.players.PlayerRegistry;
import haslam.blackstone.players.human.HumanPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for facilitating interaction between the game and the GUI. An
 * instance of this is sent down to all GUI controllers so that they can
 * perform certain actions on a game e.g. start/pause/undo.
 */
public class GameController {
    private static final Logger LOGGER =
            LogManager.getLogger(GameController.class.getName());

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
            listeners.forEach(GameListener::gameStarted);
            this.currentState = loadedState != null ? loadedState.clone() :
                    new GameState(settings.getSize());
            if(this.loadedState != null) {
                listeners.forEach(listener -> listener.positionLoaded(
                        loadedState.getMovesMade()));
            }
            this.gameThread = new GameThread(currentState, settings,
                    settings.getPlayer1(), settings.getPlayer2(), listeners);
            this.gameThread.start();
        }
    }

    /**
     * Stop the game. Safely interrupts the thread and cancels any pending
     * moves and calls join() to wait for the thread to resolve. Has no
     * effect if the game thread is not running.
     */
    public void stop() {
        if(this.gameThread != null && this.gameThread.isAlive()) {
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
            listeners.forEach(GameListener::gameResumed);
            // Create a new game thread, but pass in the times from the previous
            // instance to stop resetting of times.
            this.gameThread = new GameThread(currentState, settings,
                    settings.getPlayer1(), settings.getPlayer2(),
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
        if(this.gameThread != null && this.gameThread.isAlive()) {
            this.stop();
        }
        this.loadedState = state;
        if(state != null) {
            settings.setSize(loadedState.getSize());
            listeners.forEach(listener -> listener.positionLoaded(
                    state.getMovesMade()));
        } else {
            listeners.forEach(GameListener::positionCleared);
        }
    }

    /**
     * @return Copy of the current game state, or a new state if no state is loaded.
     */
    public GameState getState() {
        return this.currentState != null ? this.currentState.clone() : new GameState(settings.getSize());
    }

    /**
     * Register an external AI.
     * @param file File path to external AI
     */
    public void addExternalPlayer(File file) {
        String[] splitFileName = file.getName().split("-", 2);
        if(!splitFileName[0].equalsIgnoreCase("pbrain")) {
            LOGGER.error("Could not load external AI. File name " +
                    "must follow the format: pbrain-<name>.exe");
        } else {
            // We've removed "pbrain", so now strip the extension and return the
            // string remaining to identify the AI
            // e.g. pbrain-name.exe -> pbrain | name.exe -> name
            String aiName = splitFileName[1].substring(0,
                    splitFileName[1].lastIndexOf('.'));
            String aiNameCapitalised = aiName.substring(0, 1).toUpperCase()
                    + aiName.substring(1);
            PlayerRegistry.addPiskvorkPlayer(aiNameCapitalised, file.getAbsolutePath());
            listeners.forEach(GameListener::playerAdded);
            LOGGER.info("Successfully registered new player: {}", aiNameCapitalised);
        }
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
