package cbtis239.front.ui.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class AlumnoController {

    @FXML private TextField txtNombre, txtApPaterno, txtApMaterno, txtCurp, txtEdoCivil, txtCorreo, txtNss;
    @FXML private TextField txtMatricula, txtEstatus, txtSemestre, txtCarrera, txtGrupo;
    @FXML private TextField txtCalle, txtNumero, txtColonia, txtEstado, txtMunicipio, txtLocalidad;
    @FXML private TextField txtCelPadre, txtCelMadre;

    @FXML private Button btnGuardar, btnEliminar, btnCancelar, btnFinalizar, btnFoto, btnFirma, btnVolver;
    @FXML private ImageView imgFoto, imgFirma;

    @FXML private TableView<ObservableList<String>> tablaAlumnos;
    @FXML private TableColumn<ObservableList<String>, String> colMatricula, colNombre, colApPaterno, colApMaterno, colSemestre, colCarrera;

    private ObservableList<ObservableList<String>> listaAlumnos;

    @FXML
    public void initialize() {
        listaAlumnos = FXCollections.observableArrayList();

        colMatricula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colApPaterno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colApMaterno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        colSemestre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        colCarrera.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        tablaAlumnos.setItems(listaAlumnos);
    }

    @FXML
    private void onGuardar() {
        ObservableList<String> fila = FXCollections.observableArrayList(
                txtMatricula.getText(),
                txtNombre.getText(),
                txtApPaterno.getText(),
                txtApMaterno.getText(),
                txtSemestre.getText(),
                txtCarrera.getText()
        );
        listaAlumnos.add(fila);
        limpiarCampos();
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
    private void onFinalizar() {
        System.out.println("Proceso finalizado.");
    }

    @FXML
    private void onVolver() {
        System.out.println("Volviendo al menú principal...");
        // aquí puedes cargar otra escena o cerrar la ventana
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
        txtMatricula.clear();
        txtNombre.clear();
        txtApPaterno.clear();
        txtApMaterno.clear();
        txtSemestre.clear();
        txtCarrera.clear();
    }
}
