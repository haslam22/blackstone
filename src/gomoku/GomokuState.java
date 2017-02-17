package gomoku;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the state of a Gomoku game at any point.
 * @author Hassan
 */
public class GomokuState {
    
    protected final int[][] board;
    private final int playerIndex;
    private final int intersections;
    
    /**
     * Create a new GomokuState instance
     * @param intersections Number of intersections on the board
     */
    public GomokuState(int intersections) {
        this.intersections = intersections;
        this.board = new int[intersections][intersections];
        this.playerIndex = 1;
    }    
    
    /**
     * Copy constructor for GomokuState, to apply a new move
     * @param previousState The previous state
     * @param move The move to make
     */
    private GomokuState(GomokuState previousState, GomokuLocation move) {
        this.intersections = previousState.intersections;
        this.board = new int[intersections][intersections];
        // Copy the previous state
        for(int i = 0; i < intersections; i++) {
            System.arraycopy(previousState.board[i], 0, this.board[i], 0, 
                    intersections);
        }
        // Apply the move and update the current player
        this.board[move.row][move.col] = previousState.playerIndex;
        this.playerIndex = previousState.playerIndex == 1 ? 2 : 1;
    }
    
    /**
     * Calculate the legal moves (unoccupied spaces) on the board
     * @return A list of legal moves
     */
    public List<GomokuLocation> getLegalMoves() {
        List<GomokuLocation> moves = new ArrayList<>();
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] == 0) {
                    moves.add(new GomokuLocation(i, j));
                }
            }
        }
        return moves;
    }
    
    /**
     * Make a move on the current state, returning a new state with that
     * move applied.
     * @param move The move to make
     * @return
     */
    public GomokuState makeMove(GomokuLocation move) {
        return new GomokuState(this, move);
    }
    
    /**
     * Return the index of the current player
     * @return
     */
    public int getCurrentPlayerIndex() {
        return this.playerIndex;
    }
    
    /**
     * Determine if the specified player has won the game
     * @param index Player index
     * @return
     */
    public boolean isWinner(int index) {
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                if(board[i][j] == index) {
                    if(searchVerticalWin(i, j, index)) return true;
                    if(searchHorizontalWin(i, j, index)) return true;
                    if(searchDiagonalLeftWin(i, j, index)) return true;
                    if(searchDiagonalRightWin(i, j, index)) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determine if the board is full, to check if the game has ended in a draw
     * @return
     */
    public boolean isFull() {
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                if(board[i][j] == 0) return false;
            }
        }
        return true;
    }
    
    /**
     * Search vertically for a winning sequence at a specific point
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @return
     */
    public boolean searchVerticalWin(int row, int col, int index) {
        if(row + 4 < intersections) {
            int count = 0;
            for(int k = 1; k <= 4; k++) {
                if(board[row+k][col] == index) {
                    count++;
                }
            }
            return count == 4;
        }
        return false;
    }    
    
    /**
     * Search horizontally for a winning sequence at a specific point
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @return
     */
    public boolean searchHorizontalWin(int row, int col, int index) {
        if(col + 4 < intersections) {
            int count = 0;
            for(int k = 1; k <= 4; k++) {
                if(board[row][col+k] == index) {
                    count++;
                }
            }
            return count == 4;
        }
        return false;
    }    
    
    /**
     * Search diagonally and down to the right for a winning sequence at a 
     * specific point
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @return
     */
    public boolean searchDiagonalRightWin(int row, int col, int index) {
        if(col + 4 < intersections && row + 4 < intersections) {
            int count = 0;
            for(int k = 1; k <= 4; k++) {
                if(board[row+k][col+k] == index) {
                    count++;
                }
            }
            return count == 4;
        }
        return false;
    }
    
    /**
     * Search diagonally and down to the left for a winning sequence at a
     * specific point
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @return
     */
    public boolean searchDiagonalLeftWin(int row, int col, int index) {
        if(col - 4 >= 0 && row + 4 < intersections) {
            int count = 0;
            for(int k = 1; k <= 4; k++) {
                if(board[row+k][col-k] == index) {
                    count++;
                }
            }  
            return count == 4;
        }
        return false;
    }
    
}
