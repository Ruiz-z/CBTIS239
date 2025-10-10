package cbtis239.front.ui.users;

import cbtis239.bo.EspecialidadBO;
import cbtis239.model.Especialidad;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EspecialidadController {

    @FXML private TextField txtClave;
    @FXML private TextField txtNombre;

    @FXML private TableView<Especialidad> tabla;
    @FXML private TableColumn<Especialidad, Number> colClave;
    @FXML private TableColumn<Especialidad, String> colNombre;

    private final EspecialidadBO bo = new EspecialidadBO();
    private final ObservableList<Especialidad> datos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colClave.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getClave()));
        colNombre.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));

        tabla.setItems(datos);
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                txtClave.setText(String.valueOf(sel.getClave()));
                txtNombre.setText(sel.getNombre());
            }
        });

        cargar();
    }

    private void cargar() {
        try { datos.setAll(bo.listar()); }
        catch (Exception e) { showError("No se pudieron cargar especialidades\n\n" + e.getMessage()); }
    }

    @FXML
    private void onAgregar() {
        try {
            Especialidad e = bo.crear(txtClave.getText(), txtNombre.getText());
            datos.add(0, e);
            limpiar();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        Especialidad sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona una especialidad de la tabla."); return; }
        try {
            int clave = sel.getClave();
            bo.modificar(clave, txtNombre.getText());
            sel.setNombre(txtNombre.getText());
            tabla.refresh();
            showInfo("Especialidad actualizada.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Especialidad sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona una especialidad de la tabla."); return; }
        try {
            bo.eliminar(sel.getClave());
            datos.remove(sel);
            limpiar();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo volver al menú\n\n" + e.getMessage());
        }
    }

    private void limpiar() { txtClave.clear(); txtNombre.clear(); tabla.getSelectionModel().clearSelection(); }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
}
