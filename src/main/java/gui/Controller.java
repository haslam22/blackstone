package gui;

import core.Game;
import core.GameSettings;

/**
 * Interface for a controller. Provides the controller with access to the game.
 */
public interface Controller {

    /**
     * Initialise the controller with a game instance.
     */
    void initialise(Game game);
}
