package core;

/**
 * Game information given to a player instance to determine game settings.
 */
public class GameInfo {

    private final GameSettings settings;
    private int gameTimeRemaining;
    private int playerIndex;
    private int opponentIndex;

    /**
     * Create a new game information object for a player.
     * @param settings Game configuration
     * @param playerIndex Index of this player (1/2)
     * @param opponentIndex Index of the opponent (1/2)
     */
    public GameInfo(GameSettings settings, int playerIndex, int
            opponentIndex) {
        this.settings = settings;
        this.playerIndex = playerIndex;
        this.opponentIndex = opponentIndex;
    }

    /**
     * Get the index (player number) assigned to this player.
     * @return 1 or 2
     */
    public int getPlayerIndex() {
        return this.playerIndex;
    }

    /**
     * Get the index assigned to the opponent.
     * @return 1 or 2
     */
    public int getOpponentIndex() {
        return this.opponentIndex;
    }

    /**
     * Get the board size n, where n*n intersections exist on the board.
     * @return Board size, e.g. 19 for 19*19 intersections
     */
    public int getSize() {
        return settings.getSize();
    }

    /**
     * Get the maximum time for this game in milliseconds.
     * @return Max game time in milliseconds
     */
    public long getGameTimeMillis() {
        return settings.getGameTimeMillis();
    }

    /**
     * Get the maximum time for a single move in milliseconds.
     * @return Max time per move in milliseconds
     */
    public long getMoveTimeMillis() {
        return settings.getMoveTimeMillis();
    }
}
