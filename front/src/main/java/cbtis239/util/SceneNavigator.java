package cbtis239.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    /**
     * Cambia de escena desde un evento (ej: botón).
     * @param e     El evento que dispara el cambio
     * @param fxml  Ruta del archivo FXML
     * @param title Título de la ventana
     */
    public static void switchFromEvent(ActionEvent e, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxml));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);

            // 👇 Forzar pantalla completa (maximizado con barra de tareas visible)
            stage.setMaximized(true);

            // Si quieres ocultar también barra de tareas, cambia por:
            // stage.setFullScreen(true);

            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
