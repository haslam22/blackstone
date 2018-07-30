package core;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class used to provide partial state loading/saving capability.
 * States are saved in a simple human-readable text format containing the
 * board size on the first line, then on the second line a list of moves made
 * (in order) in algebraic notation.
 * e.g.
 * 15
 * H8, H9, I7, ...
 *
 * TODO: Save full state - times, players, all settings
 */
public class GameStateSerializer {

    public static void serializeState(GameState state, Writer writer) throws
            IOException {
        writer.write(state.getSize() + "\n");

        StringBuilder allMovesString = new StringBuilder();
        List<Move> moves = state.getMovesMade();
        for (Move move : moves) {
            allMovesString.append(move.getAlgebraicString(state.getSize()));
            allMovesString.append(" ");
        }
        writer.write(allMovesString.toString());
        writer.flush();
    }

    public static GameState loadState(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        List<String> fileStrings = bufferedReader.lines().collect(Collectors.toList());
        int boardSize = Integer.parseInt(fileStrings.get(0));
        String[] moves = fileStrings.get(1).trim().split(" ");

        GameState state = new GameState(boardSize);
        for(String algebraicMove : moves) {
            state.makeMove(getMoveFromAlgebraicString(algebraicMove, boardSize));
        }
        return state;
    }

    public static Move getMoveFromAlgebraicString(String algebraicString,
                                                  int boardSize) {
        char[] chars = algebraicString.toCharArray();
        int col = chars[0] - 'A';
        int row;
        if(chars.length == 3) {
            row = boardSize - Integer.parseInt(new String((new char[] {chars[1],
                    chars[2]})));
        } else {
            row = boardSize - Integer.parseInt(Character.toString(chars[1]));
        }

        System.out.println("Translated move: " + algebraicString + " to: " +
                new Move(row, col));
        return new Move(row, col);
    }

}
