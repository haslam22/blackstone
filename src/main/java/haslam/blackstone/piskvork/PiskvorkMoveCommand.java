package haslam.blackstone.piskvork;

import haslam.blackstone.core.Move;

/**
 * Extension to the PiskvorkCommand interface when a command is expected to
 * receive a move as a response. Implementations should provide a method to
 * retrieve the move from the response.
 */
public interface PiskvorkMoveCommand extends PiskvorkCommand {
    Move getMove(String response);
}
