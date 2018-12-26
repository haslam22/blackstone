package gui;

import core.GameController;
import javafx.stage.Stage;

/**
 * Basic interface for a GUI.
 */
public interface GameGUI {

    /**
     * Launch the GUI implementation.
     * @param gameController Game controller - use this to perform actions on
     *                       the game (stop/start/etc) and add listeners for
     *                       game events.
     * @param primaryStage Primary stage - root JavaFX stage to add any
     *                     scenes/elements desired.
     */
    void launch(GameController gameController, Stage primaryStage);

}
