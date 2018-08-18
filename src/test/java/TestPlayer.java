import core.GameInfo;
import core.GameState;
import core.Move;
import players.Player;

import java.util.List;

/**
 * For mocking a game, creates a player which takes in a list of moves that
 * it will make (in order). Moves must not clash with the other test player
 * that you're using in the game (or things will break!)
 */
public class TestPlayer extends Player {

    private final List<Move> movesToMake;
    private int moveCount = 0;

    public TestPlayer(GameInfo info, List<Move> movesToMake) {
        super(info);
        this.movesToMake = movesToMake;
    }

    @Override
    public Move getMove(GameState state) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        return move;
    }
}
