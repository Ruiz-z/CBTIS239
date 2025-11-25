package cbtis239.front.ui.users;

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

public class Menu3Controller {

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
    @FXML
    private void openDirector(ActionEvent event) {
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
    @FXML
    private void onCerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Inicio de Sesión");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            showError("No se pudo volver al inicio de sesión.\n" + e.getMessage());
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
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
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
