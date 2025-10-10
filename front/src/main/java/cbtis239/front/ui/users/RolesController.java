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

    // --------- UI (coincidir con RolesView.fxml) ---------
    @FXML private TextField txtId;            // solo lectura
    @FXML private TextField txtNombre;
    @FXML private TextArea  txtDescripcion;

    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, Number> colId;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, String> colDescripcion;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;

    // --------- Estado / Servicios ---------
    private final RolBO rolBO = new RolBO();
    private final ObservableList<Rol> data = FXCollections.observableArrayList();
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private Integer selectedId = null;

    // =====================================================
    // init
    // =====================================================
    @FXML
    private void initialize() {
        // columnas
        colId.setCellValueFactory(c -> Bindings.createIntegerBinding(c.getValue()::getIdRol));
        colNombre.setCellValueFactory(c -> Bindings.createStringBinding(c.getValue()::getNombre));
        colDescripcion.setCellValueFactory(c -> Bindings.createStringBinding(() -> {
            String d = c.getValue().getDescripcion();
            return d == null ? "" : d;
        }));

        // datos
        reloadTable();

        // ordenar con encabezados
        SortedList<Rol> sorted = new SortedList<>(data);
        sorted.comparatorProperty().bind(tblRoles.comparatorProperty());
        tblRoles.setItems(sorted);

        // bindings de botones (sin setDisable manual)
        btnEliminar.disableProperty().bind(tblRoles.getSelectionModel().selectedItemProperty().isNull());
        btnGuardar.disableProperty().bind(editing);           // Guardar solo en modo crear
        btnActualizar.disableProperty().bind(editing.not());  // Actualizar solo en modo edición

        // doble clic para editar
        tblRoles.setOnMouseClicked(this::onTableDoubleClick);

        // estado inicial
        setCreateMode();
        limpiarForm();
    }

    // =====================================================
    // Acciones UI
    // =====================================================
    @FXML
    private void onGuardar() {
        // crear
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
            error("Error", "Ocurrió un error al guardar el rol.", ex);
        }
    }

    @FXML
    private void onActualizar() {
        // actualizar
        if (selectedId == null) return;
        try {
            Rol r = new Rol();
            r.setIdRol(selectedId);
            r.setNombre(safe(txtNombre.getText()));
            r.setDescripcion(safe(txtDescripcion.getText()));

            rolBO.update(r);
            info("Rol actualizado", "Se actualizó el rol con ID: " + selectedId);

            reloadTable();
            setCreateMode();
            limpiarForm();
        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "Ocurrió un error al actualizar el rol.", ex);
        }
    }

    @FXML
    private void onEliminar() {
        Rol sel = tblRoles.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Optional<ButtonType> resp = confirm(
                "Eliminar rol",
                "¿Seguro que deseas eliminar el rol \"" + sel.getNombre() + "\" (ID " + sel.getIdRol() + ")?"
        );
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            try {
                rolBO.delete(sel.getIdRol());
                reloadTable();
                setCreateMode();
                limpiarForm();
                info("Eliminado", "Rol eliminado correctamente.");
            } catch (BusinessException be) {
                warn("No se puede eliminar", be.getMessage());
            } catch (Exception ex) {
                error("Error", "Ocurrió un error al eliminar el rol.", ex);
            }
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarForm();
        setCreateMode();
    }

    // Volver al menú principal
    @FXML
    private void onVolverMenu() {
        try {
            Stage current = (Stage) btnVolver.getScene().getWindow();
            current.close();

            var url = getClass().getResource("/cbtis239/front/views/menu.fxml");
            if (url == null) throw new IllegalStateException("No se encontró /cbtis239/front/views/menu.fxml");
            Parent root = FXMLLoader.load(url);

            Stage menu = new Stage();
            menu.setTitle("Menú Principal");
            menu.setScene(new Scene(root, 900, 600));
            menu.setMaximized(true);
            menu.show();

        } catch (Exception e) {
            e.printStackTrace();
            error("Error al volver al menú", "No se pudo cargar el menú.", e);
        }
    }

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
    // Helpers
    // =====================================================
    private void reloadTable() {
        data.setAll(rolBO.findAll());
    }

    private void cargarEnFormulario(Rol r) {
        txtId.setText(String.valueOf(r.getIdRol()));
        txtNombre.setText(r.getNombre());
        txtDescripcion.setText(r.getDescripcion() == null ? "" : r.getDescripcion());
    }

    private void limpiarForm() {
        txtId.clear();
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

    // =====================================================
    // Alerts
    // =====================================================
    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void error(String title, String msg, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(msg);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }

    private Optional<ButtonType> confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait();
    }
}
