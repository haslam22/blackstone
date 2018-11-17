package piskvork;

public class EndCommand implements PiskvorkCommand {
    @Override
    public String getCommandString() {
        return "END";
    }

    @Override
    public boolean requiresResponse() {
        return false;
    }
}
