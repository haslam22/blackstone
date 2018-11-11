package piskvork;

import core.GameInfo;
import core.GameState;
import core.Move;
import players.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PiskvorkPlayer extends Player {

    private static final Logger LOGGER =
            Logger.getLogger(PiskvorkPlayer.class.getName());

    private final Thread playerInputThread;
    private final PrintWriter playerOutputWriter;
    private PiskvorkCommand lastCommand;
    private Move lastReceivedMove;
    private static final List<String> MESSAGE_COMMANDS = Arrays.asList(
            "MESSAGE", "ERROR", "DEBUG", "UNKNOWN"
    );

    /**
     * Create a new player based on an executable program which implements
     * the Piskvork protocol. This program must communicate via stdin/out.
     * @param executablePath Path to executable (e.g C:/Windows/gomoku.exe)
     */
    public PiskvorkPlayer(GameInfo info, String executablePath) throws IOException {
        super(info);
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        Process playerProcess = processBuilder.start();
        this.playerInputThread = new Thread(new PlayerInputThread(
                playerProcess.getInputStream(),
                input -> processPiskvorkInput(input)));
        this.playerOutputWriter = new PrintWriter(playerProcess.getOutputStream());
        playerInputThread.start();
        writePiskvorkCommand(new StartCommand(info.getSize()));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_TURN,
                String.valueOf(info.getMoveTimeMillis())));
        writePiskvorkCommand(new InfoCommand(InfoCommand.InfoCommandKey.TIMEOUT_MATCH,
                String.valueOf(info.getGameTimeMillis())));
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
     * @param command
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

    @Override
    public Move getMove(GameState state) {
        if(state.getMovesMade().isEmpty()) {
            writePiskvorkCommand(new BeginCommand());
        }
        writePiskvorkCommand(new TurnCommand(state.getLastMove()));
        return this.lastReceivedMove;
    }

}
