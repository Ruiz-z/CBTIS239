package cbtis239.front.ui.users;

import cbtis239.front.MainApp;
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
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Objects;

public class MenuController {

    @FXML private StackPane contentArea;
    @FXML private Label lblBienvenida;

    // ================== INICIALIZACIÓN ==================
    @FXML
    private void initialize() {
        aplicarBienvenidaDesdeSesion();
    }

    /** Construye el mensaje "¡Bienvenido de vuelta, NOMBRE PATERNO MATERNO!" */
    private void aplicarBienvenidaDesdeSesion() {
        Usuario u = SesionActual.getUsuario();
        if (u == null) {
            lblBienvenida.setText("");
            return;
        }

        String nombre  = nvl(u.getNombre());
        String paterno = nvl(u.getPaterno());
        String materno = nvl(u.getMaterno());

        String nombreCompleto = (nombre + " " + paterno + " " + materno)
                .trim()
                .replaceAll(" +", " ");

        if (nombreCompleto.isEmpty()) {
            lblBienvenida.setText("¡Bienvenido de vuelta!");
        } else {
            lblBienvenida.setText("¡Bienvenido de vuelta, " + nombreCompleto.toUpperCase() + "!");
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    // ===== Helper para cargar vistas dentro del contentArea =====
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(
                    getClass().getResource(fxmlResource)
            );
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // ===== Botón "Más Opciones" → cambia a Menu2 (pantalla completa) =====
    @FXML
    private void openMenu2(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Menu2.fxml", "Menu2");
    }

    private void openFullScreenStage(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");

            // 🔹 Icono del CBTis en el nuevo Stage
            newStage.getIcons().add(
                    new Image(Objects.requireNonNull(
                            MainApp.class.getResourceAsStream("/cbtis239/front/logo.png"),
                            "No se encontró el icono del CBTis"
                    ))
            );

            newStage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }


    // ===== Botones de la pantalla de bienvenida (si los usas) =====
    @FXML private void onActualizarPagado() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText("Simulación: actualizar pagado.");
        a.show();
    }

    @FXML private void onCancelar() {
        contentArea.getScene().getWindow().hide();
    }

    @FXML
    private void openRegistrarUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/register_user.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Registrar Usuario");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Registro de Usuario\n" + e.getMessage());
        }
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(
                            MainApp.class.getResource("/cbtis239/front/views/Login.fxml"),
                            "No se encontró Login.fxml"
                    )
            );

            Scene scene = new Scene(root);
            stage.setTitle("Inicio de Sesión");
            stage.setScene(scene);
            stage.setResizable(false);
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

    @FXML
    private void openRolesView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/RolesView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Roles");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Roles\n" + e.getMessage());
        }
    }
    @FXML
    private void openHistorialAsistencia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/HistorialAsistencia.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Historial Asistencia");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Horario de Grupos\n" + e.getMessage());
        }
    }
    @FXML
    private void openHorarioGrupo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/HorarioGrupos.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Horario de Grupos");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Horario de Grupos\n" + e.getMessage());
        }
    }

    @FXML
    private void openAlumnoView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Alumno.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Alumnos");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Alumnos\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }

    @FXML
    private void openAspiranteView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Aspirante.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Gestión de Aspirantes");
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Aspirantes\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }
}
