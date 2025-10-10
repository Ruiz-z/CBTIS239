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

import java.io.IOException;

public class Menu2Controller {

    @FXML private StackPane contentArea;

    // Helpers
    private void loadContent(String fxmlResource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlResource));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showError("No se pudo cargar la vista: " + fxmlResource + "\n" + e.getMessage());
        }
    }

    // Acciones del menú
    @FXML private void openExpediente() { loadContent("home.fxml"); }
    @FXML private void openDocente()    { loadContent("home.fxml"); }
    @FXML private void openCredencial() { loadContent("home.fxml"); }

    @FXML private void openAsignatura() { loadContent("home.fxml"); }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }
    @FXML
    private void openCursos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Curso.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Curso");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // cerrar menú actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Grupos\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }

    @FXML
    private void openGrupo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Grupo.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Grupos");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // cerrar menú actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Grupos\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }

    @FXML
    private void openAulas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/aula.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Aulas");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // cerrar menú actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Aulas\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }

    @FXML
    private void openEspecialidad(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/cbtis239/front/views/EspecialidadView.fxml")
            );
            Stage st = new Stage();
            st.setTitle("Especialidades");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();

            // si deseas cerrar Menu2 al abrir la ventana nueva:
            Stage current = (Stage) ((Node) event.getSource()).getScene().getWindow();
            current.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la ventana de Especialidades.\n\n" + e.getMessage(),
                    ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
        }
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

    // Acción especial: volver al menú principal
    @FXML
    private void onVolver(ActionEvent event) {
        SceneNavigator.switchFromEvent(event, "/cbtis239/front/views/Menu.fxml", "Menú Principal");
    }


    // Botón cancelar
    @FXML
    private void onCancelar() {
        contentArea.getScene().getWindow().hide();
    }
}