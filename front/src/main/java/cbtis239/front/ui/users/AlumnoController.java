package cbtis239.front.ui.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;

public class AlumnoController {

    // Generales
    @FXML private TextField txtNombre, txtApPaterno, txtApMaterno, txtCurp, txtCorreo, txtNss;
    @FXML private ComboBox<String> cmbEdoCivil;

    // Escolares
    @FXML private TextField txtMatricula;
    @FXML private ComboBox<String> cmbEstatus, cmbSemestre, cmbCarrera, cmbGrupo;

    // Contacto
    @FXML private TextField txtCalle, txtNumero, txtColonia, txtEstado, txtMunicipio, txtLocalidad;
    @FXML private TextField txtCelPadre, txtCelMadre;

    // Imagenes
    @FXML private ImageView imgFoto, imgFirma;

    // Botones
    @FXML private Button btnGuardar, btnEliminar, btnCancelar, btnFoto, btnFirma, btnVolver;

    // Tabla
    @FXML private TableView<ObservableList<String>> tablaAlumnos;
    @FXML private TableColumn<ObservableList<String>, String> colMatricula, colNombre, colApPaterno, colApMaterno, colSemestre, colCarrera;

    private ObservableList<ObservableList<String>> listaAlumnos;

    @FXML
    public void initialize() {
        // inicializar listas
        listaAlumnos = FXCollections.observableArrayList();

        colMatricula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colApPaterno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colApMaterno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        colSemestre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        colCarrera.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        tablaAlumnos.setItems(listaAlumnos);

        // Combos
        cmbEdoCivil.setItems(FXCollections.observableArrayList("Soltero", "Casado", "Divorciado"));
        cmbEstatus.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cmbSemestre.setItems(FXCollections.observableArrayList("1","2","3","4","5","6","7","8","9"));
        cmbCarrera.setItems(FXCollections.observableArrayList("Sistemas", "Electrónica", "Mecánica"));
        cmbGrupo.setItems(FXCollections.observableArrayList("A","B","C","D"));

        // Validaciones: NSS y Matrícula solo números
        txtNss.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtNss.setText(newVal.replaceAll("[^\\d]", ""));
        });
        txtMatricula.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtMatricula.setText(newVal.replaceAll("[^\\d]", ""));
        });
    }

    @FXML
    private void onGuardar() {
        if (!validarCampos()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        ObservableList<String> fila = FXCollections.observableArrayList(
                txtMatricula.getText(),
                txtNombre.getText(),
                txtApPaterno.getText(),
                txtApMaterno.getText(),
                cmbSemestre.getValue(),
                cmbCarrera.getValue()
        );
        listaAlumnos.add(fila);
        limpiarCampos();
    }

    private boolean validarCampos() {
        return !(txtNombre.getText().isBlank() ||
                txtApPaterno.getText().isBlank() ||
                txtApMaterno.getText().isBlank() ||
                txtCurp.getText().isBlank() ||
                cmbEdoCivil.getValue() == null ||
                txtCorreo.getText().isBlank() ||
                txtNss.getText().isBlank() ||
                txtMatricula.getText().isBlank() ||
                cmbEstatus.getValue() == null ||
                cmbSemestre.getValue() == null ||
                cmbCarrera.getValue() == null ||
                cmbGrupo.getValue() == null ||
                txtCalle.getText().isBlank() ||
                txtNumero.getText().isBlank() ||
                txtColonia.getText().isBlank() ||
                txtEstado.getText().isBlank() ||
                txtMunicipio.getText().isBlank() ||
                txtLocalidad.getText().isBlank() ||
                txtCelPadre.getText().isBlank() ||
                txtCelMadre.getText().isBlank());
    }

    @FXML
    private void onEliminar() {
        ObservableList<String> seleccionado = tablaAlumnos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) listaAlumnos.remove(seleccionado);
    }

    @FXML
    private void onCancelar() {
        limpiarCampos();
    }

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
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }



@FXML
    private void onSubirFoto() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = fc.showOpenDialog(null);
        if (archivo != null) {
            imgFoto.setImage(new Image(archivo.toURI().toString()));
        }
    }

    @FXML
    private void onSubirFirma() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = fc.showOpenDialog(null);
        if (archivo != null) {
            imgFirma.setImage(new Image(archivo.toURI().toString()));
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApPaterno.clear();
        txtApMaterno.clear();
        txtCurp.clear();
        cmbEdoCivil.getSelectionModel().clearSelection();
        txtCorreo.clear();
        txtNss.clear();
        txtMatricula.clear();
        cmbEstatus.getSelectionModel().clearSelection();
        cmbSemestre.getSelectionModel().clearSelection();
        cmbCarrera.getSelectionModel().clearSelection();
        cmbGrupo.getSelectionModel().clearSelection();
        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtEstado.clear();
        txtMunicipio.clear();
        txtLocalidad.clear();
        txtCelPadre.clear();
        txtCelMadre.clear();
        imgFoto.setImage(null);
        imgFirma.setImage(null);
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }
}
