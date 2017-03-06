package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Minimax player, with alpha-beta pruning and a simple evaluation function. 
 * Can search up to a depth of 4 in a reasonable amount of time.
 * @author Hassan
 */
public class MinimaxPlayer extends GomokuPlayer {
    
    private GomokuMove bestMove;
    
    public MinimaxPlayer(int playerIndex, int opponentIndex) {
        super(playerIndex, opponentIndex);
    }
    
    // Too many moves are still evaluated, even after being pruned.
    
    /**
     * Prune moves by focusing on areas where stones already exist to reduce
     * the search space, and sort the nodes using a heuristic evaluation
     * @param state State to find moves for
     * @return A list of pruned moves
     */
    public List<GomokuMove> pruneMoves(GomokuState state) {
        int[][] board = state.getBoardArray();
        List<GomokuMove> moves = state.getMoves();
        
        // Comparator for our TreeSet, use the heuristic evaluation to sort
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
        
        // Use a TreeSet - no duplicates, and sorted by heuristic score
        Set<GomokuMove> prunedMoves = new TreeSet<>(stateCompare);
        
        // Board is empty, we have to make an opening move
        if(moves.size() == board.length * board.length) {
            prunedMoves.add(new GomokuMove(board.length / 2, board.length / 2));
            return new ArrayList(prunedMoves);
        }
        
        // Focus on moves that occur up to k intersections around an existing
        // stone on the board
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
        
        return new ArrayList(prunedMoves);
    }
    
    /**
     * Return the diagonals of a 2D array as a list of 1D integer arrays
     * @param board Board array
     * @return List of 1D diagonals in the 2D array
     * length
     */
    private List<int[]> getDiagonals(int[][] array) {
        List<int[]> diagonals = new ArrayList<>();
        
        // Loop the top half of the diagonals, moving to the left
        for(int i = 0; i < array.length; i++) {
            if(i >= 5 - 1) {
                int rowlength = i + 1;
                int[] diagonal = new int[rowlength];
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[j][i - j];
                }
                diagonals.add(diagonal);
            }
        }
        
