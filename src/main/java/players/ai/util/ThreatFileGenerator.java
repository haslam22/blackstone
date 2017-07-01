package players.ai.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for generating the text files containing threats and
 * refutations for every possible 9-length pattern.
 * @author Hasan
 */
public class ThreatFileGenerator {
    
    static class ThreatPattern {
        String threatString;
        int threatClass; 
        int[] threatSquares;
        int playerIndex;

        public ThreatPattern(String threatString, int threatClass, 
                int[] threatSquares, int playerIndex) {
            this.threatString = threatString;
            this.threatSquares = threatSquares;
            this.playerIndex = playerIndex;
            this.threatClass = threatClass;
        }
    }

    static class RefutationPattern {
        String threatString;
        int[] threatSquares;
        int playerIndex;

        public RefutationPattern(String threatString, int[] threatSquares,
                int playerIndex) {
            this.threatString = threatString;
            this.threatSquares = threatSquares;
            this.playerIndex = playerIndex;
        }
    }
    
    /**
     * Generate all possible patterns of length 9, containing values 0/1/2/3,
     * and store the threats and refutations found.
     */
    public static void generateThreatFiles() {
        List<RefutationPattern> refutations = new ArrayList<>();
        
        refutations.add(new RefutationPattern("10011", new int[] { 1, 2 }, 1));
        refutations.add(new RefutationPattern("11001", new int[] { 2, 3 }, 1));
        refutations.add(new RefutationPattern("11100", new int[] { 3, 4 }, 1));
        refutations.add(new RefutationPattern("00111", new int[] { 0, 1 }, 1));
        refutations.add(new RefutationPattern("01101", new int[] { 0, 3 }, 1));
        refutations.add(new RefutationPattern("01110", new int[] { 0, 4 }, 1));
        refutations.add(new RefutationPattern("11010", new int[] { 2, 4 }, 1));
        
        refutations.add(new RefutationPattern("20022", new int[] { 1, 2 }, 2));
        refutations.add(new RefutationPattern("22002", new int[] { 2, 3 }, 2));
        refutations.add(new RefutationPattern("22200", new int[] { 3, 4 }, 2));
        refutations.add(new RefutationPattern("00222", new int[] { 0, 1 }, 2));
        refutations.add(new RefutationPattern("02202", new int[] { 0, 3 }, 2));
        refutations.add(new RefutationPattern("02220", new int[] { 0, 4 }, 2));
        refutations.add(new RefutationPattern("22020", new int[] { 2, 4 }, 2));
        
        List<ThreatPattern> threats = new ArrayList<>();
        
        // Four
        threats.add(new ThreatPattern("01111", 1, new int[] { 0 }, 1));
        threats.add(new ThreatPattern("10111", 1, new int[] { 1 }, 1));
        threats.add(new ThreatPattern("11011", 1, new int[] { 2 }, 1));
        threats.add(new ThreatPattern("11101", 1, new int[] { 3 }, 1));
        threats.add(new ThreatPattern("11110", 1, new int[] { 4 }, 1));
        threats.add(new ThreatPattern("02222", 1, new int[] { 0 }, 2));
        threats.add(new ThreatPattern("20222", 1, new int[] { 1 }, 2));
        threats.add(new ThreatPattern("22022", 1, new int[] { 2 }, 2));
        threats.add(new ThreatPattern("22202", 1, new int[] { 3 }, 2));
        threats.add(new ThreatPattern("22220", 1, new int[] { 4 }, 2));
        
        // Three
        threats.add(new ThreatPattern("0111002", 2, new int[] { 0, 4, 5 }, 1));
        threats.add(new ThreatPattern("0011102", 2, new int[] { 0, 1, 5 }, 1));
        threats.add(new ThreatPattern("0111003", 2, new int[] { 0, 4, 5 }, 1));
        threats.add(new ThreatPattern("0011103", 2, new int[] { 0, 1, 5 }, 1));
        threats.add(new ThreatPattern("2001110", 2, new int[] { 1, 2, 6 }, 1));
        threats.add(new ThreatPattern("2011100", 2, new int[] { 1, 5, 6 }, 1));
        threats.add(new ThreatPattern("3001110", 2, new int[] { 1, 2, 6 }, 1));
        threats.add(new ThreatPattern("3011100", 2, new int[] { 1, 5, 6 }, 1));
        threats.add(new ThreatPattern("0222001", 2, new int[] { 0, 4, 5 }, 2));
        threats.add(new ThreatPattern("0022201", 2, new int[] { 0, 1, 5 }, 2));
        threats.add(new ThreatPattern("0222003", 2, new int[] { 0, 4, 5 }, 2));
        threats.add(new ThreatPattern("0022203", 2, new int[] { 0, 1, 5 }, 2));
        threats.add(new ThreatPattern("1002220", 2, new int[] { 1, 2, 6 }, 2));
        threats.add(new ThreatPattern("1022200", 2, new int[] { 1, 5, 6 }, 2));
        threats.add(new ThreatPattern("3002220", 2, new int[] { 1, 2, 6 }, 2));
        threats.add(new ThreatPattern("3022200", 2, new int[] { 1, 5, 6 }, 2));
        threats.add(new ThreatPattern("0011100", 2, new int[] { 1, 5 }, 1));
        threats.add(new ThreatPattern("0022200", 2, new int[] { 1, 5 }, 2));
        
        // Broken three
        threats.add(new ThreatPattern("011010", 2, new int[] { 0, 3, 5 }, 1));
        threats.add(new ThreatPattern("010110", 2, new int[] { 0, 2, 5 }, 1));
        threats.add(new ThreatPattern("022020", 2, new int[] { 0, 3, 5 }, 2));
        threats.add(new ThreatPattern("020220", 2, new int[] { 0, 2, 5 }, 2));
        
        try {
            BufferedWriter threatWriter = new BufferedWriter(
                    new FileWriter("Threats.txt"));
            BufferedWriter refutationWriter = new BufferedWriter(
                    new FileWriter("Refutations.txt"));

            // Generate all possible numbers of length 9 in radix 4 (0,1,2,3)
            // E.g. 1234 -> 000103102
            for(int i = 0; i < 262144; i++) {
                String directionStr = String.format("%9s", 
                        Integer.toString(i, 4)).replace(" ", "0");                

                // A direction cannot contain an out of bounds value (3) 
                // followed by a player stone (1/2), so anything like 203310002 
                // is invalid
                String left = directionStr.substring(0, 4);
                String right = directionStr.substring(5, 9);
                
                if(left.contains("03") || left.contains("13") || 
                        left.contains("23")) {
                    continue;
                }
                if(right.contains("30") || right.contains("31") || 
                        right.contains("32")) {
                    continue;
                }
                
                // Convert string to integer array
                int[] direction = new int[directionStr.length()];
                for(int j = 0; j < direction.length; j++) {
                    direction[j] = Character.getNumericValue(directionStr
                            .charAt(j));
                }

                int threatCount = 0;

                for(ThreatPattern pattern : threats) {
                    // Search for the threat in this direction
                    int patternIndex = directionStr
                            .indexOf(pattern.threatString);
                    if(patternIndex >= 0) {
                        StringBuilder threatString = new StringBuilder();
                        threatCount++;
                        threatString.append(directionStr);
                        threatString.append(",");
                        threatString.append(pattern.playerIndex);
                        threatString.append(",");
                        threatString.append(pattern.threatClass);
                        threatString.append(",");
                        for(int j = 0; j < pattern.threatSquares.length; j++) {
                            threatString.append(patternIndex + 
                                    pattern.threatSquares[j]);
                            if(j != pattern.threatSquares.length - 1) {
                                threatString.append(",");
                            }
                        }
                        threatWriter.write(threatString.toString());
                        threatWriter.newLine();
                    }

                }
                // If no threats exist, check for refutations (moves that may 
                // turn into threats)
                if(threatCount == 0) {
                    for(RefutationPattern pattern : refutations) {
                        int patternIndex = directionStr
                                .indexOf(pattern.threatString);
                        // Found a refutation pattern
                        if (patternIndex >= 0) {
                            StringBuilder refString = new StringBuilder();
                            refString.append(directionStr);
                            refString.append(",");
                            refString.append(pattern.playerIndex);
                            refString.append(",");
                            for(int j = 0; j < pattern.threatSquares.length; 
                                    j++) {
                                refString.append(patternIndex + 
                                        pattern.threatSquares[j]);
                                if(j != pattern.threatSquares.length - 1) {
                                    refString.append(",");
                                }
                            }
                            refutationWriter.write(refString.toString());
                            refutationWriter.newLine();
                        }
                    }
                }
            }
            
            threatWriter.close();
            refutationWriter.close();
            
        } catch(IOException ex) {
            System.out.println("Failed to generate threat/refutation files");
            System.out.println("Error:" + ex);
        }
    }
}
