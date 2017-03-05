package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;

/**
 * An abstract class for a Gomoku player. The player is given it's own index
 * and the opponent's index respectively and must implement getMove(), given
 * some state in the game.
 * @author Hassan
 */
public abstract class GomokuPlayer {

    protected final int playerIndex;
    protected final int opponentIndex;
    
    public GomokuPlayer(int playerIndex, int opponentIndex) {
        this.playerIndex = playerIndex;
        this.opponentIndex = opponentIndex;
    }
    
    public int getPlayerIndex() {
        return playerIndex;
    }
    
    public int getOpponentIndex() {
        return opponentIndex;
    }
    
    public abstract GomokuMove getMove(GomokuState state);
    
}
