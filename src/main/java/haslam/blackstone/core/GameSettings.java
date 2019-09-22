package haslam.blackstone.core;

import haslam.blackstone.events.SettingsListener;
import haslam.blackstone.players.Player;
import haslam.blackstone.players.PlayerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents settings for a Gomoku game.
 */
public class GameSettings {

    private String player1;
    private String player2;
    private int size;
    private boolean gameTimingEnabled;
    private boolean moveTimingEnabled;
    private long gameTimeMillis;
    private long moveTimeMillis;
    private List<SettingsListener> listeners;

    /**
     * Create a new GameSettings instance.
     * @param player1 Player 1 name
     * @param player2 Player 2 name
     * @param gameTimingEnabled Whether or not game timing is enabled
     * @param moveTimingEnabled Whether or not move timing is enabled
     * @param gameTimeMillis Maximum game time in milliseconds
     * @param moveTimeMillis Maximum move time in milliseconds
     * @param size Size of the board
     * @see haslam.blackstone.players.PlayerRegistry
     */
    GameSettings(String player1, String player2,
                 boolean gameTimingEnabled, boolean moveTimingEnabled,
                 long gameTimeMillis, long moveTimeMillis, int size) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameTimingEnabled = gameTimingEnabled;
        this.moveTimingEnabled = moveTimingEnabled;
        this.gameTimeMillis = gameTimeMillis;
        this.moveTimeMillis = moveTimeMillis;
        this.size = size;
        this.listeners = new ArrayList<>();
    }

    /**
     * Register a listener to receive updates when settings change.
     * @param listener Listener to register
     */
    public void addListener(SettingsListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Update player 1
     * @param playerName Name of the player
     */
    public void setPlayer1(String playerName) {
        this.player1 = playerName;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Update player 2
     * @param playerName Name of the player
     */
    public void setPlayer2(String playerName) {
        this.player2 = playerName;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Get the player instance for player 1.
     */
    Player getPlayer1() {
        return PlayerRegistry.getPlayer(player1);
    }

    /**
     * Get the player instance for player 2.
     */
    Player getPlayer2() {
        return PlayerRegistry.getPlayer(player2);
    }

    /**
     * @return Player name for player 1
     */
    public String getPlayer1Name() {
        return player1;
    }

    /**
     * @return Player name for player 2
     */
    public String getPlayer2Name() {
        return player2;
    }

    /**
     * Get the board size for this game.
     * @return Board size value (e.g. 15 for 15x15)
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Set the board size for this game.
     * @param size New board size value (e.g. 15 for 15x15)
     */
    public void setSize(int size) {
        this.size = size;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Check if game timing is enabled for this game.
     * @return True if game timing is enabled
     */
    public boolean gameTimingEnabled() {
        return this.gameTimingEnabled;
    }

    /**
     * Check if move timing is enabled for this game.
     * @return True if move timing is enabled
     */
    public boolean moveTimingEnabled() {
        return this.moveTimingEnabled;
    }

    /**
     * Enable or disable game timing.
     * @param enabled Enabled value
     */
    public void setGameTimingEnabled(boolean enabled) {
        this.gameTimingEnabled = enabled;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Enable or disable move timing.
     */
    public void setMoveTimingEnabled(boolean enabled) {
        this.moveTimingEnabled = enabled;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Get the game timeout value. The game ends if the player exceeds this
     * value and game timing is enabled.
     * @return Game timeout in milliseconds
     */
    public long getGameTimeMillis() {
        return this.gameTimeMillis;
    }

    /**
     * Get the move timeout value. The game ends if the player exceeds this
     * value for a single move and move timing is enabled.
     * @return Move timeout in milliseconds
     */
    public long getMoveTimeMillis() {
        return this.moveTimeMillis;
    }

    /**
     * Set the game timeout value.
     * @param millis Game timeout in milliseconds
     */
    public void setGameTimeMillis(long millis) {
        this.gameTimeMillis = millis;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    /**
     * Set the move timeout value.
     * @param millis Move timeout in milliseconds
     */
    public void setMoveTimeMillis(long millis) {
        this.moveTimeMillis = millis;
        listeners.forEach(SettingsListener::settingsChanged);
    }

    public static GameSettings withDefaults() {
        return new GameSettings(Defaults.PLAYER_1, Defaults.PLAYER_2,
                Defaults.GAME_TIMING_ENABLED, Defaults.MOVE_TIMING_ENABLED,
                Defaults.GAME_TIMEOUT_MILLIS, Defaults.MOVE_TIMEOUT_MILLIS,
                Defaults.SIZE);
    }
}
