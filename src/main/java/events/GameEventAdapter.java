package events;

import core.Move;

import java.util.List;

/**
 * Convenience class to allow components to only implement handler methods
 * for game events they are interested in.
 * @see GameListener
 */
public class GameEventAdapter implements GameListener {

    @Override
    public void moveAdded(int playerIndex, Move move) {
    }

    @Override
    public void moveRemoved(Move move) {
    }

    @Override
    public void gameTimeChanged(int playerIndex, long timeMillis) {
    }

    @Override
    public void moveTimeChanged(int playerIndex, long timeMillis) {
    }

    @Override
    public void turnStarted(int playerIndex) {
    }

    @Override
    public void userMoveRequested(int playerIndex) {
    }

    @Override
    public void gameStarted() {
    }

    @Override
    public void gameResumed() {
    }

    @Override
    public void gameFinished() {
    }

    @Override
    public void positionLoaded(List<Move> orderedMoves) {

    }
}
