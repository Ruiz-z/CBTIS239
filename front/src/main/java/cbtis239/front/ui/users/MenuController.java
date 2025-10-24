package cbtis239.front.ui.users;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController {

    @FXML private StackPane contentArea;
    @FXML
    private Label lblBienvenida;


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

            // ✅ Cargar el FXML correctamente (usando la ruta absoluta dentro de resources)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();

            // ✅ Crear la escena y aplicar el CSS de forma segura
            Scene scene = new Scene(root);
            String css = getClass().getResource("/cbtis239/front/css/menu2.css").toExternalForm();
            scene.getStylesheets().add(css);

            // ✅ Crear la nueva ventana sin barra de Windows
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Menú 2");
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // 🔥 Quita la barra superior de Windows
            newStage.setMaximized(true);
            newStage.show();

            // ✅ Cerrar la ventana anterior
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo abrir el Menú 2:\n\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error inesperado al abrir Menú 2:\n\n" + e.getMessage());
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
    private void openRegistrarUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/register_user.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Registrar Usuario");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Registro de Usuario\n" + e.getMessage());
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
    @FXML
    private void openRolesView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/RolesView.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Roles");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo abrir la ventana de Roles\n" + e.getMessage());
        }
    }
    @FXML
    private void openAlumnoView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Alumno.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Gestión de Alumnos");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Alumnos\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }

    @FXML
    private void openAspiranteView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Aspirante.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Gestión de Aspirantes");
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setMaximized(true);
            newStage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            String msg = (t.getMessage() == null) ? t.toString() : t.getMessage();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Aspirantes\n\n" + msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
            t.printStackTrace();
        }
    }
    }
