package haslam.blackstone.players.negamax.faststate;

/**
 * State with fast pattern lookup.
 * Stores the status of the 4 fields around a field (in every direction - see
 * direction vectors below) in an 8 bit lookup for each direction, per player.
 *
 * e.g. 0 0 0 1 | Field | 0 0 1 1
 * This would be stored as the decimal value 19 (1+2+16).
 * The range of possible values is 0-255. So we can store each pattern in a
 * lookup array of size [256][256] (1 dimension per player).
 *
 * This allows us to perform quick lookups e.g. for evaluating a field or
 * returning the threats around a field.
 */
public class FastState {

    private final Field[][] board;
    private int currentPlayer = 1;

    // Defines the directions in which we iterate on the board.
    // Horizontal (1, 0)
    // Vertical (0, 1)
    // Diagonal (1, 1)
    // Reverse diagonal (1, -1)
    private final int[] directionVectorX = {1, 0, 1, 1};
    private final int[] directionVectorY = {0, 1, 1, -1};

    /**
     * Create a new state.
     * @param size Size of the board (n*n)
     */
    public FastState(int size) {
        // Initialise the board.
        // Pad the outside of the board with "out of bounds" fields to avoid
        // doing a bounds check every time we iterate
        this.board = new Field[size + 8][size + 8];
        for(int x = 0; x < size + 8; x++) {
            for(int y = 0; y < size + 8; y++) {
                if(x < 4 || y < 4 || x >= size + 4 || y >= size + 4) {
                    board[x][y] = new Field(3);
                } else {
                    board[x][y] = new Field(0);
                }
            }
        }
        // Initialise the pattern lookup. Some fields are out of bounds, so
        // we set them as occupied for both players.
        for(int x = 4; x < size + 4; x++) {
            for(int y = 4; y < size + 4; y++) {
                for(int direction = 0; direction < 4; direction++) {
                    int currentX = x - directionVectorX[direction];
                    int currentY = y - directionVectorY[direction];

                    // Iterate 8 4 2 1 (bits on the right side of the
                    // pattern) - use right shift to move across
                    for(int i = 8; i != 0; i >>= 1) {
                        if(board[currentX][currentY].fieldState == 3) {
                            // Mark field as occupied in pattern lookup
                            board[x][y].patternLookupValues[0][direction] |= i;
                            board[x][y].patternLookupValues[1][direction] |= i;
                        }
                        currentX -= directionVectorX[direction];
                        currentY -= directionVectorY[direction];
                    }

                    currentX = x + directionVectorX[direction];
                    currentY = y + directionVectorY[direction];

                    // Iterate 16 32 64 128 (bits on the left side of the
                    // pattern) - use left shift to move across
                    for(int i = 16; i <= 128; i <<= 1) {
                        if(board[currentX][currentY].fieldState == 3) {
                            // Mark field as occupied in pattern lookup
                            board[x][y].patternLookupValues[0][direction] |= i;
                            board[x][y].patternLookupValues[1][direction] |= i;
                        }
                        currentX += directionVectorX[direction];
                        currentY += directionVectorY[direction];
                    }

                }
            }
        }
    }

    /**
     * Make a move on this state.
     * @param x
     * @param y
     */
    public void makeMove(int x, int y) {
        this.board[x][y].fieldState = currentPlayer;
        // Update pattern lookup for all neighbouring fields.
        updatePatternLookup(x, y);
        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    /**
     * Undo a move on this state.
     * @param x
     * @param y
     */
    public void undoMove(int x, int y) {
        this.board[x][y].fieldState = 0;
        currentPlayer = currentPlayer == 1 ? 2 : 1;
        // Update pattern lookup for all neighbouring fields.
        updatePatternLookup(x, y);
    }

    /**
     * Get a field.
     * @param x
     * @param y
     * @return
     */
    public Field getField(int x, int y) {
        return board[x][y];
    }

    private void updatePatternLookup(int x, int y) {
        for(int direction = 0; direction < 4; direction++) {
            int currentX = x;
            int currentY = y;

            for (int i = 8; i != 0; i >>= 1) {
                currentX += directionVectorX[direction];
                currentY += directionVectorY[direction];
                board[currentX][currentY].patternLookupValues[currentPlayer - 1][direction] ^= i;
            }

            currentX = x;
            currentY = y;
            for(int i = 16; i <= 128; i <<= 1) {
                currentX -= directionVectorX[direction];
                currentY -= directionVectorY[direction];
                board[currentX][currentY].patternLookupValues[currentPlayer - 1][direction] ^= i;
            }
        }
    }

}
