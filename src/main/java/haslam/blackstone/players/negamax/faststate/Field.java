package haslam.blackstone.players.negamax.faststate;

public class Field {

    // 0 -> Empty
    // 1 -> Player 1 occupied
    // 2 -> Player 2 occupied
    // 3 -> Out of bounds
    protected int fieldState;

    // Pattern lookup (for the 4 neighbouring fields forming a star pattern
    // around this one)
    // Each byte represents the state of a direction for a particular player
    // 0 0 1 1 <- Left side | Field | Right side -> 0 1 1 1
    // Indexed first by player (1/2), then by a direction
    // (diagonal/horizontal/etc)
    protected int[][] patternLookupValues;

    public Field(int fieldState) {
        this.fieldState = fieldState;
        this.patternLookupValues = new int[2][4];
    }

    public int[][] getPatternLookupValues() {
        return patternLookupValues;
    }

    public int getFieldState() {
        return fieldState;
    }

}
