package gui;

import core.GameManager;

/**
 * Abstract superclass for all GUI controllers.
 */
public abstract class Controller {

    /**
     * Initialise the controller with a game manager object, allowing it to
     * receive events from the current game and perform game related actions.
     * @param manager GameManager object
     */
    public abstract void initialise(GameManager manager);

}
