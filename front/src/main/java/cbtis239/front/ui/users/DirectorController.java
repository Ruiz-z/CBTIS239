package cbtis239.front.ui.users;

import cbtis239.bo.DirectorBO;
import cbtis239.model.Director;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public class DirectorController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPaterno;
    @FXML private TextField txtMaterno;
    @FXML private ImageView imgFirma;

    @FXML private Button btnCambiarFirma;
    @FXML private Button btnGuardar;
    @FXML private Button btnCerrar;
    @FXML private Button btnVolverMenu;

    @FXML private Label lblMensaje;

    private final DirectorBO bo = new DirectorBO();
    private Director director;
    private byte[] firmaBytes;

    @FXML
    public void initialize() {
        limpiarMensaje();
        cargarDirector();
    }

    private void cargarDirector() {
        try {
            director = bo.obtenerDirector();
            if (director != null) {
                txtNombre.setText(director.getNombre());
                txtPaterno.setText(director.getPaterno());
                txtMaterno.setText(director.getMaterno());

                firmaBytes = director.getFirma();
                if (firmaBytes != null) {
                    imgFirma.setImage(new Image(new ByteArrayInputStream(firmaBytes)));
                }
                mostrarInfo("Director cargado.");
            } else {
                director = new Director();
                director.setIdDirector(1);
                mostrarInfo("Capture la información del director.");
            }

        } catch (SQLException e) {
            mostrarError("No se pudo cargar: " + e.getMessage());
        }
    }

    @FXML
    private void onCambiarFirma() {
        limpiarMensaje();
        try {
            Window owner = imgFirma.getScene().getWindow();
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar imagen de la firma");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
            );
            File f = fc.showOpenDialog(owner);
            if (f != null) {
                firmaBytes = Files.readAllBytes(f.toPath());
                imgFirma.setImage(new Image(new ByteArrayInputStream(firmaBytes)));
                mostrarInfo("Firma actualizada.");
            }

        } catch (IOException e) {
            mostrarError("Error al cargar firma.");
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        try {
            if (director == null) {
                director = new Director();
                director.setIdDirector(1);
            }

            director.setNombre(txtNombre.getText());
            director.setPaterno(txtPaterno.getText());
            director.setMaterno(txtMaterno.getText());
            director.setFirma(firmaBytes);

            bo.guardarDirector(director);
            mostrarInfo("Datos guardados correctamente.");

        } catch (SQLException e) {
            mostrarError("No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void onCerrar() {
        btnCerrar.getScene().getWindow().hide();
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader fx = new FXMLLoader(
                    getClass().getResource("/cbtis239/front/views/Menu3.fxml")
            );

            Parent root = fx.load();

            Stage s = new Stage();
            s.setTitle("Menú Principal");
            s.setScene(new Scene(root));
            s.initStyle(StageStyle.UNDECORATED);
            s.setFullScreen(true);
            s.setFullScreenExitHint("");
            s.show();

            // Cerrar ventana actual
            Stage actual = (Stage) ((Node) event.getSource()).getScene().getWindow();
            actual.close();

        } catch (Exception e) {
            mostrarError("No se pudo abrir el menú.");
        }
    }

    // ================== MENSAJES ==================

    private void mostrarInfo(String msg) {
        lblMensaje.setStyle("-fx-text-fill: #0077cc; -fx-font-weight: bold;");
        lblMensaje.setText(msg);
    }

    private void mostrarError(String msg) {
        lblMensaje.setStyle("-fx-text-fill: #cc0000; -fx-font-weight: bold;");
        lblMensaje.setText(msg);
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
    }
}
