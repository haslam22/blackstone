package players;

import gomoku.GomokuMove;
import gomoku.GomokuState;
import gui.GomokuBoardPanel;
import gui.GomokuBoardPanel.StoneColor;
import static gui.GomokuBoardPanel.StoneColor.BLACK;
import static gui.GomokuBoardPanel.StoneColor.WHITE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Human player, basically a mouse listener attached to the board which calls
 * notify() on this thread when a valid move is given.
 * @author Hassan
 */
public class HumanPlayer extends GomokuPlayer {

    private GomokuMove move;
    private final GomokuBoardPanel board;

    public HumanPlayer(int playerIndex, int opponentIndex, 
            GomokuBoardPanel board) {
        super(playerIndex, opponentIndex);
        this.board = board;
    }
    
    @Override
    public GomokuMove getMove(GomokuState state) {
        // Tell the board to enable the stone overlay when the user moves
        // the mouse, giving the colour for this index
        board.enableStonePicker(getColor(state.getCurrentIndex()));
        // Add a mouse listener to the board and listen for a valid click
        // On a valid click, this thread will be notified and resumed
        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the nearest intersection to where the user clicked
                int row = board.getNearestRow(e.getY());
                int col = board.getNearestCol(e.getX());
                for(GomokuMove move : state.getMoves()) {
                    if(move.row == row && move.col == col) {
                        synchronized(HumanPlayer.this) {
                            // User clicked on a valid move, notify() to wake
                            // up
                            HumanPlayer.this.move = move;
                            HumanPlayer.this.notify();
                        }
                        board.removeMouseListener(this);
                        board.disableStonePicker();
                        break;
                    }
                }
            }
        });
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
    
    private StoneColor getColor(int index) {
        return index == 1? BLACK : WHITE;
    }
    
}
