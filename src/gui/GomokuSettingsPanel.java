package gui;

import gui.GomokuBoardPanel.CoordinateDisplay;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * A JPanel showing any available settings for the game.
 * @author Hassan
 */
public class GomokuSettingsPanel extends JPanel {
    
    private final GomokuApplication app;
    private JComboBox intersections;
    private JComboBox time;
    
    protected GomokuSettingsPanel(GomokuApplication app) {
        this.app = app;
        init();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.intersections.setEnabled(enabled);
        this.time.setEnabled(enabled);
    }
    
    private void init() {
        Border loweredetched = BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED);
        this.setBorder(BorderFactory.createTitledBorder(
                loweredetched, "Settings"));
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel intersectionsLabel = new JLabel("Intersections (n*n)");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(intersectionsLabel, gbc);
        
        this.intersections = new JComboBox(new String[] {
            "15", 
            "19"
        });
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(intersections, gbc);
        
        JLabel timeLabel = new JLabel("AI Time Limit (ms)");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        this.add(timeLabel, gbc);
        
        this.time = new JComboBox(new String[] {
            "1000", 
            "2000",
            "3000",
            "4000",
            "5000",
        });
        time.setSelectedIndex(2);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(time, gbc);
        
        JLabel coordinateDisplayLabel = new JLabel("Coordinate Display Mode");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        this.add(coordinateDisplayLabel, gbc);
        
        JComboBox coordinateDisplayBox = new JComboBox(new CoordinateDisplay[] {
            CoordinateDisplay.ALGEBRAIC,
            CoordinateDisplay.NUMERIC
        });
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(coordinateDisplayBox, gbc);
        
        JLabel emptyLabel = new JLabel("");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 20;
        gbc.weighty = 20;
        this.add(emptyLabel, gbc);
        
        // Add event listeners to handle any settings changes
        this.intersections.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.updateIntersections(Integer.parseInt(
                        (String) intersections.getSelectedItem()));
            }
        });        
        this.time.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    app.updateTime(Integer.parseInt(
                            (String) time.getSelectedItem()));
                } catch(NumberFormatException ex) {
                    app.updateTime(0);
                }
            }
        });
        coordinateDisplayBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.updateDisplayMode(
                        (CoordinateDisplay) 
                                coordinateDisplayBox.getSelectedItem());
            }
        });
    }
    
}
