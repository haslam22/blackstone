package players.negamax;

/**
 * Represents a field (intersection) on the Gomoku board.
 */
public class Field {
    // Location of the field relative to the rest of the board
    protected final int row;
    protected final int col;
    
    // The index (state) of the field, 0 if empty, 1/2 if player 1/2 has
    // occupied it, and 3 if out of bounds
    protected int index;

    /**
     * Default constructor for a field, set to out of bounds.
     */
    protected Field() {
        this.row = 0;
        this.col = 0;
        this.index = 3;
    }

    /**
     * Create a field with a row/column identifier.
     * @param row Row on the board
     * @param col Column on the board
     */
    protected Field(int row, int col) {
        this.row = row;
        this.col = col;
        this.index = 0;
    }
}