        // Loop the bottom half of the diagonals, moving to the left
        for(int i = 1; i < array.length; i++) {
            if(i <= array.length - 5) {
                int rowlength = array.length - i;
                int[] diagonal = new int[rowlength];
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[i + j][array.length - 1 - j];
                }
                diagonals.add(diagonal);
            }
        }
        
        // Loop the top half of the diagonals, moving to the right
        for(int i = 0; i < array.length; i++) {
            int rowlength = array.length - i;
            if(rowlength >= 5) {
                int[] diagonal = new int[rowlength];
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[j][i + j];
                }
                diagonals.add(diagonal);
            }
        }
        
        // Loop the bottom half of the diagonals, moving to the right
        for(int i = 1; i < array.length; i++) {
            int rowlength = array.length - i;
            if(rowlength >= 5) {
                int[] diagonal = new int[rowlength];
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[i + j][j];
                }
                diagonals.add(diagonal);
            }
        }
        
        return diagonals;
    }
    
    /**
     * Return the columns of a 2D array as a list of 1D integer arrays
     * @param board Board array
     * @return List of 1D columns in the 2D array
     */
    private List<int[]> getColumns(int[][] board) {
        List<int[]> columns = new ArrayList<>();
        
        for(int j = 0; j < board.length; j++) {
            int[] column = new int[board.length];
            for(int i = 0; i < board.length; i++) {
                column[i] = board[i][j];
            }
            columns.add(column);
        }
        return columns;
    }
    
    /**
     * Return the columns of a 2D array as a list of 1D integer arrays
     * @param board Board array
     * @return List of 1D rows in the 2D array
     */
    private List<int[]> getRows(int[][] board) {        
        List<int[]> rows = new ArrayList<>();

        for(int i = 0; i < board.length; i++) {
            int[] row = new int[board.length];
            for(int j = 0; j < board.length; j++) {
                row[j] = board[i][j];
            }
            rows.add(row);
        }
        return rows;
    }
    
    /**
     * Run the minimax algorithm up to a certain depth, with alpha-beta
     * pruning.
     * @param state Starting state
     * @param depth How deep to search the tree
     * @param alpha Best possible value for the maximising player
     * @param beta Best possible value for the minimising player
     * @return
     */
    private int minimax(GomokuState state, int depth, int alpha, int beta) {
        // Leaf nodes/terminal nodes need to return a heuristic evaluation
        if(depth == 0 || state.isTerminal()) {
            return evaluateState(state);
        }
        
        // Max's turn
        if(state.getCurrentIndex() == this.playerIndex) {
            // Get the maximum of the child states
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                // Found a better move
                if(score > alpha) {
                    if(depth == 4) this.bestMove = move;
                    alpha = score;
                }
                // We don't need to continue this branch, because min can
                // already do better elsewhere
                if(alpha >= beta) {
                    return alpha;
                }
            }
            return alpha;
        }
        // Min's turn
        else {
            // Get the minimum of the child states
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, beta);
                state.undoMove(move);
                // Found a better move
                if(score < beta) {
                    beta = score;
                }
                // We don't need to continue this branch, because max
                // can already do better elsewhere
                if(alpha >= beta) {
                    return beta;
                }
            }
            return beta;
        }
    }
    
    // Evaluation takes too long. Need to look into transposition tables, and
    // maybe bit boards could help for faster pattern matching
    
    /**
     * Evaluate a state, count how many 5/4/3/2/1's we have, and subtract
     * from how many our opponent has.
     * @param state State to evaluate
     * @return Score of the state
     */
    private int evaluateState(GomokuState state) {
        int[][] board = state.getBoardArray();
        
        List<int[]> axes = new ArrayList<>();
        List<int[]> diagonals = getDiagonals(board);
        List<int[]> columns = getColumns(board);
        List<int[]> rows = getRows(board);
        
        axes.addAll(diagonals);
        axes.addAll(columns);
        axes.addAll(rows);
        
        // Store the patterns we find, maximum row of consecutive stones is 7
        int[] patterns_opponent = new int[7];
        int[] patterns_player = new int[7];
        
        // For every diagonal, column, and row
        for(int[] array : axes) {
            for(int i = 0; i < array.length; i++) {
                if(array[i] == playerIndex || array[i] == opponentIndex) {
                    int index = array[i];
                    int count = 1;
                    // Count consecutive stones belonging to the index
                    for(int j = i + 1; j < array.length; j++) {
                        if(array[j] == index) count++;
                        else break;
                    }
                    int open = 0;
                    // Check if this row of stones is open on the left/right
                    if(i - 1 >= 0 && array[i - 1] == 0) open++;
                    if(i + count < array.length && array[i + count] == 0) open++;
                    
                    // Increment our patterns array, giving double open rows
                    // a bigger weight, and ignoring closed rows
                    if(index == playerIndex) {
                        if(count <= 4) {
                            patterns_player[count - 1] += open;
                        } else {
                            patterns_player[count - 1]++;
                        }
                    } else {
                        if(count <= 4) {
                            patterns_opponent[count - 1] += open;
                        } else {
                            patterns_opponent[count - 1]++;
                        }
                    }

                    // Move to the next index
                    i = i + count - 1;
                }
            }
        }
        
        return  + (patterns_player[6] * 15000)
                + (patterns_player[5] * 15000)
                + (patterns_player[4] * 15000)
                + (patterns_player[3] * 2500)
                + (patterns_player[2] * 50)
                + (patterns_player[1] * 5)
                + (patterns_player[0] * 1)
                - (patterns_opponent[6] * 15000)
                - (patterns_opponent[5] * 15000)
                - (patterns_opponent[4] * 15000)
                - (patterns_opponent[3] * 2500)
                - (patterns_opponent[2] * 50)
                - (patterns_opponent[1] * 5)
                - (patterns_opponent[0] * 1);
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        minimax(state, 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return bestMove;
    }
    
}