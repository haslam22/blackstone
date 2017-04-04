package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Minimax player, with alpha-beta pruning and a simple evaluation function. 
 * Can search up to a depth of 8 in a reasonable amount of time.
 * @author Hassan
 */

public class MinimaxPlayer extends GomokuPlayer {
    
    // Create a HashMap with a limited capacity to store state evaluation
    // values. Each time a value is accessed, it gets moved to the top of the
    // list. Least accessed values get removed once the capacity is reached
    private class TranspositionTable extends LinkedHashMap {
        private final int capacity;

        public TranspositionTable(int capacity) {
            super(capacity + 1, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return this.size() > capacity;
        }
    }
    
    private final TranspositionTable transpositionTable;
    
    public MinimaxPlayer(int playerIndex, int opponentIndex) {
        super(playerIndex, opponentIndex);
        this.transpositionTable = new TranspositionTable(1000000);
    }
    
    /**
     * Prune moves by focusing on areas where stones already exist to reduce
     * the search space, and sort the nodes using the evaluation function
     * @param state State to find moves for
     * @return A list of pruned moves
     */
    public List<GomokuMove> pruneMoves(GomokuState state) {
        int[][] board = state.getBoardArray();
        List<GomokuMove> moves = state.getMoves();        
        
        Comparator<GomokuMove> stateCompare = new Comparator<GomokuMove>() {
            @Override
            public int compare(GomokuMove move1, GomokuMove move2) {
                state.makeMove(move1);
                int move1heuristic = evaluateState(state);
                state.undoMove(move1);
                state.makeMove(move2);
                int move2heuristic = evaluateState(state);
                state.undoMove(move2);
                return move1heuristic - move2heuristic;
            }
        };
        
        // Store the pruned moves, avoid duplicates
        HashSet<GomokuMove> prunedMoves = new HashSet<>();
        
        // Board is empty, we have to make an opening move
        if(moves.size() == board.length * board.length) {
            prunedMoves.add(new GomokuMove(board.length / 2, board.length / 2));
            return new ArrayList(prunedMoves);
        }
        
        // Focus on moves that occur up to k intersections around an existing
        // stone on the board (avoid evaluating moves too far away)
        for(int k = 1; k <= 2; k++) {
            for(int i = 0; i < board.length; i++) {
                for(int j = 0; j < board.length; j++) {
                    if(board[i][j] != 0) {
                        // Left
                        if(j-k >= 0 && board[i][j-k] == 0) {
                            prunedMoves.add(new GomokuMove(i, j-k));
                        }
                        // Right
                        if(j+k < board.length && board[i][j+k] == 0) {
                            prunedMoves.add(new GomokuMove(i, j+k));
                        }
                        // Top
                        if(i-k >= 0 && board[i-k][j] == 0) {
                            prunedMoves.add(new GomokuMove(i-k, j));
                        }
                        // Bottom
                        if(i+k < board.length && board[i+k][j] == 0) {
                            prunedMoves.add(new GomokuMove(i+k, j));
                        }
                        // Diagonal up and left
                        if(i-k >= 0 && j-k >= 0 && board[i-k][j-k] == 0) {
                            prunedMoves.add(new GomokuMove(i-k, j-k));
                        }
                        // Diagonal up and right
                        if(i-k >= 0 && j+k < board.length 
                                && board[i-k][j+k] == 0) {
                            prunedMoves.add(new GomokuMove(i-k, j+k));
                        }
                        // Diagonal down and left
                        if(i+k < board.length && j-k >= 0 
                                && board[i+k][j-k] == 0) {
                            prunedMoves.add(new GomokuMove(i+k, j-k));
                        }
                        // Diagonal down and right
                        if(i+k < board.length && j+k < board.length 
                                && board[i+k][j+k] == 0) {
                            prunedMoves.add(new GomokuMove(i+k, j+k));
                        }
                    }
                }
            }
        }
        
        // Return the 10 best moves on each level to reduce branching factor
        
        // Sort by worst heuristic scores first if opponent
        if(state.getCurrentIndex() == this.opponentIndex) {
            List<GomokuMove> prunedList = new ArrayList(prunedMoves);
            prunedList.sort(stateCompare);
            return prunedList.size() > 10 ? prunedList.subList(0, 10) 
                    : prunedList;
        // Sort by best heuristic scores first if player (reverse the list)
        } else {
            List<GomokuMove> prunedList = new ArrayList(prunedMoves);
            prunedList.sort(stateCompare);
            Collections.reverse(prunedList);
            return prunedList.size() > 10 ? prunedList.subList(0, 10) 
                    : prunedList;
        }
    }
    
