package gomoku;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import players.GomokuPlayer;
import players.RandomPlayer;

/**
 *
 * @author Hassan
 */
public class GomokuFrame extends JFrame {
    
    private GomokuBoard gamePanel;
    private JPanel settingsPanel;
    
    private GomokuFrame() {
        init();
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            // Use the system style so we can support high DPI displays better
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | 
                    InstantiationException | 
                    IllegalAccessException | 
                    UnsupportedLookAndFeelException ex) {
                System.out.println(ex);
            }        
            GomokuFrame mainFrame = new GomokuFrame();
            mainFrame.setVisible(true);
        });
    }
    
    public GomokuPlayer getPlayer(String name, int index) {
        switch(name) {
            case "Random":
                return new RandomPlayer(index);
            default:
                return null;
        }
    }
    
    private void addVerticalSpacing(JPanel panel, int spacing) {
        panel.add(Box.createRigidArea(new Dimension(
                panel.getWidth(), spacing)));
    }
    
    private void init() {
        this.setTitle("Gomoku");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.setResizable(false);
        
        // Create the game panel
        this.gamePanel = new GomokuBoard(15);
        gamePanel.setLocation(0, 0);
        this.add(gamePanel);
        
        // TODO: Use GridBagLayout here instead. BoxLayout is too restrictive
        
        // Create a settings panel to the left, set it to 1/3 * board width
        int settingsPanelWidth = (int) Math.floor(gamePanel.getWidth() / 3);
        this.settingsPanel = new JPanel();
        settingsPanel.setLocation(gamePanel.getWidth(), 0);
        settingsPanel.setSize(settingsPanelWidth, gamePanel.getHeight());
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(settingsPanel);
        
        addVerticalSpacing(settingsPanel, 10);
        
        // Player 1 selection
        JLabel player1Label = new JLabel("Player 1 (Black)");
        player1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        player1Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(player1Label);
        addVerticalSpacing(settingsPanel, 10);  

        JComboBox player1Box = new JComboBox(new String[] { "Random" });
        player1Box.setAlignmentX(Component.CENTER_ALIGNMENT);
        player1Box.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        settingsPanel.add(player1Box);
        addVerticalSpacing(settingsPanel, 10);  
        
        // Player 2 selection
        JLabel player2Label = new JLabel("Player 2 (White)");
        player2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        player2Label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        settingsPanel.add(player2Label);      
        addVerticalSpacing(settingsPanel, 10);  

        JComboBox player2Box = new JComboBox(new String[] { "Random" });
        player2Box.setAlignmentX(Component.CENTER_ALIGNMENT);
        player2Box.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        settingsPanel.add(player2Box);
        addVerticalSpacing(settingsPanel, 10);
        
        // Start button
        JButton startButton = new JButton("Start");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        settingsPanel.add(startButton);
        
        startButton.addActionListener((ActionEvent e) -> {
            handleStartButton(e);
        });
        
        // Fill the remaining space
        addVerticalSpacing(settingsPanel, gamePanel.getHeight());
        
        // Set the JFrame pane to the size of the components inside
        this.getContentPane().setPreferredSize(new Dimension(
                gamePanel.getWidth() + settingsPanelWidth, 
                gamePanel.getHeight()));
        this.pack();
    }
    
    public void handleStartButton(ActionEvent e) {
        GomokuPlayer[] players = new GomokuPlayer[] {
            getPlayer("Random", 1), 
            getPlayer("Random", 2)
        };
        Color[] playerColours = new Color[] { 
            Color.BLACK, 
            Color.WHITE
        };
        
        GomokuGame game = new GomokuGame(gamePanel, 
                gamePanel.getIntersections(), players, playerColours);
        game.run();
    }
}
