package haslam.blackstone.players.negamax;

import haslam.blackstone.core.Move;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreatUtilsTest {

    @Test
    public void testStraightThreePatterns() {
        State state = new State(15);
        // Setup a straight three (OOXXXOO) pattern on this state
        state.makeMove(new Move(7, 7));
        state.makeMove(new Move(0, 0));
        state.makeMove(new Move(7, 8));
        state.makeMove(new Move(0, 1));
        state.makeMove(new Move(7, 9));

        List<Move> threeMoves = ThreatUtils.getThrees(state, state.getField(7, 7),
                1);

        // Expected moves - we can block it on the right/left
        assertTrue(threeMoves.contains(new Move(7, 5)));
        assertTrue(threeMoves.contains(new Move(7, 6)));
        assertTrue(threeMoves.contains(new Move(7, 10)));
        assertTrue(threeMoves.contains(new Move(7, 11)));
        // There should be no more possible moves, although duplicates may exist
        assertEquals(new HashSet<>(threeMoves).size(), 4);
    }

    @Test
    public void testBrokenThreePatterns() {
        State state = new State(15);
        // Setup a broken three (OXXOXO) pattern on this state
        state.makeMove(new Move(7, 7));
        state.makeMove(new Move(0, 0));
        state.makeMove(new Move(7, 8));
        state.makeMove(new Move(0, 1));
        state.makeMove(new Move(7, 10));

        List<Move> threeMoves = ThreatUtils.getThrees(state, state.getField(7, 7),
                1);

        // Expected moves - we can block it in the middle, and right/left
        assertTrue(threeMoves.contains(new Move(7, 9)));
        assertTrue(threeMoves.contains(new Move(7, 6)));
        assertTrue(threeMoves.contains(new Move(7, 11)));
        // There should be no more possible moves, although duplicates may exist
        assertEquals(new HashSet<>(threeMoves).size(), 3);
    }

}
