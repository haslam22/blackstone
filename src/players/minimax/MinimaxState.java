package players.minimax;
import gomoku.GomokuMove;

import java.util.Random;

/**
 * Internal state representation for the Minimax player. Provides Zobrist 
 * hashing and fast neighbour lookup.
 * @author Hassan
 */
public class MinimaxState {
    
    /**
     * Class representing an intersection on the Gomoku board.
     */
    protected class GomokuField {
        // Location of the field
        public final int row;
        public final int col;
        // The index (state) of the field, 0 if empty, 1/2 if player 1/2 has
        // occupied it, and 3 if out of bounds.
        public int index;
        // References to 4 fields in each direction around the field
        public final GomokuField[][] directions;
        
        public GomokuField() {
            this.row = -1;
            this.col = -1;
            this.index = 3;
            this.directions = null;
        }
        
        public GomokuField(int row, int col) {
            this.row = row;
            this.col = col;
            this.directions = new GomokuField[4][9];
        }
    }
    
    protected final GomokuField[][] board;
    protected final int intersections;
    protected int currentIndex;
    protected int moves;
    
    private long zobristHash;
    private final long[][][] zobristKeys;
    
    /**
     * Create a new MinimaxState instance.
     * @param intersections Number of intersections on the board
     */
    public MinimaxState(int intersections) {
        this.intersections = intersections;
        this.board = new GomokuField[intersections][intersections];
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                board[i][j] = new GomokuField(i, j);
            }
        }
        setDirections(board);
        this.currentIndex = 1;
        this.zobristKeys = new long[2][intersections][intersections];
        this.zobristHash = 0;
        generateZobristKeys();
    }
    
    /**
     * Determine if this state is terminal.
     * @return 0 if not terminal, 1/2 if players have won, 3 if board is full
     */
    public int terminal() {
        if(isWinner(1)) return 1;
        if(isWinner(2)) return 2;
        if(moves == (intersections * intersections)) return 3;
        return 0;
    }
    
    /**
     * Generate random bit strings (64-bit) for every board position, and for
     * every possible state that particular board position can be in.
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
     * Apply a move to this state.
     * @param move Move to apply
     */
    public void makeMove(GomokuMove move) {
        moves++;
        this.board[move.row][move.col].index = this.currentIndex;
        this.zobristHash ^= zobristKeys[board[move.row][move.col].index - 1]
                [move.row][move.col];
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }
    
    /**
     * Undo a move on this state.
     * @param move Move to undo
     */
    public void undoMove(GomokuMove move) {
        moves--;
        this.zobristHash ^= zobristKeys[board[move.row][move.col].index - 1]
                [move.row][move.col];
        this.board[move.row][move.col].index = 0;
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }
    
    /**
     * Return the Zobrist hash value for this state, a unique 64-bit long value 
     * representing this state. Updated automatically as moves are made/unmade.
     * @return Zobrist hash value (64-bit, type long)
     */
    public long getZobristHash() {
        return zobristHash;
    }
    
    /**
     * Return whether or not this field has adjacent occupied fields around it, 
     * given some maximum distance.
     * @param row Field row
     * @param col Field col
     * @param distance How far to look in each direction, must be <= 4
     * @return 
     */
    public boolean hasAdjacent(int row, int col, int distance) {
        for(int i = 0; i < 4; i++) {
            for(int j = 1; j <= distance; j++) {
                if(board[row][col].directions[i][4 + j].index == 1
                        || board[row][col].directions[i][4 - j].index == 1
                        || board[row][col].directions[i][4 + j].index == 2
                        || board[row][col].directions[i][4 - j].index == 2) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Add references in each GomokuField instance to the 4 fields around them
     * in every direction. Avoids computation of neighbouring fields during
     * the Minimax search.
     * @param board Field array
     */
    private void setDirections(GomokuField[][] board) {
        for(int row = 0; row < board.length; row++) {
            for(int col = 0; col < board.length; col++) {
                GomokuField field = board[row][col];

                // [0][0-9] -> Diagonal from top left to bottom right
                field.directions[0][4] = field; 
                // [1][0-9] -> Diagonal from top right to bottom left
                field.directions[1][4] = field;
                // [2][0-9] -> Vertical from top to bottom
                field.directions[2][4] = field;
                // [3][0-9] -> Horizontal from left to right
                field.directions[3][4] = field;
        
                for(int k = 0; k < 5; k++) {
                    // Diagonal 1, top left
                    if(row - k >= 0 && col - k >=0) {
                        field.directions[0][4 - k] = board[row - k][col - k];
                    } else {
                        field.directions[0][4 - k] = new GomokuField();
                    }

                    // Diagonal 1, bottom right
                    if(row + k < board.length && col + k < board.length) {
                        field.directions[0][4 + k] = board[row + k][col + k];
                    } else {
                        field.directions[0][4 + k] = new GomokuField();
                    }

                    // Diagonal 2, top right
                    if(row - k >= 0 && col + k < board.length) {
                        field.directions[1][4 - k] = board[row - k][col + k];
                    } else {
                        field.directions[1][4 - k] = new GomokuField();
                    }

                    // Diagonal 2, bottom left
                    if(row + k < board.length && col - k >=0) {
                        field.directions[1][4 + k] = board[row + k][col - k];
                    } else {
                        field.directions[1][4 + k] = new GomokuField();
                    }

                    // Vertical top
                    if(row - k >= 0) {
                        field.directions[2][4 - k] = board[row - k][col];
                    } else {
                        field.directions[2][4 - k] = new GomokuField();
                    }

                    // Vertical bottom
                    if(row + k < board.length) {
                        field.directions[2][4 + k] = board[row + k][col];
                    } else {
                        field.directions[2][4 + k] = new GomokuField();
                    }

                    // Horizontal left
                    if(col - k >= 0) {
                        field.directions[3][4 - k] = board[row][col - k];
                    } else {
                        field.directions[3][4 - k] = new GomokuField();
                    }

                    // Horizontal right
                    if(col + k < board.length) {
                        field.directions[3][4 + k] = board[row][col + k];
                    } else {
                        field.directions[3][4 + k] = new GomokuField();
                    }
                }
            }
        }
    }
    
    /**
     * Determine if the specified player has won the game
     * @param index Player index (1 or 2)
     * @return True if the index has won
     */
    public boolean isWinner(int index) {
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                if(board[i][j].index == index) {
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
                if(board[row+k][col].index == index) {
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
                if(board[row][col+k].index == index) {
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
                if(board[row+k][col+k].index == index) {
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
                if(board[row+k][col-k].index == index) {
                    count++;
                }
            }  
            return count == amount;
        }
        return false;
    }
    
}
