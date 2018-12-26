package gui.minimal.controllers;

import core.GameController;
import core.GameSettings;
import gui.minimal.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

/**
 * Controller for the settings dialog.
 */
public class SettingsPaneController implements Controller {

    @FXML
    public ComboBox<Integer> sizeComboBox;
    @FXML
    public ComboBox<Integer> moveTimeComboBox;
    @FXML
    public ComboBox<Integer> gameTimeComboBox;
    @FXML
    public CheckBox moveTimingCheckBox;
    @FXML
    public CheckBox gameTimingCheckBox;

    private GameController game;

    @Override
    public void initialise(GameController game) {
        this.game = game;
        this.loadSettings();
    }

    /**
     * Load the current settings and populate the GUI pane with the correct
     * values.
     */
    public void loadSettings() {
        GameSettings settings = game.getSettings();
        this.sizeComboBox.setValue(settings.getSize());
        // Avoid showing millisecond values to the user
        this.gameTimeComboBox.setValue((int) TimeUnit.MINUTES.convert
                (settings.getGameTimeMillis(), TimeUnit.MILLISECONDS));
        this.moveTimeComboBox.setValue((int) TimeUnit.SECONDS.convert
                (settings.getMoveTimeMillis(), TimeUnit.MILLISECONDS));
        if(settings.gameTimingEnabled()) {
            gameTimingCheckBox.setSelected(true);
            gameTimeComboBox.setDisable(false);
        }
        if(settings.moveTimingEnabled()) {
            moveTimingCheckBox.setSelected(true);
            moveTimeComboBox.setDisable(false);
        }
    }

    /**
     * Handle the game timing checkbox being selected/deselected.
     */
    public void gameTimingEnabled() {
        if(gameTimingCheckBox.isSelected()) {
            gameTimeComboBox.setDisable(false);
        } else {
            gameTimeComboBox.setDisable(true);
        }
    }

    /**
     * Handle the move timing checkbox being selected/deselected.
     */
    public void moveTimingEnabled() {
        if(moveTimingCheckBox.isSelected()) {
            moveTimeComboBox.setDisable(false);
        } else {
            moveTimeComboBox.setDisable(true);
        }
    }

    /**
     * Grab the settings from the GUI pane and update the settings accordingly.
     */
    public void updateSettings() {
        game.getSettings().setMoveTimeMillis(TimeUnit.MILLISECONDS
                .convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS));
        game.getSettings().setMoveTimingEnabled(moveTimingCheckBox.isSelected());
        game.getSettings().setGameTimeMillis(TimeUnit.MILLISECONDS.convert
                (gameTimeComboBox.getValue(), TimeUnit.MINUTES));
        game.getSettings().setGameTimingEnabled(gameTimingCheckBox.isSelected());
        game.getSettings().setSize(sizeComboBox.getValue());

        Stage stage = (Stage) sizeComboBox.getScene().getWindow();
        stage.close();
    }

    /**
     * Close the settings without saving changes.
     */
    public void closeSettings() {
        Stage stage = (Stage) sizeComboBox.getScene().getWindow();
        stage.close();
    }
}
