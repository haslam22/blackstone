package haslam.blackstone.gui.minimal;

import haslam.blackstone.core.GameController;
import haslam.blackstone.gui.GameGUI;
import haslam.blackstone.gui.minimal.controllers.BoardPaneController;
import haslam.blackstone.gui.minimal.views.BoardPane;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class MinimalGUI implements GameGUI {

    @Override
    public void launch(GameController gameController, Stage primaryStage) {
        Font.loadFont(getClass().getClassLoader().getResource
                ("FontAwesome.otf").toExternalForm(), 10);
        Pane boardPane = loadBoardPane(gameController);
        Pane topPane = null;
        Pane rightPane = null;
        try {
            topPane = loadTopPane(gameController);
            rightPane = loadRightPane(gameController);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BorderPane root = new BorderPane();
        root.setRight(rightPane);
        root.setCenter(boardPane);
        root.setTop(topPane);

        primaryStage.setTitle("Blackstone");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            gameController.stop();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Load the right pane for the main stage.
     * @param game Game instance
     * @return Right pane
     * @throws IOException
     */
    private Pane loadRightPane(GameController game) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("haslam/blackstone/gui/minimal/views/RightPane.fxml"));
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
    private Pane loadTopPane(GameController game) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("FontAwesome"));
        loader.setLocation(getClass().getClassLoader().getResource
                ("haslam/blackstone/gui/minimal/views/TopPane.fxml"));
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
    private Pane loadBoardPane(GameController game) {
        BoardPane boardPane = new BoardPane(15);
        Controller controller = new BoardPaneController(boardPane);
        controller.initialise(game);
        return boardPane;
    }
}
