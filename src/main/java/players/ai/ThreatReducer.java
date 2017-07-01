package players.ai;

import core.Move;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * In State, we store the neighbour fields around a field, up to
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
 * threats, and then stored in a 9-dimensional array (PATTERNS).
 * 
 * @author Hasan
 */
public class ThreatReducer {
    
    private static final Pattern[][][][][][][][][] PATTERNS;
    
    private static class Pattern {
        List<ThreatInstance> threats;
        List<RefutationInstance> refutations;
        
        public Pattern() {
            this.threats = new ArrayList<>();
            this.refutations = new ArrayList<>();
        }
    }
    
    /*
     * A refutation instance represents a pattern occuring in some direction
     * around a stone that can form a four. E.g. 120220001 -> four in one move
     */
     static class RefutationInstance {
        // Squares to create the refutation
        private final int[] refutationSquares;
        // Who the refutation belongs to (player 1/2)
        private final int playerIndex;
        
        public RefutationInstance(int[] refutationSquares, int playerIndex) {
            this.refutationSquares = refutationSquares;
            this.playerIndex = playerIndex;
        }
    }
    
    /*
     * A threat instance is a threat occurring in some direction around a stone
     * (e.g. 122220111 -> four). The defensive squares are stored so they can be 
     * mapped to a defensive move.
     */
    static class ThreatInstance {
        // The defensive/offensive squares to create/defend against the threat
        private final int[] threatSquares;
        // Who the threat belongs to (player 1/2)
        private final int playerIndex;
        // Threat class -> how many moves required to win from the threat
        private final int threatClass;
        
        public ThreatInstance(int[] threatSquares, int playerIndex, 
                int threatClass) {
            this.threatSquares = threatSquares;
            this.playerIndex = playerIndex;
            this.threatClass = threatClass;
        }
    }
    
