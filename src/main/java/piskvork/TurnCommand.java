package piskvork;

import core.Move;

/**
 * Represents a TURN command in the Piskvork protocol. This is sent whenever
 * the opponent makes a move, and the AI is expected to respond with a move
 * of their own.
 */
public class TurnCommand implements PiskvorkMoveCommand {

    private final Move move;

    /**
     * Create a new turn command for a move that was made by the opponent.
     * @param move Move that was made
     */
    public TurnCommand(Move move) {
        this.move = move;
    }

    @Override
    public String getCommandString() {
        System.out.println(move.row);
        return String.format("TURN %d,%d", move.col, move.row);
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
