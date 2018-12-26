package haslam.blackstone.gui.minimal.controllers;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameState;
import haslam.blackstone.events.GameEventAdapter;
import haslam.blackstone.gui.minimal.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

/**
 * Controller for the top pane of the GUI.
 */
public class TopPaneController implements Controller {

    @FXML
    public Button playButton;
    @FXML
    public Button stopButton;
    @FXML
    public Button settingsButton;
    @FXML
    public MenuItem clearPositionMenuItem;

    private GameController game;

    @Override
    public void initialise(GameController game) {
        this.game = game;
        this.game.addListener(new GameEventAdapter() {
            @Override
            public void gameFinished() {
                playButton.setVisible(true);
                stopButton.setVisible(false);
                settingsButton.setDisable(false);
            }
            @Override
            public void gameStarted() {
                playButton.setVisible(false);
                stopButton.setVisible(true);
                settingsButton.setDisable(true);
            }
            @Override
            public void gameResumed() {
                playButton.setVisible(false);
                stopButton.setVisible(true);
                settingsButton.setDisable(true);
            }
        });
    }

    public void newGame() {
        game.start();
    }

    public void stopGame() {
        game.stop();
    }

    public void undo() {
        game.undo();
    }

    public void openSettings() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("haslam/blackstone/gui/minimal/views/SettingsPane.fxml"));
        Pane settingsPane = loader.load();
        Controller controller = loader.getController();
        controller.initialise(game);
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(settingsPane));
        stage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        stage.setResizable(false);
        stage.show();
    }

    public void savePosition(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gomoku State File", "*.gomoku"));
        Stage stage = (Stage) settingsButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if(file != null) {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(game.getState());
        }
    }

    public void loadPosition(ActionEvent actionEvent) throws IOException,
            ClassNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gomoku State File", "*.gomoku"));
        Stage stage = (Stage) settingsButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            game.setLoadedState((GameState) in.readObject());
            clearPositionMenuItem.setDisable(false);
        }
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void clearPosition(ActionEvent actionEvent) {
        game.setLoadedState(null);
        clearPositionMenuItem.setDisable(true);
    }

    public void loadAI(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gomoku AI", "*.exe"));
        Stage stage = (Stage) settingsButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            game.addExternalPlayer(file);
        }
    }
}
