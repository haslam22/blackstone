package players.human;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;
import players.GomokuPlayer;

/**
 * Class for a non-AI player. Attaches a mouse listener to the board and waits
 * for a valid move. Listener calls notify() to wake this thread up and return a
 * move when the user clicks on the board.
 * @author Hassan
 */
public class HumanPlayer extends GomokuPlayer {

    public GomokuMove move;

    public HumanPlayer(GomokuGame game, int playerIndex, int opponentIndex) {
        super(game, playerIndex, opponentIndex);
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        game.addBoardListener(new HumanListener(this, this.game, state), true);
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
