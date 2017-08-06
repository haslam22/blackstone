package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * State for a Gomoku game.
 */
public class GameState {

    private int size;
    private int[][] board;
    private Stack<Move> moves;
    private int currentIndex = 1;

    /**
     * Create a new game state.
     * @param size Board size
     */
    protected GameState(int size) {
        this.size = size;
        this.board = new int[size][size];
        this.moves = new Stack<>();
    }

    /**
     * Return the terminal status of the game.
     * @return 0 if not terminal, the player index of the winning player, or
     * 3 if the game ended in a draw.
     */
    protected int terminal() {
        if(isWinner(1)) return 1;
        if(isWinner(2)) return 2;
        if(moves.size() == size * size) return 3;
        return 0;
    }

    /**
     * Get the current player index for this state
     * @return Current player # who has to make a move
     */
    protected int getCurrentIndex() {
        return this.currentIndex;
    }

    /**
     * Get an ordered list of moves that were made on this state.
     * @return ArrayList of moves, ordered from first move to last move made
     */
    public List<Move> getMoves() {
        return new ArrayList(moves);
    }

    /**
     * Return the last move made on this state.
     * @return Previous move that was made
     */
    public Move getLastMove() {
        return !moves.isEmpty() ? moves.peek() : null;
    }

    /**
     * Make a move on this state.
     * @param move Move to make
     */
    protected void makeMove(Move move) {
        this.moves.push(move);
        this.board[move.row][move.col] = currentIndex;
        this.currentIndex = currentIndex == 1 ? 2 : 1;
    }

    /**
     * Undo the last move and return it.
     * @return Move that was removed from the state, or null if no moves exist
     */
    protected Move undo() {
        if(this.moves.empty()) {
            return null;
        }
        Move move = this.moves.pop();
        this.board[move.row][move.col] = 0;
        this.currentIndex = currentIndex == 1 ? 2: 1;
        return move;
    }

    /**
     * Determine if the specified player has won the game
     * @param playerIndex Player index (1 or 2)
     * @return True if the index has won
     */
    private boolean isWinner(int playerIndex) {
        if(moves.size() < 5) return false;
        Move lastMove = getLastMove();
        int row = lastMove.row;
        int col = lastMove.col;
        if(board[row][col] == playerIndex) {
            // Diagonal from the bottom left to the top right
            if(countConsecutiveStones(row, col, 1, -1) +
                    countConsecutiveStones(row, col, -1, 1) == 4) {
                return true;
            }
            // Diagonal from the top left to the bottom right
            if(countConsecutiveStones(row, col, -1, -1) +
                    countConsecutiveStones(row, col, 1, 1) == 4) {
                return true;
            }
            // Horizontal
            if(countConsecutiveStones(row, col, 0, 1) +
                    countConsecutiveStones(row, col, 0, -1) == 4) {
                return true;
            }
            // Vertical
            if(countConsecutiveStones(row, col, 1, 0) +
                    countConsecutiveStones(row, col, -1, 0) == 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to check if an index lies within the bounds of the board.
     * @param index Value to check
     * @return True if this value lies between the bounds of the board (0 to
     * size - 1)
     */
    private boolean inBounds(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Iterates along the board from a start position and counts the
     * consecutive stones belonging to a player. The row/column increment
     * defines the direction of the iteration - e.g. +1, -1 would iterate
     * diagonally down to the right. The start position must be occupied by
     * the player in question.
     * @param row Row start pos
     * @param col Column start pos
     * @param rowIncrement Row increment
     * @param colIncrement Column increment
     * @return The number of consecutive unbroken stones found
     */
    private int countConsecutiveStones(int row, int col, int rowIncrement,
                                       int colIncrement) {
        int count = 0;
        int index = board[row][col];
        for(int i = 1; i <= 4; i++) {
            if(inBounds(row + (rowIncrement*i)) && inBounds(col +
                    (colIncrement*i))) {
                if(board[row + (rowIncrement*i)][col + (colIncrement*i)] ==
                        index) {
                    count++;
                } else {
                    break;
                }
            }
        }
        return count;
    }
}
