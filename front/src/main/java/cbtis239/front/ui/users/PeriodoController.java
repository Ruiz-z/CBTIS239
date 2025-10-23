package cbtis239.front.ui.users;

import cbtis239.bo.BusinessException;
import cbtis239.bo.PeriodoBo;
import cbtis239.model.Periodo;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PeriodoController {

    // Ya corregido el FXML, solo declaramos los elementos restantes
    @FXML private TextField txtNombre;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;

    @FXML private TableView<Periodo> tablaPeriodos;
    @FXML private TableColumn<Periodo, String> colNombre;
    @FXML private TableColumn<Periodo, String> colInicio;
    @FXML private TableColumn<Periodo, String> colFin;

    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    
    // --------- Estado / Servicios ---------
private final PeriodoBo periodoBO = new PeriodoBo(); // <--- NUEVO
    private final ObservableList<Periodo> data = FXCollections.observableArrayList();
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private Integer selectedId = null; 
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @FXML
    private void initialize() {
        // 1. Configurar Columnas (ColId eliminado)
        colNombre.setCellValueFactory(c -> Bindings.createStringBinding(c.getValue()::getNombre));
        colInicio.setCellValueFactory(c -> Bindings.createStringBinding(() -> {
            return c.getValue().getFechaInicio() != null ? c.getValue().getFechaInicio().format(DATE_FORMAT) : "";
        }));
        colFin.setCellValueFactory(c -> Bindings.createStringBinding(() -> {
            return c.getValue().getFechaFin() != null ? c.getValue().getFechaFin().format(DATE_FORMAT) : "";
        }));

        // 2. Cargar datos
        reloadTable();

        // 3. Ordenar y establecer elementos en la tabla
        SortedList<Periodo> sorted = new SortedList<>(data);
        sorted.comparatorProperty().bind(tablaPeriodos.comparatorProperty());
        tablaPeriodos.setItems(sorted);
        
        // 4. Bindings de botones y handlers
        btnEliminar.disableProperty().bind(tablaPeriodos.getSelectionModel().selectedItemProperty().isNull());
        btnAgregar.disableProperty().bind(editing);
        btnModificar.disableProperty().bind(editing.not());

        tablaPeriodos.setOnMouseClicked(this::onTableDoubleClick);

        // 5. Estado inicial
        setCreateMode();
        onCancelar(); 
    }
    
    // =====================================================
    // Acciones UI (Implementadas)
    // =====================================================

    @FXML
    private void onAgregar() {
        try {
            Periodo p = new Periodo();
            p.setNombre(txtNombre.getText());
            p.setFechaInicio(dpInicio.getValue());
            p.setFechaFin(dpFin.getValue());

            long id = periodoBO.create(p); // <--- Llama a la lógica de negocio (INSERT)
            showInfo("Periodo creado", "Se creó el periodo con ID: " + id);

            reloadTable();
            setCreateMode();
            onCancelar();
        } catch (BusinessException be) {
            showWarning("Validación", be.getMessage());
        } catch (Exception ex) {
            showError("Error al guardar", "Ocurrió un error al guardar el periodo.", ex);
        }
    }

    @FXML
    private void onModificar() {
        if (selectedId == null) return;
        try {
            Periodo p = new Periodo();
            p.setIdPeriodo(selectedId);
            p.setNombre(txtNombre.getText());
            p.setFechaInicio(dpInicio.getValue());
            p.setFechaFin(dpFin.getValue());

            periodoBO.update(p); // <--- Llama a la lógica de negocio (UPDATE)
            showInfo("Periodo actualizado", "Se actualizó el periodo con ID: " + selectedId);

            reloadTable();
            setCreateMode();
            onCancelar();
        } catch (BusinessException be) {
            showWarning("Validación", be.getMessage());
        } catch (Exception ex) {
            showError("Error al actualizar", "Ocurrió un error al actualizar el periodo.", ex);
        }
    }

    @FXML
    private void onEliminar() {
        Periodo sel = tablaPeriodos.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Optional<ButtonType> resp = showConfirm(
                "Eliminar Periodo",
                "¿Seguro que deseas eliminar el periodo \"" + sel.getNombre() + "\"?"
        );
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            try {
                periodoBO.delete(sel.getIdPeriodo()); // <--- Llama a la lógica de negocio (DELETE)
                reloadTable();
                setCreateMode();
                onCancelar();
                showInfo("Eliminado", "Periodo eliminado correctamente.");
            } catch (BusinessException be) {
                showWarning("No se puede eliminar", be.getMessage());
            } catch (Exception ex) {
                showError("Error al eliminar", "Ocurrió un error al eliminar el periodo.", ex);
            }
        }
    }

    @FXML
    private void onCancelar() {
        txtNombre.clear();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        tablaPeriodos.getSelectionModel().clearSelection();
        setCreateMode();
    }
    
    @FXML
    private void onVolver(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu2.fxml")); 
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
    
    private void onTableDoubleClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Periodo sel = tablaPeriodos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                cargarEnFormulario(sel);
                setEditMode(sel.getIdPeriodo());
            }
        }
    }
    
    // =====================================================
    // Helpers
    // =====================================================
    private void reloadTable() {
        data.setAll(periodoBO.findAll());
    }
    
    private void cargarEnFormulario(Periodo p) {
        txtNombre.setText(p.getNombre());
        dpInicio.setValue(p.getFechaInicio());
        dpFin.setValue(p.getFechaFin());
    }
    
    private void setCreateMode() {
        selectedId = null;
        editing.set(false);
    }

    private void setEditMode(int id) {
        selectedId = id;
        editing.set(true);
    }
    
    // Métodos de alerta...
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    
    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    
    private void showError(String title, String msg, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(msg); a.setContentText(ex.getMessage()); a.showAndWait();
    }
    
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }
    
    private Optional<ButtonType> showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait();
    }
}