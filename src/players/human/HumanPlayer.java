package players.human;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;
import players.GomokuPlayer;

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

    @Override
    public String toString() {
        return "Human";
    }
    
}
