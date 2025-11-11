package cbtis239.front.ui.users;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuDocenteController {

    @FXML private StackPane contentArea;

    // ===== Helper: mostrar errores genéricos =====
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // ===========================================================
    // 🔹 Vistas internas dentro del panel (si deseas usarlas)
    // ===========================================================
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlResource));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    // ===========================================================
    // 🔹 Abrir vistas como pantallas completas (igual a MenuController)
    // ===========================================================

    @FXML
    private void openCalificaciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Calificaciones.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Administración de Calificaciones");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Calificaciones\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }

    @FXML
    private void openVerGrupo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/VerGrupo.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Visualización del Grupo Asignado");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Grupo\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Inicio de Sesión");
            stage.setScene(new Scene(root));
            stage.show();

            // Cierra el menú docente actual
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            showError("No se pudo volver al inicio de sesión.\n" + e.getMessage());
        }
    }
}
