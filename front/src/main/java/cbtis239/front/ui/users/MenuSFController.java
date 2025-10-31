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

public class MenuSFController {

    @FXML
    private StackPane contentArea;

    /** Carga una vista dentro del área central del menú */
    private void loadContent(String fxmlResource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("⚠️ No se pudo cargar la vista:\n" + fxmlResource + "\n\n" + e.getMessage());
        }
    }

    /** Muestra una alerta de error */
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    // === OPCIONES DEL MENÚ ===

    @FXML
    private void openRegistrarPago(ActionEvent event) {
        try {
            System.out.println("Intentando cargar pagos");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cbtis239/front/views/pagos.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/cbtis239/front/css/pagos.css").toExternalForm()
            );

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setMaximized(true);
            newStage.setTitle("Registrar Pagos");
            newStage.show();


            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir registro de pagos: " + e.getMessage());
        }
    }


    @FXML
    private void openHistorialPago(ActionEvent event) {
        try {
            System.out.println("Intentando cargar Historial pagos.");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cbtis239/front/views/historialPago.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/cbtis239/front/css/historialPago.css").toExternalForm()
            );

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setMaximized(true);
            newStage.setTitle("Historial de pagos");
            newStage.show();


            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir Historial de pagos: " + e.getMessage());
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
}
