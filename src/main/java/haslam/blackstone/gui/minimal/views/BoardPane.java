package haslam.blackstone.gui.minimal.views;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/*
 * A dynamic pane which draws a Gomoku board to fill the available space.
 */
public class BoardPane extends Pane {

    private static Font BOARD_FONT = new Font("Arial", 11);

    private final Canvas canvas;
    private BoardStone[][] board;
    private int size;

    private double paddingY;
    private double paddingX;
    private double cellSize;
    private EventHandler<MouseEvent> mouseListener;

    /**
     * Create a new Gomoku Board with a specified number of intersections.
     * @param size Number of intersections (n*n) on the board
     */
    public BoardPane(int size) {
        this.size = size;
        this.board = new BoardStone[size][size];
        this.canvas = new Canvas();
        this.getChildren().add(canvas);
        widthProperty().addListener((observable, oldValue, newValue) ->
                canvas.setWidth(newValue.intValue()));
        heightProperty().addListener((observable, oldValue, newValue) ->
                canvas.setHeight(newValue.intValue()));
    }

    /**
     * Update the board size and repaint the board to reflect the change.
     * @param intersections New intersections value
     */
    public void setIntersections(int intersections) {
        this.size = intersections;
        this.layoutChildren();
    }

    /**
     * Clear the board, removing all stones.
     */
    public void clear() {
        this.board = new BoardStone[size][size];
        this.layoutChildren();
    }

    /**
     * Add a stone to the board.
     * @param index Index of the stone (1 if black, 2 if white)
     * @param row Row (0 to n)
     * @param col Col (0 to n)
     * @param transparent
     */
    public void addStone(int index, int row, int col, boolean transparent) {
        this.board[row][col] = new BoardStone(index, transparent);
        this.layoutChildren();
    }

    /**
     * Remove a stone from the board.
     * @param row Row (0 to n)
     * @param col Col (0 to n)
     */
    public void removeStone(int row, int col) {
        this.board[row][col] = null;
        this.layoutChildren();
    }

    /**
     * Draw a grid on some given graphics context.
     * @param gc Graphics context
     * @param startX Start point on x axis
     * @param startY Start point on y axis
     * @param rows Number of rows
     * @param columns Number of columns
     * @param cellSize Size of each cell in the grid
     */
    private void drawGrid(GraphicsContext gc, double startX, double startY, int
            rows, int columns, double cellSize) {
        gc.save();
        gc.setStroke(Color.rgb(0, 0, 0, 0.5));
        gc.setLineWidth(1.2);

        for(int i = 0; i <= columns; i++) {
            double offset = i*cellSize;
            gc.strokeLine(startX + offset, startY, startX + offset,
                    startY + cellSize * rows);
        }
        for(int i = 0; i <= rows; i++) {
            double offset = i*cellSize;
            gc.strokeLine(startX, startY + offset, startX +
                    cellSize * columns, startY + offset);
        }
        gc.restore();
    }

    /**
     * Draw numbers along the X axis for a previously drawn grid. Each number
     * aligns with the corresponding intersection on the grid.
     * @param gc Graphics context
     * @param startX Start point on x axis
     * @param startY Start point on y axis
     * @param rows Number of rows
     * @param columns Number of columns
     * @param cellSize Size of each cell in the grid
     * @param distance Draw distance away from the left of the grid
     */
    private void drawNumbers(GraphicsContext gc, double startX, double
            startY, int rows, int columns, double cellSize, double distance) {
        gc.save();
        gc.setFont(BOARD_FONT);
        gc.setFill(Color.rgb(0,0,0, 0.75));
        for(int i = 0; i < size; i++) {
            double offset = i*cellSize;
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(Integer.toString(rows + 1 - i), startX - distance,
                    startY + offset);
        }
        gc.restore();
    }

    /**
     * Draw letters along the Y axis for a previously drawn grid. Each letter
     * aligns with the corresponding intersection on the grid.
     * @param gc Graphics context
     * @param startX Start point on x axis
     * @param startY Start point on y axis
     * @param rows Number of rows
     * @param columns Number of columns
     * @param cellSize Size of each cell in the grid
     * @param distance Draw distance away from the bottom of the grid
     */
    private void drawLetters(GraphicsContext gc, double startX, double
            startY, int rows, int columns, double cellSize, double distance) {
        gc.save();
        gc.setFont(BOARD_FONT);
        gc.setFill(Color.rgb(0,0,0, 0.75));
        for(int i = 0; i < size; i++) {
            double offset = i*cellSize;
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(Character.toString((char)('A' + i)), startX + offset,
                    startY + cellSize*(rows) + distance);
        }
        gc.restore();
    }

