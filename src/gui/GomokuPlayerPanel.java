package gui;

import gui.GomokuStone.StoneColor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * A small panel showing the currently selected player, the stone colour,
 * and the time remaining.
 * @author Hassan
 */
public class GomokuPlayerPanel extends JPanel {
    
    private final int player;
    private final StoneColor color;
    private JLabel timeLabel;
    private JComboBox selectionBox;
    
    protected GomokuPlayerPanel(int player, StoneColor color) {
        this.player = player;
        this.color = color;
        this.init();
    }
    
    public String getPlayerString() {
        return (String) selectionBox.getSelectedItem();
    }
    
    public void updateTime(String time) {
        this.timeLabel.setText(time);
    }
    
    public void setSelectionEnabled(boolean enabled) {
        this.selectionBox.setEnabled(enabled);
    }
    
    private void init() {
        this.setPreferredSize(new Dimension(0, 150));
        this.setLayout(new GridBagLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED);
        this.setBorder(BorderFactory.createTitledBorder(
                etchedBorder, "Player " + player));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        this.selectionBox = new JComboBox(getPlayerStrings());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(selectionBox, gbc);

        ColourPanel playerColourPanel = new ColourPanel(color);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        gbc.weighty = 2;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(playerColourPanel, gbc);

        this.timeLabel = new JLabel("5:00", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        this.add(timeLabel, gbc);
    }    
    
    private String[] getPlayerStrings() {
        return new String[] {
            "Human",
            "Minimax",
            "Monte Carlo",
            "Neural",
            "Random"
        };
    }
    
    private class ColourPanel extends JPanel {
        
        private final StoneColor color;
        
        public ColourPanel(StoneColor color) {
            this.color = color;
        }
        
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(40, 40);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(40, 40);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            int size = Math.min(this.getHeight(), this.getWidth()) - 10;
            int x = (this.getWidth() / 2) - size / 2;
            int y = (this.getHeight() / 2) - size / 2;
            
            GomokuStone stone = new GomokuStone(color);
            stone.paintStone(g2d, x, y, size, size, Color.WHITE);
        }
    }
    
}
