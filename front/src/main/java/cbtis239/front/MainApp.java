package cbtis239.front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                java.util.Objects.requireNonNull(
                        MainApp.class.getResource("/cbtis239/front/MainApp.fxml"),
                        "No se encontró /cbtis239/front/MainApp.fxml"
                )
        );

        Scene scene = new Scene(loader.load(), 420, 520);
        stage.setTitle("CBTIS239 - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
