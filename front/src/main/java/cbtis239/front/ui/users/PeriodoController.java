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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PeriodoController {

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

    private final PeriodoBo periodoBO = new PeriodoBo();
    private final ObservableList<Periodo> data = FXCollections.observableArrayList();
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private Integer selectedId = null;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c ->
                Bindings.createStringBinding(c.getValue()::getNombre));
        colInicio.setCellValueFactory(c ->
                Bindings.createStringBinding(() ->
                        c.getValue().getFechaInicio() != null ? c.getValue().getFechaInicio().format(DATE_FORMAT) : ""));
        colFin.setCellValueFactory(c ->
                Bindings.createStringBinding(() ->
                        c.getValue().getFechaFin() != null ? c.getValue().getFechaFin().format(DATE_FORMAT) : ""));

        reloadTable();

        SortedList<Periodo> sorted = new SortedList<>(data);
        sorted.comparatorProperty().bind(tablaPeriodos.comparatorProperty());
        tablaPeriodos.setItems(sorted);

        btnEliminar.disableProperty().bind(tablaPeriodos.getSelectionModel().selectedItemProperty().isNull());
        btnAgregar.disableProperty().bind(editing);
        btnModificar.disableProperty().bind(editing.not());

        tablaPeriodos.setOnMouseClicked(this::onTableDoubleClick);

        setCreateMode();
        onCancelar();
    }

    // =========================================================
    // AGREGAR
    // =========================================================
    @FXML
    private void onAgregar() {
        try {
            Periodo p = new Periodo();
            p.setNombre(txtNombre.getText());
            p.setFechaInicio(dpInicio.getValue());
            p.setFechaFin(dpFin.getValue());

            // 🔥 VALIDACIÓN DE FECHAS
            validarFechas(p, null);

            long id = periodoBO.create(p);
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

    // =========================================================
    // MODIFICAR
    // =========================================================
    @FXML
    private void onModificar() {
        if (selectedId == null) return;

        try {
            Periodo p = new Periodo();
            p.setIdPeriodo(selectedId);
            p.setNombre(txtNombre.getText());
            p.setFechaInicio(dpInicio.getValue());
            p.setFechaFin(dpFin.getValue());

            // 🔥 VALIDACIÓN DE FECHAS
            validarFechas(p, selectedId);

            periodoBO.update(p);

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

    // =========================================================
    // ELIMINAR
    // =========================================================
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
                periodoBO.delete(sel.getIdPeriodo());
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
    private void onVolver(javafx.event.ActionEvent event) {
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

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
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

    private void onTableDoubleClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Periodo sel = tablaPeriodos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                cargarEnFormulario(sel);
                setEditMode(sel.getIdPeriodo());
            }
        }
    }

    // =========================================================
    // VALIDACIÓN DE FECHAS (LO QUE PEDISTE)
    // =========================================================
    private void validarFechas(Periodo nuevo, Integer idEditar) throws BusinessException {

        if (nuevo.getFechaInicio() == null || nuevo.getFechaFin() == null) {
            throw new BusinessException("Debes seleccionar fecha de inicio y fin.");
        }

        if (nuevo.getFechaFin().isBefore(nuevo.getFechaInicio())) {
            throw new BusinessException("La fecha fin no puede ser menor que la fecha inicio.");
        }

        for (Periodo p : data) {

            // Ignorar cuando es el mismo periodo (modificación)
            if (idEditar != null && p.getIdPeriodo() == idEditar) {
                continue;
            }

            // ❌ VALIDAR QUE NO SE TRASLAPEN
            boolean traslape =
                    !(nuevo.getFechaFin().isBefore(p.getFechaInicio())
                            || nuevo.getFechaInicio().isAfter(p.getFechaFin()));

            if (traslape) {
                throw new BusinessException(
                        "El periodo se traslapa con el periodo existente:\n" +
                                p.getNombre() + " (" +
                                p.getFechaInicio().format(DATE_FORMAT) + " - " +
                                p.getFechaFin().format(DATE_FORMAT) + ")"
                );
            }
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================
    private void reloadTable() {
        try { data.setAll(periodoBO.findAll()); }
        catch (Exception ex) { showError("Error al cargar", "No se pudieron cargar los periodos.", ex); }
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

    private Stage getStage() {
        javafx.stage.Window w = javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isFocused)
                .findFirst()
                .orElseGet(() ->
                        javafx.stage.Window.getWindows().stream()
                                .filter(javafx.stage.Window::isShowing)
                                .findFirst()
                                .orElse(null));
        return (w instanceof Stage) ? (Stage) w : null;
    }

    private void showError(String title, String msg, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(title);
        a.setContentText(msg + (ex != null ? "\n\nDetalle: " + ex.getMessage() : ""));
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void showWarning(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Advertencia");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private Optional<ButtonType> showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        return a.showAndWait();
    }
}
