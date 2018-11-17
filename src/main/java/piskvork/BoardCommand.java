package piskvork;

import core.Move;

import java.util.List;

/**
 * Represents a board command in the Piskvork protocol. Sent to allow the AI
 * to start from a non-empty position. Requires a move in response.
 */
public class BoardCommand implements PiskvorkMoveCommand {

    private final List<Move> moves;
    private final int index;

    public BoardCommand(List<Move> moves, int index) {
        this.moves = moves;
        this.index = index;
    }

    @Override
    public String getCommandString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BOARD \r\n");

        int currentIndex = 1;
        for(Move move : moves) {
            int fieldValue = currentIndex == index ? 1 : 2;
            builder.append(String.format("%s,%s,%s\r\n", move.col, move.row,
                    fieldValue));
            currentIndex = currentIndex == 1 ? 2 : 1;
        }
        builder.append("DONE");
        return builder.toString();
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
