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

public class Menu2Controller {

    @FXML private StackPane contentArea;

    // Helpers
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

    // Acciones del menú (carga de pantallas dentro del contentArea)
    @FXML private void openExpediente() { loadContent("home.fxml"); /* cambia a expediente.fxml */ }
    @FXML private void openDocente()    { loadContent("home.fxml"); }
    @FXML private void openCursos()     { loadContent("home.fxml"); }
    @FXML private void openCredencial() { loadContent("home.fxml"); }
    @FXML private void openGrupo()      { loadContent("home.fxml"); }
    @FXML private void openAsignatura() { loadContent("home.fxml"); }
    @FXML private void openAulas()      { loadContent("home.fxml"); }
    @FXML private void onVolver()       { /* navega a menú anterior o login */ }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // Acciones del menú 2 (carga de pantallas dentro del contentArea)
    @FXML private void onAccion1() { loadContent("home.fxml"); /* cambia a tu propio FXML */ }
    @FXML private void onAccion2() { loadContent("home.fxml"); }

    // Acción especial: volver al menú principal
    @FXML
        private void onVolver(ActionEvent event) {
        try {
            System.out.println("Regresando al Menu.fxml...");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cbtis239/front/views/Menu.fxml") // M mayúscula
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/cbtis239/front/css/menu.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo volver al Menú principal: " + e.getMessage());
        }
    }


    // Botón cancelar
    @FXML
    private void onCancelar() {
        // Cierra la ventana o vuelve a login
        contentArea.getScene().getWindow().hide();
    }
}
