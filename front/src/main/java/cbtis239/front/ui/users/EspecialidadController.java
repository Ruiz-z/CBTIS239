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

        // ===== BLOQUEA LA CLAVE AL SELECCIONAR UNA FILA =====
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                txtClave.setText(String.valueOf(sel.getClave()));
                txtNombre.setText(sel.getNombre());
                txtClave.setDisable(true);      // 🔒 BLOQUEAR CLAVE
            }
        });

        cargar();
    }

    private void cargar() {
        try {
            datos.setAll(bo.listar());
        } catch (Exception e) {
            showError("No se pudieron cargar especialidades\n\n" + e.getMessage());
        }
    }

    // ==================================================
    // AGREGAR — LIMPIA CAMPOS Y REFRESCA
    // ==================================================
    @FXML
    private void onAgregar() {
        try {
            Especialidad e = bo.crear(txtClave.getText(), txtNombre.getText());
            datos.add(0, e);
            limpiar();
            showInfo("Especialidad agregada correctamente.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ==================================================
    // MODIFICAR — ACTUALIZA TABLA Y LIMPIA CAMPOS
    // ==================================================
    @FXML
    private void onModificar() {
        Especialidad sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Selecciona una especialidad de la tabla.");
            return;
        }

        try {
            int clave = sel.getClave();
            bo.modificar(clave, txtNombre.getText());

            sel.setNombre(txtNombre.getText());
            tabla.refresh();

            limpiar();
            showInfo("Especialidad actualizada.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ==================================================
    // ELIMINAR — ELIMINA DE BD, TABLA Y LIMPIA CAMPOS
    // ==================================================
    @FXML
    private void onEliminar() {
        Especialidad sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Selecciona una especialidad de la tabla.");
            return;
        }
        try {
            bo.eliminar(sel.getClave());
            datos.remove(sel);
            limpiar();
            showInfo("Especialidad eliminada.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu3.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo volver al Menú\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.initOwner(getStage());
            alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
            alert.showAndWait();
        }
    }

    // ==================================================
    // LIMPIAR — AHORA REHABILITA LA CLAVE
    // ==================================================
    private void limpiar() {
        txtClave.clear();
        txtNombre.clear();
        txtClave.setDisable(false);   // 🔓 REACTIVAR CLAVE
        tabla.getSelectionModel().clearSelection();
    }

    private Stage getStage() {
        return (Stage) tabla.getScene().getWindow();
    }

    private void showError(String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText("Error");
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    private void showInfo(String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.setHeaderText(c);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

}
