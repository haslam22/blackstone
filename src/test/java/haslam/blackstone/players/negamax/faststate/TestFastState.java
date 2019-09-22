package haslam.blackstone.players.negamax.faststate;

import org.junit.Test;
import haslam.blackstone.players.negamax.faststate.Field.Player;
import haslam.blackstone.players.negamax.faststate.Field.Direction;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests to ensure the pattern lookup works as expected in the state.
 */
public class TestFastState {

    @Test
    public void testCreateNewState() {
        FastState state = new FastState(15);
        // Upper left corner
        Field testField = state.getField(4, 4);
        assertPatternLookup(testField, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 15, Direction.VERTICAL, 15,
                Direction.DIAGONAL, 15, Direction.REVERSE_DIAGONAL, 255));

        // Bottom left
        Field testField2 = state.getField(8, 4);
        assertPatternLookup(testField2, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 0, Direction.VERTICAL, 15,
                Direction.DIAGONAL, 15, Direction.REVERSE_DIAGONAL, 240));

        // Upper right corner
        Field testField3 = state.getField(18, 4);
        assertPatternLookup(testField3, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 240, Direction.VERTICAL, 15,
                Direction.DIAGONAL, 255, Direction.REVERSE_DIAGONAL, 240));

        // Bottom right
        Field testField4 = state.getField(18, 18);
        assertPatternLookup(testField4, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 240, Direction.VERTICAL, 240,
                Direction.DIAGONAL, 240, Direction.REVERSE_DIAGONAL, 255));

        // Test a field near the middle of the board - pattern lookup should
        // be 0 for all directions
        Field testField5 = state.getField(12, 12);
        assertPatternLookup(testField5, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 0, Direction.VERTICAL, 0,
                Direction.DIAGONAL, 0, Direction.REVERSE_DIAGONAL, 0));
    }

    @Test
    public void testMoveMaking() {
        FastState state = new FastState(15);
        state.makeMove(10, 10);

        Field field1 = state.getField(9, 9);
        assertPatternLookup(field1, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 0, Direction.VERTICAL, 0,
                Direction.DIAGONAL, 16, Direction.REVERSE_DIAGONAL, 0));

        Field field2 = state.getField(8, 8);
        assertPatternLookup(field2, Player.PLAYER_ONE, Map.of(Direction.HORIZONTAL, 0, Direction.VERTICAL, 0,
                Direction.DIAGONAL, 32, Direction.REVERSE_DIAGONAL, 0));

        // Now undo and check the pattern lookup has been reversed.
        state.undoMove(10, 10);
        assertPatternLookup(field1, Player.PLAYER_ONE, Map.of(Direction.DIAGONAL, 0));
        assertPatternLookup(field2, Player.PLAYER_ONE, Map.of(Direction.DIAGONAL, 0));
    }

    @Test
    /**
     */
    public void testMockGame() {
        // Mock a game between two players.
        FastState state = new FastState(15);




    }

    private void assertPatternLookup(Field field, Player player,
                                     Map<Direction, Integer> expectedDirectionPatternValues) {
        for (Map.Entry<Direction, Integer> directionEntry : expectedDirectionPatternValues.entrySet()) {
            String assertionMsg = String.format("Pattern lookup for [%s, %s] should be {%s}, actual: {%s}", field.x,
                    field.y, directionEntry.getValue(), field.patternLookupValues[player.index][directionEntry.getKey().index]);
            assertEquals(assertionMsg, directionEntry.getValue().intValue(),
                    field.patternLookupValues[player.index][directionEntry.getKey().index]);
        }
    }
}
