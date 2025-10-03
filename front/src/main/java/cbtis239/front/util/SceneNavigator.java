package cbtis239.front.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class SceneNavigator {

    // Cambia esta constante a la hoja que SÍ tengas, o déjala null para no cargar CSS global
    private static final String GLOBAL_CSS = "/cbtis239/front/css/login.css"; // o "/css/app.css" si lo tienes ahí

    public static void show(Stage stage, String fxml, String title) {
        try {
            // 1) Cargar FXML con verificación
            URL fxmlUrl = SceneNavigator.class.getResource(fxml);
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("FXML no encontrado: " + fxml +
                        " (¿Existe en src/main/resources" + fxml + "?)");
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            // 2) Crear escena y cargar CSS si existe
            Scene scene = new Scene(root);

            if (GLOBAL_CSS != null) {
                URL cssUrl = SceneNavigator.class.getResource(GLOBAL_CSS);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.out.println("[AVISO] Hoja de estilo no encontrada: " + GLOBAL_CSS);
                }
            }

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void switchFromEvent(ActionEvent e, String fxml, String title) {
        Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        show(stage, fxml, title);
    }
}
