package players.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Static evaluator, providing heuristic evaluations for positions in the game.
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
 * @author Hasan
 */
public class Evaluator {
    
    private static final int[][][][][][][][][][] SCORES;
    
    /**
     * Evaluate a state from the perspective of the current player
     * @param state State to evaluate
     * @return Score from the current players perspective
     */
    protected int evaluate(State state, int depth) {
        int playerIndex = state.currentIndex;
        int opponentIndex = playerIndex == 1 ? 2 : 1;
        int terminal = state.terminal();
        if(terminal == playerIndex) return 10000 + depth;
        if(terminal == opponentIndex) return -10000 - depth;

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
    protected int evaluateField(Field field, int index) {
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
    
    /*
     * Load all the scores for every possible 9-length direction from the
     * scores file. Save the scores in a lookup array, with separate entries
     * for player 1 and 2.
     */
    static {
        SCORES = new int[2][4][4][4][4][4][4][4][4][4];

        InputStream scoresFile = ClassLoader
                .getSystemResourceAsStream("Scores.txt");
        
        try(BufferedReader scoresReader = new BufferedReader(
                new InputStreamReader(scoresFile))) {
            
            String scoreLine;
            while ((scoreLine = scoresReader.readLine()) != null) {
                String[] scoreLineParts = scoreLine.split(",");
                
                // First part is the pattern (e.g. 012002333)
                String directionStr = scoreLineParts[0];
                // Second part identifies which player the score is for
                int playerIndex = Integer.parseInt(scoreLineParts[1]);
                // Third part is the score value
                int score = Integer.parseInt(scoreLineParts[2]);
                // Copy the direction string[] to int[]
                int[] direction = new int[directionStr.length()];
                for(int i = 0; i < direction.length; i++) {
                    direction[i] = Character.getNumericValue(
                            directionStr.charAt(i));
                }
                // Place the score in the lookup array
                SCORES[playerIndex - 1][direction[0]][direction[1]]
                        [direction[2]][direction[3]][direction[4]][direction[5]]
                        [direction[6]][direction[7]][direction[8]] = score;
            }

        } catch(IOException | NullPointerException ex) {
            System.out.println("Failed to load the scores file: " + ex);
            throw new RuntimeException(ex);
        }
    }
    
}
