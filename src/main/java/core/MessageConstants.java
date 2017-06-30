package core;

/**
 * Class containing static string constants, for quickly creating game messages
 */
class MessageConstants {

    private static String toColor(int index) {
        switch(index) {
            case 1:
                return "Black";
            case 2:
                return "White";
            default:
                return "";
        }
    }

    static String playerMoved(int index, Move move) {
        return String.format("Player %d (%s) move: %d, %d", index, toColor
                        (index), move.getRow(), move.getCol());
    }

    static String gameOver(int index) {
        return String.format("Game over, winner: Player %d (%s)", index,
                toColor(index));
    }

    static String timeout(int index) {
        return String.format("Player %d (%s) ran out of time", index,
                toColor(index));
    }

    static String gameInterrupted() {
        return "Game was interrupted";
    }

}
