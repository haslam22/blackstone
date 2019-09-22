package haslam.blackstone.gui.minimal;

import haslam.blackstone.core.GameController;

/**
 * Interface for a GUI controller. Provides the GUI controller with access to the game.
 */
public interface Controller {

    /**
     * Initialise the controller with a game instance.
     * @param game Main game controller
     */
    void initialise(GameController game);
}
