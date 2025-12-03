package cbtis239.front.ui.users;

import cbtis239.front.MainApp;
import cbtis239.model.Docente;
import cbtis239.model.Usuario;
import cbtis239.session.SesionActual;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MenuDocenteController {

    @FXML private StackPane contentArea;
    @FXML private Label lblBienvenida;   // <--- asegúrate de tener fx:id="lblBienvenida" en el FXML

    // ====== initialize: se ejecuta al cargar el FXML ======
    @FXML
    private void initialize() {
        actualizarMensajeBienvenida();
    }

    /** Arma el mensaje "Bienvenido <nombre docente>" usando la sesión global. */
    private void actualizarMensajeBienvenida() {
        String nombre = "Docente";

        Docente d = SesionActual.getDocente();
        if (d != null) {
            String n = safe(d.getNombre());
            String p = safe(d.getPaterno());
            String m = safe(d.getMaterno());
            String full = (n + " " + p + " " + m).trim().replaceAll("\\s+", " ");
            if (!full.isEmpty()) {
                nombre = full;
            }
        } else {
            // Si por alguna razón no hay Docente en sesión, usamos Usuario como respaldo
            Usuario u = SesionActual.getUsuario();
            if (u != null) {
                String full = (safe(u.getNombre()) + " " +
                               safe(u.getPaterno()) + " " +
                               safe(u.getMaterno()))
                               .trim()
                               .replaceAll("\\s+", " ");
                if (!full.isEmpty()) {
                    nombre = full;
                } else if (u.getUsuario() != null && !u.getUsuario().isBlank()) {
                    nombre = u.getUsuario();
                }
            }
        }

        if (lblBienvenida != null) {
            lblBienvenida.setText("Bienvenido " + nombre);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

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
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la ventana de Calificaciones\n\n" + msg, ButtonType.OK);
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
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la ventana de Grupo\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            // 1. Obtener el Stage actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 2. Cargar de nuevo el Login.fxml usando la MISMA ruta que en MainApp
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(
                            MainApp.class.getResource("/cbtis239/front/views/Login.fxml"),
                            "No se encontró Login.fxml"
                    )
            );

            // 3. Crear la nueva escena y asignarla al mismo stage
            Scene scene = new Scene(root);
            stage.setTitle("Inicio de Sesión");
            stage.setScene(scene);
            stage.setResizable(false);

            // ⚠️ IMPORTANTE: NO cierres el stage, solo cambia la escena
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error al cerrar sesión");
            a.setHeaderText(e.getClass().getSimpleName() + ": " + e.getMessage());
            a.setContentText(e.toString());
            a.showAndWait();
        }
    }

}
