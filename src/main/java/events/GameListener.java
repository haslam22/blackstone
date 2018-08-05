package events;

import core.Move;

import java.util.List;

/**
 * Listener interface for receiving interesting game events.
 */
public interface GameListener {

    /**
     * Called when a player makes a move in the game.
     * @param playerIndex Player identifier
     * @param move Move made
     */
    void moveAdded(int playerIndex, Move move);

    /**
     * Called when a move is undone.
     */
    void moveRemoved(Move move);

    /**
     * Called when a players turn has started.
     * @param playerIndex Player identifier
     */
    void turnStarted(int playerIndex);

    /**
     * Called when the game has started.
     */
    void gameStarted();

    /**
     * Called when a previous game is resumed.
     */
    void gameResumed();

    /**
     * Called when the game has finished.
     */
    void gameFinished();

    /**
     * Called when the game requests a move from the user.
     */
    void userMoveRequested(int playerIndex);

    /**
     * Called when a new position is loaded by the user.
     * @param orderedMoves A list of moves made, in order, to assemble the new
     * position.
     */
    void positionLoaded(List<Move> orderedMoves);

    /**
     * Called when a position is cleared by the user.
     */
    void positionCleared();

}
