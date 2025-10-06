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

public class MenuController {

    @FXML private StackPane contentArea;

    // ===== Helper para cargar vistas dentro del contentArea =====
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

    // ===== Botón "Más Opciones" → cambia a Menu2 (pantalla completa) =====
    @FXML
    private void openMenu2(ActionEvent event) {
        try {
            System.out.println("Intentando cargar Menu2.fxml...");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cbtis239/front/views/Menu2.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/cbtis239/front/css/menu2.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir el Menu 2: " + e.getMessage());
        }
    }

    // ===== Botones de la pantalla de bienvenida =====
    @FXML private void onActualizarPagado() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText("Simulación: actualizar pagado.");
        a.show();
    }

    @FXML private void onCancelar() {
        // Cierra ventana
        contentArea.getScene().getWindow().hide();
    }

    @FXML
    private void openRegistrarUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/register_user.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Registrar Usuario");
            stage.setScene(new Scene(root, 900, 550));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace(); // 👈 muestra el error real
            showError("No se pudo abrir la ventana de Registro de Usuario\n\n" + e.getMessage());
        }
    }

    @FXML
    private void openRolesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/RolesView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Roles");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir la ventana de Roles\n\n" + e.getMessage());
        }
    }

    // ===== Nuevo método: abrir pantalla de Alumno =====
    @FXML
    private void openAlumnoView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Alumno.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Alumnos");
            stage.setScene(new Scene(root, 1000, 650));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir la ventana de Alumnos\n\n" + e.getMessage());
        }
    }
}
