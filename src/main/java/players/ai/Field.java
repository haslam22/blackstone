package players.ai;

/**
 * A class representing a field (intersection) on the Gomoku board. May be
 * occupied by player 1/2, be empty, or be a null field (out of bounds).
 * @author Hasan
 */
public class Field {
    // Location of the field
    protected final int row;
    protected final int col;
    
    // The index (state) of the field, 0 if empty, 1/2 if player 1/2 has
    // occupied it, and 3 if out of bounds
    protected int index;
    
    // References to 9 fields in each direction around the field
    protected final Field[][] directions;

    /**
     * Create a null Field. Default state is 3 (out of bounds).
     */
    protected Field() {
        this.row = -1;
        this.col = -1;
        this.index = 3;
        this.directions = null;
    }

    /**
     * Create a Field with a specified row/column identifier. Default
     * state is 0 (empty).
     * @param row
     * @param col
     */
    protected Field(int row, int col) {
        this.row = row;
        this.col = col;
        this.directions = new Field[4][9];
    }
}
