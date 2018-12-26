package haslam.blackstone.piskvork;

/**
 * Represents the START command in the Piskvork protocol. Sent to an AI to
 * perform any initial setup it needs to do.
 */
public class StartCommand implements PiskvorkCommand {

    private final int boardSize;

    /**
     * Create a new start command.
     * @param boardSize Board size of the game - n*n intersections, e.g. 19
     *                  or 15.
     */
    public StartCommand(int boardSize) {
        // Increment board size by 1 because Piskvork uses size 20 for a
        // 19x19 board, 16 for a 15x15 board, etc.
        this.boardSize = boardSize + 1;
    }

    @Override
    public String getCommandString() {
        return String.format("START %d", boardSize);
    }

    @Override
    public boolean validateResponse(String response) {
        return response.equals("OK");
    }
}
