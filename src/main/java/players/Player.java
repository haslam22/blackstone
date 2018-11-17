package players;

import core.Move;

import java.util.List;

/**
 * Interface for a Gomoku player. At the most basic level, the game will call
 * getMove() on the player continuously, passing in the opponents last move. It
 * is up to the player to keep track of the game state - e.g. by storing moves
 * in a list.
 *
 * @see players.negamax.NegamaxPlayer
 */
public interface Player {

    /**
     * Setup and initialise the player. Called once by the game manager before
     * a game begins.
     * @param index Position (1: black, 2: white)
     * @param boardSize Size of the board (n*n intersections), usually 15 or 19
     * @param moveTimeMillis Max time per move in milliseconds (100ms leniency)
     * @param gameTimeMillis Max time per game in milliseconds
     */
    void setupGame(int index, int boardSize, long moveTimeMillis,
                   long gameTimeMillis);

    /**
     * Load in a board to start the game from a non-empty position, and
     * return a move.
     * @param orderedMoves List of moves made in the game, in order
     * @return Next move to be played
     */
    Move loadBoard(List<Move> orderedMoves);

    /**
     * Return a move in the game.
     * @param opponentsMove Opponents move
     * @return Response to opponents move
     */
    Move getMove(Move opponentsMove);

    /**
     * Return an opening move.
     * @return Opening move
     */
    Move beginGame();

    /**
     * Clean up any resources. Called at the end of a game. Optional.
     */
    default void cleanup() { }
    
}
