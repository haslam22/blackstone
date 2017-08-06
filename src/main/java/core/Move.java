package core;

import java.util.Objects;

/**
 * Represents a move or position on the Gomoku board.
 */
public class Move {

    public final int row;
    public final int col;

    /**
     * Create a new move.
     * @param row Row identifer
     * @param col Column identifier
     */
    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Move) {
            Move move = (Move) obj;
            return move.row == this.row && move.col == this.col;
        }
        return false;
    }
}
