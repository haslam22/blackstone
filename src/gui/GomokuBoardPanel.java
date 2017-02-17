package gui;

import gui.GomokuStone.StoneColor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;

/**
 * Adaptive JPanel which draws a square Gomoku grid in some given space.
 * @author Hassan
 */
public class GomokuBoardPanel extends JPanel {
    
    // Board properties, calculated dynamically based on the available space,
    // used to map from row/col to x/y on the board
    private int startX;
    private int startY;
    private int cellSize;
    private int padding;
    
    private int intersections;
    private GomokuStone[][] stones;
    private final Color backgroundColor = new Color(220, 180, 120);
    
    protected GomokuBoardPanel(int intersections) {
        this.intersections = intersections;
        this.stones = new GomokuStone[intersections][intersections];
        this.setDoubleBuffered(true);
        this.setBackground(backgroundColor);
    }
    
    public int getIntersections() {
        return this.intersections;
    }
    
    /**
     * Update the intersections, and redraw the grid
     * @param intersections Number of intersections
     */
    protected void updateIntersections(int intersections) {
        this.stones = new GomokuStone[intersections][intersections];
        this.intersections = intersections;
        this.repaint();
    }
    
    /**
     * Add a black stone to the board at a specified location
     * @param row Row of the stone to add
     * @param col Column of the stone to add
     * @param alpha Alpha transparency value
     */
    public void addBlackStone(int row, int col, float alpha) {
        this.stones[row][col] = new GomokuStone(StoneColor.BLACK, alpha);
        repaint();
    }
    
    /**
     * Add a white stone to the board at a specified location
     * @param row Row of the stone to add
     * @param col Column of the stone to add
     * @param alpha Alpha transparency value
     */
    public void addWhiteStone(int row, int col, float alpha) {
        this.stones[row][col] = new GomokuStone(StoneColor.WHITE, alpha);
        repaint();
    }
    
    /**
     * Remove the stone at the specified location
     * @param row Row of stone to remove
     * @param col Column of stone to remove
     */
    public void removeStone(int row, int col) {
        this.stones[row][col] = null;
        repaint();
    }
    
    /**
     * Reset the board, removing all pieces and removing any listeners that were
     * not removed from the previous game
     */
    public void reset() {
        this.stones = new GomokuStone[intersections][intersections];
        for(MouseMotionListener listener : this.getMouseMotionListeners()) {
            this.removeMouseMotionListener(listener);
        }
        for(MouseListener listener : this.getMouseListeners()) {
            this.removeMouseListener(listener);
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);    
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        
        // Draw a grid spanning the space we have - the panels lowest dimension
        int lowestDimension = Math.min(this.getWidth(), this.getHeight());
        
        // Minus one pixel here, so the stroke doesn't get cut off at the end
        int boardSize = lowestDimension;
        
        // Divide the space by intersections + 1. The grid is actually 
        // (intersections - 1)*(intersections - 1), but we add extra grid space
        // around the board for some padding
        this.cellSize = boardSize / (intersections + 1);
        
        // Division won't always be exact, so calculate the leftover space:
        int remainder = boardSize % (intersections + 1);
        
        // Set piece size to be a fraction of the cell size
        int stoneSize = (int) (cellSize * 0.8);
        
        // Set padding to cellsize, and spread the remainder around the board
        this.padding = cellSize + remainder / 2;
        
        // Get the highest dimension, so we can center the grid
        int highestDimension = Math.max(this.getWidth(), this.getHeight());
        int start = (highestDimension - boardSize) / 2;
        
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
                g2d.drawRect(x, y, cellSize, cellSize);
                // Add a subtle shadow around the grid
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawRect(x + 1, y + 1, cellSize, cellSize);
            }
        }
        
        // Draw the pieces
        for(int row = 0; row < intersections; row++) {
            for(int col = 0; col < intersections; col++) {
                if(stones[row][col] != null) {
                    stones[row][col].paintStone(g2d, 
                            getPanelX(col) - stoneSize/2, 
                            getPanelY(row) - stoneSize/2, 
                            stoneSize, 
                            stoneSize, 
                            this.backgroundColor
                    );
                }
            }
        }
    }
    
    /**
     * Get the X coordinate of a specified intersection column on the grid
     * @param col
     * @return
     */
    protected int getPanelX(int col) {
        return startX + padding + col*cellSize;
    }
    
    /**
     * Get the Y coordinate of a specified intersection row on the grid
     * @param row
     * @return
     */
    protected int getPanelY(int row) {
        return startY + padding + row*cellSize;
    }
    
    /**
     * Get the closest intersection row on the grid from a Y coordinate
     * @param y
     * @return
     */
    public int getNearestRow(int y) {
        y = (y - padding - startY) + cellSize/2;
        return y / cellSize;
    }
    
    /**
     * Get the closest intersection column on the grid from an X coordinate
     * @param x
     * @return
     */
    public int getNearestCol(int x) {
        x = (x - padding - startX) + cellSize/2;
        return x / cellSize;
    }
    
}