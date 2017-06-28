package core;

import gui.BoardPane;
import gui.RightPaneController;
import gui.TopPaneController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("prism.lcdtext", "false");
        Font font = Font.loadFont(getClass().getClassLoader().getResource
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
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                manager.stopGame();
                Platform.exit();
            }
        });
        primaryStage.getIcons().add(new Image(getClass().getClassLoader()
                .getResource("AppIcon.png").toExternalForm()));
        primaryStage.show();
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
        RightPaneController controller = (RightPaneController)
                loader.getController();
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
        TopPaneController controller = (TopPaneController)
                loader.getController();
        controller.initialise(manager);
        return topPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
