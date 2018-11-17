package players;

import piskvork.PiskvorkPlayer;
import players.human.HumanPlayer;
import players.negamax.NegamaxPlayer;
import players.random.RandomPlayer;

import java.util.*;

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

    private static final List<String> availablePlayers = Arrays.asList(
            "Negamax",
            "Human",
            "Random"
    );

    private static Map<String, String> piskvorkPlayers = new HashMap<>();

    /**
     * @return List of available player names
     */
    public static List<String> getAvailablePlayers() {
        List<String> players = new ArrayList<>(availablePlayers);
        for(Map.Entry<String, String> entry : piskvorkPlayers.entrySet()) {
            players.add(entry.getKey());
        }
        return players;
    }

    public static void addPiskvorkPlayer(String name, String executablePath) {
        piskvorkPlayers.put(name, executablePath);
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
                if(piskvorkPlayers.containsKey(playerName)) {
                    return new PiskvorkPlayer(piskvorkPlayers.get(playerName));
                }
                throw new RuntimeException("Could not find player: " +
                        playerName);
        }
    }

}
