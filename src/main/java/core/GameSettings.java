package core;

import events.SettingsListener;
import players.Player;
import players.PlayerRegistry;
import players.negamax.NegamaxPlayer;
import players.human.HumanPlayer;

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
     * Create a new GameSettings instance with default values.
     */
    public GameSettings() {
        this.player1 = "Human";
        this.player2 = "Negamax";
        this.gameTimingEnabled = true;
        this.moveTimingEnabled = false;
        this.gameTimeMillis = 1200000;
        this.moveTimeMillis = 5000;
        this.size = 15;
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
        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Update player 2
     * @param playerName Name of the player
     */
    public void setPlayer2(String playerName) {
        this.player2 = playerName;
        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Get the player instance for player 1.
     */
    public Player getPlayer1() {
        GameInfo gameInfo = new GameInfo(this, 1, 2);
        return PlayerRegistry.getPlayer(gameInfo, player1);
    }

    /**
     * Get the player instance for player 2.
     */
    public Player getPlayer2() {
        GameInfo gameInfo = new GameInfo(this, 2, 1);
        return PlayerRegistry.getPlayer(gameInfo, player2);
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
        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Check if game timing is enabled for this game.
     */
    public boolean gameTimingEnabled() {
        return this.gameTimingEnabled;
    }

    /**
     * Check if move timing is enabled for this game.
     */
    public boolean moveTimingEnabled() {
        return this.moveTimingEnabled;
    }

    /**
     * Enable or disable game timing.
     */
    public void setGameTimingEnabled(boolean enabled) {
        this.gameTimingEnabled = enabled;
        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Enable or disable move timing.
     */
    public void setMoveTimingEnabled(boolean enabled) {
        this.moveTimingEnabled = enabled;
        listeners.forEach(listener -> listener.settingsChanged());
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
        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Set the move timeout value.
     * @param millis Move timeout in milliseconds
     */
    public void setMoveTimeMillis(long millis) {
        this.moveTimeMillis = millis;
        listeners.forEach(listener -> listener.settingsChanged());
    }

}
