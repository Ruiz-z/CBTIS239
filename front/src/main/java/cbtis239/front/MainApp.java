package cbtis239.front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final String fxmlPath = "/cbtis239/front/Login.fxml";

        URL fxmlUrl = Objects.requireNonNull(
                MainApp.class.getResource(fxmlPath),
                "No se encontró el FXML en: " + fxmlPath + "\n" +
                        "Verifica que exista en src/main/resources" + fxmlPath
        );

        FXMLLoader loader = new FXMLLoader(fxmlUrl);

        Scene scene = new Scene(loader.load(), 740, 420);


         scene.getStylesheets().add(
             Objects.requireNonNull(
                 MainApp.class.getResource("/cbtis239/front/css/login.css"),
                 "No se encontró el CSS"
             ).toExternalForm()
         );

        stage.setTitle("Inicio de sesión");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
