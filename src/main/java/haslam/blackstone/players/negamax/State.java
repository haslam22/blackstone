package haslam.blackstone.players.negamax;
import haslam.blackstone.core.Move;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal game state representation for the AI.
 */
public class State {

    /**
     * This object stores a 2D board array representing the status of each
     * field (intersection) on the Gomoku board. A field can either be empty
     * (0), 1/2 (occupied by a player) or 3 which is an out of bounds field,
     * used for detecting that a field is at/near the edge of the board.
     *
     * Board directions are stored in a 4D array. This 4D array maps each field
     * on the board to a set of neighbouring fields, 4 on each side of the
     * stone, forming a star pattern:
     *
     *  *       *        *
     *    *     *      *
     *      *   *    *
     *        * *  *
     * * * * * [X] * * * *
     *       *  *  *
     *     *    *    *
     *   *      *      *
     * *        *        *
     *
     * To get the neighbouring fields, we index as follows:
     * [row][col][direction][field #]
     *
     * [0][0-9] -> Diagonal from top left to bottom right
     * [1][0-9] -> Diagonal from top right to bottom left
     * [2][0-9] -> Vertical from top to bottom
     * [3][0-9] -> Horizontal from left to right
     */
    protected final Field[][] board;
    protected final Field[][][][] directions;

    // The current player
    protected int currentIndex;

    // Zobrist hashing, for using the state in a hash data structure
    // https://en.wikipedia.org/wiki/Zobrist_hashing
    private long zobristHash;
    private final long[][][] zobristKeys;

    // Keep track of the moves made on this state
    private Stack<Move> moveStack;
    
    /**
     * Create a new state.
     * @param intersections Number of intersections on the board
     */
    public State(int intersections) {
        this.board = new Field[intersections][intersections];
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                board[i][j] = new Field(i, j);
            }
        }
        this.directions = new Field[intersections][intersections][4][9];
        this.currentIndex = 1;
        this.zobristKeys = new long[2][intersections][intersections];
        this.zobristHash = 0;
        this.moveStack = new Stack<>();
        this.generateDirections(board);

        // Generate Zobrist keys
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
     * Return the Zobrist hash for this state, a unique 64-bit long value
     * representing this state. Updated automatically as moves are made/unmade.
     * @return Hash value (Long)
     */
    public long getZobristHash() {
        return zobristHash;
    }
    
    /**
     * Apply a move to this state.
     * @param move Move to apply
     */
    public void makeMove(Move move) {
        moveStack.push(move);
        this.board[move.row][move.col].index = this.currentIndex;
        this.zobristHash ^= zobristKeys[board[move.row][move.col]
                .index - 1][move.row][move.col];
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }
    
    /**
     * Undo a move on this state.
     * @param move Move to undo
     */
    public void undoMove(Move move) {
        moveStack.pop();
        this.zobristHash ^= zobristKeys[board[move.row][move.col]
                .index - 1][move.row][move.col];
        this.board[move.row][move.col].index = 0;
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }
    
    /**
     * Return whether or not this field has occupied fields around it, within
     * some given distance. Used to determine if a field on the board is
     * worth evaluating as a possible move.
     * @param row Field row
     * @param col Field col
     * @param distance How far to look in each direction, limit 4
     * @return 
     */
    protected boolean hasAdjacent(int row, int col, int distance) {
        for(int i = 0; i < 4; i++) {
            for(int j = 1; j <= distance; j++) {
                if(directions[row][col][i][4 + j].index == 1
                        || directions[row][col][i][4 - j].index == 1
                        || directions[row][col][i][4 + j].index == 2
                        || directions[row][col][i][4 - j].index == 2) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Generate the 4D board directions array, by iterating
     * diagonally/horizontally/vertically and storing the references to the
     * neighbouring fields.
     * @param board Field array
     */
    private void generateDirections(Field[][] board) {
        for(int row = 0; row < board.length; row++) {
            for(int col = 0; col < board.length; col++) {
                directions[row][col][0][4] = board[row][col];
                directions[row][col][1][4] = board[row][col];
                directions[row][col][2][4] = board[row][col];
                directions[row][col][3][4] = board[row][col];

                for(int k = 0; k < 5; k++) {
                    // Diagonal 1, top left
                    if(row - k >= 0 && col - k >=0) {
                        directions[row][col][0][4 - k] = board[row -
                                k][col - k];
                    } else {
                        directions[row][col][0][4 - k] = new Field();
                    }

                    // Diagonal 1, bottom right
                    if(row + k < board.length && col + k < board.length) {
                        directions[row][col][0][4 + k] =
                                board[row + k][col + k];
                    } else {
                        directions[row][col][0][4 + k] = new Field();
                    }

                    // Diagonal 2, top right
                    if(row - k >= 0 && col + k < board.length) {
                        directions[row][col][1][4 - k] =
                                board[row - k][col + k];
                    } else {
                        directions[row][col][1][4 - k] = new Field();
                    }

                    // Diagonal 2, bottom left
                    if(row + k < board.length && col - k >=0) {
                        directions[row][col][1][4 + k] =
                                board[row + k][col - k];
                    } else {
                        directions[row][col][1][4 + k] = new Field();
                    }

                    // Vertical top
                    if(row - k >= 0) {
                        directions[row][col][2][4 - k] =
                                board[row - k][col];
                    } else {
                        directions[row][col][2][4 - k] = new Field();
                    }

                    // Vertical bottom
                    if(row + k < board.length) {
                        directions[row][col][2][4 + k] =
                                board[row + k][col];
                    } else {
                        directions[row][col][2][4 + k] = new Field();
                    }

                    // Horizontal left
                    if(col - k >= 0) {
                        directions[row][col][3][4 - k] =
                                board[row][col - k];
                    } else {
                        directions[row][col][3][4 - k] = new Field();
                    }

                    // Horizontal right
                    if(col + k < board.length) {
                        directions[row][col][3][4 + k] =
                                board[row][col + k];
                    } else {
                        directions[row][col][3][4 + k] = new Field();
                    }
                }
            }
        }
    }

    /**
     * Determine if this state is terminal
     * @return 0 if not terminal, index (1/2) of the player who has won, or 3
     * if the board is full
     */
    protected int terminal() {
        Move move = moveStack.peek();
        int row = move.row;
        int col = move.col;
        int lastIndex = currentIndex == 1 ? 2 : 1;

        // Check around the last move placed to see if it formed a five
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 6; j++) {
                if(directions[row][col][i][j].index == lastIndex) {
                    int count = 0;
                    for(int k = 1; k < 5; k++) {
                        if(directions[row][col][i][j+k].index == lastIndex) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    if(count == 4) return lastIndex;
                }
            }
        }
        return moveStack.size() == board.length * board.length ? 3 : 0;
    }

    /**
     * Get the total number of moves made on this state
     * @return # of moves
     */
    protected int getMoves() {
        return moveStack.size();
    }

    /**
     * Get a field instance on the board at a given row/col position. For
     * unit testing purposes.
     * @param row Row
     * @param col Column
     * @return Field instance at given position
     */
    public Field getField(int row, int col) {
        return this.board[row][col];
    }
    
}
