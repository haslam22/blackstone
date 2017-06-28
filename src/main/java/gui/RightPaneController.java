package gui;

import core.GameEventAdapter;
import core.GameManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.logging.*;

public class RightPaneController extends Controller {

    private Label[] gameTimeLabels;
    private Label[] moveTimeLabels;
    private GridPane[] playerBoxes;
    public ComboBox<String> player2ComboBox;
    public ComboBox<String> player1ComboBox;
    public TextArea textbox;
    public GridPane player1Box;
    public GridPane player2Box;
    public Label player2MoveTimeLabel;
    public Label player2GameTimeLabel;
    public Label player1GameTimeLabel;
    public Label player1MoveTimeLabel;
    private GameManager manager;

    public RightPaneController() {
        setupLog();
    }

    /**
     * Setup the game log, which simply connects to the global logger and
     * logs any INFO level messages sent from any class.
     */
    private void setupLog() {
        Handler TextBoxHandler = new StreamHandler () {
            @Override
            public void publish(LogRecord record) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String message = getFormatter().formatMessage(record);
                    textbox.appendText(message + "\n");
                }
            });
            }
        };
        TextBoxHandler.setFormatter(new SimpleFormatter());
        Logger.getGlobal().addHandler(TextBoxHandler);
        Logger.getGlobal().setLevel(Level.INFO);
    }

    /**
     * Format a milliseconds value as minutes:seconds
     * @param millis Input nanoseconds
     * @return Time string in minutes/seconds separated by a colon
     */
    private String getTimeString(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void initialise(GameManager manager) {
        this.manager = manager;
        // Put these components in an array so we can index them using a player
        // number, avoiding switch statements
        this.gameTimeLabels = new Label[] {
                player1GameTimeLabel,
                player2GameTimeLabel
        };
        this.moveTimeLabels = new Label[] {
                player1MoveTimeLabel,
                player2MoveTimeLabel
        };
        this.playerBoxes = new GridPane[] {
                player1Box,
                player2Box
        };

        // Events we need to respond to
        manager.addListener(new GameEventAdapter() {
            @Override
            public void gameStarted() {
                retrieveSettings();
                player1ComboBox.setDisable(true);
                player2ComboBox.setDisable(true);
            }
            @Override
            public void moveTimingEnabled(boolean enabled) {
                handleMoveTimingEnabled(enabled);
            }
            @Override
            public void gameTimingEnabled(boolean enabled) {
                handleGameTimingEnabled(enabled);
            }
            @Override
            public void gameTimeChanged(int player, long time) {
                handleGameTimeChanged(player, time);
            }
            @Override
            public void moveTimeChanged(int player, long time) {
                handleMoveTimeChanged(player, time);
            }
            @Override
            public void turn(int player) {
                handleTurn(player);
            }
            @Override
            public void gameOver() {
                handleGameOver();
            }
        });

        // Retrieve the current settings and update the GUI accordingly
        retrieveSettings();
    }

    /**
     * Retrieve the current settings from the manager, to modify the player
     * boxes to show the correct time/move restrictions
     */
    private void retrieveSettings() {
        if(manager.gameTimingEnabled()) {
            this.handleGameTimeChanged(1, manager.getGameTime());
            this.handleGameTimeChanged(2, manager.getGameTime());
        } else {
            this.handleGameTimingEnabled(false);
        }
        if(manager.moveTimingEnabled()) {
            this.handleMoveTimeChanged(1, manager.getMoveTime());
            this.handleMoveTimeChanged(2, manager.getMoveTime());
        } else {
            this.handleMoveTimingEnabled(false);
        }
    }

    /**
     * Handle move timing being enabled/disabled by the user
     * @param enabled
     */
    private void handleMoveTimingEnabled(boolean enabled) {
        if(enabled) {
            handleMoveTimeChanged(1, manager.getMoveTime());
            handleMoveTimeChanged(2, manager.getMoveTime());
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    moveTimeLabels[0].setText("No limit");
                    moveTimeLabels[1].setText("No limit");
                }
            });
        }
    }

    /**
     * Handle game timing being enabled/disabled by the user
     * @param enabled
     */
    private void handleGameTimingEnabled(boolean enabled) {
        if(enabled) {
            handleGameTimeChanged(1, manager.getGameTime());
            handleGameTimeChanged(2, manager.getGameTime());
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameTimeLabels[0].setText("No limit");
                    gameTimeLabels[1].setText("No limit");
                }
            });
        }
    }

    /**
     * Handle the game time being changed to a new value
     * @param player Player with a changed game time
     * @param time New time in milliseconds
     */
    private void handleGameTimeChanged(int player, long time) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gameTimeLabels[player - 1].setText(getTimeString(time));
            }
        });
    }

    /**
     * Handle the move time being changed to a new value
     * @param player Player with a changed move time
     * @param time New time in milliseconds
     */
    private void handleMoveTimeChanged(int player, long time) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                moveTimeLabels[player - 1].setText(getTimeString(time));
            }
        });
    }

    /**
     * Handle a turn changed event
     * @param player Player who is currently moving
     */
    private void handleTurn(int player) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                playerBoxes[0].setStyle("-fx-background-color:#eee;");
                playerBoxes[1].setStyle("-fx-background-color:#eee;");
                playerBoxes[player - 1].setStyle
                        ("-fx-background-color:#e1e1e1;");
            }
        });
    }

    /**
     * Handle a game over event
     */
    private void handleGameOver() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                player1Box.setStyle("-fx-background-color:#eee;");
                player2Box.setStyle("-fx-background-color:#eee;");
                player1ComboBox.setDisable(false);
                player2ComboBox.setDisable(false);
            }
        });
    }

    /**
     * Handle a player 1 changed event from the GUI
     * @param actionEvent
     */
    public void player1Changed(ActionEvent actionEvent) {
        manager.updatePlayer1(player1ComboBox.getValue());
    }

    /**
     * Handle a player 2 changed event from the GUI
     * @param actionEvent
     */
    public void player2Changed(ActionEvent actionEvent) {
        manager.updatePlayer2(player2ComboBox.getValue());
    }
}
