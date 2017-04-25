package players.minimax;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import players.GomokuPlayer;
import players.minimax.MinimaxState.GomokuField;

/**
 * Minimax player, with alpha-beta pruning.
 * @author Hassan
 */
public class MinimaxPlayer extends GomokuPlayer {
    
    private static class ThreatPattern {
        int[] threatSquares;
        int startIndex;
        public ThreatPattern(int[] threatSquares, int startIndex) {
            this.threatSquares = threatSquares;
            this.startIndex = startIndex;
        }
    }
    
    private int nodes;
    private final int intersections;
    private MinimaxState state;
    private static final int[][][][][][][][][][] SCORES;
    private static final ThreatPattern[][][][][][][][][][] THREAT_PATTERNS;
    private int timeout = 7500;
    private long startTime;
    
    public MinimaxPlayer(GomokuGame game, int playerIndex, int opponentIndex) {
        super(game, playerIndex, opponentIndex);
        this.intersections = game.getIntersections();
        this.state = new MinimaxState(game.getIntersections());
    }

    // Compare two moves based on the evaluation of the field
    Comparator<GomokuMove> fieldCompare = new Comparator<GomokuMove>() {
        @Override
        public int compare(GomokuMove move1, GomokuMove move2) {
            return getScore(state.board[move2.row][move2.col],
                        state.currentIndex) - 
                    getScore(state.board[move1.row][move1.col],
                        state.currentIndex);
        }
    };
    
