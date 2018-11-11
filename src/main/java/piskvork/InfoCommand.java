package piskvork;

/**
 * Key-value pair sent to the AI to determine settings for the game.
 */
public class InfoCommand implements PiskvorkCommand {

    public enum InfoCommandKey {
        TIMEOUT_TURN, // Timeout for a move in milliseconds
        TIMEOUT_MATCH, // Timeout for the game in milliseconds
        MAX_MEMORY, // Max memory limit in bytes
        TIME_LEFT, // Time remaining for the game in milliseconds
        GAME_TYPE, // 0 (Human opponent), 1 (AI opponent), 2 (Tournament), 3
        // (Network tournament)
        RULE, // Bitmask or sum of: 1 (Exactly five to win), 2 (Continuous
        // game), 4 (Renju)
        EVALUATE, // Coordinate pair of position to evaluate
        FOLDER // Path for persistent files to be stored
    }

    private final InfoCommandKey key;
    private final String value;

    public InfoCommand(InfoCommandKey key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getCommandString() {
        return String.format("INFO %s %s", key.name(), value);
    }

    @Override
    public boolean requiresResponse() {
        return false;
    }
}