    /**
     * Reduce the moves for this state if threats exist, focusing only on the
     * possible defensive moves.
     * @param state State
     * @return Reduced list of moves, or null if all moves are possible
     */
    protected List<Move> reduceMoves(State state) {
        int player = state.currentIndex;
        int opponent = player == 1 ? 2 : 1;
        
        HashSet<Move> threatMoves = new HashSet<>();
        HashSet<Move> fourMoves = new HashSet<>();
        int[] threatCount = new int[2];
        int[] fourCount = new int[2];
        
        // Loop over every field
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board[0].length; j++) {
                if(state.board[i][j].index != 0) {
                    // Loop over all 4 directions around the field
                    for(int k = 0; k < 4; k++) {
                        // Get the threats for this direction
                        List<ThreatInstance> threats = 
                                getThreats(state.board[i][j], k);
                        if(threats != null && !threats.isEmpty()) {
                            // Convert the threat squares to threat moves
                            for(ThreatInstance threat : threats) {
                                for(int square : threat.threatSquares) {
                                    Field squareField = state.board[i][j]
                                            .directions[k][square];
                                    threatMoves.add(new Move(
                                            squareField.row, 
                                            squareField.col));
                                    if(threat.threatClass == 1) {
                                        fourMoves.add(new Move(
                                                squareField.row,
                                                squareField.col));
                                    }
                                }
                                threatCount[threat.playerIndex - 1]++;
                                if(threat.threatClass == 1) {
                                    fourCount[threat.playerIndex - 1]++;
                                }
                            }
                        }
                    }
                }
            }
        }

        // If fours exist, we can ignore everything else
        if(fourCount[player - 1] > 0 || fourCount[opponent - 1] > 0) {
            return new ArrayList(fourMoves);
        }
        // Threat from the opponent exists, reduce moves to refutations and
        // threat squares
        if(threatCount[opponent - 1] > 0) {
            threatMoves.addAll(searchRefutations(state, player));
            return new ArrayList(threatMoves);
        }
        
        return null;
    }
    
    /**
     * Search for refutation moves in a state. A refutation is a move that 
     * creates a four in response to a threat, forcing the opponent to block.
     * @param state
     * @param index
     * @return
     */
    private HashSet<Move> searchRefutations(State state, int index) {
        HashSet<Move> refutationMoves = new HashSet<>();
        
        // Loop over every field
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board[0].length; j++) {
                if(state.board[i][j].index != 0) {
                    // Loop over all 4 directions around the field
                    for(int k = 0; k < 4; k++) {
                        // Grab the refutations
                        List<RefutationInstance> refutations = 
                                getRefutations(state.board[i][j], k);
                        if(refutations != null && !refutations.isEmpty()) {
                            // Convert the refutations to refutation moves
                            for(RefutationInstance ref : refutations) {
                                if(ref.playerIndex == index) {
                                    for(int square : ref.refutationSquares) {
                                        Field squareField =
                                                state.board[i][j].directions[k]
                                                [square];
                                        refutationMoves.add(new Move(
                                                squareField.row, 
                                                squareField.col));
                                    }
                                }
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
    public List<ThreatInstance> getThreats(Field field, int direction) {
        Pattern pattern = PATTERNS
                    [field.directions[direction][0].index]
                    [field.directions[direction][1].index]
                    [field.directions[direction][2].index]
                    [field.directions[direction][3].index]
                    [field.directions[direction][4].index]
                    [field.directions[direction][5].index]
                    [field.directions[direction][6].index]
                    [field.directions[direction][7].index]
                    [field.directions[direction][8].index];
        return pattern != null ? pattern.threats : null;
    }    
    
    /**
     * Lookup and return the refutation patterns for a field.
     * @param field Field to search
     * @return Array of threats found around this field
     */
    public List<RefutationInstance> getRefutations(Field field,
            int direction) {
        Pattern pattern = PATTERNS
                    [field.directions[direction][0].index]
                    [field.directions[direction][1].index]
                    [field.directions[direction][2].index]
                    [field.directions[direction][3].index]
                    [field.directions[direction][4].index]
                    [field.directions[direction][5].index]
                    [field.directions[direction][6].index]
                    [field.directions[direction][7].index]
                    [field.directions[direction][8].index];
        return pattern != null ? pattern.refutations : null;
    }
    
    /**
     * Load pattern data from the files, store threats/refutations for each
     * pattern in a lookup array (PATTERNS).
     */
    static {
        PATTERNS = new Pattern[4][4][4][4][4][4][4][4][4];
        
        InputStream threatsFile = ClassLoader
                .getSystemResourceAsStream("Threats.txt");
        
        try(BufferedReader threatsReader = new BufferedReader(
                new InputStreamReader(threatsFile))) {
            
            String threatLine;
            while ((threatLine = threatsReader.readLine()) != null) {
                String[] threatString = threatLine.split(",");
                String directionStr = threatString[0];
                int[] direction = new int[directionStr.length()];
                for(int i = 0; i < directionStr.length(); i++) {
                    direction[i] = Character.getNumericValue(
                            directionStr.charAt(i));
                }
                int playerIndex = Integer.parseInt(threatString[1]);
                int threatClass = Integer.parseInt(threatString[2]);
                int[] threatSquares = new int[threatString.length - 3];
                for(int i = 3; i < threatString.length; i++) {
                    threatSquares[i - 3] = Integer.parseInt(
                            threatString[i]);
                }
                if(PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]] == null) {
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]] = new Pattern();
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]].threats.add(
                                new ThreatInstance(threatSquares, playerIndex, 
                                        threatClass));
                } else {
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]].threats.add(
                                new ThreatInstance(threatSquares, playerIndex, 
                                        threatClass));
                }
            }
            
        } catch(IOException | NullPointerException ex) {
            System.out.println("Failed to load the threats file: " + ex);
            throw new RuntimeException(ex);
        }
        
        InputStream refutationsFile = ClassLoader
                .getSystemResourceAsStream("Refutations.txt");
        
        try(BufferedReader refutationsReader = new BufferedReader(
                new InputStreamReader(refutationsFile))) {
            
            String refutationLine;
            while ((refutationLine = refutationsReader.readLine()) != null) {
                String[] refutationString = refutationLine.split(",");
                String directionStr = refutationString[0];
                int[] direction = new int[directionStr.length()];
                for(int i = 0; i < directionStr.length(); i++) {
                    direction[i] = Character.getNumericValue(
                            directionStr.charAt(i));
                }
                int playerIndex = Integer.parseInt(refutationString[1]);
                int[] refutationSquares = new int[refutationString.length - 2];
                for(int i = 2; i < refutationString.length; i++) {
                    refutationSquares[i - 2] = Integer.parseInt(
                            refutationString[i]);
                }
                if(PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]] == null) {
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]] = new Pattern();
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]].refutations.add(
                                new RefutationInstance(refutationSquares,
                                        playerIndex));
                } else {
                    PATTERNS[direction[0]][direction[1]][direction[2]]
                        [direction[3]][direction[4]][direction[5]][direction[6]]
                        [direction[7]][direction[8]].refutations.add(
                                new RefutationInstance(refutationSquares,
                                        playerIndex));
                }
            }
            
        } catch(IOException | NullPointerException ex) {
            System.out.println("Failed to load the refutations file: " + ex);
            throw new RuntimeException(ex);
        }
    }
    
}
