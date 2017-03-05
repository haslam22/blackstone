package gomoku;

/**
 * A simple class representing a move or position on the Gomoku board
 * @author Hassan
 */
public class GomokuMove {
    
    public int row;
    public int col;
    
    public GomokuMove() {}
    
    public GomokuMove(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof GomokuMove) {
            GomokuMove move = (GomokuMove) o;
            return move.col == this.col && move.row == this.row;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.row;
        hash = 97 * hash + this.col;
        return hash;
    }
}
