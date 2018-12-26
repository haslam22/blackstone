package haslam.blackstone.piskvork;

import haslam.blackstone.core.Move;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import haslam.blackstone.players.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class PiskvorkPlayer implements Player {

    private static final Logger LOGGER =
            LogManager.getLogger(PiskvorkPlayer.class.getName());
    private static final List<String> MESSAGE_COMMANDS = Arrays.asList(
            "MESSAGE", "ERROR", "DEBUG", "UNKNOWN"
    );

    private final String executablePath;
    private Thread playerInputThread;
    private PrintWriter playerOutputWriter;
    private PiskvorkCommand lastCommand;
    private Move lastReceivedMove;
    private Process playerProcess;
    private int index;

    public PiskvorkPlayer(String executablePath) {
        this.executablePath = executablePath;
    }

    @Override
    public void setupGame(int index, int boardSize, long moveTimeMillis, long gameTimeMillis) {
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        try {
            playerProcess = processBuilder.start();
        } catch (IOException ex) {
            throw new RuntimeException("Could not initialize Piskvork player", ex);
        }
        this.index = index;
        this.playerInputThread = new Thread(new PlayerInputThread(
                playerProcess.getInputStream(),
                input -> processPiskvorkInput(input)));
        this.playerOutputWriter = new PrintWriter(playerProcess.getOutputStream());
        playerInputThread.start();
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.MAX_MEMORY, "0"));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_TURN,
                String.valueOf(moveTimeMillis)));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_MATCH,
                String.valueOf(gameTimeMillis)));
        writePiskvorkCommand(new StartCommand(boardSize));
    }

    @Override
    public Move loadBoard(List<Move> orderedMoves, long gameTimeRemainingMillis) {
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIME_LEFT,
                String.valueOf(gameTimeRemainingMillis)));
        writePiskvorkCommand(new BoardCommand(orderedMoves, index));
        return this.lastReceivedMove;
    }

    @Override
    public Move getMove(Move opponentsMove, long gameTimeRemainingMillis) {
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIME_LEFT,
                String.valueOf(gameTimeRemainingMillis)));
        writePiskvorkCommand(new TurnCommand(opponentsMove));
        return this.lastReceivedMove;
    }

    @Override
    public Move beginGame(long gameTimeRemainingMillis) {
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIME_LEFT,
                String.valueOf(gameTimeRemainingMillis)));
        writePiskvorkCommand(new BeginCommand());
        return this.lastReceivedMove;
    }

    @Override
    public void cleanup() {
        writePiskvorkCommand(new EndCommand());
        playerProcess.destroy();
        try {
            playerProcess.waitFor();
            LOGGER.debug("Successfully closed Piskvork process.");
        } catch (InterruptedException ignored) {}
    }

    /**
     * Processes line input from the AI.
     * @param input AI's input stream content
     */
    private void processPiskvorkInput(String input) {
        LOGGER.debug("Received Piskvork input: {}", input);
        // Checks if the input is a command from the AI to us.
        String[] inputSplit = input.split(" ", 2);
        if(MESSAGE_COMMANDS.contains(inputSplit[0].toUpperCase())) {
            // Wake up thread in case AI errors and we're still waiting for a
            // move. Ensures we don't crash.
            if(inputSplit[0].toUpperCase().trim().equals("ERROR")) {
                synchronized (this) {
                    this.notify();
                }
            }
            LOGGER.info(inputSplit[1]);
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
                LOGGER.debug("Received Piskvork move: {}", lastReceivedMove);
            }
            synchronized (this) {
                this.notify();
            }
        }
    }

    /**
     * Writes a Piskvork command to the haslam.blackstone.players output stream.
     * @param command PiskvorkCommand to write
     */
    private void writePiskvorkCommand(PiskvorkCommand command) {
        LOGGER.debug("Wrote Piskvork command: {}", command.getCommandString());
        this.lastCommand = command;
        playerOutputWriter.println(command.getCommandString());
        playerOutputWriter.flush();
        if(command.requiresResponse()) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException ignored) {}
        }
    }
}
