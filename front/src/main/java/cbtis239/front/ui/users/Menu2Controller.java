package cbtis239.front.ui.users;

import cbtis239.util.SceneNavigator;
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

public class Menu2Controller {

    @FXML private StackPane contentArea;

    // Helpers
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlResource));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    // Acciones del menú
    @FXML private void openExpediente() { loadContent("home.fxml"); }
    @FXML private void openDocente()    { loadContent("home.fxml"); }
    @FXML private void openCredencial() { loadContent("home.fxml"); }
    @FXML private void openAsignatura() { loadContent("home.fxml"); }

    @FXML
    private void openCursos(ActionEvent event) {
        openNewStage(event, "/cbtis239/front/views/Curso.fxml", "Gestión de Curso");
    }

    @FXML
    private void openGrupo(ActionEvent event) {
        openNewStage(event, "/cbtis239/front/views/Grupo.fxml", "Gestión de Grupos");
    }

    @FXML
    private void openAulas(ActionEvent event) {
        openNewStage(event, "/cbtis239/front/views/Aula.fxml", "Gestión de Aulas");
    }

    @FXML
    private void openEspecialidad(ActionEvent event) {
        openNewStage(event, "/cbtis239/front/views/EspecialidadView.fxml", "Gestión de Especialidades");
    }

    @FXML
    private void openPeriodo(ActionEvent event) {
        openNewStage(event, "/cbtis239/front/views/Periodo.fxml", "Gestión de Periodos");
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


    @FXML
    private void openEdoCivil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/EdoCivilView.fxml"));
            Stage st = new Stage();
            st.setTitle("Estado Civil");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();

            // Si quieres cerrar el menú al abrir la ventana:
            // ((Stage)((Node)event.getSource()).getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir Estado Civil\n\n" + e.getMessage());
            a.setHeaderText("Error"); a.showAndWait();
        }
    }
    @FXML
    private void openGenero(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/GeneroView.fxml"));
            Stage st = new Stage();
            st.setTitle("Géneros");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();
            // ((Stage)((Node)event.getSource()).getScene().getWindow()).close(); // si quieres cerrar el menú
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Género.\n\n" + e.getMessage()).showAndWait();
        }
    }


    // Acción especial: volver al menú principal
    @FXML
    private void onVolver(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Stage st = new Stage();
            st.setTitle("Menú");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();
            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir el menú:\n\n" + e.getMessage());
        }
    }
  
// Botón cancelar
    @FXML
    private void onCancelar() {
        contentArea.getScene().getWindow().hide();
    }
}
