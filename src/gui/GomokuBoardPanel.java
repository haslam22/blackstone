package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

/**
 * Adaptive JPanel which draws a square Gomoku grid in some given space.
 * @author Hassan
 */
public class GomokuBoardPanel extends JPanel {
    
    
    // Board properties, calculated dynamically
    private int startX;
    private int startY;
    private int cellsize;
    private int piecesize;
    private int padding;
    private int boardsize;
    
    private int intersections;
    private Color[][] stones;
    
    protected GomokuBoardPanel(int intersections) {
        this.intersections = intersections;
        this.stones = new Color[intersections][intersections];
        this.setDoubleBuffered(true);
        this.setBackground(new Color(242, 187, 119));
    }
    
    public int getIntersections() {
        return this.intersections;
    }
    
    public void updateIntersections(int intersections) {
        this.stones = new Color[intersections][intersections];
        this.intersections = intersections;
        this.repaint();
    }
    
    public void clearTransparentStone(int row, int col) {
        stones[row][col] = null;
        repaint();
    }
    
    public void addTransparentStone(int row, int col) {
        stones[row][col] = new Color(0, 0, 0, 90);
        repaint();
    }
    
    /**
     * Reset the board, removing all pieces and removing any listeners that were
     * not removed from the previous game
     */
    public void reset() {
        for(int row = 0; row < intersections; row++) {
            for(int col = 0; col < intersections; col++) {
                stones[row][col] = null;
            }
        }
        removeListeners();
        repaint();
    }
    
    public void removeListeners() {
        for(MouseMotionListener listener : this.getMouseMotionListeners()) {
            this.removeMouseMotionListener(listener);
        }
        for(MouseListener listener : this.getMouseListeners()) {
            this.removeMouseListener(listener);
        }
    }
    
    /**
     * Draw a given Gomoku state onto the panel
     * @param state 2D integer array where [i][j] maps to a player index, or 0
     * if empty
     * @param colours An ordered array of colours forming a map between a player 
     * index and a colour, e.g. [Black, White, Purple, ...]
     * @param terminal
     */
    
    // This should not be in the panel, but be handled in the game
    public void drawState(int[][] state, Color[] colours, boolean terminal) {
        for(int row = 0; row < state.length; row++) {
            for(int col = 0; col < state.length; col++) {
                // Check if the current row/col in state isn't empty
                if(state[row][col] != 0) {
                    // Set the corresponding colour
                    stones[row][col] = colours[state[row][col] - 1];
                }
            }
        }
        this.repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a grid spanning the space we have - the panels lowest dimension
        int lowestDimension = Math.min(this.getWidth(), this.getHeight());
        
        // Minus one pixel here, so the stroke doesn't get cut off at the end
        this.boardsize = lowestDimension;
        
        // Divide the space by intersections + 1. The grid is actually 
        // (intersections - 1)*(intersections - 1), but we add extra grid space
        // around the board for some padding
        this.cellsize = boardsize / (intersections + 1);
        
        // Division won't always be exact, so calculate the leftover space:
        int remainder = boardsize % (intersections + 1);
        
        // Set piece size to be a fraction of the cell size
        this.piecesize = (int) (cellsize * 0.8);
        
        // Set padding to cellsize, and spread the remainder around the board
        this.padding = cellsize + remainder / 2;
        
        // Get the highest dimension, so we can center the grid
        int highestDimension = Math.max(this.getWidth(), this.getHeight());
        int start = (highestDimension - boardsize) / 2;
        
        this.startX = highestDimension == this.getWidth() ? start : 0;
        this.startY = highestDimension == this.getHeight() ? start : 0;
        
        // Draw the grid
        for(int row = 0; row < intersections - 1; row++) {
            for(int col = 0; col < intersections - 1; col++) {
                // Get the panel location for the current row, col, which is
                // padding + row/col*cellsize
                int x = getPanelX(col);
                int y = getPanelY(row);
                // Draw the rectangle outline
                g2d.setColor(new Color(0, 0, 0));
                g2d.drawRect(x, y, cellsize, cellsize);
                // Add a subtle shadow around the grid
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawRect(x + 1, y + 1, cellsize, cellsize);
            }
        }
        
        // Draw the pieces
        for(int row = 0; row < intersections; row++) {
            for(int col = 0; col < intersections; col++) {
                if(stones[row][col] != null) {
                    g2d.setColor(stones[row][col]);
                    g2d.fill(new Ellipse2D.Double(
                            getPanelX(col) - piecesize/2, 
                            getPanelY(row) - piecesize/2, 
                            piecesize, 
                            piecesize));
                }
            }
        }
        
        g2d.dispose();
    }
    
    /**
     * Get the X coordinate of a specified intersection column on the grid
     * @param col
     * @return
     */
    protected int getPanelX(int col) {
        return startX + padding + col*cellsize;
    }
    
    /**
     * Get the Y coordinate of a specified intersection row on the grid
     * @param row
     * @return
     */
    protected int getPanelY(int row) {
        return startY + padding + row*cellsize;
    }
    
    /**
     * Get the closest intersection row on the grid from a Y coordinate
     * @param y
     * @return
     */
    public int getNearestRow(int y) {
        y = (y - padding - startY) + cellsize/2;
        return y / cellsize;
    }
    
    /**
     * Get the closest intersection column on the grid from an X coordinate
     * @param x
     * @return
     */
    public int getNearestCol(int x) {
        x = (x - padding - startX) + cellsize/2;
        return x / cellsize;
    }
    
}