package players;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;

/**
 * Class for a non-AI player. Attaches a mouse listener to the board and waits
 * for a valid move.
 * @author Hassan
 */
public class HumanPlayer extends GomokuPlayer {

    public GomokuMove move;

    public HumanPlayer(GomokuGame game, int playerIndex, int opponentIndex) {
        super(game, playerIndex, opponentIndex);
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        game.addBoardListener(this);
        try {
            // Wait until the mouse listener calls notify() on this thread
            synchronized(this) {
                this.wait();
            }
        } catch(InterruptedException e) {
            return null;
        }
        return move;
    }
    
}
