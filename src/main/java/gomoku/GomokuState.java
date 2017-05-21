package gomoku;

import java.util.Stack;

/**
 * State representing an ongoing Gomoku game. Modified only by the game process, 
 * AI players can read the state of the game from this object but not modify it.
 * @author Hassan
 */
public class GomokuState {
    
    private final int[][] board;
    private final int intersections;
    private final Stack<GomokuMove> moveHistory;
    private int currentIndex;
    private int movesCount;
    
    /**
     * Create a new GomokuState instance
     * @param intersections Number of intersections on the board
     */
    protected GomokuState(int intersections) {
        this.intersections = intersections;
        this.board = new int[intersections][intersections];
        this.currentIndex = 1;
        this.moveHistory = new Stack<>();
    }
    
    /**
     * Apply a move to this state. Must be unoccupied.
     * @param move Move to apply
     */
    protected void makeMove(GomokuMove move) {
        if(board[move.row][move.col] == 0) {
            movesCount++;
            this.board[move.row][move.col] = this.currentIndex;
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.push(move);
        }
    }
    
    /**
     * Undo a move on this state. Must be the previous move applied to this
     * state.
     * @param move Move to undo
     */
    protected void undoMove(GomokuMove move) {
        if(moveHistory.peek().equals(move)) {
            movesCount--;
            this.board[move.row][move.col] = 0;
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.pop();
        }
    }
    
    /**
     * Check if this state is a terminal state (game is over)
     * @return Returns 0 (Not terminal), 1 (Player 1 wins), 2 (Player 2 wins) or
     * 3 (Board is full, draw)
     */
    public int terminal() {
        if(isWinner(1)) return 1;
        else if(isWinner(2)) return 2;
        else if(movesCount == intersections * intersections) return 3;
        else return 0;
    }
    
    /**
     * Return the index of the player who is required to make a move.
     * @return Player index (1 or 2)
     */
    protected int getCurrentIndex() {
        return this.currentIndex;
    }
    
    public int getIntersectionIndex(int row, int col) {
        return this.board[row][col];
    }
    
    public Stack<GomokuMove> getMoveHistory() {
        Stack<GomokuMove> moveHistoryTemp = new Stack<>();
        moveHistoryTemp.addAll(moveHistory);
        return moveHistoryTemp;
    }
    
    /**
     * Return the last move that was made on this state.
     * @return Move on top of the stack, or null if no moves have been made
     */
    public GomokuMove getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.peek();
    }
    
    /**
     * Determine if the specified player has won the game
     * @param index Player index (1 or 2)
     * @return True if the index has won
     */
    private boolean isWinner(int index) {
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                if(board[i][j] == index) {
                    if(searchVertical(i, j, index, 4)) return true;
                    if(searchHorizontal(i, j, index, 4)) return true;
                    if(searchDiagonalLeft(i, j, index, 4)) return true;
                    if(searchDiagonalRight(i, j, index, 4)) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Search vertically for a sequence of stones belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchVertical(int row, int col, int index, int amount) {
        if(row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }    
    
    /**
     * Search horizontally for a sequence of stones belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchHorizontal(int row, int col, int index, int amount) {
        if(col + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row][col+k] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }    
    
    /**
     * Search diagonally and down to the right for a sequence of stones 
     * belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchDiagonalRight(int row, int col, int index, 
            int amount) {
        if(col + amount < intersections && row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col+k] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }
    
    /**
     * Search diagonally and down to the left for a sequence of stones 
     * belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchDiagonalLeft(int row, int col, int index, int amount) {
        if(col - amount >= 0 && row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col-k] == index) {
                    count++;
                }
            }  
            return count == amount;
        }
        return false;
    }
    
}
