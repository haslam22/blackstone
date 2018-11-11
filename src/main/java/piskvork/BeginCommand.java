package piskvork;

import core.Move;

/**
 * Represents a BEGIN command in the Piskvork protocol. Sent to an AI when it
 * is expected to return a move to open the game.
 */
public class BeginCommand implements PiskvorkMoveCommand {

    @Override
    public String getCommandString() {
        return "BEGIN";
    }

    @Override
    public boolean validateResponse(String response) {
        // Expected response is a comma-separated pair of coordinates (x,y)
        // on the board. 0-indexed.
        return response.matches("\\d{1,2},\\d{1,2}");
    }

    @Override
    public Move getMove(String response) {
        String[] moveCoordinates = response.split(",");
        return new Move(
                Integer.parseInt(moveCoordinates[1]),
                Integer.parseInt(moveCoordinates[0]));
    }
}