    /**
     * Evaluate the stone at this position, returning a value based on how 
     * possible this stone can be used to form 5's.
     * We look around the stone in every direction (horizontal, vertical,
     * 2x diagonal) and assign a score to each direction individually, based
     * on how many 5's we can create in this direction and in how many moves.
     * The evaluation for this stone is then the sum of all the directions.
     * @param board
     * @param row
     * @param col
     * @return 
     */
    public int evaluateStone(int[][] board, int row, int col) {
        // Record the index of the current stone
        int stoneIndex = board[row][col];
        int score = 0;
        
        // Diagonal, vertical, and horizontal directions
        int[] diag1 = new int[] { -1, -1, -1, -1, stoneIndex, -1, -1, -1, -1 };
        int[] diag2 = new int[] { -1, -1, -1, -1, stoneIndex, -1, -1, -1, -1 };
        int[]  vert = new int[] { -1, -1, -1, -1, stoneIndex, -1, -1, -1, -1 };
        int[] horiz = new int[] { -1, -1, -1, -1, stoneIndex, -1, -1, -1, -1 };
        
        // Diagonal 1, top left
        for(int i = 1; i < 5; i++) {
            if(row - i >= 0 && col - i >=0) {
                if(board[row - i][col - i] == 0) {
                    diag1[4 - i] = board[row - i][col - i];
                } else if(board[row - i][col - i] == stoneIndex) {
                    diag1[4 - i] = board[row - i][col - i];
                } else {
                    break;
                }
            }
        }
        
        // Diagonal 1, bottom right
        for(int i = 1; i < 5; i++) {
            if(row + i < board.length && col + i < board.length) {
                if(board[row + i][col + i] == 0) {
                    diag1[4 + i] = board[row + i][col + i];
                } else if(board[row + i][col + i] == stoneIndex) {
                    diag1[4 + i] = board[row + i][col + i];
                } else {
                    break;
                }
            }
        }
        
        // Diagonal 2, top right
        for(int i = 1; i < 5; i++) {
            if(row - i >= 0 && col + i < board.length) {
                if(board[row - i][col + i] == 0) {
                    diag2[4 - i] = board[row - i][col + i];
                } else if(board[row - i][col + i] == stoneIndex) {
                    diag2[4 - i] = board[row - i][col + i];
                } else {
                    break;
                }
            }
        }
        
        // Diagonal 2, bottom left
        for(int i = 1; i < 5; i++) {
            if(row + i < board.length && col - i >=0) {
                if(board[row + i][col - i] == 0) {
                    diag2[4 + i] = board[row + i][col - i];
                } else if(board[row + i][col - i] == stoneIndex) {
                    diag2[4 + i] = board[row + i][col - i];
                } else {
                    break;
                }
            }
        }
        
        // Vertical top
        for(int i = 1; i < 5; i++) {
            if(row - i >= 0) {
                if(board[row - i][col] == 0) {
                    vert[4 - i] = board[row - i][col];
                } else if(board[row - i][col] == stoneIndex) {
                    vert[4 - i] = board[row - i][col];
                } else {
                    break;
                }
            }
        }        

        // Vertical bottom
        for(int i = 1; i < 5; i++) {
            if(row + i < board.length) {
                if(board[row + i][col] == 0) {
                    vert[4 + i] = board[row + i][col];
                } else if(board[row + i][col] == stoneIndex) {
                    vert[4 + i] = board[row + i][col];
                } else {
                    break;
                }
            }
        }        
        
        // Horizontal left
        for(int i = 1; i < 5; i++) {
            if(col - i >= 0) {
                if(board[row][col - i] == 0) {
                    horiz[4 - i] = board[row][col - i];
                } else if(board[row][col - i] == stoneIndex) {
                    horiz[4 - i] = board[row][col - i];
                } else {
                    break;
                }
            }
        }         

        // Horizontal right
        for(int i = 1; i < 5; i++) {
            if(col + i < board.length) {
                if(board[row][col + i] == 0) {
                    horiz[4 + i] = board[row][col + i];
                } else if(board[row][col + i] == stoneIndex) {
                    horiz[4 + i] = board[row][col + i];
                } else {
                    break;
                }
            }
        }
        
        score+= scoreDirection(diag1, stoneIndex);
        score+= scoreDirection(diag2, stoneIndex);
        score+= scoreDirection(vert, stoneIndex);
        score+= scoreDirection(horiz, stoneIndex);
        return score;
    }
    
