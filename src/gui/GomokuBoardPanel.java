package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    
    protected enum CoordinateDisplay {
        ALGEBRAIC("Algebraic"), NUMERIC("Numeric");
        
        private final String name; 
        private CoordinateDisplay(String name) { 
            this.name = name; 
        }
        
        @Override 
        public String toString(){ 
            return name; 
        } 
    }
    
    // Board properties, calculated dynamically based on the available space
    private int startX;
    private int startY;
    private int cellSize;
    private int padding;
    
    private int intersections;
    private GomokuStone[][] stones;
    private MouseMotionListener motionListener;
    private final Font[] fonts;
    private CoordinateDisplay coordinateMode;
    
    protected GomokuBoardPanel(int intersections) {
        this.intersections = intersections;
        this.stones = new GomokuStone[intersections][intersections];
        this.setDoubleBuffered(true);
        this.setBackground(new Color(220, 180, 120)); // 220 180 120
        this.fonts = new Font[16];
        for(int i = 0; i < fonts.length; i++) {
            fonts[i] = new Font("Sans Serif", Font.PLAIN, i);
        }
        this.coordinateMode = CoordinateDisplay.ALGEBRAIC;
    }
    
    /**
     * Get the current board size.
     * @return Board size n, (total of n*n intersections)
     */
    public int getIntersections() {
        return this.intersections;
    }
    
    /**
     * Switch the display mode of the coordinates around the board edges. 
     * Supports either Algebraic mode (15A, 15B, ..) or Numeric (rows and 
     * columns start run from 0 -> intersections).
     * @param displayMode Display mode
     */
    public void changeCoordinateDisplay(CoordinateDisplay displayMode) {
        this.coordinateMode = displayMode;
        this.repaint();
    }
    
    /**
     * Update the intersections and redraw the grid
     * @param intersections Number of intersections
     */
    protected void updateIntersections(int intersections) {
        this.stones = new GomokuStone[intersections][intersections];
        this.intersections = intersections;
        this.repaint();
    }
    
    /**
     * Add a stone to the board at the specified intersection
     * @param color StoneColor.BLACK or StoneColor.WHITE
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
     * @param color StoneColor.BLACK or StoneColor.WHITE
     * @param row Row of the stone to add
     * @param col Column of the stone to add
     */
    private void addTransparentStone(StoneColor color, int row, int col) {
        if(stones[row][col] == null) {
            this.stones[row][col] = new GomokuStone(color, true);
            repaint();
        }
    }
    
    /**
     * Reset the board, removing all pieces and repainting
     */
    public void reset() {
        this.stones = new GomokuStone[intersections][intersections];
        repaint();
    }
    
    /**
     * Remove any listeners that were added to the board.
     */
    public void removeListeners() {
        MouseListener[] mouseListeners = this.getMouseListeners();
        for(MouseListener listener : mouseListeners) {
            this.removeMouseListener(listener);
        }
    }
    
    /**
     * Enable the stone picker for the board, showing a semi-transparent piece
     * on the intersection closest to the mouse cursor.
     * @param color
     */
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
    
    /**
     * Disable the stone picker for the board, if enabled.
     */
    public void disableStonePicker() {
        if(this.motionListener != null) {
            this.removeMouseMotionListener(motionListener);
            this.motionListener = null;
            this.repaint();
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
        int stoneSize = (int) (cellSize * 0.725);
        
        // Set padding to cellsize, and spread the remainder around the board
        this.padding = cellSize + (remainder / 2);
        
        // Get the highest dimension, so we can center the grid
        int highestDimension = Math.max(this.getWidth(), this.getHeight());
        int start = (highestDimension - boardSize) / 2;
        
        this.startX = highestDimension == this.getWidth() ? start : 0;
        this.startY = highestDimension == this.getHeight() ? start : 0;
        
        // Set the bounding rectangle size for our column/row strings
        int stringBoundingSize = (int) (cellSize * 1.3);
        
        // Get the current Sans Serif font
        int fontSize = (stringBoundingSize / 4) < 16 ? stringBoundingSize / 4 
                : 16;
        Font font = this.fonts[fontSize - 1];
        FontMetrics metrics = g2d.getFontMetrics(font);
        
        // Draw numbers for the rows
        for(int row = 0; row < intersections; row++) {
            // Row strings (1, 2, 3...)
            String rowString;
            if(this.coordinateMode == CoordinateDisplay.ALGEBRAIC) {
                rowString = Integer.toString(intersections - row);
            } else {
                rowString = Integer.toString(row);
            }
            int rectX = getPanelX(0) - stringBoundingSize; // Rectangle y
            int rectY = getPanelY(row) - stringBoundingSize / 2; // Rectangle x
            
            int stringWidth = (int) metrics.getStringBounds(rowString, g2d)
                    .getWidth();
            int stringHeight = (int) metrics.getAscent();
            
            int stringX = (rectX + stringBoundingSize / 2) - (stringWidth / 2);
            int stringY = (rectY + stringBoundingSize / 2) + (stringHeight / 2);
            
            g2d.setFont(font);
            g2d.drawString(rowString, stringX, stringY);
        }
        
        // Draw letters for the columns
        for(int col = 0; col < intersections; col++) {
            String columnString;
            if(this.coordinateMode == CoordinateDisplay.ALGEBRAIC) {
                columnString = String.valueOf((char)((col + 1) + 'A' - 1));
            } else {
                columnString = Integer.toString(col);
            }
            int rectX = getPanelX(col) - stringBoundingSize / 2; // Rectangle y
            int rectY = getPanelY(intersections - 1); // Rectangle x
            
            int stringWidth = (int) metrics.getStringBounds(columnString, g2d)
                    .getWidth();
            int stringHeight = (int) metrics.getAscent();
            
            int stringX = (rectX + stringBoundingSize / 2) - (stringWidth / 2);
            int stringY = (rectY + stringBoundingSize / 2) + (stringHeight / 2);
            
            g2d.setFont(font);
            g2d.drawString(columnString, stringX, stringY);
        }
        
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
    
    /**
     * Create the RadialGradientPaint for painting the stone.
     * @param color StoneColor.WHITE or StoneColor.BLACK
     * @param x X coordinate (starting at upper left)
     * @param y Y coordinate (starting at upper left)
     * @param width Width of the stone
     * @param height Height of the stone
     * @return
     */
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
     * Paint a fancy looking Gomoku stone with a radial gradient on some
     * given graphics context.
     * @param g2d Graphics context
     * @param x X coordinate (starting at upper left)
     * @param y Y coordinate (starting at upper left)
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