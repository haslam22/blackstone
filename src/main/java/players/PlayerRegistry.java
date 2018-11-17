package players;

import players.human.HumanPlayer;
import players.negamax.NegamaxPlayer;
import players.random.RandomPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * A class holding available players. This is used by the GUI to determine which
 * players are available, and for the game to instantiate player objects.
 *
 * To add a new player, simply create a class which extends Player, and place it
 * in the "players" package. Then add an entry to getAvailablePlayers() and
 * add an entry to the switch statement in getPlayer(), mapping the player name
 * to a Player object.
 */
public class PlayerRegistry {

    /**
     * @return List of available player names
     */
    public static List<String> getAvailablePlayers() {
        return Arrays.asList(
                "Negamax",
                "Human",
                "Random"
        );
    }

    /**
     * @param playerName Player class to get
     * @return Player instance corresponding to the given player name
     */
    public static Player getPlayer(String playerName) {
        switch(playerName) {
            case "Negamax":
                return new NegamaxPlayer();
            case "Human":
                return new HumanPlayer();
            case "Random":
                return new RandomPlayer();
            default:
                throw new RuntimeException("Could not find player: " +
                        playerName);
        }
    }

}
