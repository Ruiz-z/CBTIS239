package cbtis239.front.ui.users;

import cbtis239.bo.EdoCivilBO;
import cbtis239.model.EdoCivil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EdoCivilController {

    @FXML private TextField txtClave;
    @FXML private TextField txtNombre;

    @FXML private TableView<EdoCivil> tabla;
    @FXML private TableColumn<EdoCivil, Number> colClave;
    @FXML private TableColumn<EdoCivil, String> colNombre;

    private final EdoCivilBO bo = new EdoCivilBO();
    private final ObservableList<EdoCivil> datos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colClave.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdEdoCivil()));
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));

        tabla.setItems(datos);
        tabla.getSelectionModel().selectedItemProperty().addListener((obs,o,sel)->{
            if (sel != null) {
                txtClave.setText(String.valueOf(sel.getIdEdoCivil()));
                txtNombre.setText(sel.getNombre());
            }
        });

        cargar();
    }

    private void cargar() {
        try { datos.setAll(bo.listar()); }
        catch (Exception e) { showError("No se pudieron cargar estados civiles\n\n" + e.getMessage()); }
    }

    @FXML
    private void onAgregar() {
        try {
            EdoCivil ec = bo.crear(txtClave.getText(), txtNombre.getText());
            datos.add(0, ec);
            limpiar();
            showInfo("Estado civil agregado.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        EdoCivil sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un registro."); return; }
        try {
            bo.modificar(sel.getIdEdoCivil(), txtNombre.getText());
            sel.setNombre(txtNombre.getText());
            tabla.refresh();
            showInfo("Estado civil actualizado.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        EdoCivil sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un registro."); return; }
        try {
            bo.eliminar(sel.getIdEdoCivil());
            datos.remove(sel);
            limpiar();
            showInfo("Estado civil eliminado.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

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
            showError("No se pudo regresar al menú\n\n" + e.getMessage());
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
