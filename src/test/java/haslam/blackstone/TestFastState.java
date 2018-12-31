package haslam.blackstone;

import haslam.blackstone.players.negamax.faststate.FastState;
import haslam.blackstone.players.negamax.faststate.Field;
import org.junit.Test;

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

        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 4, 4 should be 15", 15,
                testField.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                "on field 4, 4 should be 15", 15,
                testField.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                "on field 4, 4 should be 15", 15,
                testField.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 4, 4 should be 255", 255,
                testField.getPatternLookupValues()[0][3]);

        Field testField2 = state.getField(8, 4);

        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 8, 4 should be 0", 0,
                testField2.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 8, 4 should be 15", 15,
                testField2.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 8, 4 should be 15", 15,
                testField2.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 8, 4 should be 240", 240,
                testField2.getPatternLookupValues()[0][3]);

        // Upper right corner
        Field testField3 = state.getField(18, 4);

        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 18, 4 should be 240", 240,
                testField3.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 18, 4 should be 15", 15,
                testField3.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 18, 4 should be 15", 255,
                testField3.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 18, 4 should be 240", 240,
                testField3.getPatternLookupValues()[0][3]);

        // Bottom right
        Field testField4 = state.getField(18, 18);
        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 18, 18 should be 240", 240,
                testField4.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 18, 18 should be 240", 240,
                testField4.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 18, 18 should be 240", 240,
                testField4.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 18, 18 should be 255", 255,
                testField4.getPatternLookupValues()[0][3]);

        // Test a field near the middle of the board - pattern lookup should
        // be 0 for all directions
        Field testField5 = state.getField(12, 12);
        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 12, 12 should be 0", 0,
                testField5.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 12, 12 should be 0", 0,
                testField5.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 12, 12 should be 0", 0,
                testField5.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 12, 12 should be 0", 0,
                testField5.getPatternLookupValues()[0][3]);
    }

    @Test
    public void testMoveMaking() {
        FastState state = new FastState(15);
        state.makeMove(10, 10);

        Field field1 = state.getField(9, 9);
        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 9, 9 should be 0", 0,
                field1.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 9, 9 should be 0", 0,
                field1.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 9, 9 should be 16", 16,
                field1.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 9, 9 should be 0", 0,
                field1.getPatternLookupValues()[0][3]);

        Field field2 = state.getField(8, 8);
        assertEquals("The pattern lookup value for the horizontal direction " +
                        "on field 8, 8 should be 0", 0,
                field2.getPatternLookupValues()[0][0]);
        assertEquals("The pattern lookup value for the vertical direction " +
                        "on field 8, 8 should be 0", 0,
                field2.getPatternLookupValues()[0][1]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 8, 8 should be 32", 32,
                field2.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the reverse diagonal " +
                        "direction on field 8, 8 should be 0", 0,
                field2.getPatternLookupValues()[0][3]);

        // Now undo and check the pattern lookup has been reversed.
        state.undoMove(10, 10);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 9, 9 should be 0", 0,
                field1.getPatternLookupValues()[0][2]);
        assertEquals("The pattern lookup value for the diagonal direction " +
                        "on field 8, 8 should be 0", 0,
                field2.getPatternLookupValues()[0][2]);
    }

}
