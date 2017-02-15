package players;

import gomoku.GomokuGame;
import gomoku.GomokuLocation;
import gomoku.GomokuState;

/**
 *
 * @author Hassan
 */
public class HumanPlayer extends GomokuPlayer {

    public GomokuLocation location;
    private final GomokuGame game;
    
    public HumanPlayer(int index, GomokuGame game) {
        super(index);
        this.game = game;
    }

    @Override
    public GomokuLocation getMove(GomokuState state) {
        game.addListener(this);
        synchronized(this) {
            try {
                // Wait until a valid mouse event has been received
                wait();
            } catch (InterruptedException ex) {}
        }
        return location;
    }
}
