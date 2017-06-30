package players.ai;
import core.Move;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal state representation for the Minimax player. Provides Zobrist 
 * hashing and fast neighbour lookup.
 * @author Hasan
 */
public class State {
    
    protected final Field[][] board;
    protected final int intersections;
    protected int currentIndex;
    protected int moves;
    
    private long zobristHash;
    private final long[][][] zobristKeys;
    
    /**
     * Create a new State instance.
     * @param intersections Number of intersections on the board
     */
    public State(int intersections) {
        this.intersections = intersections;
        this.board = new Field[intersections][intersections];
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                board[i][j] = new Field(i, j);
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
        for(int i = 0; i < zobristKeys.length; i++) {
            for(int j = 0; j < zobristKeys[0].length; j++) {
                for(int k = 0; k < zobristKeys[0][0].length; k++) {
                    zobristKeys[i][j][k] = ThreadLocalRandom.current().nextLong
                            (Long.MAX_VALUE);
                }
            }
        }
    }
    
    /**
     * Apply a move to this state.
     * @param move Move to apply
     */
    public void makeMove(Move move) {
        moves++;
        this.board[move.getRow()][move.getCol()].index = this.currentIndex;
        this.zobristHash ^= zobristKeys[board[move.getRow()][move.getCol()]
                .index - 1][move.getRow()][move.getCol()];
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }
    
    /**
     * Undo a move on this state.
     * @param move Move to undo
     */
    public void undoMove(Move move) {
        moves--;
        this.zobristHash ^= zobristKeys[board[move.getRow()][move.getCol()]
                .index - 1][move.getRow()][move.getCol()];
        this.board[move.getRow()][move.getCol()].index = 0;
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
     * @param distance How far to look in each direction, limit 4
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
    
    public boolean hasAdjacent(int row, int col, int distance, int index) {
        for(int i = 0; i < 4; i++) {
            for(int j = 1; j <= distance; j++) {
                if(board[row][col].directions[i][4 + j].index == index
                        || board[row][col].directions[i][4 - j].index == index) {
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
    private void setDirections(Field[][] board) {
        for(int row = 0; row < board.length; row++) {
            for(int col = 0; col < board.length; col++) {
                Field field = board[row][col];

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
                        field.directions[0][4 - k] = new Field();
                    }

                    // Diagonal 1, bottom right
                    if(row + k < board.length && col + k < board.length) {
                        field.directions[0][4 + k] = board[row + k][col + k];
                    } else {
                        field.directions[0][4 + k] = new Field();
                    }

                    // Diagonal 2, top right
                    if(row - k >= 0 && col + k < board.length) {
                        field.directions[1][4 - k] = board[row - k][col + k];
                    } else {
                        field.directions[1][4 - k] = new Field();
                    }

                    // Diagonal 2, bottom left
                    if(row + k < board.length && col - k >=0) {
                        field.directions[1][4 + k] = board[row + k][col - k];
                    } else {
                        field.directions[1][4 + k] = new Field();
                    }

                    // Vertical top
                    if(row - k >= 0) {
                        field.directions[2][4 - k] = board[row - k][col];
                    } else {
                        field.directions[2][4 - k] = new Field();
                    }

                    // Vertical bottom
                    if(row + k < board.length) {
                        field.directions[2][4 + k] = board[row + k][col];
                    } else {
                        field.directions[2][4 + k] = new Field();
                    }

                    // Horizontal left
                    if(col - k >= 0) {
                        field.directions[3][4 - k] = board[row][col - k];
                    } else {
                        field.directions[3][4 - k] = new Field();
                    }

                    // Horizontal right
                    if(col + k < board.length) {
                        field.directions[3][4 + k] = board[row][col + k];
                    } else {
                        field.directions[3][4 + k] = new Field();
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
