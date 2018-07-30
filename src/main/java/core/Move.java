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

    /**
     * @param boardSize Size of the board (15, 19..)
     * @return Algebraic representation of this move (e.g. 0,0 -> A15)
     */
    public String getAlgebraicString(int boardSize) {
        int rowAlgebraic = boardSize - row;
        char colAlgebraic = (char) ('A' + col);
        return new String(Character.toString(colAlgebraic) + rowAlgebraic);
    }

    public String toString() {
        return String.format("[%d, %d]", row, col);
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
