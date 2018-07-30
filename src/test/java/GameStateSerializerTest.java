import core.GameState;
import core.GameStateSerializer;
import core.Move;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameStateSerializerTest {

    @Test
    public void testMoveTranslation() {
        assertEquals("H5 translates to 10, 7 on a 15x15 board", new Move(10, 7),
                GameStateSerializer.getMoveFromAlgebraicString("H5", 15));
        assertEquals("A10 translates to 5, 0 on a 15x15 board", new Move(5, 0),
                GameStateSerializer.getMoveFromAlgebraicString("A10", 15));
    }

    @Test
    public void testStateSerialization() throws IOException {
        // Setup a random state
        GameState state = new GameState(15);
        state.makeMove(new Move(2, 5));
        state.makeMove(new Move(3, 5));
        state.makeMove(new Move(4, 5));
        state.makeMove(new Move(0, 14));
        state.makeMove(new Move(0, 13));

        // Serialize and check
        try(StringWriter writer = new StringWriter()) {
            GameStateSerializer.serializeState(state, writer);
            assertEquals("Serialization should print the board size on one " +
                            "line, followed by the correct moves in algebraic" +
                            " form",
                    "15\nF13 F12 F11 O15 N15 ",
                    writer.toString());

            // Deserialize and check
            GameState loadedState = GameStateSerializer.loadState(
                    new StringReader(writer.toString()));
            assertTrue("Moves must be the same after serialization and " +
                    "deserialization",
                    state.getMovesMade().equals(loadedState.getMovesMade()));
        }

    }
}
