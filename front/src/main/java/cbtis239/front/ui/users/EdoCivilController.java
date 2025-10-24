package cbtis239.front.ui.users;

import cbtis239.bo.EdoCivilBO;
import cbtis239.bo.EdoCivilBO.BusinessException;
import cbtis239.model.EdoCivil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EdoCivilController {

    @FXML private TextField txtNombre;

    @FXML private TableView<EdoCivil> tabla;
    @FXML private TableColumn<EdoCivil, String> colNombre;

    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnVolverMenu;

    private final EdoCivilBO bo = new EdoCivilBO();
    private final ObservableList<EdoCivil> datos = FXCollections.observableArrayList();
    private Integer editingId = null;

    @FXML
    public void initialize() {
        // Columna Nombre
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));

        // Listener de selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
                editingId = sel.getIdEdoCivil();

                btnModificar.setDisable(false);
                btnEliminar.setDisable(false);
                btnAgregar.setDisable(true);
            } else {
                editingId = null;
                btnModificar.setDisable(true);
                btnEliminar.setDisable(true);
                btnAgregar.setDisable(false);
            }
        });

        reloadTable();
        limpiar();
    }

    private void reloadTable() {
        try {
            datos.setAll(bo.findAll());
            tabla.setItems(datos);
        } catch (BusinessException e) {
            showError("Error al cargar datos: " + e.getMessage());
        }
    }

    @FXML
    private void onAgregar() {
        try {
            String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                showError("El nombre no puede estar vacío.");
                return;
            }

            EdoCivil nuevo = new EdoCivil();
            nuevo.setNombre(nombre);

            int newId = bo.agregar(nuevo);

            reloadTable();
            limpiar();
            showInfo("Estado civil guardado con ID: " + newId);
        } catch (BusinessException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Error desconocido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onModificar() {
        if (editingId == null || editingId <= 0) {
            showError("Debe seleccionar un registro de la tabla para modificar.");
            return;
        }
        try {
            String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                showError("El nombre no puede estar vacío.");
                return;
            }

            EdoCivil modificado = new EdoCivil();
            modificado.setIdEdoCivil(editingId);
            modificado.setNombre(nombre);

            bo.modificar(modificado);

            reloadTable();
            limpiar();
            showInfo("Estado civil modificado.");
        } catch (BusinessException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Error desconocido al modificar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEliminar() {
        EdoCivil sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Selecciona un registro para eliminar.");
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar el estado civil '" + sel.getNombre() + "'?",
                ButtonType.YES, ButtonType.NO
        );
        confirmation.setHeaderText("Confirmar eliminación");
        // === Ajuste: owner y modalidad para que no cierre/oculte la pantalla ===
        confirmation.initOwner(getStage());
        confirmation.initModality(javafx.stage.Modality.WINDOW_MODAL);

        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                bo.eliminar(sel.getIdEdoCivil());
                reloadTable();
                limpiar();
                showInfo("Estado civil eliminado.");
            } catch (BusinessException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Error desconocido al eliminar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        tabla.getSelectionModel().clearSelection();
        editingId = null;
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        btnAgregar.setDisable(false);
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo volver al Menú\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            // === Ajuste: owner y modalidad para que no cierre/oculte la pantalla ===
            alert.initOwner(getStage());
            alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
            alert.showAndWait();
        }
    }

    private Stage getStage() {
        return (Stage) tabla.getScene().getWindow();
    }

    private void showError(String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    private void showInfo(String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

}
