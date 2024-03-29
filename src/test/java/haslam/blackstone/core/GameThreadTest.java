package haslam.blackstone.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import haslam.blackstone.players.Player;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        List<Move> player1Moves = new ArrayList<>();
        player1Moves.add(new Move('F', 7, 19));
        player1Moves.add(new Move('F', 8, 19));
        player1Moves.add(new Move('F', 9, 19));
        player1Moves.add(new Move('F', 10, 19));
        player1Moves.add(new Move('F', 11, 19));
        Player player1 = new TestPlayer(player1Moves, 230);

        // Player 2 makes a 5 in the G column.
        List<Move> player2Moves = new ArrayList<>();
        player2Moves.add(new Move('G', 7, 19));
        player2Moves.add(new Move('G', 8, 19));
        player2Moves.add(new Move('G', 9, 19));
        player2Moves.add(new Move('G', 10, 19));
        player2Moves.add(new Move('G', 11, 19));
        Player player2 = new TestPlayer(player2Moves, 120);

        // Run the game
        GameThread game = new GameThread(state, settings, player1, player2,
                new ArrayList<>());
        game.start();

        // Wait here until it finishes.
        game.join();
        assertEquals(1, state.terminal(), "Game should be won by player 1");
        assertFalse(game.isAlive(), "Game thread should die after game finishes");
    }

    @Test
    public void testGameWithTimeouts() throws InterruptedException {
        GameState state = new GameState(15);
        GameSettings settings = new GameSettings("", "", true, true, 120000, 1000,
                15);

        List<Move> player1Moves = new ArrayList<>();
        player1Moves.add(new Move('F', 11, 15));
        Player player1 = new TestPlayer(player1Moves, 230);

        // Player 2 has a large delay to simulate running out of time.
        List<Move> player2Moves = new ArrayList<>();
        player2Moves.add(new Move('G', 7, 15));
        Player player2 = new TestPlayer(player2Moves, 2000);


        // Run the game
        GameThread game = new GameThread(state, settings, player1, player2,
                new ArrayList<>());
        game.start();

        // Wait here until it finishes.
        game.join();
        assertEquals(0, state.terminal(), "Game should be won by neither player (timeout)");
        assertFalse(game.isAlive(), "Game thread should die after game finishes");
    }

}
