package cbtis239.front.ui.users;

import cbtis239.front.util.SceneNavigator;
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
    @FXML private void openCursos()     { loadContent("home.fxml"); }
    @FXML private void openCredencial() { loadContent("home.fxml"); }
    @FXML private void openGrupo()      { loadContent("home.fxml"); }
    @FXML private void openAsignatura() { loadContent("home.fxml"); }
    @FXML private void openAulas()      { loadContent("home.fxml"); }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
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
