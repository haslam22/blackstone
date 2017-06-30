package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Contains all the information about the game, including the settings
 * (time/board size) and the moves made. This object is visible to players
 * but should not be modified outside this package.
 */
public class GameState {

    private int[][] board;
    private Stack<Move> moves;
    private int intersections;
    private int currentIndex = 1;

    /**
     * Create a new game state
     * @param intersections Number of intersections on the board
     */
    GameState(int intersections) {
        this.intersections = intersections;
        this.board = new int[intersections][intersections];
        this.moves = new Stack<>();
    }

    /**
     * Return the terminal status of the game
     * @return 0 (game is not terminal), 1/2 if player 1/2 has won, 3 if the
     * board is full
     */
    int terminal() {
        if(isWinner(1)) return 1;
        if(isWinner(2)) return 2;
        if(moves.size() == intersections * intersections) return 3;
        return 0;
    }

    /**
     * Get the current player index for this state
     * @return Current player # who has to make a move
     */
    int getCurrentIndex() {
        return this.currentIndex;
    }

    /**
     * Get a list of moves that were made on this state
     * @return ArrayList of moves, from first move to last move made
     */
    public List<Move> getMoves() {
        return new ArrayList(moves);
    }

    /**
     * Return the last move made on this state
     * @return Previous move that was made
     */
    public Move getLastMove() {
        return moves.peek();
    }

    /**
     * Check if a move is valid
     * @param move Move to check
     * @return True if position is unoccupied, false otherwise
     */
    public boolean isLegalMove(Move move) {
        return board[move.getRow()][move.getCol()] == 0;
    }

    /**
     * Make a move on this state
     * @param move Move to make
     */
    void makeMove(Move move) {
        this.moves.push(move);
        this.board[move.getRow()][move.getCol()] = currentIndex;
        this.currentIndex = currentIndex == 1 ? 2 : 1;
    }

    /**
     * Undo the last move and return it.
     */
    Move undo() {
        if(this.moves.empty()) {
            return null;
        }
        Move move = this.moves.pop();
        this.board[move.getRow()][move.getCol()] = 0;
        this.currentIndex = currentIndex == 1 ? 2: 1;
        return move;
    }

    /**
     * Determine if the specified player has won the game
     * @param index Player index (1 or 2)
     * @return True if the index has won
     */
    private boolean isWinner(int index) {
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                if(board[i][j] == index) {
                    if(searchVertical(i, j, index, 4)) return true;
                    if(searchHorizontal(i, j, index, 4)) return true;
                    if(searchDiagonalLeft(i, j, index, 4)) return true;
                    if(searchDiagonalRight(i, j, index, 4)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Search vertically for a sequence of stones belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchVertical(int row, int col, int index, int amount) {
        if(row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }

    /**
     * Search horizontally for a sequence of stones belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchHorizontal(int row, int col, int index, int amount) {
        if(col + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row][col+k] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }

    /**
     * Search diagonally and down to the right for a sequence of stones
     * belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchDiagonalRight(int row, int col, int index,
                                        int amount) {
        if(col + amount < intersections && row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col+k] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }

    /**
     * Search diagonally and down to the left for a sequence of stones
     * belonging to an index
     * @param row Row position
     * @param col Column position
     * @param index Player index
     * @param amount Amount of stones to find
     * @return
     */
    private boolean searchDiagonalLeft(int row, int col, int index,
                                       int amount) {
        if(col - amount >= 0 && row + amount < intersections) {
            int count = 0;
            for(int k = 1; k <= amount; k++) {
                if(board[row+k][col-k] == index) {
                    count++;
                }
            }
            return count == amount;
        }
        return false;
    }
}
