package gomoku;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * A class representing the state of a Gomoku game at any point. Supports
 * Zobrist Hashing.
 * @author Hassan
 */
public class GomokuState {
    
    private final int[][] board;
    private final int intersections;
    private final Stack<GomokuMove> moveHistory;
    private int currentIndex;
    
    // Zobrist Hashing information for this state
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
    
    public long getZobristHash() {
        return zobristHash;
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
     * Copy constructor for GomokuState
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
     * Return available moves (unoccupied intersections) on the board
     * @return A list of moves
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
    
    public void makeMove(GomokuMove move) {
        if(board[move.row][move.col] == 0) {
            this.board[move.row][move.col] = this.currentIndex;
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.push(move);
            this.zobristHash ^= zobristKeys[currentIndex - 1][move.row][move.col];
        }
    }
    
    public void undoMove(GomokuMove move) {
        if(moveHistory.peek().equals(move)) {
            this.board[move.row][move.col] = 0;
            this.zobristHash ^= zobristKeys[currentIndex - 1][move.row][move.col];
            this.currentIndex = this.currentIndex == 1 ? 2 : 1;
            this.moveHistory.pop();
        }
    }
    
    /**
     * Deep copy the current state and return a new instance.
     * @return
     */
    public GomokuState copy() {
        return new GomokuState(this);
    }
    
    /**
     * Check if this state is a terminal state.
     * @return
     */
    public boolean isTerminal() {
        return isWinner(1) || isWinner(2);
    }
    
    /**
     * Return the index of the current player for this state
     * @return Integer index (Player 1 or 2)
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
     * Return an integer array representing the board, where [i][j] maps to
     * a player index, or 0 if empty.
     * @return
     */
    public int[][] getBoardArray() {
        return board;
    }
    
    /**
     * Check if the board is full
     * @return True if the board has no more available moves
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
    public boolean searchVertical(int row, int col, int index, int amount) {
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
    public boolean searchHorizontal(int row, int col, int index, int amount) {
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
    public boolean searchDiagonalRight(int row, int col, int index, 
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
    public boolean searchDiagonalLeft(int row, int col, int index, int amount) {
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
