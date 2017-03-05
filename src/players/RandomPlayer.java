package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Hassan
 */
public class RandomPlayer extends GomokuPlayer {

    public RandomPlayer(int playerIndex, int opponentIndex) {
        super(playerIndex, opponentIndex);
    }

    @Override
    public GomokuMove getMove(GomokuState state) {
        Random random = new Random();
        List<GomokuMove> legalMoves = state.getMoves();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            return null;
        }
        return legalMoves.get(random.nextInt(legalMoves.size()));
    }

    
}
