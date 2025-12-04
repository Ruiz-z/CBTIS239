package cbtis239.front.ui.users;

import cbtis239.bo.BusinessException;
import cbtis239.bo.RolBO;
import cbtis239.model.Rol;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.Optional;

public class RolesController {

    // --------- UI ---------
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;

    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, String> colDescripcion;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;

    // --------- Estado ---------
    private final RolBO rolBO = new RolBO();
    private final ObservableList<Rol> data = FXCollections.observableArrayList();
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private Integer selectedId = null;

    // =====================================================
    @FXML
    private void initialize() {

        // ====== VALIDACIONES AÑADIDAS ======
        soloLetras(txtNombre, 50);           // NombreRol VARCHAR(50)
        soloLetrasMulti(txtDescripcion, 500); // Descripcion tipo TEXT, límite lógico 500

        // columnas
        colNombre.setCellValueFactory(c ->
                Bindings.createStringBinding(c.getValue()::getNombre));

        colDescripcion.setCellValueFactory(c ->
                Bindings.createStringBinding(() -> {
                    String d = c.getValue().getDescripcion();
                    return d == null ? "" : d;
                })
        );

        reloadTable();

        SortedList<Rol> sorted = new SortedList<>(data);
        sorted.comparatorProperty().bind(tblRoles.comparatorProperty());
        tblRoles.setItems(sorted);

        btnEliminar.disableProperty().bind(tblRoles.getSelectionModel().selectedItemProperty().isNull());
        btnGuardar.disableProperty().bind(editing);
        btnActualizar.disableProperty().bind(editing.not());

        tblRoles.setOnMouseClicked(this::onTableDoubleClick);

        setCreateMode();
        limpiarForm();
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    /**
     * Solo letras y espacios (fortísimo)
     */
    private void soloLetras(TextField txt, int maxLen) {
        txt.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]*")) {
                txt.setText(oldVal);
                return;
            }
            if (newVal.length() > maxLen) {
                txt.setText(oldVal);
            }
        });
    }

    /**
     * Solo letras y espacios en TextArea
     */
    private void soloLetrasMulti(TextArea txt, int maxLen) {
        txt.textProperty().addListener((obs, oldVal, newVal) -> {
            // Solo letras, espacios, saltos de línea, puntos y comas
            if (!newVal.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ .,\\n]*")) {
                txt.setText(oldVal);
                return;
            }
            if (newVal.length() > maxLen) {
                txt.setText(oldVal);
            }
        });
    }


    // =====================================================
    // ACCIONES
    // =====================================================

    @FXML
    private void onGuardar() {
        try {
            Rol r = new Rol();
            r.setNombre(safe(txtNombre.getText()));
            r.setDescripcion(safe(txtDescripcion.getText()));

            long id = rolBO.create(r);
            info("Rol creado", "Se creó el rol con ID: " + id);

            reloadTable();
            setCreateMode();
            limpiarForm();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "Error al guardar.", ex);
        }
    }

    @FXML
    private void onActualizar() {
        if (selectedId == null) return;
        try {
            Rol r = new Rol();
            r.setIdRol(selectedId);
            r.setNombre(safe(txtNombre.getText()));
            r.setDescripcion(safe(txtDescripcion.getText()));

            rolBO.update(r);
            info("Rol actualizado", "Rol ID: " + selectedId);

            reloadTable();
            setCreateMode();
            limpiarForm();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "Error al actualizar.", ex);
        }
    }

    @FXML
    private void onEliminar() {
        Rol sel = tblRoles.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Optional<ButtonType> resp = confirm(
                "Eliminar rol",
                "¿Eliminar el rol \"" + sel.getNombre() + "\"?"
        );

        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            try {
                rolBO.delete(sel.getIdRol());
                reloadTable();
                setCreateMode();
                limpiarForm();
                info("Rol eliminado", "Eliminación correcta.");
            } catch (BusinessException be) {
                warn("No se puede eliminar", be.getMessage());
            } catch (Exception ex) {
                error("Error", "Error al eliminar.", ex);
            }
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarForm();
        setCreateMode();
    }

    @FXML
    private void onVolverMenu() {
        try {
            Stage current = (Stage) btnVolver.getScene().getWindow();
            current.close();

            var url = getClass().getResource("/cbtis239/front/views/Menu.fxml");
            if (url == null)
                throw new IllegalStateException("No se encontró Menu.fxml");

            Parent root = FXMLLoader.load(url);
            Stage menu = new Stage();
            menu.setTitle("Menú Principal");
            menu.setScene(new Scene(root));
            menu.initStyle(javafx.stage.StageStyle.UNDECORATED);
            menu.setFullScreen(true);
            menu.setFullScreenExitHint("");
            menu.show();

        } catch (Exception e) {
            error("Error", "No se pudo abrir el menú", e);
        }
    }

    // =====================================================
    // Helpers
    // =====================================================
    private void reloadTable() { data.setAll(rolBO.findAll()); }

    private void cargarEnFormulario(Rol r) {
        txtNombre.setText(r.getNombre());
        txtDescripcion.setText(r.getDescripcion() == null ? "" : r.getDescripcion());
    }

    private void limpiarForm() {
        txtNombre.clear();
        txtDescripcion.clear();
        tblRoles.getSelectionModel().clearSelection();
    }

    private void setCreateMode() {
        selectedId = null;
        editing.set(false);
    }

    private void setEditMode(int id) {
        selectedId = id;
        editing.set(true);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private void onTableDoubleClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Rol sel = tblRoles.getSelectionModel().getSelectedItem();
            if (sel != null) {
                cargarEnFormulario(sel);
                setEditMode(sel.getIdRol());
            }
        }
    }

    // =====================================================
    // Alerts
    // =====================================================
    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void error(String title, String msg, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
        ex.printStackTrace();
    }

    private Optional<ButtonType> confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(title);
        return a.showAndWait();
    }
}
