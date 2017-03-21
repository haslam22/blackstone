package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Minimax player, with alpha-beta pruning and a simple evaluation function. 
 * Can search up to a depth of 8 in a reasonable amount of time.
 * @author Hassan
 */
public class MinimaxPlayer extends GomokuPlayer {
    
    private GomokuMove bestMove;
    
    public MinimaxPlayer(int playerIndex, int opponentIndex) {
        super(playerIndex, opponentIndex);
    }
    
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
        for(int k = 1; k <= 1; k++) {
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
        
        // Evaluate the best 10 moves on each level, and ignore all others
        
        // Sort by worst heuristic scores first if opponent
        if(state.getCurrentIndex() == this.opponentIndex) {
            List<GomokuMove> prunedList = new ArrayList(prunedMoves);
            return prunedList.size() > 10 ? prunedList.subList(0, 10) 
                    : prunedList;
        // Sort by best heuristic scores first if player (reverse the list)
        } else {
            List<GomokuMove> prunedList = new ArrayList(prunedMoves);
            Collections.reverse(prunedList);
            return prunedList.size() > 10 ? prunedList.subList(0, 10) 
                    : prunedList;
        }
    }
    
    /**
     * Return the diagonals, columns and rows of a 2D array as a list of 1D 
     * arrays for more readable processing.
     * @param array Input array
     * @return List of 1D arrays for each row/column/diagonal
     * length
     */
    private List<int[]> getAxes(int[][] array) {
        List<int[]> axes = new ArrayList<>();
        
        // Loop the top half of the diagonals, moving to the left
        for(int i = 0; i < array.length; i++) {
            if(i >= 5 - 1) {
                int rowlength = i + 1;
                int[] diagonal = new int[rowlength];
                boolean empty = true;
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[j][i - j];
                    if(diagonal[j] != 0) empty = false;
                }
                if(!empty) axes.add(diagonal);
            }
        }
        
        // Loop the bottom half of the diagonals, moving to the left
        for(int i = 1; i < array.length; i++) {
            if(i <= array.length - 5) {
                int rowlength = array.length - i;
                int[] diagonal = new int[rowlength];
                boolean empty = true;
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[i + j][array.length - 1 - j];
                    if(diagonal[j] != 0) empty = false;
                }
                if(!empty) axes.add(diagonal);
            }
        }
        
        // Loop the top half of the diagonals, moving to the right
        for(int i = 0; i < array.length; i++) {
            int rowlength = array.length - i;
            if(rowlength >= 5) {
                int[] diagonal = new int[rowlength];
                boolean empty = true;
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[j][i + j];
                    if(diagonal[j] != 0) empty = false;
                }
                if(!empty) axes.add(diagonal);
            }
        }
        
        // Loop the bottom half of the diagonals, moving to the right
        for(int i = 1; i < array.length; i++) {
            int rowlength = array.length - i;
            if(rowlength >= 5) {
                int[] diagonal = new int[rowlength];
                boolean empty = true;
                for(int j = 0; j < rowlength; j++) {
                    diagonal[j] = array[i + j][j];
                    if(diagonal[j] != 0) empty = false;
                }
                if(!empty) axes.add(diagonal);
            }
        }
        
        // Loop the columns
        for(int j = 0; j < array.length; j++) {
            int[] column = new int[array.length];
            boolean empty = true;
            for(int i = 0; i < array.length; i++) {
                column[i] = array[i][j];
                if(column[i] != 0) empty = false;
            }
            if(!empty) axes.add(column);
        }
        
        // Loop the rows
        for(int i = 0; i < array.length; i++) {
            int[] row = new int[array.length];
            boolean empty = true;
            for(int j = 0; j < array.length; j++) {
                row[j] = array[i][j];
                if(row[j] != 0) empty = false;
            }
            if(!empty) axes.add(row);
        }
        
        return axes;
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
            int maximum = alpha;
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, maximum, beta);
                state.undoMove(move);
                if(score > maximum) {
                    if(depth == 8) bestMove = move;
                    maximum = score;
                }
                if(beta <= maximum) break;
            }
            return maximum;
        }
        else {
            int minimum = beta;
            for(GomokuMove move : pruneMoves(state)) {
                state.makeMove(move);
                int score = minimax(state, depth - 1, alpha, minimum);
                state.undoMove(move);
                if(score < minimum) {
                    minimum = score;
                }
                if(minimum <= alpha) break;
            }
            return minimum;
        }
    }
    
    /**
     * Evaluate a state, count how many 5/4/3/2/1's we have, and subtract
     * from how many our opponent has.
     * @param state State to evaluate
     * @return Score of the state
     */
    private int evaluateState(GomokuState state) {
        int[][] board = state.getBoardArray();
        List<int[]> axes = getAxes(board);
        
        // Store the patterns we find, rows of 1-5
        int[] patterns_opponent = new int[5];
        int[] patterns_player = new int[5];
        
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
                    // a bigger weight
                    if(index == playerIndex) {
                        if(count <= 4) {
                            patterns_player[count - 1] += open;
                        } else {
                            patterns_player[4]++;
                        }
                    } else {
                        if(count <= 4) {
                            patterns_opponent[count - 1] += open;
                        } else {
                            patterns_opponent[4]++;
                        }
                    }
                    i = i + count - 1;
                }
            }
        }
        
        return + (patterns_player[4] * 15000)
                + (patterns_player[3] * 2500)
                + (patterns_player[2] * 50)
                + (patterns_player[1] * 5)
                + (patterns_player[0] * 1)
                - (patterns_opponent[4] * 15000)
                - (patterns_opponent[3] * 2500)
                - (patterns_opponent[2] * 50)
                - (patterns_opponent[1] * 5)
                - (patterns_opponent[0] * 1);
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        System.out.println("Best score: " + minimax(state, 8, Integer.MIN_VALUE, 
                Integer.MAX_VALUE));
        System.out.println("Best move: " + bestMove.row + ", " + bestMove.col);
        return bestMove;
    }
    
}