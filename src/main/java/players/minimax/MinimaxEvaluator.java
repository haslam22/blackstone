package players.minimax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

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
