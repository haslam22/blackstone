package players;

import gomoku.GomokuLocation;
import gomoku.GomokuState;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Hassan
 */
public class RandomPlayer extends GomokuPlayer {

    public RandomPlayer(int index) {
        super(index);
    }
    
    @Override
    public GomokuLocation getMove(GomokuState state) {
        Random random = new Random();
        List<GomokuLocation> legalMoves = state.getLegalMoves();
        return legalMoves.get(random.nextInt(legalMoves.size()));
    }

    
}
