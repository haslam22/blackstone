package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/**
 * Adaptive JPanel which draws a square Gomoku grid in some given space.
 * @author Hassan
 */
public class GomokuBoardPanel extends JPanel {
    
    public static enum StoneColor {
        BLACK, WHITE
    }
    
    private class GomokuStone {
        private final StoneColor color;
        private final boolean transparent;
        
        public GomokuStone(StoneColor color, boolean transparent) {
            this.color = color;
            this.transparent = transparent;
        }
    }
    
    // Board properties, calculated dynamically based on the available space,
    // used to map from row/col to x/y on the board
    private int startX;
    private int startY;
    private int cellSize;
    private int padding;
    
    private int intersections;
    private GomokuStone[][] stones;
    private MouseMotionListener motionListener;
    
    protected GomokuBoardPanel(int intersections) {
        this.intersections = intersections;
        this.stones = new GomokuStone[intersections][intersections];
        this.setDoubleBuffered(true);
        this.setBackground(new Color(220, 180, 120));
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
     * Add a stone to the board at the specified intersection
     * @param color
     * @param row Row of the stone to add
     * @param col Column of the stone to add
     */
    public void addStone(StoneColor color, int row, int col) {
        if(stones[row][col] == null) {
            this.stones[row][col] = new GomokuStone(color, false);
            repaint();
        }
    }
    
    /**
     * Add a transparent stone at the specified intersection, which is removed
     * when the panel is repainted
     * @param color
     * @param row
     * @param col
     */
    private void addTransparentStone(StoneColor color, int row, int col) {
        if(stones[row][col] == null) {
            this.stones[row][col] = new GomokuStone(color, true);
            repaint();
        }
    }
    
    /**
     * Reset the board, removing all pieces
     */
    public void reset() {
        this.stones = new GomokuStone[intersections][intersections];
        repaint();
    }
    
    public void enableStonePicker(StoneColor color) {
        if(this.motionListener == null) {
            this.motionListener = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int row = getNearestRow(e.getY());
                    int col = getNearestCol(e.getX());
                    if(row >= 0 && col >= 0 && row <= intersections - 1 &&
                            col <= intersections - 1) {
                        addTransparentStone(color, row, col);
                    }
                }
            };
            this.addMouseMotionListener(motionListener);
        }
    }
    
    public void disableStonePicker() {
        if(this.motionListener != null) {
            this.removeMouseMotionListener(motionListener);
            this.motionListener = null;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);    
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);

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
        
        for(int row = 0; row < intersections; row++) {
            for(int col = 0; col < intersections; col++) {
                if(stones[row][col] != null) {
                    paintStone(g2d, 
                            getPanelX(col) - stoneSize/2, 
                            getPanelY(row) - stoneSize/2,
                            stoneSize, 
                            stoneSize, 
                            stones[row][col].color, 
                            stones[row][col].transparent
                    );
                    if(stones[row][col].transparent) stones[row][col] = null;
                }
            }
        }
        
        g2d.dispose();
    }
    
    private static RadialGradientPaint getGradientPaint(StoneColor color,
            int x, int y, int width, int height) {
        Color[] colors = new Color[2];

        if(color == StoneColor.BLACK) {
            colors[0] = new Color(0xA0A0A0);
            colors[1] = new Color(0xE6000000);
        }
        else if(color == StoneColor.WHITE) {
            colors[0] = Color.WHITE;
            colors[1] = new Color(0xA0A0A0);
        }

        return new RadialGradientPaint(
                new Point2D.Double(0.5, 0.5), 
                0.5f, 
                new Point2D.Double(0.75, 0.75), 
                new float[]{0, 1}, 
                colors, 
                NO_CYCLE, 
                SRGB, 
                new AffineTransform(width, 0, 0, height, x, y)
        );
    }
    
    /**
     * Paint a semi-realistic looking Gomoku stone with a radial gradient
     * @param g2d Graphics context
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of the stone
     * @param height Height of the stone
     * @param color StoneColor.BLACK or StoneColor.WHITE
     * @param transparent
     */
    protected static void paintStone(Graphics2D g2d, int x, int y, int width, 
            int height, StoneColor color, boolean transparent) {
        
        Composite previousComposite = g2d.getComposite();
        
        if(transparent) {
            AlphaComposite alphacom = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f);
            g2d.setComposite(alphacom);
        }
        
        Shape stone = new Ellipse2D.Double(x, y, width, height);
        g2d.setPaint(getGradientPaint(color, x, y, width, height));
        g2d.fill(stone);
        g2d.setComposite(previousComposite);
    }
    
    /**
     * Get the X coordinate of a specified intersection column on the grid
     * @param col
     * @return
     */
    private int getPanelX(int col) {
        return startX + padding + col*cellSize;
    }
    
    /**
     * Get the Y coordinate of a specified intersection row on the grid
     * @param row
     * @return
     */
    private int getPanelY(int row) {
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