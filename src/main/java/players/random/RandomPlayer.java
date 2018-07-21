package players.random;

import core.GameInfo;
import core.GameState;
import core.Move;
import players.Player;

import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    private Random random;

    /**
     * Create a new player which makes a random move each time.
     * @param info Game information
     */
    public RandomPlayer(GameInfo info) {
        super(info);
        this.random = new Random();
    }

    @Override
    public Move getMove(GameState state) {
        List<Move> moves = state.getAvailableMoves();
        return moves.get(random.nextInt(moves.size()));
    }
}
