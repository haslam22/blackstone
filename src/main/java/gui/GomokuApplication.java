package gui;

import gomoku.GomokuGame;
import gui.GomokuBoardPanel.CoordinateDisplay;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The root frame of the Gomoku application, holding the board panel on the left
 * and any additional components in a panel on the right.
 * @author Hassan
 */
public class GomokuApplication {
    
    private final GomokuBoardPanel boardPanel;
    private final GomokuGamePanel gamePanel;
    private final GomokuSettingsPanel settingsPanel;
    private final GomokuLogPanel logPanel;
    private GomokuGame game;
    
    private int time = 3000;
    private int intersections = 15;
    
    /**
     * Create the JFrame. Initialises the child panels (board, game, settings)
     * and assembles them in a GridLayout.
     */
    private GomokuApplication() {
        JFrame gomokuFrame = new JFrame();
        gomokuFrame.setTitle("Gomoku");
        gomokuFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        gomokuFrame.setResizable(true);
        gomokuFrame.setMinimumSize(new Dimension(900, 700));
        gomokuFrame.setPreferredSize(new Dimension(1000, 800));
        
        this.boardPanel = new GomokuBoardPanel(intersections);
        this.gamePanel = new GomokuGamePanel(this);
        this.settingsPanel = new GomokuSettingsPanel(this);
        this.logPanel = new GomokuLogPanel(this);
        
        // Create a panel to the right of the board
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(0, 1));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidePanel.add(this.gamePanel);
        sidePanel.add(this.settingsPanel);
        sidePanel.add(this.logPanel);
        
        // Create a split pane to divide the board panel/side panel
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                boardPanel, sidePanel);
        gomokuFrame.add(jSplitPane);
        gomokuFrame.pack();
        
        jSplitPane.setResizeWeight(1);
        jSplitPane.setDividerLocation(0.68);
        jSplitPane.setDividerSize(0);
        
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
    
    public void updateDisplayMode(CoordinateDisplay displayMode) {
        boardPanel.changeCoordinateDisplay(displayMode);
    }
    
    public void updateIntersections(int intersections) {
        this.intersections = intersections;
        this.boardPanel.updateIntersections(intersections);
    }
    
    public void updateTime(int time) {
        this.time = time;
    }
    
    public void updateStatus(String status) {
        logPanel.setStatus(status);
    }
    
    public void newGame(String[] playerStrings) {
        this.game = new GomokuGame(this, intersections, 
                playerStrings[0], playerStrings[1], time);
        this.game.start(this);
    }
    
    public void setChangesEnabled(boolean enabled) {
        this.settingsPanel.setEnabled(enabled);
        this.gamePanel.setEnabled(enabled);
    }
    
    public void forfeit() {
        this.game.stop(this);
    }
    
    public void writeLog(String text) {
        this.logPanel.appendText(text);
    }
    
    public void clearLog() {
        this.logPanel.clear();
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
