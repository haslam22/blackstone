package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
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
    private final Color colour;
    private JLabel timeLabel;
    private JComboBox selectionBox;
    
    protected GomokuPlayerPanel(int player, Color colour) {
        this.player = player;
        this.colour = colour;
        this.init();
    }
    
    public String getPlayerString() {
        return (String) selectionBox.getSelectedItem();
    }
    
    public void updateTime(String time) {
        this.timeLabel.setText(time);
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

        ColourPanel playerColourPanel = new ColourPanel(colour);
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
        
        private final Color colour;
        
        public ColourPanel(Color colour) {
            this.colour = colour;
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
            Ellipse2D circle = new Ellipse2D.Double(x, y, size, size);
            g2d.setColor(colour);
            g2d.fill(circle);
        }
    }
    
}
