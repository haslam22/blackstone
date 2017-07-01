package core;

import java.util.Objects;

/**
 * Class representing a move/position on the Gomoku board
 */
public class Move {

    private int row;
    private int col;

    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Move() {}

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    public boolean equals(Object obj) {
        if(obj instanceof Move) {
            Move move = (Move) obj;
            return move.row == this.row && move.col == this.col;
        }
        return false;
    }
}
