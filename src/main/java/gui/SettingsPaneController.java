package gui;

import core.GameManager;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import static core.GameTimer.*;

/**
 * Controller for the settings pane dialog
 */
public class SettingsPaneController implements Controller {

    public ComboBox<Integer> intersectionsComboBox;
    public ComboBox<Integer> moveTimeComboBox;
    public ComboBox<Integer> gameTimeComboBox;
    public CheckBox moveTimingCheckBox;
    public CheckBox gameTimingCheckBox;
    private GameManager manager;

    public void gameTimingSelected() {
        if(gameTimingCheckBox.isSelected()) {
            gameTimeComboBox.setDisable(false);
        } else {
            gameTimeComboBox.setDisable(true);
        }
    }

    public void moveTimingSelected() {
        if(moveTimingCheckBox.isSelected()) {
            moveTimeComboBox.setDisable(false);
        } else {
            moveTimeComboBox.setDisable(true);
        }
    }

    @Override
    public void initialise(GameManager manager) {
        this.manager = manager;
        // Grab the current settings and populate the dialog
        int intersections = manager.getIntersections();
        boolean gameTimingEnabled = manager.gameTimingEnabled();
        boolean moveTimingEnabled = manager.moveTimingEnabled();
        if(gameTimingEnabled) {
            gameTimingCheckBox.setSelected(true);
            gameTimeComboBox.setDisable(false);
            gameTimeComboBox.setValue(millisToMinutes(manager.getGameTime()));
        } else {
            gameTimingCheckBox.setSelected(false);
            gameTimeComboBox.setDisable(true);
            gameTimeComboBox.setValue(millisToMinutes(manager.getGameTime()));
        }
        if(moveTimingEnabled) {
            moveTimingCheckBox.setSelected(true);
            moveTimeComboBox.setDisable(false);
            moveTimeComboBox.setValue(millisToSeconds(manager.getMoveTime()));
        } else {
            moveTimingCheckBox.setSelected(false);
            moveTimeComboBox.setDisable(true);
            moveTimeComboBox.setValue(millisToSeconds(manager.getMoveTime()));
        }
        intersectionsComboBox.setValue(intersections);
    }

    public void updateSettings() {
        if(moveTimingCheckBox.isSelected()) {
            manager.setMoveTimingEnabled(true);
            manager.updateMoveTime(secondsToMillis(moveTimeComboBox.getValue()));
        } else {
            manager.setMoveTimingEnabled(false);
        }
        if(gameTimingCheckBox.isSelected()) {
            manager.setGameTimingEnabled(true);
            manager.updateGameTime(minutesToMillis(gameTimeComboBox.getValue()));
        } else {
            manager.setGameTimingEnabled(false);
        }
        manager.updateIntersections(intersectionsComboBox.getValue());
        
        // Hack-ish way of closing the dialog window
        Stage window = (Stage) intersectionsComboBox.getScene().getWindow();
        window.close();
    }

    public void closeSettings() {
        Stage window = (Stage) intersectionsComboBox.getScene().getWindow();
        window.close();
    }
}
