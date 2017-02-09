package gomoku;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * GomokuPanel draws the current state of Gomoku game, given a 2D integer array 
 * containing the values (0: Empty, 1: Black, 2: White).
 * @author Hassan
 */
public class GomokuBoard extends JPanel implements MouseListener {

    // A class representing any shape on the Gomoku board
    private class GomokuPiece {
        Shape shape;
        Color colour;
        Stroke stroke;
        Color strokeColour;
    }
    
    // CELLSIZE: Size for each square on the grid
    // INTERSECTIONS: Number of intersections on the grid
    // PIECESIZE: Size of the stones
    // PADDING: Outer padding on the grid
    private final int intersections;
    private final int cellsize = 40;
    private final int piecesize = 30;
    private final int padding = 20;
    
    // If we have N intersections, our 2D grid size is (N - 1) * (N - 1)
    // So the full board size is (N - 1) * CELLSIZE in each direction
    // We add PADDING*2 for some padding on the outside
    private final int boardsize;
    
    // Current pieces visible on the board
    private final List<GomokuPiece> pieces;
    
    protected GomokuBoard(int intersections) {
        this.intersections = intersections;
        this.boardsize = (this.intersections - 1)*cellsize + padding*2;
        this.pieces = new ArrayList<>();
        this.setSize(boardsize, boardsize);
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.addMouseListener(this);
    }
    
    /**
     * Draw a given Gomoku state onto the panel
     * @param state 2D integer array where [i][j] maps to a player index, or 0
     * if empty
     * @param colours An ordered array of colours forming a map between a player 
     * index and a colour, e.g. {Black, White, Purple, ...}
     */
    public void drawState(int[][] state, Color[] colours) {
        pieces.clear();
        
        for(int i = 0; i < state.length; i++) {
            for(int j = 0; j < state.length; j++) {
                int x = (padding) + j*cellsize;
                int y = (padding) + i*cellsize;
                
                // Check if there's a piece here
                if(state[i][j] != 0) {
                    pieces.add(createCirclePiece(x - piecesize/2, 
                            y - piecesize/2, piecesize, piecesize, 
                            colours[state[i][j] - 1], Color.BLACK, 1));
                }
                // Check if there's a win here:
                
            }
        }
        this.repaint();
    }
    
    private GomokuPiece createCirclePiece(int x, int y, int width, int height, 
            Color colour, Color strokeColour, int strokeAmount) {
        GomokuPiece piece = new GomokuPiece();
        piece.shape = new Ellipse2D.Double(x, y, width, height);
        piece.colour = colour;
        piece.stroke = new BasicStroke(strokeAmount);
        piece.strokeColour = strokeColour;
        return piece;
    }
    
    /**
     * Draw a line between two points (row, col) on the board
     * @param row1 Row of the first point 
     * @param col1 Col of the first point
     * @param row2 Row of the second point
     * @param col2 Col of the second point
     */
    public void highlightSection(int row1, int col1, int row2, int col2) {
        int x1 = (padding) + col1*cellsize;
        int y1 = (padding) + row1*cellsize;        
        
        int x2 = (padding) + col2*cellsize;
        int y2 = (padding) + row2*cellsize;
        
        GomokuPiece currentPiece = new GomokuPiece();
        currentPiece.shape = new Line2D.Double(x1, y1, x2, y2);
        currentPiece.colour = Color.GREEN;
        currentPiece.stroke = new BasicStroke(5);
        currentPiece.strokeColour = Color.GREEN;
        pieces.add(currentPiece);
        this.repaint();
    }
    
    public int getIntersections() {
        return this.intersections;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the grid
        for(int i = 0; i < intersections - 1; i++) {
            for(int j = 0; j < intersections - 1; j++) {
                int x = (padding) + i*cellsize;
                int y = (padding) + j*cellsize;
                g2d.drawRect(x, y, cellsize, cellsize);
            }
        }
        
        // Draw the pieces
        pieces.forEach((piece) -> {
            g2d.setColor(piece.colour);
            g2d.fill(piece.shape);
            g2d.setPaint(piece.strokeColour);
            g2d.setStroke(piece.stroke);
            g2d.draw(piece.shape);
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
               
    
}
