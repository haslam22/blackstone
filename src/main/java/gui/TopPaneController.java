package gui;

import core.GameEventAdapter;
import core.GameManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the top pane of the GUI
 */
public class TopPaneController extends Controller {

    public Button playButton;
    public Button stopButton;
    public Button settingsButton;
    private GameManager manager;
    private Stage settingsStage;

    /**
     * Handle the new game button
     * @param actionEvent
     */
    public void newGame(ActionEvent actionEvent) {
        manager.startGame();
    }

    /**
     * Handle the stop game button
     * @param actionEvent
     */
    public void stopGame(ActionEvent actionEvent) {
        manager.stopGame();
    }

    /**
     * Handle the undo button
     * @param actionEvent
     */
    public void undo(ActionEvent actionEvent) {
        manager.undo();
    }

    @Override
    public void initialise(GameManager manager) {
        this.manager = manager;
        // Respond to game started/stopped events by activating and
        // deactivating buttons
        this.manager.addListener(new GameEventAdapter() {
            @Override
            public void gameStarted() {
                playButton.setVisible(false);
                stopButton.setVisible(true);
                settingsButton.setDisable(true);
            }
            @Override
            public void gameOver() {
                stopButton.setVisible(false);
                playButton.setVisible(true);
                settingsButton.setDisable(false);
            }
        });
    }

    /**
     * Open a new window to show the settings
     * @param actionEvent
     * @throws IOException
     */
    public void openSettings(ActionEvent actionEvent) throws IOException {
        if(this.settingsStage == null) {
            this.settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource
                    ("SettingsPane.fxml"));
            Parent settingsPane = loader.load();
            SettingsPaneController controller = (SettingsPaneController) loader
                    .getController();
            controller.initialise(this.manager);
            settingsStage.setScene(new Scene(settingsPane, 300, 300));
            settingsStage.getIcons().add(new Image(getClass().getClassLoader()
                    .getResource("AppIcon.png").toExternalForm()));
            settingsStage.setResizable(false);
            settingsStage.show();
            settingsStage.setOnCloseRequest(e -> this.settingsStage = null);
        } else {
            this.settingsStage.show();
        }
    }
}
