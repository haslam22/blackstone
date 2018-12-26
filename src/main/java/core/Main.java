package core;

import gui.minimal.MinimalGUI;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point of the application. Launches a single GUI implementation with
 * a game controller and the primary JavaFX stage.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameController gameController = new GameController(new GameSettings(
                Defaults.PLAYER_1,
                Defaults.PLAYER_2,
                Defaults.GAME_TIMING_ENABLED,
                Defaults.MOVE_TIMING_ENABLED,
                Defaults.GAME_TIMEOUT_MILLIS,
                Defaults.MOVE_TIMEOUT_MILLIS,
                Defaults.SIZE));

        new MinimalGUI().launch(gameController, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
