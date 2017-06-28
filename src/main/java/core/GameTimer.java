package core;

/**
 * Simple countdown timer used to track move/game times for a single player.
 */
public class GameTimer {

    private final int moveTimeMillis;
    private final int gameTimeMillis;
    private final boolean moveTimeEnabled;
    private final boolean gameTimeEnabled;
    private long gameTimeRemaining;
    private long moveTimeRemaining;
    private long startGameTime;
    private long startMoveTime;
    private boolean timerStarted;

    /**
     * Create a new game timer for two players
     * @param gameTimeMillis
     * @param moveTimeMillis
     * @param gameTimeEnabled
     * @param moveTimeEnabled
     */
    public GameTimer(int gameTimeMillis, int moveTimeMillis, boolean
            gameTimeEnabled, boolean moveTimeEnabled) {
        this.moveTimeMillis = moveTimeMillis;
        this.gameTimeMillis = gameTimeMillis;
        long gameTimeNanos = millisToNanos(gameTimeMillis);
        long moveTimeNanos = millisToNanos(moveTimeMillis);
        this.gameTimeRemaining = gameTimeNanos;
        this.moveTimeRemaining = moveTimeNanos;
        this.gameTimeEnabled = gameTimeEnabled;
        this.moveTimeEnabled = moveTimeEnabled;
    }

    /**
     * Reset the move timer back to the original start time.
     */
    public void resetMoveTimer() {
        long moveTimeNanos = millisToNanos(moveTimeMillis);
        this.moveTimeRemaining = moveTimeNanos;
    }

    /**
     * Reset the game timer back to the original start time.
     */
    public void resetGameTimer() {
        long gameTimeNanos = millisToNanos(gameTimeMillis);
        this.gameTimeRemaining = gameTimeNanos;
    }

    /**
     * Start the move timer and game timer
     */
    public void startTimer() {
        this.timerStarted = true;
        if(gameTimeEnabled) {
            this.startGameTime = System.nanoTime();
        }
        if(moveTimeEnabled) {
            this.startMoveTime = System.nanoTime();
        }
    }

    /**
     * Stop the move timer and game timer, updating the remaining time for
     * the game timer and resetting the move timer
     */
    public void stopTimer() {
        this.timerStarted = false;
        if(gameTimeEnabled) {
            this.gameTimeRemaining -= System.nanoTime() - startGameTime;
        }
        if(moveTimeEnabled) {
            resetMoveTimer();
        }
    }

    /**
     * Query the current remaining game time
     * @return Remaining game time in milliseconds
     */
    public long getRemainingGameTime() {
        if(timerStarted && gameTimeEnabled) {
            long elapsed = System.nanoTime() - startGameTime;
            return nanosToMillis(gameTimeRemaining - elapsed);
        } else {
            return nanosToMillis(gameTimeRemaining);
        }
    }

    /**
     * Query the current remaining game time
     * @return Remaining game time in milliseconds
     */
    public long getRemainingMoveTime() {
        if(timerStarted && moveTimeEnabled) {
            long elapsed = System.nanoTime() - startMoveTime;
            return nanosToMillis(moveTimeRemaining - elapsed);
        } else {
            return nanosToMillis(moveTimeRemaining);
        }
    }

    /**
     * Return the timeout value for this player, which is the minimum of the
     * game time remaining and move time.
     * @return Timeout value in milliseconds, or 0 if no timeout
     */
    public long getTimeout() {
        if(this.moveTimeEnabled && this.gameTimeEnabled) {
            return Math.min(nanosToMillis(gameTimeRemaining),
                    nanosToMillis(moveTimeRemaining));
        }
        else if(this.moveTimeEnabled) {
            return nanosToMillis(moveTimeRemaining);
        }
        else if(this.gameTimeEnabled) {
            return nanosToMillis(gameTimeRemaining);
        } else {
            return 0;
        }
    }

    /**
     *  Static time conversion methods
     */

    /**
     * Convert nanoseconds to miliseconds
     * @param nanos Input nanoseconds
     * @return Milliseconds equivalent of input
     */
    public static long nanosToMillis(long nanos) {
        return nanos / 1000000;
    }

    /**
     * Convert milliseconds to nanoseconds
     * @param millis Input milliseconds
     * @return Nanoseconds equivalent of input
     */
    public static long millisToNanos(long millis) {
        return millis * 1000000;
    }

    /**
     * Convert milliseconds to minutes
     * @param millis Input milliseconds
     * @return Minutes equivalent of input
     */
    public static int millisToMinutes(long millis) {
        return (int) ((millis / 1000) / 60);
    }

    /**
     * Convert minutes to milliseconds
     * @param minutes Input minutes
     * @return Milliseconds equivalent of input
     */
    public static int minutesToMillis(int minutes) {
        return ((minutes * 1000) * 60);
    }

    /**
     * Convert milliseconds to seconds
     * @param millis Input milliseconds
     * @return Seconds equivalent to input
     */
    public static int millisToSeconds(long millis) {
        return (int) millis / 1000;
    }

    /**
     * Convert seconds to milliseconds
     * @param seconds Input seconds
     * @return Milliseconds equivalent to input
     */
    public static int secondsToMillis(int seconds) {
        return seconds * 1000;
    }

}
