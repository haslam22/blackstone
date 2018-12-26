package haslam.blackstone.gui.minimal.controllers;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameSettings;
import haslam.blackstone.events.GameEventAdapter;
import haslam.blackstone.events.SettingsListener;
import haslam.blackstone.gui.minimal.Controller;
import haslam.blackstone.gui.minimal.TextAreaAppender;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import haslam.blackstone.players.PlayerRegistry;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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

    private GameController game;
    private Timer timer;
    private TimerTask playerTimeUpdater;

    @Override
    public void initialise(GameController game) {
        this.game = game;
        this.timer = new Timer();
        this.playerTimeUpdater = null;
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
            public void gameFinished() {
                handleGameFinished();
            }
            @Override
            public void turnStarted(int playerIndex) {
                handleTurnStarted(playerIndex);
            }
            @Override
            public void playerAdded() {
                loadPlayers();
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
        loadPlayers();
    }

    private void loadPlayers() {
        // Get currently selected haslam.blackstone.players (may be null if first load)
        String player1Val = player1Selector.getValue();
        String player2Val = player2Selector.getValue();

        player1Selector.getItems().clear();
        player2Selector.getItems().clear();
        // Load in available haslam.blackstone.players
        for(String player : PlayerRegistry.getAvailablePlayers()) {
            player1Selector.getItems().add(player);
            player2Selector.getItems().add(player);
        }
        if(player1Val != null) {
            player1Selector.setValue(player1Val);
            updatePlayer1();
        }
        if(player2Val != null) {
            player2Selector.setValue(player2Val);
            updatePlayer2();
        }
    }

    private void handleTurnStarted(int playerIndex) {
        if(playerTimeUpdater != null) {
            this.playerTimeUpdater.cancel();
        }
        this.playerTimeUpdater = new PlayerTimeUpdater(playerIndex,
                game);
        this.timer.scheduleAtFixedRate(playerTimeUpdater, 0, 100);
    }

    /**
     * Setup the game log, which simply connects to the global logger and
     * logs any INFO level messages sent from any class.
     */
    private void setupLog() {
        TextAreaAppender.addTextArea(textBox);
    }

    /**
     * Handle a gameFinished() event from the game.
     */
    private void handleGameFinished() {
        if(playerTimeUpdater != null) {
            this.playerTimeUpdater.cancel();
        }
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
        long moveTime = game.getSettings().getMoveTimeMillis() - timeMillis;
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

    private class PlayerTimeUpdater extends TimerTask {
        private final GameSettings settings;
        private final int playerIndex;
        private final GameController controller;
        private long gameTime;
        private long moveTime;
        private long startTime;
        private long elapsedTime;

        public PlayerTimeUpdater(int playerIndex, GameController controller) {
            this.startTime = System.currentTimeMillis();
            this.settings = game.getSettings();
            this.playerIndex = playerIndex;
            this.controller = controller;
            this.gameTime = controller.getGameTime(playerIndex);
            this.moveTime = controller.getSettings().getMoveTimeMillis();
        }

        @Override
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime;
            gameTime -= elapsedTime;
            moveTime -= elapsedTime;
            if(settings.moveTimingEnabled()) {
                handleMoveTimeChanged(playerIndex, moveTime);
            }
            if(settings.gameTimingEnabled()) {
                handleGameTimeChanged(playerIndex, gameTime);
            }
            startTime = System.currentTimeMillis();
        }
    }

}
