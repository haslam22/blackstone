package gomoku;

import java.awt.Color;
import players.GomokuPlayer;

/**
 * This class manages the game between two players, calling getMove() on
 * each player until the game ends.
 * @author Hassan
 */
public class GomokuGame {
    
    private GomokuState gameState;
    private final GomokuPlayer[] players;
    private final Color[] playerColours;
    private final GomokuBoard gamePanel;
    
    public GomokuGame(GomokuBoard gamePanel, int intersections,
            GomokuPlayer[] players, Color[] playerColours) {
        this.gameState = new GomokuState(intersections, players.length);
        this.gamePanel = gamePanel;
        this.players = players;
        this.playerColours = playerColours;
    }
    
    public void run() {
        // Call getMove() on the the current player
        while(!gameOver()) {
            GomokuLocation move = getPlayer(gameState.getCurrentPlayerIndex())
                    .getMove(gameState);
            this.gameState = gameState.makeMove(move);
            // Update the state
            gamePanel.drawState(gameState.board, playerColours);
        }
        
        // Highlight the winning move
    }
    
    /**
     * Check if the game has finished, i.e. at least one of the players has won,
     * or the game has ended in a draw (board is full).
     * @return
     */
    private boolean gameOver() {
        for(GomokuPlayer player : players) {
            if(gameState.isWinner(player.getIndex())) {
                return true;
            }
        }
        return gameState.isFull();
    }
    
    private GomokuPlayer getPlayer(int index) {
        for(GomokuPlayer player : players) {
            if(player.getIndex() == index) {
                return player;
            }
        }
        return null;
    }
    
    
}
