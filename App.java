import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main class to invoke the frontend of the application
 *
 * @author Ruixuan Tu
 */
public class App extends Application {
    public static MapController controller;

    /**
     * Main method, entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Start the application
     *
     * @param stage stage to be displayed
     * @throws Exception if the stage cannot be displayed
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("MainWindow.fxml")));
        Scene scene = new Scene(fxmlLoader.load());
        App.controller = fxmlLoader.getController();
        App.controller.stage = stage;
        stage.setScene(scene);
        scene.setOnKeyPressed(scene.lookup("#scrollPane")::fireEvent);
        stage.setTitle("BadgerMap - Untitled [New]");
        stage.show();
    }
}