    private static RadialGradient whiteGradient = new RadialGradient(55,
            0.75, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE),
            new Stop(1, Color.web("#A0A0A0"))
    );

    private static RadialGradient blackGradient = new RadialGradient(55,
            0.75, 0.5, 0.5, 0.5, true,
            CycleMethod.NO_CYCLE,
            new Stop(1, Color.web("#222")),
            new Stop(0, Color.web("#A0A0A0"))
    );

    /**
     * Paint a black/white stone onto a graphics context with a grid
     * @param gc Graphics context
     * @param startX Start (top left) x coordinate of the grid
     * @param startY Start (top left) y coordinate of the grid
     * @param cellSize Size of the grid cells
     * @param row Row position of the stone
     * @param col Column position of the stone
     * @param index Index of the stone (1 = black, 2 = white)
     * @param transparent Transparency value, 0.5 alpha if true
     */
    private static void paintStone(GraphicsContext gc, double startX, double
            startY, double cellSize, int row, int col, int index,
                                   boolean transparent) {
        double x = startX + col*cellSize;
        double y = startY + row*cellSize;
        double offset = (cellSize * 0.7) / 2;
        gc.save();
        if(transparent) {
            gc.setGlobalAlpha(0.5);
        }
        switch(index) {
            case 1:
                gc.setFill(blackGradient);
                gc.fillOval(x - offset, y - offset, cellSize * 0.7,
                        cellSize * 0.7);
                break;
            case 2:
                gc.setFill(whiteGradient);
                gc.fillOval(x - offset, y - offset, cellSize * 0.7,
                        cellSize * 0.7);
                break;
        }
        gc.restore();
    }

    /**
     * Given a mouse coordinate y axis value, return the closest row (0-n) on
     * the board
     * @param mouseY Mouse y axis position
     * @return
     */
    public int getClosestRow(double mouseY) {
        int closest = (int) Math.round((mouseY - paddingY) / cellSize);
        if(closest < 0) return 0;
        if(closest > size - 1) return size - 1;
        return closest;
    }

    /**
     * Given a mouse coordinate x axis value, return the closest column (0-n) on
     * the board
     * @param mouseX Mouse x axis position
     * @return
     */
    public int getClosestCol(double mouseX) {
        int closest = (int) Math.round((mouseX - paddingX) / cellSize);
        if(closest < 0) return 0;
        if(closest > size - 1) return size - 1;
        return closest;
    }

    /**
     * Add a mouse listener to display a transparent stone on the closest
     * intersection to the mouse cursor.
     */
    public void enableStonePicker(final int index) {
        this.mouseListener = e -> {
            int closestRow = getClosestRow(e.getY());
            int closestCol = getClosestCol(e.getX());
            if(board[closestRow][closestCol] == null) {
                addStone(index, closestRow, closestCol, true);
            }
        };
        this.addEventHandler(MouseEvent.MOUSE_MOVED, this.mouseListener);
    }

    /**
     * Remove the mouse listener added by enableStonePicker()
     */
    public void disableStonePicker() {
        if(this.mouseListener != null) {
            this.removeEventHandler(MouseEvent.MOUSE_MOVED, this.mouseListener);
            this.mouseListener = null;
        }
        this.layoutChildren();
    }

    @Override
    protected void layoutChildren() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Fit the grid into the available space, which may be limited by
        // either the height or the width of the pane
        double smallestAxis = Math.min(getHeight(), getWidth());
        this.cellSize = smallestAxis / (size - 1);

        // Add some padding around the grid, so we can display numbers/letters
        cellSize = (smallestAxis - (cellSize * 2)) / (size - 1);

        // Center the grid by accounting for any extra space around it
        double remainingSpaceX = getWidth() - (cellSize * (size - 1));
        double remainingSpaceY = getHeight() - (cellSize * (size - 1));

        // Spread the remaining space around the board
        this.paddingX = remainingSpaceX / 2;
        this.paddingY = remainingSpaceY / 2;

        // Draw the grid
        drawGrid(gc, paddingX, paddingY, size - 1, size - 1,
                cellSize);

        // Draw the numbers/letters
        drawNumbers(gc, paddingX, paddingY, size - 1, size - 1,
                cellSize, 0.7 * cellSize);
        drawLetters(gc, paddingX, paddingY, size - 1, size - 1,
                cellSize, 0.7 * cellSize);

        // Paint the stones
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                if(board[i][j] != null) {
                    paintStone(gc, paddingX, paddingY, cellSize, i, j,
                            board[i][j].index, board[i][j].transparent);
                    if(board[i][j].transparent) {
                        board[i][j] = null;
                    }
                }
            }
        }
    }

    /**
     * Represents a stone on the board.
     */
    private class BoardStone {
        private int index;
        private boolean transparent;

        public BoardStone(int index, boolean transparent) {
            this.index = index;
            this.transparent = transparent;
        }
    }

}