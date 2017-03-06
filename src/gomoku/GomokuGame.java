package gomoku;

import gui.GomokuApplication;
import gui.GomokuBoardPanel;
import gui.GomokuBoardPanel.StoneColor;
import static gui.GomokuBoardPanel.StoneColor.BLACK;
import static gui.GomokuBoardPanel.StoneColor.WHITE;
import players.GomokuPlayer;

/**
 * This class manages a game between two players, calling getMove() on
 * each player until the game ends.
 * @author Hassan
 */
public class GomokuGame implements Runnable {
    
    private final GomokuState state;
    private final GomokuPlayer[] players;
    private final GomokuBoardPanel board;
    private final GomokuApplication app;
    
    public GomokuGame(GomokuApplication app, int intersections,
            GomokuPlayer[] players) {
        this.state = new GomokuState(intersections);
        this.players = players;
        this.app = app;
        this.board = app.getBoardPanel();
    }
    
    @Override
    public void run() {
        board.reset();
        
        // Continuously grab the move from each player
        while(!gameOver() && !Thread.interrupted()) {
            app.updateStatus("Waiting for turn from player " + 
                    state.getCurrentIndex() + "...");
            GomokuMove move;
            try {
                // Pass a copy of the state and request a move
                move = players[state.getCurrentIndex() - 1]
                        .getMove(state.copy());
                // Add a stone to the board panel
                board.addStone(getColor(state.getCurrentIndex()), move.row, 
                        move.col);
                // Make the move on our copy of the state
                this.state.makeMove(move);
            } catch (NullPointerException e) {
                // A move wasn't returned, exit
                break;
            }
        }
        
        // Set the winner in the panel
        int winner = getWinner();
        if(winner == 1 || winner == 2) {
            app.updateStatus("Game over. Winner: Player " + winner);
        } else {
            app.updateStatus("Game over. Winner: N/A (Draw)");
        }
        
        // Game has finished, cleanup
        board.repaint();
        board.disableStonePicker();
        app.forfeit();
    }
    
    public GomokuState getState() {
        return this.state;
    }
    
    private boolean gameOver() {
        return state.isWinner(1) || state.isWinner(2) || state.isFull();
    }
    
    private int getWinner() {
        if(state.isWinner(1)) return 1;
        if(state.isWinner(2)) return 2;
        else return 0;
    }
    
    private StoneColor getColor(int index) {
        return index == 1? BLACK : WHITE;
    }
}
