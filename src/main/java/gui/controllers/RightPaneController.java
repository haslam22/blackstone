package gui.controllers;

import core.Game;
import core.GameSettings;
import events.GameEventAdapter;
import events.SettingsListener;
import gui.Controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import players.PlayerRegistry;

import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Controller for the right pane of the GUI.
 */
public class RightPaneController implements Controller {

    @FXML
    public ComboBox<String> player1Selector;
    @FXML
    public ComboBox<String> player2Selector;
    @FXML
    public TextArea textBox;
    @FXML
    public Label player1GameTimeLabel;
    @FXML
    public Label player1MoveTimeLabel;
    @FXML
    public Label player2GameTimeLabel;
    @FXML
    public Label player2MoveTimeLabel;

    private Game game;

    @Override
    public void initialise(Game game) {
        this.game = game;
        game.addListener(new GameEventAdapter() {
            @Override
            public void gameStarted() {
                handleGameStarted();
            }
            @Override
            public void gameResumed() {
                handleGameResumed();
            }
            @Override
            public void gameTimeChanged(int playerIndex, long timeMillis) {
                handleGameTimeChanged(playerIndex, timeMillis);
            }
            @Override
            public void moveTimeChanged(int playerIndex, long timeMillis) {
                handleMoveTimeChanged(playerIndex, timeMillis);
            }
            @Override
            public void gameFinished() {
                handleGameFinished();
            }
        });
        game.getSettings().addListener(new SettingsListener() {
            @Override
            public void settingsChanged() {
                loadSettings();
            }
        });
        loadSettings();
        setupLog();

        // Load in available players
        for(String player : PlayerRegistry.getAvailablePlayers()) {
            player1Selector.getItems().add(player);
            player2Selector.getItems().add(player);
        }
        player1Selector.setValue(game.getSettings().getPlayer1Name());
        player2Selector.setValue(game.getSettings().getPlayer2Name());
    }

    /**
     * Setup the game log, which simply connects to the global logger and
     * logs any INFO level messages sent from any class.
     */
    private void setupLog() {
        Handler TextBoxHandler = new StreamHandler() {
            @Override
            public void publish(LogRecord record) {
                Platform.runLater(() -> {
                    String message = getFormatter().formatMessage(record);
                    textBox.appendText(message + "\n");
                });
            }
        };
        TextBoxHandler.setFormatter(new SimpleFormatter());
        Logger.getGlobal().addHandler(TextBoxHandler);
        Logger.getGlobal().setLevel(Level.INFO);
    }

    /**
     * Handle a gameFinished() event from the game.
     */
    private void handleGameFinished() {
        Platform.runLater(() -> {
            player1Selector.setDisable(false);
            player2Selector.setDisable(false);
        });
    }

    /**
     * Handle a moveTimeChanged() event from the game.
     * @param playerIndex Player identifier
     * @param timeMillis New time
     */
    private void handleMoveTimeChanged(int playerIndex, long timeMillis) {
        Platform.runLater(() -> {
            switch(playerIndex) {
                case 1:
                    player1MoveTimeLabel.setText(getTimeString(timeMillis));
                    break;
                case 2:
                    player2MoveTimeLabel.setText(getTimeString(timeMillis));
                    break;
            }
        });
    }

    /**
     * Handle a gameTimeChanged() event for a player.
     * @param playerIndex Player identifier
     * @param timeMillis New time
     */
    private void handleGameTimeChanged(int playerIndex, long timeMillis) {
        Platform.runLater(() -> {
            switch(playerIndex) {
                case 1:
                    player1GameTimeLabel.setText(getTimeString(timeMillis));
                    break;
                case 2:
                    player2GameTimeLabel.setText(getTimeString(timeMillis));
                    break;
            }
        });
    }

    /**
     * Handle the gameStarted() event from the game.
     */
    private void handleGameStarted() {
        Platform.runLater(() -> {
            player1Selector.setDisable(true);
            player2Selector.setDisable(true);
        });
    }

    /**
     * Handle the gameResumed() event from the game.
     */
    private void handleGameResumed() {
        Platform.runLater(() -> {
            player1Selector.setDisable(true);
            player2Selector.setDisable(true);
        });
    }

    /**
     * Load the current game settings and update the panel accordingly.
     */
    private void loadSettings() {
        GameSettings settings = game.getSettings();
        if(settings.moveTimingEnabled()) {
            player1MoveTimeLabel.setText(getTimeString(settings
                    .getMoveTimeMillis()));
            player2MoveTimeLabel.setText(getTimeString(settings.
                    getMoveTimeMillis()));
        } else {
            player1MoveTimeLabel.setText("No limit");
            player2MoveTimeLabel.setText("No limit");
        }
        if(settings.gameTimingEnabled()) {
            player1GameTimeLabel.setText(getTimeString(settings
                    .getGameTimeMillis()));
            player2GameTimeLabel.setText(getTimeString(settings.
                    getGameTimeMillis()));
        } else {
            player1GameTimeLabel.setText("No limit");
            player2GameTimeLabel.setText("No limit");
        }
    }

    /**
     * Format a milliseconds value to a mm:ss time string.
     * @param millis Input milliseconds
     * @return Minutes/seconds string equivalent to input, formatted as mm:ss
     */
    private static String getTimeString(long millis) {
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(millis))
        );
    }

    /**
     * Update player 1 from the GUI
     */
    public void updatePlayer1() {
        String playerName = player1Selector.getValue();
        game.getSettings().setPlayer1(playerName);
    }

    /**
     * Update player 2 from the GUI
     */
    public void updatePlayer2() {
        String playerName = player2Selector.getValue();
        game.getSettings().setPlayer2(playerName);
    }
}
