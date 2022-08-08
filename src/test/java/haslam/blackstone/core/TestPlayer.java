package haslam.blackstone.core;

import haslam.blackstone.players.Player;

import java.util.List;

/**
 * For mocking a game, creates a player which takes in a list of moves that
 * it will make (in order). Moves must not clash with the other test player
 * that you're using in the game (or things will break!)
 */
public class TestPlayer implements Player {

    private final List<Move> movesToMake;
    private final long delayMs;
    private int moveCount = 0;

    public TestPlayer(List<Move> movesToMake, long delayMs) {
        this.movesToMake = movesToMake;
        this.delayMs = delayMs;
    }

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {

    }

    @Override
    public Move loadBoard(List<Move> orderedMoves, long gameTimeRemainingMillis) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            return null;
        }
        return move;
    }

    @Override
    public Move getMove(Move opponentsMove, long gameTimeRemainingMillis) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            return null;
        }
        return move;
    }

    @Override
    public Move beginGame(long gameTimeRemainingMillis) {
        Move move = movesToMake.get(moveCount);
        moveCount++;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            return null;
        }
        return move;
    }
}
