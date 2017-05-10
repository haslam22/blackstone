package players.minimax;

import gomoku.GomokuMove;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class searches a state for threats and reduces the search space to only
 * the defensive moves. This improves the performance of the AI by focusing on
 * less moves in situations with lots of threats.
 * 
 * A threat is a pattern in Gomoku where the player has to respond, e.g. a four: 
 * 12222_ or a three: _222_, or else the player loses on the next turn, because 
 * a four creates a five, and a three creates a straight four (five possible on 
 * both sides).
 * 
 * In MinimaxState, we store the neighbour fields around a field, up to 
 * 4 intersections in each direction (diagonal backwards, diagonal forward, 
 * vertical, horizontal) forming a star shape:
 * 
 *  *       *       *
 *    *     *     *
 *      *   *   *
 *        * * *
 *  * * * * 0 * * * *
 *        * * * 
 *      *   *   *
 *    *     *     *
 *  *       *       *
 * 
 * Each direction is 9 fields long (as seen above), and there are 9^4 possible
 * arrangements of a direction if the direction contains: 0 (empty), 1 (black)
 * 2 (white) and 3 (out of bounds). Every arrangement is generated, searched for
 * threats, and then stored in a 9-dimensional array (THREAT_PATTERNS).
 * 
 * @author Hassan
 */
public class MinimaxThreatReducer {
    
    /*
     * A refutation instance represents patterns occuring in some direction
     * around a stone that can form a four. E.g. 120220001 -> four in one move
     * It is called a "refutation" because it can refute an offensive threat
     * in some cases, and must be accounted for when we reduce the search space
     * to threats.
     */
    private static class RefutationInstance {
        List<Integer> refutationSquares;
        public RefutationInstance() {
            this.refutationSquares = new ArrayList<>();
        }
    }
    
    /*
     * A threat instance is a threat occuring in some direction around a stone
     * (e.g. 122220111 -> four). The start index and defensive squares are 
     * stored so they can be mapped to a defensive move.
     */
    private static class ThreatInstance {
        // The defensive/offensive squares to create/defend against the threat
        private final int[] threatSquares;
        // The start index of the threat
        private final int startIndex;
        // Who the threat belongs to (player 1/2)
        private final int playerIndex;
        // Threat class -> how many moves required to win from the threat
        private final int threatClass;
        
        public ThreatInstance(int[] threatSquares, int startIndex, 
                int playerIndex, int threatClass) {
            this.threatSquares = threatSquares;
            this.startIndex = startIndex;
            this.playerIndex = playerIndex;
            this.threatClass = threatClass;
        }
    }
    
    private static final ThreatInstance[][][][][][][][][][] THREAT_PATTERNS;
    private static final RefutationInstance[][][][][][][][][][] REFUTATIONS;
    
