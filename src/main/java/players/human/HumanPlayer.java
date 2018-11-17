package players.human;

import core.Move;
import players.Player;

import java.util.List;

/**
 * Class for a non-AI player. Sleeps until the game receives a valid move
 * from the user.
 * @author Hasan
 */
public class HumanPlayer implements Player {

    private Move move;

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) { }

    @Override
    public Move loadBoard(List<Move> orderedMoves) { return waitForMove(); }

    @Override
    public Move getMove(Move opponentsMove) {
        return waitForMove();
    }

    @Override
    public Move beginGame() {
        return waitForMove();
    }

    private Move waitForMove() {
        // Suspend until the user clicks a valid move (handled by the game)
        try {
            synchronized(this) {
                this.wait();
            }
        } catch(InterruptedException e) {
            return null;
        }
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }
}
