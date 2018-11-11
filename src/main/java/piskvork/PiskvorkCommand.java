package piskvork;

/**
 * Root interface for a Piskvork command. A command must provide a string to
 * send to the AI, and a method to validate the response.
 */
public interface PiskvorkCommand {

    /**
     * Get the string for this command to send to the AI.
     * @return String identifier of the command, e.g. TURN or MESSAGE, plus
     * any other parameters to send to the AI.
     */
    String getCommandString();

    /**
     * Return whether or not this command requires a response.
     * @return True by default, overridden to false if required.
     */
    default boolean requiresResponse() {
        return true;
    }

    /**
     * Validate a response for this command.
     * @param response Response to validate
     * @return Boolean representing whether this response is valid
     */
    default boolean validateResponse(String response) {
        return true;
    }

}
