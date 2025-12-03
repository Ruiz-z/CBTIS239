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

            // ⚠️ IMPORTANTE: NO cierres el stage, solo cambia la escena
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

}
