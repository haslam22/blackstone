package players.minimax;

/**
 * Static evaluator, providing heuristic evaluations for positions in the game.
 * 
 * In MinimaxState, we store the neighbour fields around a field, up to 
 * 4 intersections in each direction (diagonal backwards, diagonal forward, 
 * vertical, horizontal) forming a star shape:
 * 
 *  *       *       *
 *    *     *     *
 *      *   *   *
 *        * * *
 *  * * * * 0 * * * *
 *        * * * 
 *      *   *   *
 *    *     *     *
 *  *       *       *
 * 
 * To evaluate a field, all 4 directions are analysed and the amount of "fives"
 * that can be created passing through the field are counted. Each five is 
 * assigned a score based on how many moves were needed to create it.
 * 
 * @author Hassan
 */
public class MinimaxEvaluator {
    
    private static final int[][][][][][][][][][] SCORES;
    
    /**
     * Evaluate a state by looking up the evaluation for each stone on the
     * board.
     * @param state
     * @param playerIndex
     * @param opponentIndex
     * @return 
     */
    protected int evaluate(MinimaxState state, int playerIndex, 
            int opponentIndex) {
        int score = 0;
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    score -= evaluateField(state.board[i][j], 
                            opponentIndex);
                } else if(state.board[i][j].index == playerIndex) {
                    score += evaluateField(state.board[i][j], 
                            playerIndex);
                }
            }
        }
        return score;
    }    
    
    /**
     * Lookup the evaluation of a field from the perspective of a player.
     * @param field Field to evaluate
     * @param index Player index to evaluate for
     * @return Score of this field
     */
    protected int evaluateField(MinimaxField field, int index) {
        int score = 0;
        for(int i = 0; i < 4; i++) {
            score+= SCORES[index - 1]
                    [field.directions[i][0].index]
                    [field.directions[i][1].index]
                    [field.directions[i][2].index]
                    [field.directions[i][3].index]
                    [field.directions[i][4].index]
                    [field.directions[i][5].index]
                    [field.directions[i][6].index]
                    [field.directions[i][7].index]
                    [field.directions[i][8].index];
        }
        return score;
    }    
    
    /**
     * Given some array representing a vertical/horizontal/diagonal direction
     * on the board, calculate a score based on how many possible 5's can be 
     * formed for a player and in how many moves.
     * @param direction A 1D array representing a direction on the board
     * @param index The player index to check (1 or 2)
     * @return Score for this direction
     */
    private static int scoreDirection(int[] direction, int index) {
        int score = 0;
        // Scores for making a 5
        int[] scores = {19, 15, 11, 7, 3};
        
        for(int i = 0; i < direction.length; i++) {
            if(i + 4 < direction.length) {
                int stones = 0;
                int empty = 0;
                // Pass a window of 5 across the direction and check how many
                // stones and empty spots there are
                for(int j = 0; j <= 4; j++) {
                    if(direction[i + j] == index) stones++;
                    else if(direction[i + j] == 0) empty++;
                }
                if(stones == 5) {
                    return 10000;
                }
                // First check if it's possible to form a 5 in this window
                if(stones + empty == 5 && empty != 5) {
                    // Amount of empty spots = # of moves needed to make a 5
                    score += scores[empty];
                }
            }
        }
        return score;
    }
    
    static {
        SCORES = new int[2][4][4][4][4][4][4][4][4][4];
        
        // Generate all possible numbers of length 9 in radix 4 (0,1,2,3)
        for(int i = 0; i < 262144; i++) {
            String numStr = String.format("%9s", Integer.toString(i, 4))
                    .replace(" ", "0");
            // Convert radix 4 string to integer array
            int[] numArray = new int[numStr.length()];
            for(int j = 0; j < numArray.length; j++) {
                numArray[j] = Character.getNumericValue(numStr.charAt(j));
            }
            // Calculate heuristic score for player 1 and 2
            int[] score = new int[] {
                scoreDirection(numArray, 1), 
                scoreDirection(numArray, 2)
            };
            
            // Place scores in the lookup array
            if(score[0] > 0) {
                SCORES[0][numArray[0]][numArray[1]][numArray[2]][numArray[3]]
                        [numArray[4]][numArray[5]][numArray[6]][numArray[7]]
                        [numArray[8]] = score[0];
            }
            if(score[1] > 0) {
                SCORES[1][numArray[0]][numArray[1]][numArray[2]][numArray[3]]
                        [numArray[4]][numArray[5]][numArray[6]][numArray[7]]
                        [numArray[8]] = score[1];
            }
        }
    }
    
}
