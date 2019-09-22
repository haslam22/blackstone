package haslam.blackstone.players.negamax.faststate;

/**
 * Class representing a field on the Gomoku board for a {@link FastState}.
 * Stores a unique lookup value for neighbouring fields to perform quick
 * lookups (e.g. to determine threats, or a score for the pattern of stones
 * around the field).
 *
 * Everything in this class is mutable for performance.
 */
public class Field {

    enum Player {
        PLAYER_ONE(0),
        PLAYER_TWO(1);

        public final int index;
        Player(int index) {
            this.index = index;
        }
    }

    enum Direction {
        HORIZONTAL(0),
        VERTICAL(1),
        DIAGONAL(2),
        REVERSE_DIAGONAL(3);

        public final int index;
        Direction(int index) {
            this.index = index;
        }
    }

    /**
     * 0 -> Empty
     * 1 -> Player 1
     * 2 -> Player 2
     * 3 -> Out of bounds
     */
    int fieldState;
    final int x;
    final int y;

    /**
     * Pattern lookup array for the 4 neighbouring fields around this field,
     * forming a star pattern:
     *
     * *       *       *
     *   *     *     *
     *     *   *   *
     *       * * *
     * * * * * X * * * *
     *       * * *
     *     *   *   *
     *   *     *     *
     * *       *       *
     *
     * The first index is the player number (0 or 1)
     * The second index is the direction:
     * 1: Horizontal (Vector 1, 0)
     * 2: Vertical (Vector 0, 1)
     * 3: Diagonal (Vector 1, 1)
     * 4: Reverse diagonal (Vector 1, -1)
     *
     * Each byte value in the array represents the state of a direction for a
     * particular player relative to this fields position on the board, for
     * example:
     *
     * 1 2 4 8                                      16 32 64 128
     * 0 0 1 1 <- Left side | Field | Right side -> 0  1  1  1
     * Byte value = 4 + 8 + 32 + 64 + 128 = 236
     */
    int[][] patternLookupValues;

    Field(int x, int y, int fieldState) {
        this.fieldState = fieldState;
        this.patternLookupValues = new int[2][4];
        this.x = x;
        this.y = y;
    }

}
