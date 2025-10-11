package cbtis239.front.ui.users;

import cbtis239.bo.GeneroBO;
import cbtis239.model.Genero;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GeneroController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TableView<Genero> tabla;
    @FXML private TableColumn<Genero, Number> colId;
    @FXML private TableColumn<Genero, String> colNombre;

    private final GeneroBO bo = new GeneroBO();
    private final ObservableList<Genero> datos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdGenero()));
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        tabla.setItems(datos);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs,o,sel)->{
            if (sel != null) {
                txtId.setText(String.valueOf(sel.getIdGenero()));
                txtNombre.setText(sel.getNombre());
            }
        });

        cargar();
    }

    private void cargar() {
        try { datos.setAll(bo.listar()); }
        catch (Exception e) { showError("No se pudieron cargar los géneros.\n\n" + e.getMessage()); }
    }

    @FXML
    private void onAgregar() {
        try {
            Genero g = bo.crear(txtId.getText(), txtNombre.getText());
            datos.add(0, g);
            limpiar();
            showInfo("Género agregado.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    private void onModificar() {
        Genero sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un registro."); return; }
        try {
            bo.modificar(sel.getIdGenero(), txtNombre.getText());
            sel.setNombre(txtNombre.getText());
            tabla.refresh();
            showInfo("Género actualizado.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        Genero sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un registro."); return; }
        try {
            bo.eliminar(sel.getIdGenero());
            datos.remove(sel);
            limpiar();
            showInfo("Género eliminado.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    private void onCancelar() { limpiar(); }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Stage st = new Stage();
            st.setTitle("Menú Principal");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();
            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo regresar al menú.\n\n" + e.getMessage());
        }
    }

    private void limpiar() {
        txtId.clear();
        txtNombre.clear();
        tabla.getSelectionModel().clearSelection();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
