package gomoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * A class representing the state of a Gomoku game at any point.
 * @author Hassan
 */
public class GomokuState {
    
    private final int[][] board;
    private final int intersections;
    private final Stack<GomokuMove> moveHistory;
    private int currentIndex;
    
    private long zobristHash;
    private long[][][] zobristKeys;
    
    /**
     * Generate random bit strings (64-bit) for every board position, and for
     * every possible state that particular board position can be in
     */
    private void generateZobristKeys() {
        Random keyGenerator = new Random();
        for(int i = 0; i < zobristKeys.length; i++) {
            for(int j = 0; j < zobristKeys[0].length; j++) {
                for(int k = 0; k < zobristKeys[0][0].length; k++) {
                    zobristKeys[i][j][k] = keyGenerator.nextLong();
                }
            }
        }
    }
    
    /**
     * Create a new GomokuState instance
     * @param intersections Number of intersections on the board
     */
    public GomokuState(int intersections) {
        this.intersections = intersections;
        this.board = new int[intersections][intersections];
        this.currentIndex = 1;
        this.moveHistory = new Stack<>();
        this.zobristKeys = new long[2][intersections][intersections];
        this.zobristHash = 0;
        generateZobristKeys();
    }
    
    /**
     * Deep copy constructor for GomokuState.
     * @param previousState State to copy
     */
    private GomokuState(GomokuState previousState) {
        this.intersections = previousState.intersections;
        this.board = new int[intersections][intersections];
        this.currentIndex = previousState.currentIndex;
        this.moveHistory = (Stack<GomokuMove>) previousState.moveHistory.clone();
        // Copy the previous state
        for(int i = 0; i < intersections; i++) {
            System.arraycopy(previousState.board[i], 0, this.board[i], 0, 
                    intersections);
        }
        // Copy Zobrist hash and keys
        this.zobristHash = previousState.zobristHash;
        this.zobristKeys = previousState.zobristKeys;
    }
    
    /**
     * Return available moves (unoccupied intersections) on the board.
     * @return A list of valid moves
     */
    public List<GomokuMove> getMoves() {
        List<GomokuMove> moves = new ArrayList<>();
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] == 0) {
                    moves.add(new GomokuMove(i, j));
                }
            }
        }
        return moves;
    }
    
    /**
     * Apply a move to this state. Must be unoccupied.
     * @param move Move to apply
     */
    public void makeMove(GomokuMove move) {
        if(board[move.row][move.col] == 0) {
            this.board[move.row][move.col] = this.currentIndex;
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.push(move);
            this.zobristHash ^= zobristKeys[currentIndex - 1][move.row][move.col];
        }
    }
    
    /**
     * Undo a move on this state. Must be the previous move applied to this
     * state.
     * @param move Move to undo
     */
    public void undoMove(GomokuMove move) {
        if(moveHistory.peek().equals(move)) {
            this.board[move.row][move.col] = 0;
            this.zobristHash ^= zobristKeys[currentIndex - 1][move.row][move.col];
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.pop();
        }
    }
    
    /**
     * Return the Zobrist hash value for this state, a unique 64-bit long value 
     * representing this state. This is updated automatically as moves are made
     * and unmade.
     * Note: Collisions can still occur, although very rarely.
     * @return Zobrist hash value (64-bit, type long)
     */
    public long getZobristHash() {
        return zobristHash;
    }
    
    /**
     * Deep copy the current state and return a new instance.
     * @return A deep copy of the state, with no references to the previous
     * state.
     */
    public GomokuState copy() {
        return new GomokuState(this);
    }
    
    /**
     * Check if this state is a terminal state.
     * @return True if a player has won or the board is full
     */
    public boolean isTerminal() {
        return isWinner(1) || isWinner(2) || isFull();
    }
    
    /**
     * Return the index of the player who has to make a move for this state.
     * @return Player index (Player 1 or 2)
     */
    public int getCurrentIndex() {
        return this.currentIndex;
    }
    
    /**
     * Determine if the specified player has won the game
     * @param index Player index (1 or 2)
     * @return True if the index has won
     */
    public boolean isWinner(int index) {
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
     * Get the internal 2D board array representing the stones on each
     * intersection for this state.
     * @return 2D board array, were [i][j] represents an intersection on the 
     * GomokuBoard and maps to 0 (empty) or a player index (1 or 2) if the 
     * intersection is occupied by that index.
     */
    public int[][] getBoardArray() {
        return board;
    }
    
    /**
     * Check if the board is full.
     * @return True if all the intersections for the board are occupied
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
