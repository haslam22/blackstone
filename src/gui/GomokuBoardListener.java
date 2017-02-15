package gui;

import gomoku.GomokuLocation;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import players.HumanPlayer;

/**
 * This class listens for a valid move on the board. When a valid move is 
 * detected, it calls notify() on a player instance.
 * @author Hassan
 */
public class GomokuBoardListener extends MouseAdapter {

    private final GomokuBoardPanel board;
    private final HumanPlayer player;
    private final List<GomokuLocation> moves;
    private GomokuLocation previousLocation;
    
    public GomokuBoardListener(GomokuBoardPanel board, HumanPlayer player,
            List<GomokuLocation> moves) {
        this.board = board;
        this.player = player;
        this.moves = moves;
    }    
    
    @Override
    public void mouseMoved(MouseEvent e) {
        // Clear the previous transparent stone, if any
        if(previousLocation != null) {
            board.clearTransparentStone(previousLocation.row, 
                    previousLocation.col);
        }
        int nearestRow = board.getNearestRow(e.getY());
        int nearestCol = board.getNearestCol(e.getX());
        
        // Search the pieces to see if the nearest row/col position to the
        // cursor is a valid move
        for(GomokuLocation move: moves) {
            if(move.row == nearestRow && move.col == nearestCol) {
                // Create a transparent stone
                board.addTransparentStone(move.row, move.col);
                // Set the previous location, so we can clear the stone on the
                // next mouse movement
                previousLocation = new GomokuLocation(move.row, move.col);
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Get the nearest row/col on the board from the mouse click location
        int nearestRow = board.getNearestRow(e.getY());
        int nearestCol = board.getNearestCol(e.getX());
        
        // Check if the row/col corresponds to a legal move
        for(GomokuLocation move : moves) {
            if(move.row == nearestRow && move.col == nearestCol) {
                // Player calls wait(), we call notify() on the player to
                // wake up the thread
                synchronized(this.player) {
                    player.location = new GomokuLocation(move.row, move.col);
                    player.notify();
                    board.removeMouseListener(this);
                    board.removeMouseMotionListener(this);
                }
                break;
            }
        }
    }
    
    
}