    /**
     * Reduce the moves for this state if threats exist, focusing only on the
     * possible defensive moves.
     * @param state MinimaxState
     * @return Reduced list of moves, or null if all moves are possible
     */
    protected List<GomokuMove> reduceMoves(MinimaxState state) {
        int player = state.currentIndex;
        int opponent = player == 1? 2 : 1;
        
        HashSet<GomokuMove> threatMoves = new HashSet<>();
        int[] threatCount = new int[2];
        
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board[0].length; j++) {
                if(state.board[i][j].index != 0) {
                    MinimaxField field = state.board[i][j];
                    // Get the threats for every direction around this field
                    ThreatInstance[] threats = getThreats(field);
                    // Loop every direction
                    for(int k = 0; k < threats.length; k++) {
                        // Threat was found in this direction
                        if(threats[k] != null) {
                            threatCount[threats[k].playerIndex - 1]++;
                            int[] squares = threats[k].threatSquares;
                            // Loop over every threat square
                            for(int l = 0; l < squares.length; l++) {
                                // Direction + offset + square index = field
                                MinimaxField threatField = field.directions[k]
                                        [threats[k].startIndex + squares[l]];
                                // Add this index as a possible move
                                threatMoves.add(new GomokuMove(
                                        threatField.row, threatField.col));
                            }
                        }
                    }
                }
            }
        }
        
        // Threat from the opponent exists, reduce moves to refutations and
        // threat squares
        if(threatCount[opponent - 1] > 0) {
            threatMoves.addAll(searchRefutations(state));
            return new ArrayList(threatMoves);
        }
        
        return null;
    }
    
    /**
     * Search for refutation moves in a state. A refutation is a move that 
     * creates a Four in response to a threat, forcing the opponent to block.
     * @param state
     * @return
     */
    private HashSet<GomokuMove> searchRefutations(MinimaxState state) {
        HashSet<GomokuMove> refutationMoves = new HashSet<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board[0].length; j++) {
                // Move must be within 2 intersections of a stone belonging to
                // the current player
                if(state.board[i][j].index == 0) {
                    RefutationInstance[] refs = 
                            getRefutations(state.board[i][j], 
                                    state.currentIndex);
                    for(int k = 0; k < refs.length; k++) {
                        if(refs[k] != null) {
                            for(int refIndex : refs[k].refutationSquares) {
                                refutationMoves.add(new GomokuMove(
                                        state.board[i][j].directions[k]
                                                [refIndex].row, 
                                        state.board[i][j].directions[k]
                                                [refIndex].col));
                            }
                        }
                    }
                }
            }
        }
        return refutationMoves;
    }
    
    /**
     * Lookup and return the threat patterns for a field.
     * @param field Field to search
     * @return Array of threats found around this field
     */
    private ThreatInstance[] getThreats(MinimaxField field) {
        ThreatInstance[] threats = new ThreatInstance[4];
        for(int i = 0; i < 4; i++) {
            threats[i] = THREAT_PATTERNS[field.index - 1]
                    [field.directions[i][0].index]
                    [field.directions[i][1].index]
                    [field.directions[i][2].index]
                    [field.directions[i][3].index]
                    [field.directions[i][4].index]
                    [field.directions[i][5].index]
                    [field.directions[i][6].index]
                    [field.directions[i][7].index]
                    [field.directions[i][8].index];
        }
        return threats;
    }    
    
    /**
     * Lookup and return the refutation patterns for a field.
     * @param field Field to search
     * @return Array of threats found around this field
     */
    private RefutationInstance[] getRefutations(MinimaxField field, int index) {
        RefutationInstance[] refs = new RefutationInstance[4];
        for(int i = 0; i < 4; i++) {
            refs[i] = REFUTATIONS[index - 1]
                    [field.directions[i][0].index]
                    [field.directions[i][1].index]
                    [field.directions[i][2].index]
                    [field.directions[i][3].index]
                    [field.directions[i][4].index]
                    [field.directions[i][5].index]
                    [field.directions[i][6].index]
                    [field.directions[i][7].index]
                    [field.directions[i][8].index];
        }
        return refs;
    }
    
    /**
     * Compute the possible threats for every possible direction arrangement,
     * and store the threats found in THREAT_PATTERNS.
     */
    static {
        THREAT_PATTERNS = new ThreatInstance[2][4][4][4][4][4][4][4][4][4];
        REFUTATIONS = new RefutationInstance[2][4][4][4][4][4][4][4][4][4];
        
        class ThreatPattern {
            String threatString;
            int threatClass; 
            int[] threatSquares;
            int playerIndex;

            public ThreatPattern(String threatString, int threatClass, 
                    int[] threatSquares, int playerIndex) {
                this.threatString = threatString;
                this.threatSquares = threatSquares;
                this.playerIndex = playerIndex;
            }
        }
        
        class RefutationPattern {
            String threatString;
            int[] threatSquares;
            int playerIndex;
            
            public RefutationPattern(String threatString, int[] threatSquares,
                    int playerIndex) {
                this.threatString = threatString;
                this.threatSquares = threatSquares;
                this.playerIndex = playerIndex;
            }
        }
        
        List<RefutationPattern> refutations = new ArrayList<>();
        
        refutations.add(new RefutationPattern("10011", new int[] { 1, 2 }, 1));
        refutations.add(new RefutationPattern("11001", new int[] { 2, 3 }, 1));
        refutations.add(new RefutationPattern("11100", new int[] { 3, 4 }, 1));
        refutations.add(new RefutationPattern("00111", new int[] { 0, 1 }, 1));
        refutations.add(new RefutationPattern("01101", new int[] { 0, 3 }, 1));
        refutations.add(new RefutationPattern("01110", new int[] { 0, 4 }, 1));
        
        refutations.add(new RefutationPattern("20022", new int[] { 1, 2 }, 2));
        refutations.add(new RefutationPattern("22002", new int[] { 2, 3 }, 2));
        refutations.add(new RefutationPattern("22200", new int[] { 3, 4 }, 2));
        refutations.add(new RefutationPattern("00222", new int[] { 0, 1 }, 2));
        refutations.add(new RefutationPattern("02202", new int[] { 0, 3 }, 2));
        refutations.add(new RefutationPattern("02220", new int[] { 0, 4 }, 1));
        
        List<ThreatPattern> threats = new ArrayList<>();
        
        // Four
        threats.add(new ThreatPattern("01111", 1, new int[] { 0 }, 1));
        threats.add(new ThreatPattern("10111", 1, new int[] { 1 }, 1));
        threats.add(new ThreatPattern("11011", 1, new int[] { 2 }, 1));
        threats.add(new ThreatPattern("11101", 1, new int[] { 3 }, 1));
        threats.add(new ThreatPattern("11110", 1, new int[] { 4 }, 1));
        threats.add(new ThreatPattern("02222", 1, new int[] { 0 }, 2));
        threats.add(new ThreatPattern("20222", 1, new int[] { 1 }, 2));
        threats.add(new ThreatPattern("22022", 1, new int[] { 2 }, 2));
        threats.add(new ThreatPattern("22202", 1, new int[] { 3 }, 2));
        threats.add(new ThreatPattern("22220", 1, new int[] { 4 }, 2));
        
        // Three
        threats.add(new ThreatPattern("0111002", 2, new int[] { 0, 4, 5 }, 1));
        threats.add(new ThreatPattern("0011102", 2, new int[] { 0, 1, 5 }, 1));
        threats.add(new ThreatPattern("0111003", 2, new int[] { 0, 4, 5 }, 1));
        threats.add(new ThreatPattern("0011103", 2, new int[] { 0, 1, 5 }, 1));
        threats.add(new ThreatPattern("2001110", 2, new int[] { 1, 2, 6 }, 1));
        threats.add(new ThreatPattern("2011100", 2, new int[] { 1, 5, 6 }, 1));
        threats.add(new ThreatPattern("3001110", 2, new int[] { 1, 2, 6 }, 1));
        threats.add(new ThreatPattern("3011100", 2, new int[] { 1, 5, 6 }, 1));
        threats.add(new ThreatPattern("0222001", 2, new int[] { 0, 4, 5 }, 2));
        threats.add(new ThreatPattern("0022201", 2, new int[] { 0, 1, 5 }, 2));
        threats.add(new ThreatPattern("0222003", 2, new int[] { 0, 4, 5 }, 2));
        threats.add(new ThreatPattern("0022203", 2, new int[] { 0, 1, 5 }, 2));
        threats.add(new ThreatPattern("1002220", 2, new int[] { 1, 2, 6 }, 2));
        threats.add(new ThreatPattern("1022200", 2, new int[] { 1, 5, 6 }, 2));
        threats.add(new ThreatPattern("3002220", 2, new int[] { 1, 2, 6 }, 2));
        threats.add(new ThreatPattern("3022200", 2, new int[] { 1, 5, 6 }, 2));
        threats.add(new ThreatPattern("0011100", 2, new int[] { 1, 5 }, 1));
        threats.add(new ThreatPattern("0022200", 2, new int[] { 1, 5 }, 2));
        
        // Broken three
        threats.add(new ThreatPattern("011010", 2, new int[] { 0, 3, 5 }, 1));
        threats.add(new ThreatPattern("010110", 2, new int[] { 0, 2, 5 }, 1));
        threats.add(new ThreatPattern("022020", 2, new int[] { 0, 3, 5 }, 2));
        threats.add(new ThreatPattern("020220", 2, new int[] { 0, 2, 5 }, 2));
        
        // Generate all possible numbers of length 9 in radix 4 (0,1,2,3)
        // E.g. 1234 -> 000103102
        for(int i = 0; i < 262144; i++) {
            String directionStr = String.format("%9s", Integer.toString(i, 4))
                    .replace(" ", "0");
            // Convert string to integer array
            int[] direction = new int[directionStr.length()];
            for(int j = 0; j < direction.length; j++) {
                direction[j] = Character.getNumericValue(directionStr
                        .charAt(j));
            }
            
            int threatCount = 0;
            
            for(ThreatPattern pattern : threats) {
                // Search for the threat in this direction
                int patternIndex = directionStr
                        .indexOf(pattern.threatString);
                if(patternIndex >= 0) {
                    // Create an instance of the threat found
                    ThreatInstance threat = new ThreatInstance(
                            pattern.threatSquares,
                            patternIndex,
                            pattern.playerIndex,
                            pattern.threatClass
                    );
                    // Save the threat
                    THREAT_PATTERNS[pattern.playerIndex - 1]
                            [direction[0]][direction[1]]
                            [direction[2]][direction[3]]
                            [direction[4]][direction[5]]
                            [direction[6]][direction[7]]
                            [direction[8]] = threat;
                    threatCount++;
                }
            }
            // If no threats exist, check for refutations (moves that may turn
            // into threats)
            if(threatCount == 0) {
                refutations.forEach((pattern) -> {
                    int patternIndex = directionStr
                            .indexOf(pattern.threatString);
                    // Found a refutation pattern
                    if (patternIndex >= 0) {
                        // If no refutations have been found yet, create the
                        // refutation instance
                        if(REFUTATIONS[pattern.playerIndex - 1]
                                [direction[0]][direction[1]]
                                [direction[2]][direction[3]]
                                [direction[4]][direction[5]]
                                [direction[6]][direction[7]]
                                [direction[8]] == null) {
                            REFUTATIONS[pattern.playerIndex - 1]
                                    [direction[0]][direction[1]]
                                    [direction[2]][direction[3]]
                                    [direction[4]][direction[5]]
                                    [direction[6]][direction[7]]
                                    [direction[8]] = new RefutationInstance();
                        }
                        
                        // Add all the refutation squares (moves)
                        for(int j = 0; j < pattern.threatSquares.length; 
                                j++) {
                            REFUTATIONS[pattern.playerIndex - 1]
                                    [direction[0]][direction[1]]
                                    [direction[2]][direction[3]]
                                    [direction[4]][direction[5]]
                                    [direction[6]][direction[7]]
                                    [direction[8]].refutationSquares.add(
                                            patternIndex +
                                                    pattern.threatSquares[j]);
                        }
                    }
                });
            }
        }
    }
    
}
