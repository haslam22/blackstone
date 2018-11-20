package players.random;

import core.Move;
import players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPlayer implements Player {

    private Random random;
    private List<Move> moves;
    private int size;

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {
        this.moves = new ArrayList<>();
        this.random = new Random();
        this.size = boardSize;
    }

    @Override
    public Move loadBoard(List<Move> orderedMoves, long gameTimeRemainingMillis) {
        this.moves = orderedMoves;
        Move move = getRandomMove();
        moves.add(move);
        return move;
    }

    @Override
    public Move getMove(Move opponentsMove, long gameTimeRemainingMillis) {
        moves.add(opponentsMove);
        Move move = getRandomMove();
        moves.add(move);
        return move;
    }

    private Move getRandomMove() {
        List<Move> availableMoves = new ArrayList<>();

        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                Move move = new Move(row, col);
                if(!moves.contains(move)) {
                    availableMoves.add(move);
                }
            }
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    @Override
    public Move beginGame(long gameTimeRemainingMillis) {
        Move move = new Move(size / 2, size / 2);
        moves.add(move);
        return move;
    }
}
