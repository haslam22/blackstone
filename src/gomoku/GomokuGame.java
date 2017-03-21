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
    
    /**
     * Create a new game between two players.
     * @param app App instance
     * @param intersections Game intersections
     * @param player1 Player 1
     * @param player2 Player 2
     */
    public GomokuGame(GomokuApplication app, int intersections,
            GomokuPlayer player1, GomokuPlayer player2) {
        this.state = new GomokuState(intersections);
        this.players = new GomokuPlayer[] { player1, player2 };
        this.app = app;
        this.board = app.getBoardPanel();
    }
    
    /**
     * Draw the state of the current game onto the board panel.
     * @param state Current state
     */
    private void drawState(GomokuState state) {
        int[][] boardArray = state.getBoardArray();
        for(int i = 0; i < boardArray.length; i++) {
            for(int j = 0; j < boardArray.length; j++) {
                if(boardArray[i][j] == 1) {
                    board.addStone(StoneColor.BLACK, i, j);
                }
                if(boardArray[i][j] == 2) {
                    board.addStone(StoneColor.WHITE, i, j);
                }
            }
        }
    }
    
    @Override
    public void run() {
        board.reset();
        
        // Start the game, request a move from each player in a loop
        while(!gameOver() && !Thread.interrupted()) {
            app.updateStatus("Waiting for turn from player " + 
                    state.getCurrentIndex() + "...");
            try {
                // Pass a copy of the state and request a move
                GomokuMove move = players[state.getCurrentIndex() - 1]
                        .getMove(state.copy());
                this.state.makeMove(move);
                this.drawState(state);
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
