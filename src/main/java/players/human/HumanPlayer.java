package players.human;

import core.GameInfo;
import core.GameState;
import core.Move;
import players.Player;

/**
 * Class for a non-AI player. Sleeps until the game receives a valid move
 * from the user.
 * @author Hasan
 */
public class HumanPlayer extends Player {

    private Move move;

    public HumanPlayer(GameInfo info) {
        super(info);
    }

    public void setMove(Move move) {
        this.move = move;
    }
    
    @Override
    public Move getMove(GameState state) {
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
    
}
