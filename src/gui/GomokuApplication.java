package gui;

import gomoku.GomokuGame;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import players.GomokuPlayer;
import players.HumanPlayer;
import players.MinimaxPlayer;
import players.RandomPlayer;

/**
 * The root frame of the Gomoku application, holding the board panel on the left
 * and any additional components in a panel on the right.
 * @author Hassan
 */
public class GomokuApplication {
    
    private final GomokuBoardPanel boardPanel;
    private final GomokuGamePanel gamePanel;
    private final GomokuSettingsPanel settingsPanel;
    private GomokuGame game;
    private Thread gameThread;
    
    private int time = 5;
    private int intersections = 15;
    
    private GomokuApplication() {
        JFrame gomokuFrame = new JFrame();
        gomokuFrame.setTitle("Gomoku");
        gomokuFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        gomokuFrame.setResizable(true);
        gomokuFrame.setMinimumSize(new Dimension(1000, 800));
        gomokuFrame.setPreferredSize(new Dimension(1000, 800));
        
        this.boardPanel = new GomokuBoardPanel(intersections);
        this.gamePanel = new GomokuGamePanel(this);
        this.settingsPanel = new GomokuSettingsPanel(this);
        
        // Create a panel to the right of the board
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(0, 1));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidePanel.add(this.gamePanel);
        sidePanel.add(this.settingsPanel);
        
        // Create a split pane to divide the board panel/side panel
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                boardPanel, sidePanel);
        gomokuFrame.add(jSplitPane);
        gomokuFrame.pack();
        
        jSplitPane.setResizeWeight(1);
        jSplitPane.setDividerLocation(0.7);
        jSplitPane.setDividerSize(0);
        
        // Create a menu bar, currently used for debugging only
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Debug");
        JMenuItem printStateOption = new JMenuItem("Print State");
        printStateOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printState();
            }
        });
        menu.add(printStateOption);
        menuBar.add(menu);
        
        gomokuFrame.setJMenuBar(menuBar);
        gomokuFrame.setVisible(true);
    }
    
    public GomokuBoardPanel getBoardPanel() {
        return this.boardPanel;
    }
    
    public GomokuSettingsPanel getSettingsPanel() {
        return this.settingsPanel;
    }
    
    public GomokuGamePanel getGamePanel() {
        return this.gamePanel;
    }
    
    public void updateIntersections(int intersections) {
        this.intersections = intersections;
        this.boardPanel.updateIntersections(intersections);
    }
    
    public void updateTime(int time) {
        this.time = time;
        this.gamePanel.updateTime(time);
    }
    
    public void updateStatus(String status) {
        this.gamePanel.updateStatus(status);
    }
    
    public void newGame(String[] playerStrings) {
        this.gamePanel.setEnabled(false);
        GomokuPlayer player1 = createPlayer(playerStrings[0], 1, 2);
        GomokuPlayer player2 = createPlayer(playerStrings[1], 2, 1);
        this.game = new GomokuGame(this, intersections, player1, player2);
        this.gameThread = new Thread(game);
        this.gameThread.start();
    }
    
    public void forfeit() {
        this.gameThread.interrupt();
        this.gamePanel.setEnabled(true);
    }
    
    private void printState() {
        if(game != null) {
            int[][] board = game.getState().getBoardArray();
            for(int i = 0; i < board.length; i++) {
                for(int j = 0; j < board.length; j++) {
                    if(j == board.length - 1) { 
                        System.out.print(board[i][j]);
                    } else {
                        System.out.print(board[i][j] + ", ");
                    }
                }
                System.out.println();
            }
        }
    }
    
    private GomokuPlayer createPlayer(String name, 
            int playerIndex, int opponentIndex) {
        switch(name) {
            case "Human":
                return new HumanPlayer(playerIndex, opponentIndex, 
                        this.boardPanel);
            case "Random":
                return new RandomPlayer(playerIndex, opponentIndex);
            case "Minimax":
                return new MinimaxPlayer(playerIndex, opponentIndex);
            default:
                return null;
        }
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | 
                    InstantiationException | 
                    IllegalAccessException | 
                    UnsupportedLookAndFeelException ex) {
                System.out.println("Unable to set system style: " + ex);
            }        
            GomokuApplication app = new GomokuApplication();
        });
    }
}
