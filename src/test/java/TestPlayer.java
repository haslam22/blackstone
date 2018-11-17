import core.Move;
import players.Player;

import java.util.List;

/**
 * For mocking a game, creates a player which takes in a list of moves that
 * it will make (in order). Moves must not clash with the other test player
 * that you're using in the game (or things will break!)
 */
public class TestPlayer implements Player {

    private final List<Move> movesToMake;
    private int moveCount = 0;

    public TestPlayer(List<Move> movesToMake) {
        this.movesToMake = movesToMake;
    }

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {

    }

    @Override
    public Move loadBoard(List<Move> orderedMoves) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        return move;
    }

    @Override
    public Move getMove(Move opponentsMove) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        return move;
    }

    @Override
    public Move beginGame() {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        return move;
    }
}
