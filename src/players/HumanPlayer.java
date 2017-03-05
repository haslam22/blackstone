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
 * HumanPlayer attaches a listener to the board and waits for valid input
 * before returning a move.
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
        board.enableStonePicker(getColor(state.getCurrentIndex()));
        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = board.getNearestRow(e.getY());
                int col = board.getNearestCol(e.getX());
                for(GomokuMove move : state.getMoves()) {
                    if(move.row == row && move.col == col) {
                        synchronized(HumanPlayer.this) {
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
