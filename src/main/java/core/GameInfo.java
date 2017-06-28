package core;

/**
 * Game information object given to an AI player to determine game settings
 * and timeout values. Not shared, unique to a single player instance.
 */
public class GameInfo {

    private final int intersections;
    private final int moveTimeout;
    private final int gameTimeout;
    private int timeRemaining;
    private int playerIndex;
    private int opponentIndex;

    /**
     * Create a new game information object for a player.
     * @param intersections Number of intersections on the board
     * @param gameTimeout Timeout for a game
     * @param moveTimeout Timeout for a single move
     * @param playerIndex Index of this player (1/2)
     * @param opponentIndex Index of the opponent (1/2)
     */
    protected GameInfo(int intersections, int gameTimeout, int moveTimeout,
                       int playerIndex, int opponentIndex) {
        this.intersections = intersections;
        this.gameTimeout = gameTimeout;
        this.moveTimeout = moveTimeout;
        this.playerIndex = playerIndex;
        this.opponentIndex = opponentIndex;
    }

    /**
     * Get the index (player number) assigned to this player
     * @return 1 or 2
     */
    public int getPlayerIndex() {
        return this.playerIndex;
    }

    /**
     * Get the index assigned to the opponent
     * @return 1 or 2
     */
    public int getOpponentIndex() {
        return this.opponentIndex;
    }

    /**
     * Get the board size n, where n*n intersections exist on the board
     * @return Board size, e.g. 19 for 19*19 intersections (20x20 grid)
     */
    public int getIntersections() {
        return this.intersections;
    }

    /**
     * Get the maximum time for this game in milliseconds
     * @return Max game time in milliseconds
     */
    public int getGameTimeout() {
        return this.gameTimeout;
    }

    /**
     * Get the maximum time for a single move in milliseconds
     * @return Max time per move in milliseconds
     */
    public int getMoveTimeout() {
        return this.moveTimeout;
    }

    /**
     * Get the game time remaining for this player in milliseconds (updated
     * after every move, not real-time)
     * @return Remaining game time, in milliseconds
     */
    public int getTimeRemaining() {
        return this.timeRemaining;
    }

    /**
     * Decrement this players total game time
     * @param millis Milliseconds to subtract from total game time
     */
    protected void decrementTime(int millis) {
        this.timeRemaining -= millis;
    }

    /**
     * Increment this players total game time
     * @param millis Milliseconds to add to total game time
     */
    protected void incrementTime(int millis) {
        this.timeRemaining += millis;
    }
}
