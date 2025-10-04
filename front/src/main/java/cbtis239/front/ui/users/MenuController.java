package cbtis239.front.ui.users;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MenuController {

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

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // Acciones del menú (carga de pantallas)
    @FXML private void openExpediente() { loadContent("home.fxml"); /* cambia a expediente.fxml cuando lo tengas */ }
    @FXML private void openDocente()    { loadContent("home.fxml"); }
    @FXML private void openCursos()     { loadContent("home.fxml"); }
    @FXML private void openCredencial() { loadContent("home.fxml"); }
    @FXML private void openGrupo()      { loadContent("home.fxml"); }
    @FXML private void openAsignatura() { loadContent("home.fxml"); }
    @FXML private void openAulas()      { loadContent("home.fxml"); }
    @FXML private void onVolver()       { /* navega a menú anterior o login */ }

    // Botones de la pantalla de bienvenida
    @FXML private void onActualizarPagado() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText("Simulación: actualizar pagado.");
        a.show();
    }
    @FXML private void onCancelar() {
        // cierra ventana o vuelve a login
        contentArea.getScene().getWindow().hide();
    }
}
