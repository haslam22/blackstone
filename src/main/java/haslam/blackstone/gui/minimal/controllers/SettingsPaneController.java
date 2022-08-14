package haslam.blackstone.gui.minimal.controllers;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameSettings;
import haslam.blackstone.gui.minimal.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * Controller for the settings dialog.
 */
public class SettingsPaneController implements Controller {

    @FXML
    public ComboBox<Integer> sizeComboBox;
    @FXML
    public Spinner<Integer> moveTimeSpinner;
    @FXML
    public Spinner<Integer> gameTimeSpinner;
    @FXML
    public CheckBox moveTimingCheckBox;
    @FXML
    public CheckBox gameTimingCheckBox;
    public Label validationErrorsLabel;

    private GameController game;

    @Override
    public void initialise(GameController game) {
        this.game = game;
        this.loadSettings();
        UnaryOperator<TextFormatter.Change> digitsOnlyFilter = change -> change.getText()
                .matches("\\d*") ? change : null;
        gameTimeSpinner.getEditor().setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
        moveTimeSpinner.getEditor().setTextFormatter(new TextFormatter<>(digitsOnlyFilter));
    }

    /**
     * Load the current settings and populate the GUI pane with the correct
     * values.
     */
    public void loadSettings() {
        GameSettings settings = game.getSettings();
        this.sizeComboBox.setValue(settings.getSize());
        // Avoid showing millisecond values to the user
        this.gameTimeSpinner.getValueFactory().setValue((int) TimeUnit.MINUTES.convert
                (settings.getGameTimeMillis(), TimeUnit.MILLISECONDS));
        this.moveTimeSpinner.getValueFactory().setValue((int) TimeUnit.SECONDS.convert
                (settings.getMoveTimeMillis(), TimeUnit.MILLISECONDS));
        if(settings.gameTimingEnabled()) {
            gameTimingCheckBox.setSelected(true);
            gameTimeSpinner.setDisable(false);
        }
        if(settings.moveTimingEnabled()) {
            moveTimingCheckBox.setSelected(true);
            moveTimeSpinner.setDisable(false);
        }
    }

    /**
     * Handle the game timing checkbox being selected/deselected.
     */
    public void gameTimingEnabled() {
        if(gameTimingCheckBox.isSelected()) {
            gameTimeSpinner.setDisable(false);
        } else {
            gameTimeSpinner.setDisable(true);
        }
    }

    /**
     * Handle the move timing checkbox being selected/deselected.
     */
    public void moveTimingEnabled() {
        if(moveTimingCheckBox.isSelected()) {
            moveTimeSpinner.setDisable(false);
        } else {
            moveTimeSpinner.setDisable(true);
        }
    }

    /**
     * Grab the settings from the GUI pane and update the settings accordingly.
     */
    public void updateSettings() {
        game.getSettings().setMoveTimeMillis(TimeUnit.MILLISECONDS
                .convert(moveTimeSpinner.getValue(), TimeUnit.SECONDS));
        game.getSettings().setMoveTimingEnabled(moveTimingCheckBox.isSelected());
        game.getSettings().setGameTimeMillis(TimeUnit.MILLISECONDS.convert
                (gameTimeSpinner.getValue(), TimeUnit.MINUTES));
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
