package gui;

import gomoku.GomokuGame;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The root frame of the Gomoku application, holding the board panel on the left
 * and any additional components in a panel on the right.
 * @author Hassan
 */
public class GomokuFrame extends JFrame {
    
    private GomokuBoardPanel boardPanel;
    private GomokuGamePanel gamePanel;
    private GomokuSettingsPanel settingsPanel;
    private Thread gameThread;
    
    private GomokuFrame() {
        init();
    }
    
    private void init() {
        this.setTitle("Gomoku");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(800, 600));
        this.setPreferredSize(new Dimension(800, 600));
        
        this.boardPanel = new GomokuBoardPanel(15);
        this.gamePanel = new GomokuGamePanel(this);
        this.settingsPanel = new GomokuSettingsPanel(this);
        
        // Create a panel to the right of the board, for any additional
        // components. Use a GridLayout with one column, to give each
        // component the same space.
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(0, 1));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidePanel.add(this.gamePanel);
        sidePanel.add(this.settingsPanel);
        
        // Create a split pane to divide the board panel/side panel
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                boardPanel, sidePanel);
        this.add(jSplitPane);
        this.pack();
        
        jSplitPane.setResizeWeight(1);
        jSplitPane.setDividerLocation(0.65);
        jSplitPane.setDividerSize(0);
    }
    
    protected void handleNewGame() {
        settingsPanel.lockSettings();
        gamePanel.setForfeitEnabled(true);
        gamePanel.setNewGameEnabled(false);
        
        String[] players = new String[] {
            gamePanel.getPlayerPanel(1).getPlayerString(),
            gamePanel.getPlayerPanel(2).getPlayerString()
        };
        
        GomokuGame game = new GomokuGame(this.boardPanel, 
                this.boardPanel.getIntersections(), players);
        this.gameThread = new Thread(game);
        gameThread.start();
    }
    
    protected void handleForfeit() {
        settingsPanel.unlockSettings();
        gamePanel.setForfeitEnabled(false);
        gamePanel.setNewGameEnabled(true);
    }
    
    protected void handleIntersectionsChange(int intersections) {
        boardPanel.updateIntersections(intersections);
    }
    
    protected void handleTimeChange(String time) {
        gamePanel.getPlayerPanel(1).updateTime(time);
        gamePanel.getPlayerPanel(2).updateTime(time);
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
            GomokuFrame mainFrame = new GomokuFrame();
            mainFrame.setVisible(true);
        });
    }
}
