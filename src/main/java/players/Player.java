package players;

import core.GameInfo;
import core.GameState;
import core.Move;

/**
 * Abstract class for a Gomoku player. Players are constructed with a game
 * information object containing timeouts, board size, etc.
 * @author Hasan
 */
public abstract class Player {

    protected final GameInfo info;
    
    /**
     * Create a new player.
     * @param info Game information
     */
    public Player(GameInfo info) {
        this.info = info;
    }
    
    /**
     * Get the move from this player.
     * @param state Current game state
     * @return A valid move
     */
    public abstract Move getMove(GameState state);
    
    @Override
    /*
     * Get the name identifier for this player.
     */
    public abstract String toString();
    
}
