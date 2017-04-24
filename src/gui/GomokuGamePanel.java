package gui;

import gui.GomokuBoardPanel.StoneColor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * Intermediate JPanel holding the player panels, and any other game controls
 * (new game, forfeit, etc)
 * @author Hassan
 */
public class GomokuGamePanel extends JPanel {
    
    private final GomokuApplication app;
    private final GomokuPlayerPanel[] playerPanels;
    private JButton newGameButton;
    private JButton forfeitButton;
    
    protected GomokuGamePanel(GomokuApplication app) {
        this.app = app;
        this.playerPanels = new GomokuPlayerPanel[2];
        init();
    }
    
    protected void updateTime(int time) {
        if(time == 0) {
            playerPanels[0].setTime("No time limit");
            playerPanels[1].setTime("No time limit");
        } else {
            playerPanels[0].setTime(time + ":00");
            playerPanels[1].setTime(time + ":00");
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        playerPanels[0].setEnabled(enabled);
        playerPanels[1].setEnabled(enabled);
        if(enabled) {
            forfeitButton.setEnabled(false);
            newGameButton.setEnabled(true);
        } else {
            forfeitButton.setEnabled(true);
            newGameButton.setEnabled(false);
        }
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
        
        // Add listeners to handle any game buttons
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.newGame(new String[] { 
                    playerPanels[0].getPlayerString(),
                    playerPanels[1].getPlayerString()
                });
            }
        });
        
        forfeitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.forfeit();
            }
        });
    }
}
