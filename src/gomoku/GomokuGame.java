package gomoku;

import gui.GomokuBoardListener;
import gui.GomokuBoardPanel;
import java.awt.Color;
import players.GomokuPlayer;
import players.HumanPlayer;
import players.RandomPlayer;

/**
 * This class manages a game between two players, calling getMove() on
 * each player until the game ends.
 * @author Hassan
 */
public class GomokuGame implements Runnable {
    
    private GomokuState gameState;
    private final GomokuPlayer[] players;
    private final Color[] playerColours;
    private final GomokuBoardPanel gamePanel;
    
    public GomokuGame(GomokuBoardPanel gamePanel, int intersections,
            String[] players, Color[] playerColours) {
        this.gameState = new GomokuState(intersections, players.length);
        this.gamePanel = gamePanel;
        this.players = new GomokuPlayer[players.length];
        this.playerColours = playerColours;
        for(int i = 0; i < players.length; i++) {
            this.players[i] = createPlayer(players[i], i+1);
        }
    }
    
    @Override
    public void run() {
        gamePanel.reset();
        
        // Continuously grab the move from each player
        while(!gameOver()) {
            GomokuLocation move = getPlayer(gameState.getCurrentPlayerIndex())
                    .getMove(gameState);
            this.gameState = gameState.makeMove(move);
            gamePanel.drawState(gameState.board, playerColours, false);
        }
        
        // TODO: Highlight the winning move
    }
    
    /**
     * Create a new instance of a player, given a string
     * @param name The name of the player, corresponding to [name]Player.class
     * @param index The index to assign to this player
     * @return
     */
    private GomokuPlayer createPlayer(String name, int index) {
        switch(name) {
            case "Random":
                return new RandomPlayer(index);
            case "Human":
                return new HumanPlayer(index, this);
            default:
                return null;
        }
    }
    
    /**
     * 
     * @param player
     */
    public void addListener(HumanPlayer player) {
        GomokuBoardListener listener = new GomokuBoardListener(gamePanel, 
                player, gameState.getLegalMoves());
        this.gamePanel.addMouseListener(listener);
        this.gamePanel.addMouseMotionListener(listener);
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
