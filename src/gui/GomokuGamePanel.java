package gui;

import gui.GomokuStone.StoneColor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * JPanel holding the player panels, and any other game controls
 * @author Hassan
 */
public class GomokuGamePanel extends JPanel {
    
    private final GomokuPlayerPanel[] playerPanels;
    private final GomokuFrame frame;
    private JButton newGameButton;
    private JButton forfeitButton;
    
    protected GomokuGamePanel(GomokuFrame frame) {
        this.frame = frame;
        this.playerPanels = new GomokuPlayerPanel[2];
        init();
    }
    
    protected void setForfeitEnabled(boolean enabled) {
        this.forfeitButton.setEnabled(enabled);
    }
    
    protected void setNewGameEnabled(boolean enabled) {
        this.newGameButton.setEnabled(enabled);
    }
    
    protected GomokuPlayerPanel getPlayerPanel(int player) {
        return playerPanels[player - 1];
    }
    
    private void init() {
        Border loweredetched = BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED);
        this.setBorder(BorderFactory.createTitledBorder(
                loweredetched, "Game"));
        
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        
        this.playerPanels[0] = new GomokuPlayerPanel(1, StoneColor.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(playerPanels[0], gbc);
        
        this.playerPanels[1] = new GomokuPlayerPanel(2, StoneColor.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(playerPanels[1], gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        this.add(buttonPanel, gbc);
        
        this.newGameButton = new JButton("New Game");
        this.forfeitButton = new JButton("Forfeit");
        forfeitButton.setEnabled(false);
        buttonPanel.add(newGameButton);
        buttonPanel.add(forfeitButton);
        
        newGameButton.addActionListener((ActionEvent e) -> {
            frame.handleNewGame();
        });
        
        forfeitButton.addActionListener((ActionEvent e) -> {
            frame.handleGameOver(); 
        });
        
        JLabel emptyLabel = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 20;
        gbc.weighty = 20;
        gbc.gridwidth = 2;
        this.add(emptyLabel, gbc);
    }
}
