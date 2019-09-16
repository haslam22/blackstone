package haslam.blackstone.core;

import org.junit.Test;
import haslam.blackstone.players.Player;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GameThreadTest {

    /**
     * Run a basic test of the game thread. Creates two haslam.blackstone.players, both
     * creating a 5 in a straight line. Black should win (as it goes
     * first).
     */
    @Test
    public void testGame() throws InterruptedException {
        GameState state = new GameState(19);
        GameSettings settings = new GameSettings("", "", false, false, 0, 0,
                19);

        // Player 1 makes a 5 in the F column.
        ArrayList<Move> player1Moves = new ArrayList<>();
        player1Moves.add(new Move('F', 7, 19));
        player1Moves.add(new Move('F', 8, 19));
        player1Moves.add(new Move('F', 9, 19));
        player1Moves.add(new Move('F', 10, 19));
        player1Moves.add(new Move('F', 11, 19));
        Player player1 = new TestPlayer(player1Moves);

        // Player 2 makes a 5 in the G column.
        ArrayList<Move> player2Moves = new ArrayList<>();
        player2Moves.add(new Move('G', 7, 19));
        player2Moves.add(new Move('G', 8, 19));
        player2Moves.add(new Move('G', 9, 19));
        player2Moves.add(new Move('G', 10, 19));
        player2Moves.add(new Move('G', 11, 19));
        Player player2 = new TestPlayer(player2Moves);

        // Run the game
        GameThread game = new GameThread(state, settings, player1, player2,
                new ArrayList<>());
        game.start();

        // Wait here until it finishes.
        game.join();
        assertEquals("Game should be won by player 1", 1, state.terminal());
        assertEquals("Game thread should die after game finishes", false,
                game.isAlive());
    }

}
