package piskvork;

import core.Move;
import players.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PiskvorkPlayer implements Player {

    private static final Logger LOGGER =
            Logger.getLogger(PiskvorkPlayer.class.getName());
    private static final List<String> MESSAGE_COMMANDS = Arrays.asList(
            "MESSAGE", "ERROR", "DEBUG", "UNKNOWN"
    );

    private final String executablePath;
    private Thread playerInputThread;
    private PrintWriter playerOutputWriter;
    private PiskvorkCommand lastCommand;
    private Move lastReceivedMove;

    public PiskvorkPlayer(String executablePath) {
        this.executablePath = executablePath;
    }

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        Process playerProcess;
        try {
            playerProcess = processBuilder.start();
        } catch (IOException ex) {
            throw new RuntimeException("Could not initialize Piskvork player", ex);
        }
        this.playerInputThread = new Thread(new PlayerInputThread(
                playerProcess.getInputStream(),
                input -> processPiskvorkInput(input)));
        this.playerOutputWriter = new PrintWriter(playerProcess.getOutputStream());
        playerInputThread.start();
        writePiskvorkCommand(new StartCommand(boardSize));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_TURN,
                String.valueOf(moveTimeMillis)));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_MATCH,
                String.valueOf(gameTimeMillis)));
    }

    @Override
    public Move loadBoard(List<Move> orderedMoves) {
        return null;
    }

    @Override
    public Move getMove(Move opponentsMove) {
        writePiskvorkCommand(new TurnCommand(opponentsMove));
        return this.lastReceivedMove;
    }

    @Override
    public Move beginGame() {
        writePiskvorkCommand(new BeginCommand());
        return this.lastReceivedMove;
    }

    @Override
    public void cleanup() {
        try {
            playerInputThread.interrupt();
            playerInputThread.join();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Processes line input from the AI.
     * @param input AI's input stream content
     */
    private void processPiskvorkInput(String input) {
        // Checks if the input is a command from the AI to us.
        if(MESSAGE_COMMANDS.contains(input.split(" ", 2)[0])) {
            LOGGER.log(Level.INFO, input.split(" ", 2)[1]);
            return;
        }
        // If not, we assume this is a response to the last command we sent.
        if(!lastCommand.validateResponse(input)) {
            throw new RuntimeException(
                    String.format(
                            "Invalid response for command: %s, response: %s",
                            lastCommand.getCommandString(), input));
        } else {
            if(lastCommand instanceof PiskvorkMoveCommand) {
                lastReceivedMove = ((PiskvorkMoveCommand) lastCommand).getMove(input);
            }
            synchronized (this) {
                this.notify();
            }
        }
    }

    /**
     * Writes a Piskvork command to the players output stream.
     * @param command PiskvorkCommand to write
     */
    private void writePiskvorkCommand(PiskvorkCommand command) {
        this.lastCommand = command;
        playerOutputWriter.println(command.getCommandString());
        playerOutputWriter.flush();
        if(command.requiresResponse()) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
