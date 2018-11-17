package piskvork;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Simple class which listens for player input and passes it to a function to
 * deal with.
 */
public class PlayerInputThread implements Runnable {

    private final Scanner in;
    private final PlayerInputThreadProcessor processor;

    public PlayerInputThread(InputStream playerInputStream,
                             PlayerInputThreadProcessor processor) {
        this.in = new Scanner(playerInputStream);
        this.processor = processor;
    }

    @Override
    public void run() {
        // Will block until input is received.
        while (in.hasNextLine() && !Thread.interrupted()) {
            processor.processInput(in.nextLine());
        }
    }
}
