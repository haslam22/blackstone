package players.minimax.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class responsible for generating the sores text file containing scores for 
 * every possible 9-length pattern. See MinimaxEvaluator for more details.
 * @author Hassan
 */
public class MinimaxScoreGenerator {
    
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
    
    /**
     * Generate all possible 9-length patterns and assign them a score by 
     * evaluating how many fives can be formed and in how many moves, saving
     * all the data to a file (scores.txt).
     */
    public static void generateScoreFile() {
        try {
            BufferedWriter scoresWriter = new BufferedWriter(
                    new FileWriter("Scores.txt"));
            // Generate all possible numbers of length 9 in radix 4 (0,1,2,3)
            for(int i = 0; i < 262144; i++) {
                String numStr = String.format("%9s", Integer.toString(i, 4))
                        .replace(" ", "0");
                
                // A direction cannot contain an out of bounds value (3) 
                // followed by a player stone (1/2), so anything like 203310002 
                // is invalid
                String left = numStr.substring(0, 4);
                String right = numStr.substring(5, 9);
                
                if(left.contains("03") || left.contains("13") || 
                        left.contains("23")) {
                    continue;
                }
                if(right.contains("30") || right.contains("31") || 
                        right.contains("32")) {
                    continue;
                }
                
                // Convert radix 4 string to integer array
                int[] numArray = new int[numStr.length()];
                for(int j = 0; j < numArray.length; j++) {
                    numArray[j] = Character.getNumericValue(
                            numStr.charAt(j));
                }
                // Calculate heuristic score for player 1 and 2
                int[] score = new int[] {
                    scoreDirection(numArray, 1),
                    scoreDirection(numArray, 2)
                };
                
                // Place scores in the lookup array
                if(score[0] > 0) {
                    scoresWriter.write(numStr + "," + 1 + "," + score[0]);
                    scoresWriter.newLine();
                }
                if(score[1] > 0) {
                    scoresWriter.write(numStr + "," + 2 + "," + score[1]);
                    scoresWriter.newLine();
                }
            }
            scoresWriter.close();
        } catch (IOException ex) {
            System.out.println("Failed to generate scores file");
            System.out.println("Error: " + ex);
        } 
    }
}