    /**
     * Given some array representing a vertical/horizontal/diagonal direction
     * on the board, calculate a score based on how many 5's can be formed
     * and in how many moves.
     * @param direction A 1D array representing a direction on the board,
     * of any length >=5
     * @param index The player index to check (1 or 2)
     * @return Score for this direction
     */
    public int scoreDirection(int[] direction, int index) {
        int score = 0;
        // Scores for making a 5 in 4, 3, 2, 1 and 0 moves
        int[] scores = {19, 15, 11, 7, 3};
        
        for(int i = 0; i < direction.length; i++) {
            if(i + 4 < direction.length) {
                int stones = 0;
                int empty = 0;
                // Pass a window of 5 across the direction and check how many
                // stones and empty spots there are. 
                for(int j = 0; j <= 4; j++) {
                    if(direction[i + j] == index) stones++;
                    if(direction[i + j] == 0) empty++;
                }
                // First check if it's possible to form a 5 in this window
                if(stones + empty == 5) {
                    // Amount of empty spots = # of moves needed to make a 5
                    score += scores[empty];
                }
            }
        }
        return score;
    }
    
    /**
     * Run the minimax algorithm up to a certain depth, with alpha-beta
     * pruning.
     * @param state Starting state
     * @param depth How deep to search the tree
     * @param alpha Best possible value for the maximising player so far
     * @param beta Best possible value for the minimising player so far
     * @return
     */
    private int minimax(GomokuState state, int depth, int alpha, int beta) {
        if(depth == 0 || state.isTerminal()) {
            return evaluateState(state);
        }
        else if(state.getCurrentIndex() == this.playerIndex) {
            int maximum = Integer.MIN_VALUE;
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score > maximum) {
                    maximum = score;
                }
                alpha = Math.max(alpha, maximum);
                if(beta <= alpha) break;
            }
            return maximum;
        }
        else {
            int minimum = Integer.MAX_VALUE;
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                if(score < minimum) {
                    minimum = score;
                }
                beta = Math.min(beta, minimum);
                if(beta <= alpha) break;
            }
            return minimum;
        }
    }
    
    /**
     * Evaluate a state by evaluating each stone individually for each player,
     * returning a score based on how many possible 5's pass through the stone,
     * and in how many moves.
     * @param state State to evaluate
     * @return Score of the state
     */
    private int evaluateState(GomokuState state) {
        if(transpositionTable.get(state.getZobristHash()) != null) {
            return (int) transpositionTable.get(state.getZobristHash());
        }
        
        // Check for a winning/losing situation first
        if(state.isWinner(this.opponentIndex)) {
            transpositionTable.put(state.getZobristHash(), -10000);
            return -10000;
        }
        else if(state.isWinner(this.playerIndex)) {
            transpositionTable.put(state.getZobristHash(), 10000);
            return 10000;
        }
        
        // No winning situation found, evaluate the state and return an 
        // estimate of the value
        int score = 0;
        int[][] board = state.getBoardArray();
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] != 0) {
                    // Evaluate each stone separately, minus score for opponent
                    // and increase score for us
                    if(board[i][j] == this.opponentIndex) {
                        score -= evaluateStone(board, i, j);
                    } else {
                        score += evaluateStone(board, i, j);
                    }
                }
            }
        }
        
        transpositionTable.put(state.getZobristHash(), score);
        return score;
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        this.transpositionTable.clear();
        long startTime = System.currentTimeMillis();
        
        // Initial moves for this state
        List<GomokuMove> moves = pruneMoves(state);
        
        int depth = 8;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore = alpha;
        GomokuMove bestMove = new GomokuMove();

        // Run minimax for all the children (initial moves)
        for(GomokuMove move : moves) {
            state.makeMove(move);
            int score = minimax(state, depth - 1, alpha, beta);
            state.undoMove(move);
            if(score > bestScore) {
                // Save the best move found so far
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Evaluation: " + bestScore);
        System.out.println("Time taken: " + duration + "ms");
        return bestMove;
    }
    
}