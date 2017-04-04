package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;

/**
 * Abstract class for a Gomoku player. Players are constructed with the index
 * that belongs to them and the opponent's index. (Index referring to their 
 * player # - 1 (black), 2 (white)) Must implement getMove(), given some state 
 * as a parameter.
 * @author Hassan
 */
public abstract class GomokuPlayer {

    protected final int playerIndex;
    protected final int opponentIndex;
    
    /**
     * Create a new GomokuPlayer.
     * @param playerIndex Index of this player.
     * @param opponentIndex Index of the opponent.
     */
    public GomokuPlayer(int playerIndex, int opponentIndex) {
        this.playerIndex = playerIndex;
        this.opponentIndex = opponentIndex;
    }
    
    /**
     * Get the index (player #) belonging to this player.
     * @return
     */
    public int getPlayerIndex() {
        return playerIndex;
    }
    
    /**
     * Get the index (player #) belonging to the opponent.
     * @return Opponent index
     */
    public int getOpponentIndex() {
        return opponentIndex;
    }
    
    /**
     * Get the move from this player.
     * @param state Current game state
     * @return
     */
    public abstract GomokuMove getMove(GomokuState state);
    
}
