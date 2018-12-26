package gui.minimal;

import core.GameController;

/**
 * Interface for a controller. Provides the controller with access to the game.
 */
public interface Controller {

    /**
     * Initialise the controller with a game instance.
     */
    void initialise(GameController game);
}
