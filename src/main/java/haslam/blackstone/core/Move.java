package haslam.blackstone.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a move or position on the Gomoku board.
 */
public class Move implements Serializable {

    public final int row;
    public final int col;

    /**
     * Create a new move.
     * @param row Row identifier
     * @param col Column identifier
     */
    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Create a new move from an algebraic move representation e.g. E3
     * @param columnLetter Column letter
     * @param rowNumber Row number
     * @param boardSize Size of the board
     */
    public Move(char columnLetter, int rowNumber, int boardSize) {
        this.row = boardSize - rowNumber;
        this.col = (char) (columnLetter - 'A');
    }

    /**
     * @param boardSize Size of the board (15, 19..)
     * @return Algebraic representation of this move (e.g. 0,0 -> A15)
     */
    public String getAlgebraicString(int boardSize) {
        int rowAlgebraic = boardSize - row;
        char colAlgebraic = (char) ('A' + col);
        return Character.toString(colAlgebraic) + rowAlgebraic;
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
