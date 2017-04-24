package gomoku;

/**
 * A simple class representing a move or position on the Gomoku board.
 * @author Hassan
 */
public class GomokuMove {
    
    public final int row;
    public final int col;
    
    public GomokuMove() {
        this.row = -1;
        this.col = -1;
    }
    
    public GomokuMove(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GomokuMove)) return false;
        GomokuMove move = (GomokuMove) o;
        return (move.col == this.col) && (move.row == this.row);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.row;
        hash = 29 * hash + this.col;
        return hash;
    }
}
