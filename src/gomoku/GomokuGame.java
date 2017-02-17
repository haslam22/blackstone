package gomoku;

import gui.GomokuBoardListener;
import gui.GomokuBoardPanel;
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
    private final GomokuBoardPanel gamePanel;
    private final int intersections;
    
    public GomokuGame(GomokuBoardPanel gamePanel, int intersections,
            String[] players) {
        this.intersections = intersections;
        this.gameState = new GomokuState(intersections);
        this.gamePanel = gamePanel;
        this.players = new GomokuPlayer[players.length];
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
            
            for(int row = 0; row < intersections; row++) {
                for(int col = 0; col < intersections; col++) {
                    if(gameState.board[row][col] == 1) {
                        gamePanel.addBlackStone(row, col, 1);
                    }
                    if(gameState.board[row][col] == 2) {
                        gamePanel.addWhiteStone(row, col, 1);
                    }
                }
            }
        }
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
     * Attach a move listener to the board which will notify a HumanPlayer
     * instance when a valid move has been clicked.
     * @param player The HumanPlayer instance to get a move for
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
