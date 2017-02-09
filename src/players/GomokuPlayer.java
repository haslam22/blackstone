package players;

import gomoku.GomokuLocation;
import gomoku.GomokuState;

/**
 *
 * @author Hassan
 */
public abstract class GomokuPlayer {
    
    private final int index;
    
    public GomokuPlayer(int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public abstract GomokuLocation getMove(GomokuState state);
}