    /**
     * Prune moves by focusing on areas where stones already exist to reduce
     * the search space, and sort nodes by evaluating their fields
     * @param state State to find moves for
     * @return A list of pruned moves
     */
    private List<GomokuMove> pruneMoves(MinimaxState state) {
        // If there are any threats on the board, the search space can be 
        // reduced to the threats alone
        Set<GomokuMove> threatSquares = new HashSet<>(225);
        
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                GomokuField field = state.board[i][j];
                if(field.index != 0) {
                    // Get the threats (if any) around this field
                    ThreatPattern[] threats = getThreats(field);
                    for(int k = 0; k < threats.length; k++) {
                        if(threats[k] != null) {
                            // Threat exists in this direction (k). Get the
                            // threat squares (moves to defend/create the threat)
                            int[] squares = threats[k].threatSquares;
                            // Loop over every threat square
                            for(int l = 0; l < squares.length; l++) {
                                // Direction + offset + square index
                                GomokuField threatField = field.directions[k]
                                        [threats[k].startIndex + squares[l]];
                                // Add this index as a possible move
                                threatSquares.add(new GomokuMove(
                                        threatField.row, threatField.col));
                            }
                        }
                    }
                }
            }
        }
        
        // There's at least one threat on the board, so we can just return the
        // threat squares and ignore everything else
        if(!threatSquares.isEmpty()) {
            return new ArrayList(threatSquares);
        }
        
        ArrayList<GomokuMove> pruned = new ArrayList<>(225);
        
        // Have to make an opening move, return a move in the middle
        if(state.moves == 0) {
            pruned.add(new GomokuMove(
                    state.board.length / 2, state.board.length / 2));
            return pruned;
        }
        
        // Focus on moves that occur up to 2 intersections around an existing
        // stone on the board
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0 && 
                        state.hasAdjacent(i, j, 2)) {
                    pruned.add(new GomokuMove(i, j));
                }
            }
        }
        pruned.sort(fieldCompare);
        return pruned;
    }
    
    /**
     * Run the minimax algorithm up to a certain depth, with alpha-beta
     * pruning.
     * @param state Starting state
     * @param depth How deep to search the tree before evaluating
     * @param alpha Best possible value for the maximising player so far
     * @param beta Best possible value for the minimising player so far
     * @return
     */
    private int minimax(MinimaxState state, int depth, int alpha, int beta) 
            throws InterruptedException {
        if((System.currentTimeMillis() - startTime) > timeout) {
            throw new InterruptedException();
        }
        nodes++;
        // Check if terminal node, return loss/win based on depth
        int terminal = state.terminal();
        if(terminal == this.playerIndex) {
            return 10000 + depth;
        }
        else if(terminal == this.opponentIndex) {
            return -10000 - depth;
        }
        // Reached leaf node, return evaluation
        if(depth == 0) {
            return evaluateState(state);
        }
        // Not terminal or leaf node, continue recursing
        if(state.currentIndex == this.playerIndex) {
            int maximum = Integer.MIN_VALUE;
            List<GomokuMove> prunedMoves = pruneMoves(state);
            for(GomokuMove move : prunedMoves) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score > maximum) {
                    maximum = score;
                }
                alpha = Math.max(alpha, maximum);
                if(beta <= alpha) {
                    break;
                }
            }
            return maximum;
        }
        else {
            int minimum = Integer.MAX_VALUE;
            List<GomokuMove> prunedMoves = pruneMoves(state);
            for(GomokuMove move : prunedMoves) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score < minimum) {
                    minimum = score;
                }
                beta = Math.min(beta, minimum);
                if(beta <= alpha) {
                    break;
                }
            }
            return minimum;
        }
    }
    
    /**
     * Lookup the evaluation of a field from the perspective of a player.
     * @param field Field to evaluate
     * @param index Player index to evaluate for
     * @return Score of this field
     */
    private int getScore(GomokuField field, int index) {
        int score = 0;
        for(int i = 0; i < 4; i++) {
            score+= SCORES[index - 1]
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
        return score;
    }    
    
    /**
     * Lookup and return the threat patterns for a field.
     * @param field Field to get search
     * @return ThreatPattern array of threats
     */
    private ThreatPattern[] getThreats(GomokuField field) {
        ThreatPattern[] threats = new ThreatPattern[4];
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
     * Evaluate a state by looking up the evaluation of each field that is
     * occupied. Minus score for opponent, add score for AI.
     * @param state State to evaluate
     * @return Score of the state
     */
    private int evaluateState(MinimaxState state) {
        int score = 0;
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == this.opponentIndex) {
                    score -= this.getScore(state.board[i][j], 
                            this.opponentIndex);
                } else if(state.board[i][j].index == this.playerIndex) {
                    score += this.getScore(state.board[i][j], 
                            this.playerIndex);
                }
            }
        }
        return score;
    }
    
    /**
     * Run minimax for a list of initial moves and sort them after the search
     * based on their scores.
     * @param moves Initial moves
     * @param depth Depth to search moves
     * @return List of moves, sorted by best score first
     */
    private List<GomokuMove> search(List<GomokuMove> moves, int depth) {
        class ScoredMove {
            GomokuMove move;
            int score;
            ScoredMove(GomokuMove move, int score) {
                this.move = move;
                this.score = score;
            }
        }        
        
        Comparator<ScoredMove> moveCompare = new Comparator<ScoredMove>() {
            @Override
            public int compare(ScoredMove move1, ScoredMove move2) {
                return move2.score - move1.score;
            }
        };
        
        // Copy moves
        List<ScoredMove> scoredMoves = new ArrayList<>();
        moves.forEach((move) -> {
            scoredMoves.add(new ScoredMove(move, Integer.MIN_VALUE));
        });
        
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore = alpha;

        // Run minimax for all the children (initial moves)
        try {
            for(ScoredMove move : scoredMoves) {
                state.makeMove(move.move);
                move.score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move.move);
                if(move.score > bestScore) {
                    bestScore = move.score;
                }
                alpha = Math.max(alpha, bestScore);
                if(beta <= alpha) break;
            }
        } catch(InterruptedException ex) {
            return null;
        }
        
        // Sort moves by score
        scoredMoves.sort(moveCompare);
        // Clear original list and re-add sorted moves
        moves.clear();
        for(int i = 0; i < scoredMoves.size(); i++) {
            moves.add(scoredMoves.get(i).move);
        }
        
        String bestMove = "[" + convertRow(moves.get(0).row)
                + convertCol(moves.get(0).col) + "]";
        
        game.writeLog(
                String.format("Depth: %d, Evaluation: %d, Best move: %s",
                depth, bestScore, bestMove));
        return moves;
    }
    
    @Override
    public GomokuMove getMove(GomokuState gameState) {
        // Create a new internal state object, sync with the game state
        this.state = new MinimaxState(game.getIntersections());
        ArrayList<GomokuMove> moves = new ArrayList(gameState.getMoveHistory());
        moves.forEach((move) -> {
            state.makeMove(move);
        });
        
        this.nodes = 0;
        this.startTime = System.currentTimeMillis();
        
        List<GomokuMove> initialMoves = pruneMoves(state);
        GomokuMove bestMove = new GomokuMove();
        
        int depth = 8;
        for(int i = 2; i <= depth; i++) {
            // Search and sort moves based on score
            initialMoves = search(initialMoves, i);
            // Update best move if search completed
            if(initialMoves != null) {
                bestMove = initialMoves.get(0);
            } else {
                break;
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        game.writeLog("Time: " + duration + "ms");
        game.writeLog("Nodes: " + nodes);
        game.writeLog("Nodes/ms: " + (nodes / (duration > 0 ? duration : 1)));
        
        return bestMove;
    }
    
    /**
     * Given some array representing a vertical/horizontal/diagonal direction
     * on the board, calculate a score based on how many possible 5's can be 
     * formed for a player and in how many moves.
     * @param direction A 1D array representing a direction on the board
     * @param index The player index to check (1 or 2)
     * @return Score for this direction
     */
    public static int scoreDirection(int[] direction, int index) {
        int score = 0;
        // Scores for making a 5
        int[] scores = {19, 15, 11, 7, 3};
        
        for(int i = 0; i < direction.length; i++) {
            if(i + 4 < direction.length) {
                int stones = 0;
                int empty = 0;
                // Pass a window of 5 across the direction and check how many
                // stones and empty spots there are
                for(int j = 0; j <= 4; j++) {
                    if(direction[i + j] == index) stones++;
                    else if(direction[i + j] == 0) empty++;
                }
                if(stones == 5) {
                    return 30000;
                }
                // First check if it's possible to form a 5 in this window
                if(stones + empty == 5 && empty != 5) {
                    // Amount of empty spots = # of moves needed to make a 5
                    score += scores[empty];
                }
            }
        }
        return score;
    }
    
    /**
     * Convert a board row to its board representation (15, 14, 13, 12..)
     * @param row
     * @return
     */
    private int convertRow(int row) {
        return this.intersections - row;
    }
    
    /**
     * Convert a board column to its board representation (A, B, C, D...)
     * @param col
     * @return
     */
    public String convertCol(int col) {
        return String.valueOf((char)((col + 1) + 'A' - 1));
    }
    
    /*
     * Precompute all the possible scores for every direction around a piece,
     * and record any threats found.
     */
    static {
        Map<String, int[]> THREATS = new HashMap<>();
        // Four
        THREATS.put("01111", new int[] { 0 });
        THREATS.put("10111", new int[] { 1 });
        THREATS.put("11011", new int[] { 2 });
        THREATS.put("11101", new int[] { 3 });
        THREATS.put("11110", new int[] { 4 });
        THREATS.put("02222", new int[] { 0 });
        THREATS.put("20222", new int[] { 1 });
        THREATS.put("22022", new int[] { 2 });
        THREATS.put("22202", new int[] { 3 });
        THREATS.put("22220", new int[] { 4 });
        // Three
        THREATS.put("0011100", new int[] { 1, 5 });
        THREATS.put("0022200", new int[] { 1, 5 });
        // Broken three
        THREATS.put("011010", new int[] { 0, 3, 5 });
        THREATS.put("010110", new int[] { 0, 2, 5 });
        THREATS.put("022020", new int[] { 0, 3, 5 });
        THREATS.put("020220", new int[] { 0, 2, 5 });
        
        THREAT_PATTERNS = new ThreatPattern[2][4][4][4][4][4][4][4][4][4];
        SCORES = new int[2][4][4][4][4][4][4][4][4][4];
        
        // Generate all possible numbers of length 9 in radix 4 (0,1,2,3)
        for(int i = 0; i < 262144; i++) {
            String numStr = String.format("%9s", Integer.toString(i, 4))
                    .replace(" ", "0");
            // Convert radix 4 string to integer array
            int[] numArray = new int[numStr.length()];
            for(int j = 0; j < numArray.length; j++) {
                numArray[j] = Character.getNumericValue(numStr.charAt(j));
            }
            // Calculate heuristic score for player 1 and 2
            int[] score = new int[] {
                scoreDirection(numArray, 1), 
                scoreDirection(numArray, 2)
            };
            
            // Place scores in the lookup array
            if(score[0] > 0) {
                // Check for threats in this pattern, record it along with the
                // defensive moves and offset
                if(score[0] != 30000) {
                    Iterator it = THREATS.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        int index = numStr.indexOf((String) pair.getKey());
                        if(index != -1) {
                            THREAT_PATTERNS[0][numArray[0]][numArray[1]]
                                    [numArray[2]][numArray[3]]
                                    [numArray[4]][numArray[5]]
                                    [numArray[6]][numArray[7]]
                                    [numArray[8]] = new ThreatPattern((int[]) 
                                            pair.getValue(), index);
                        }
                    }
                }
                SCORES[0][numArray[0]][numArray[1]][numArray[2]][numArray[3]]
                        [numArray[4]][numArray[5]][numArray[6]][numArray[7]]
                        [numArray[8]] = score[0];
            }
            if(score[1] > 0) {
                // Check for threats in this pattern, record it along with the
                // defensive moves and offset
                if(score[1] != 30000) {
                    Iterator it = THREATS.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        int index = numStr.indexOf((String) pair.getKey());
                        if(index != -1) {
                            THREAT_PATTERNS[1][numArray[0]][numArray[1]]
                                    [numArray[2]][numArray[3]]
                                    [numArray[4]][numArray[5]]
                                    [numArray[6]][numArray[7]]
                                    [numArray[8]] = new ThreatPattern((int[]) 
                                            pair.getValue(), index);
                        }
                    }
                }
                SCORES[1][numArray[0]][numArray[1]][numArray[2]][numArray[3]]
                        [numArray[4]][numArray[5]][numArray[6]][numArray[7]]
                        [numArray[8]] = score[1];
            }
        }
    }
    
}