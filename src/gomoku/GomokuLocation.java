package gomoku;

/**
 * A class representing a location on the Gomoku board
 * @author Hassan
 */
public class GomokuLocation {
    public int row;
    public int col;
    
    public GomokuLocation() {
    }
    
    public GomokuLocation(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
