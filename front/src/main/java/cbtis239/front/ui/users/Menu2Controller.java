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

public class Menu2Controller {

    @FXML private StackPane contentArea;

    // ---- Helpers de contenido embebido en el mismo Stage ----
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlResource));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    // ---- Acciones de menú que reemplazan el centro del layout actual ----
    @FXML private void openDocente(ActionEvent event)    {
        openFullScreenStage(event, "/cbtis239/front/views/Docente.fxml", "Gestión de Docentes");
     }
    
    @FXML private void openCredencial() { loadContent("home.fxml"); }


    // ---- Acciones que abren nuevas ventanas ----
    @FXML
    private void openCursos(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Curso.fxml", "Gestión de Curso");
    }

    @FXML
    private void openExpediente(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Expediente.fxml", "Gestión de Expediente");
    }

    @FXML
    private void openGrupo(ActionEvent event) {
        openFullScreenStage(event, "/cbtis239/front/views/Grupo.fxml", "Gestión de Grupos");
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

    // ---- Utilidades para abrir ventanas ----
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

    private void openNewStage(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // cerrar menú actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // ---- Acción especial: volver al menú principal ----
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

    // Botón cancelar en la vista embebida
    @FXML
    private void onCancelar() {
        if (contentArea != null && contentArea.getScene() != null) {
            contentArea.getScene().getWindow().hide();
        }
    }
}
