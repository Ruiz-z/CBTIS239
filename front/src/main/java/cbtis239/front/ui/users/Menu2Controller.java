package cbtis239.front.ui.users;

import cbtis239.front.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Menu2Controller {

    @FXML
    private StackPane contentArea;

    // ==========================================
    //  Carga contenido en el área central
    // ==========================================
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlResource));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    // ==========================================
    //      Acciones que abren vistas completas
    // ==========================================
    @FXML
    private void openDocente(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Docente.fxml", "Gestión de Docentes");
     }
    
    @FXML private void openCredencial(ActionEvent event)    {
        openFullScreenStage(event, "/cbtis239/front/views/Credencial.fxml", "Creacion de credenciales");
    }

    @FXML
    private void openCredencial() {
        loadContent("home.fxml");
    }

    @FXML
    private void openCursos(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Curso.fxml", "Gestión de Curso");
    }

    @FXML
    private void openExpediente(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Expediente.fxml", "Gestión de Expediente");
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
    private void openGrupo(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Grupo.fxml", "Gestión de Grupos");
    }
    @FXML
    private void openMenu3(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Menu3.fxml", "Gestión de Grupos");
    }

    @FXML
    private void openAulas(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Aula.fxml", "Gestión de Aulas");
    }

    @FXML
    private void openEspecialidad(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/EspecialidadView.fxml", "Gestión de Especialidades");
    }

    @FXML
    private void openPeriodo(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Periodo.fxml", "Gestión de Periodos");
    }

    @FXML
    private void openEdoCivil(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/EdoCivilView.fxml", "Gestión de EdoCivil");
    }

    @FXML
    private void openGenero(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/GeneroView.fxml", "Gestión de Género");
    }

    @FXML
    private void openAsignatura(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Asignatura.fxml", "Gestión de Asignaturas");
    }

    @FXML
    private void openDocenteMateria(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/DocenteMateria.fxml", "Asignar Materias");
    }

    @FXML
    private void openReticula(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Reticula.fxml", "Retículas");
    }

    // ==========================================
    //   Abre una nueva ventana fullscreen
    // ==========================================
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

    // ==========================================
    //  Abrir en nueva ventana (maximizada normal)
    // ==========================================
    private void openNewStage(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
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

    // ==========================================
    //  Mostrar errores
    // ==========================================
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // ==========================================
    //   Volver al menú principal
    // ==========================================
    @FXML
    private void onVolver(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setMaximized(true);
            st.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir el menú:\n\n" + e.getMessage());
        }
    }

    // ==========================================
    //   Botón cancelar (cierrar ventana embebida)
    // ==========================================
    @FXML
    private void onCancelar() {
        if (contentArea != null && contentArea.getScene() != null) {
            contentArea.getScene().getWindow().hide();
        }
    }
}
