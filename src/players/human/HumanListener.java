package players.human;

import gomoku.GomokuGame;
import gomoku.GomokuMove;
import gomoku.GomokuState;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mouse listener to listen for a valid move on the Gomoku board.
 * @author Hassan
 */
public class HumanListener extends MouseAdapter {

    private final GomokuGame game;
    private final GomokuState state;
    private final HumanPlayer player;
    
    public HumanListener(HumanPlayer player, GomokuGame game, 
            GomokuState state) {
        this.game = game;
        this.state = state;
        this.player = player;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Get the nearest intersection to where the user clicked
        GomokuMove move = game.getMouseMove(e.getX(), e.getY());
        if(move.row >= 0 && move.col >= 0) {
            if(state.getIntersectionIndex(move.row, move.col) == 0) {
                synchronized(player) {
                    // User clicked on a valid move, wake the thread up
                    player.move = move;
                    player.notify();
                }
            }
        }
    }
}
