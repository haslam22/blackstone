package core;

import gui.BoardPane;
import gui.RightPaneController;
import gui.TopPaneController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("prism.lcdtext", "false");
        Font.loadFont(getClass().getClassLoader().getResource
                ("FontAwesome.otf").toExternalForm(), 10);

        BoardPane boardPane = new BoardPane(15);
        GameManager manager = new GameManager(boardPane);

        Parent topPane = loadTopPane(manager);
        Parent rightPane = loadRightPane(manager);

        BorderPane root = new BorderPane();
        root.setRight(rightPane);
        root.setTop(topPane);
        root.setCenter(boardPane);

        primaryStage.setTitle("Gomoku");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setOnCloseRequest(windowEvent -> {
            manager.stopGame();
            Platform.exit();
        });
        primaryStage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        primaryStage.show();
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
     * Load the right pane for the main stage
     * @param manager Game manager
     * @return Right pane component
     * @throws IOException
     */
    private Parent loadRightPane(GameManager manager) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("RightPane.fxml"));
        Parent rightPane = loader.load();
        RightPaneController controller = loader.getController();
        controller.initialise(manager);
        return rightPane;
    }

    /**
     * Load the top pane for the main stage
     * @param manager Game manager
     * @return Top pane component
     * @throws IOException
     */
    private Parent loadTopPane(GameManager manager) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("TopPane.fxml"));
        Parent topPane = loader.load();
        TopPaneController controller = loader.getController();
        controller.initialise(manager);
        return topPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
