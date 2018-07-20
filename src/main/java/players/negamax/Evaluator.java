package players.negamax;

/**
 * Evaluation function for Gomoku board positions. The evaluation works
 * per-field (intersection) on the board by evaluating the strength of each
 * stone on the board.
 *
 *
 *  *       *        *
 *    *     *      *
 *      *   *    *
 *        * *  *
 * * * * * [X] * * * *
 *       *  *  *
 *     *    *    *
 *   *      *      *
 * *        *        *
 *
 * The strength of a stone on the board is evaluated by looking at all 4
 * directions around the stone (vertical, horizontal, and two diagonals) - as
 * seen above. A window of length 5 is passed across each direction from the
 * start to the end and the amount of moves to create 5 consecutive stones is
 * recorded.
 *
 * For every five created in one move, the evaluator assigns 19 points, 15
 * for a five created in 2 moves, 11 for 3 moves, 7 for 4 moves and 3 for 5
 * moves. After the SCORES are calculated for every stone, the opponents
 * total strength is subtracted from the score and the current players
 * strength is added to give a final value.
 *
 * This evaluation function was adapted from the open-source AI Carbon, created
 * by Michał Czardybon.
 */
public class Evaluator {

    private static final int[] SCORES = {19, 15, 11, 7, 3};

    /**
     * Given some array representing a vertical/horizontal/diagonal direction
     * on the board, calculate a score based on how many possible fives can be
     * formed and in how many moves.
     *
     * @param direction A 1D field array representing a direction on the board
     * @return Score for this direction
     */
    private static int scoreDirection(Field[] direction, int index) {
        int score = 0;

        // Pass a window of 5 across the field array
        for(int i = 0; (i + 4) < direction.length; i++) {
            int empty = 0;
            int stones = 0;
            for(int j = 0; j <= 4; j++) {
                if(direction[i + j].index == 0) {
                    empty++;
                }
                else if(direction[i + j].index == index) {
                    stones++;
                } else {
                    // Opponent stone in this window, can't form a five
                    break;
                }
            }
            // Ignore already formed fives, and empty windows
            if(empty == 0 || empty == 5) continue;

            // Window contains only empty spaces and player stones, can form
            // a five, get score based on how many moves needed
            if(stones + empty == 5) {
                score += SCORES[empty];
            }
        }
        return score;
    }

    /**
     * Evaluate a state from the perspective of the current player.
     * @param state State to evaluate
     * @return Score from the current players perspective
     */
    public static int evaluateState(State state, int depth) {
        int playerIndex = state.currentIndex;
        int opponentIndex = playerIndex == 1 ? 2 : 1;

        // Check for a winning/losing position
        int terminal = state.terminal();
        if(terminal == playerIndex) return 10000 + depth;
        if(terminal == opponentIndex) return -10000 - depth;

        // Evaluate each field separately, subtracting from the score if the
        // field belongs to the opponent, adding if it belongs to the player
        int score = 0;
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    score -= evaluateField(state, i, j, opponentIndex);
                } else if(state.board[i][j].index == playerIndex) {
                    score += evaluateField(state, i, j, playerIndex);
                }
            }
        }
        return score;
    }

    public static int evaluateField(State state, int row, int col, int index) {
        int score = 0;
        for(int direction = 0; direction < 4; direction++) {
            score += scoreDirection(state.directions[row][col][direction],
                    index);
        }
        return score;
    }

}
