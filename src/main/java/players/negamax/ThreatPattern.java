package players.negamax;

/**
 * Represents a pattern on the Gomoku board, usually for a threat such as a
 * four or a three (OXXXX and OOXXXO).
 */
public class ThreatPattern {
    private int[][] pattern;
    private final int[] patternSquares;

    /**
     * Create a new threat pattern.
     * @param pattern Pattern represented as a 1D array, where 0 is an
     *                empty space and 1 refers to a stone being present
     * @param patternSquares The offensive/defensive squares of the
     *                       threat, i.e. the indices of all 0's in the
     *                       pattern array
     */
    public ThreatPattern(int[] pattern, int[] patternSquares) {
        // Store the pattern from each players perspective in pattern[][]
        this.pattern = new int[2][1];
        this.pattern[0] = pattern;
        this.pattern[1] = switchPattern(pattern);
        this.patternSquares = patternSquares;
    }

    /**
     * Get the pattern from the perspective of a player.
     * @param playerIndex Player identifier
     * @return Pattern array
     */
    public int[] getPattern(int playerIndex) {
        return this.pattern[playerIndex - 1];
    }

    /**
     * Return the offensive/defensive square indices in the pattern.
     * @return int[] containing all the square indices
     */
    public int[] getPatternSquares() {
        return this.patternSquares;
    }

    /**
     * Convert an input pattern to player 2's perspective.
     * @param pattern Input pattern array
     * @return Same array with every 1 turned into a 2
     */
    private int[] switchPattern(int[] pattern) {
        int[] patternSwitched = new int[pattern.length];
        for(int i = 0; i < pattern.length; i++) {
            if(pattern[i] == 1) {
                patternSwitched[i] = 2;
            }
        }
        return patternSwitched;
    }
}