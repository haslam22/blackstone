package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Simple state object for a Gomoku game.
 */
public class GameState implements Cloneable, Serializable {

    private int size;
    private int[][] board;
    private Stack<Move> moves;
    private int currentIndex = 1;

    /**
     * Create a new game state.
     * @param size Board size
     */
    public GameState(int size) {
        this.size = size;
        this.board = new int[size][size];
        this.moves = new Stack<>();
    }

    /**
     * Return the terminal status of the game.
     * @return 0 if not terminal, the player index of the winning player, or
     * 3 if the game ended in a draw.
     */
    public int terminal() {
        if(isWinner(1)) return 1;
        if(isWinner(2)) return 2;
        if(moves.size() == size * size) return 3;
        return 0;
    }

    /**
     * Get the current player index for this state
     * @return Current player # who has to make a move
     */
    public int getCurrentIndex() {
        return this.currentIndex;
    }

    /**
     * @return Size of the board for this state
     */
    public int getSize() {
        return size;
    }

    /**
     * Get an ordered list of moves that were made on this state.
     * @return ArrayList of moves, ordered from first move to last move made
     */
    public List<Move> getMovesMade() {
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
     * Return whether or not this state is empty.
     * @return True if no moves made on this state.
     */
    public boolean isEmpty() {
        return getMovesMade().isEmpty();
    }

    /**
     * Make a move on this state.
     * @param move Move to make
     */
    public void makeMove(Move move) {
        this.moves.push(move);
        this.board[move.row][move.col] = currentIndex;
        this.currentIndex = currentIndex == 1 ? 2 : 1;
    }

    /**
     * Undo the last move and return it.
     * @return Move that was removed from the state, or null if no moves exist
     */
    public Move undo() {
        if(this.moves.empty()) {
            return null;
        }
        Move move = this.moves.pop();
        this.board[move.row][move.col] = 0;
        this.currentIndex = currentIndex == 1 ? 2: 1;
        return move;
    }

    /**
     * Checks the given move is valid on this state.
     * @param move Input move
     * @return True if the move is valid
     */
    public boolean validateMove(Move move) {
        return (move.row < this.board.length && move.row >= 0)
                && (move.col < this.board.length && move.col >= 0)
                && !this.getMovesMade().contains(move);
    }

    @Override
    public GameState clone() {
        GameState newState = new GameState(this.size);
        for(Move move : this.getMovesMade()) {
            newState.makeMove(move);
        }
        return newState;
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
                    countConsecutiveStones(row, col, -1, 1) >= 4) {
                return true;
            }
            // Diagonal from the top left to the bottom right
            if(countConsecutiveStones(row, col, -1, -1) +
                    countConsecutiveStones(row, col, 1, 1) >= 4) {
                return true;
            }
            // Horizontal
            if(countConsecutiveStones(row, col, 0, 1) +
                    countConsecutiveStones(row, col, 0, -1) >= 4) {
                return true;
            }
            // Vertical
            if(countConsecutiveStones(row, col, 1, 0) +
                    countConsecutiveStones(row, col, -1, 0) >= 4) {
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
