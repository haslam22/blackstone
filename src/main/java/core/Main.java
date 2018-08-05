package core;

import gui.Controller;
import gui.controllers.BoardPaneController;
import gui.views.BoardPane;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Entry point of the application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getClassLoader().getResource
                ("FontAwesome.otf").toExternalForm(), 10);

        Game game = new Game(new GameSettings(
                Defaults.PLAYER_1,
                Defaults.PLAYER_2,
                Defaults.GAME_TIMING_ENABLED,
                Defaults.MOVE_TIMING_ENABLED,
                Defaults.GAME_TIMEOUT_MILLIS,
                Defaults.MOVE_TIMEOUT_MILLIS,
                Defaults.SIZE));

        Pane boardPane = loadBoardPane(game);
        Pane topPane = loadTopPane(game);
        Pane rightPane = loadRightPane(game);

        BorderPane root = new BorderPane();
        root.setRight(rightPane);
        root.setCenter(boardPane);
        root.setTop(topPane);

        primaryStage.setTitle("Gomoku");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            game.stop();
        });
    }

    /**
     * Save a WritableImage to a file
     * @param image Input image
     * @param file File to write to
     * @throws IOException
     */
    public static void saveImage(WritableImage image, File file)
            throws IOException {
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }

    /**
     * Take a pixel-aware screenshot of some pane.
     * @param pane Input pane
     * @param pixelScale Scaling factor applied to snapshot (1 is no scaling)
     * @return WritableImage snapshot, scaled by given amount
     */
    public static WritableImage screenshot(Pane pane, double pixelScale) {
        int width = (int) Math.rint(pixelScale * pane.getWidth());
        int height = (int) Math.rint(pixelScale * pane.getHeight());

        WritableImage writableImage = new WritableImage(width, height);
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(Transform.scale(pixelScale, pixelScale));
        return pane.snapshot(params, writableImage);
    }

    /**
     * Load the right pane for the main stage.
     * @param game Game instance
     * @return Right pane
     * @throws IOException
     */
    private Pane loadRightPane(Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("gui/views/RightPane.fxml"));
        Pane rightPane = loader.load();
        Controller controller = loader.getController();
        controller.initialise(game);
        return rightPane;
    }

    /**
     * Load the top pane for the main stage.
     * @param game Game instance
     * @return Top pane
     * @throws IOException
     */
    private Pane loadTopPane(Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("gui/views/TopPane.fxml"));
        Pane topPane = loader.load();
        Controller controller = loader.getController();
        controller.initialise(game);
        return topPane;
    }

    /**
     * Load the board pane for the main stage.
     * @param game Game instance
     * @return Board pane
     */
    private Pane loadBoardPane(Game game) {
        BoardPane boardPane = new BoardPane(15);
        Controller controller = new BoardPaneController(boardPane);
        controller.initialise(game);
        return boardPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
