package core;

/**
 * Event listener adapter allowing components to register a listener and respond
 * to any events they are interested in.
 */
public class GameEventAdapter implements GameEventListener {

    @Override
    public void gameStarted() {
    }

    @Override
    public void playersChanged() {
    }

    @Override
    public void gameOver() {
    }

    @Override
    public void undo() {
    }

    @Override
    public void turn(int player) {
    }

    @Override
    public void gameTimeChanged(int player, long time) {
    }

    @Override
    public void moveTimeChanged(int player, long time) {
    }

    @Override
    public void moveTimingEnabled(boolean enabled) {
    }

    @Override
    public void gameTimingEnabled(boolean enabled) {
    }
}

interface GameEventListener {
    void gameStarted();
    void gameOver();
    void playersChanged();
    void undo();
    void turn(int player);
    void gameTimeChanged(int player, long time);
    void moveTimeChanged(int player, long time);
    void moveTimingEnabled(boolean enabled);
    void gameTimingEnabled(boolean enabled);
}