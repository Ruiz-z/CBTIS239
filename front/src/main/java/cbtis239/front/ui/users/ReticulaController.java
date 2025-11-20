package cbtis239.front.ui.users;

import cbtis239.bo.ReticulaBO;
import cbtis239.model.MateriaSelectable;
import cbtis239.model.Opcion;
import cbtis239.model.ReticulaAsignadaRow;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReticulaController {

    @FXML private ComboBox<Opcion>  cmbEspecialidad;
    @FXML private ComboBox<Integer> cmbSemestre; // null = Todos

    @FXML private TableView<MateriaSelectable> tblDisponibles;
    @FXML private TableColumn<MateriaSelectable, Boolean> colDispSel;
    @FXML private TableColumn<MateriaSelectable, String>  colDispClave;
    @FXML private TableColumn<MateriaSelectable, String>  colDispNombre;

    @FXML private TableView<ReticulaAsignadaRow> tblAsignadas;
    @FXML private TableColumn<ReticulaAsignadaRow, Boolean> colAsigSel;
    @FXML private TableColumn<ReticulaAsignadaRow, String>  colAsigClave;
    @FXML private TableColumn<ReticulaAsignadaRow, String>  colAsigNombre;
    @FXML private TableColumn<ReticulaAsignadaRow, Number>  colAsigSem;

    private final ReticulaBO bo = new ReticulaBO();

    private final ObservableList<MateriaSelectable> dispData = FXCollections.observableArrayList();
    private final ObservableList<ReticulaAsignadaRow> asigData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarCombos();
        configurarTablas();   // <- aquí vive la magia del checkbox
        cargarCatalogos();
    }

    // ---------- Combos ----------
    private void configurarCombos() {
        // Especialidad
        cmbEspecialidad.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Opcion it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });
        cmbEspecialidad.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Opcion it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });

        // Semestre con “Todos”
        StringConverter<Integer> conv = new StringConverter<>() {
            @Override public String toString(Integer val) { return val == null ? "Todos" : String.valueOf(val); }
            @Override public Integer fromString(String s) {
                if (s == null || s.isBlank() || s.equalsIgnoreCase("Todos")) return null;
                return Integer.valueOf(s.trim());
            }
        };
        cmbSemestre.setConverter(conv);
        cmbSemestre.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Integer it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty ? "" : conv.toString(it));
            }
        });
        cmbSemestre.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Integer it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty ? "" : conv.toString(it));
            }
        });

        // Filtros
        cmbEspecialidad.valueProperty().addListener((o,a,n) -> recargarListas());
        cmbSemestre.valueProperty().addListener((o,a,n) -> recargarAsignadas());
    }

    // ---------- Tablas / Checkboxes ----------
    private void configurarTablas() {
        // Hacer las tablas editables para que el CheckBoxTableCell reciba los clics
        tblDisponibles.setEditable(true);
        tblAsignadas.setEditable(true);

        // DISPO: usar factory con índice -> devuelve el BooleanProperty correcto de esa fila
        colDispSel.setEditable(true);
        colDispSel.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        colDispSel.setCellFactory(CheckBoxTableCell.forTableColumn(index ->
                dispData.get(index).selectedProperty()
        ));
        colDispClave.setCellValueFactory(p -> p.getValue().claveProperty());
        colDispNombre.setCellValueFactory(p -> p.getValue().nombreProperty());
        tblDisponibles.setItems(dispData);

        // ASIG: igual que arriba
        colAsigSel.setEditable(true);
        colAsigSel.setCellValueFactory(cd -> cd.getValue().selectedProperty());
        colAsigSel.setCellFactory(CheckBoxTableCell.forTableColumn(index ->
                asigData.get(index).selectedProperty()
        ));
        colAsigClave.setCellValueFactory(p -> p.getValue().materiaClaveProperty());
        colAsigNombre.setCellValueFactory(p -> p.getValue().materiaNombreProperty());
        colAsigSem.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().semestreProperty().get()));
        tblAsignadas.setItems(asigData);
    }

    // ---------- Datos ----------
    private void cargarCatalogos() {
        try {
            cmbEspecialidad.setItems(FXCollections.observableArrayList(bo.listarEspecialidades()));
            List<Integer> sems = new ArrayList<>();
            sems.add(null); for (int i=1; i<=6; i++) sems.add(i);
            cmbSemestre.setItems(FXCollections.observableArrayList(sems));
            cmbSemestre.getSelectionModel().select(null);
        } catch (SQLException e) {
            error("No se pudieron cargar catálogos", e.getMessage());
        }
    }

    private void recargarListas() {
        Opcion esp = cmbEspecialidad.getValue();
        if (esp == null) { dispData.clear(); asigData.clear(); return; }
        try {
            dispData.setAll(bo.listarDisponibles(esp.getId()));
            recargarAsignadas();
        } catch (SQLException e) {
            error("Error al cargar materias disponibles", e.getMessage());
        }
    }

    private void recargarAsignadas() {
        Opcion esp = cmbEspecialidad.getValue();
        if (esp == null) { asigData.clear(); return; }
        try {
            Integer sem = cmbSemestre.getValue(); // null = Todos
            asigData.setAll(bo.listarAsignadas(esp.getId(), sem));
        } catch (SQLException e) {
            error("Error al cargar materias asignadas", e.getMessage());
        }
    }

    // ---------- Acciones ----------
    @FXML
    private void onAgregarSeleccionadas() {
        Opcion esp = cmbEspecialidad.getValue();
        Integer sem = cmbSemestre.getValue();
        if (esp == null) { warn("Validación", "Selecciona una especialidad."); return; }
        if (sem == null) { warn("Validación", "Para agregar, selecciona un semestre específico (no 'Todos')."); return; }
        if (sem < 1 || sem > 12) { warn("Validación", "Semestre inválido."); return; }

        List<String> claves = dispData.stream()
                .filter(MateriaSelectable::isSelected)
                .map(MateriaSelectable::getClave)
                .collect(Collectors.toList());
        if (claves.isEmpty()) { warn("Validación", "No hay materias seleccionadas para agregar."); return; }

        try {
            bo.insertarMuchas(esp.getId(), claves, sem);
            info("Materias agregadas correctamente.");
            recargarListas();
        } catch (SQLException e) {
            error("No fue posible agregar las materias", e.getMessage());
        }
    }

    @FXML
    private void onQuitarSeleccionadas() {
        Opcion esp = cmbEspecialidad.getValue();
        if (esp == null) { warn("Validación", "Selecciona una especialidad."); return; }

        List<String> claves = asigData.stream()
                .filter(ReticulaAsignadaRow::isSelected)
                .map(ReticulaAsignadaRow::getMateriaClave)
                .collect(Collectors.toList());
        if (claves.isEmpty()) { warn("Validación", "No hay materias seleccionadas para quitar."); return; }

        try {
            bo.eliminarMuchas(esp.getId(), claves);
            info("Materias quitadas correctamente.");
            recargarListas();
        } catch (SQLException e) {
            error("No fue posible quitar las materias", e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            var url = getClass().getResource("/cbtis239/front/views/Menu3.fxml");
            if (url == null) throw new IllegalStateException("No se encontró el menú.");
            Parent root = FXMLLoader.load(url);
            Stage st = new Stage();
            st.setTitle("Menú Principal");
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setFullScreen(true);
            st.setFullScreenExitHint("");
            st.show();
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            error("No se pudo abrir el menú", e.getMessage());
        }
    }

    // ====== Alertas personalizadas (tus métodos) ======
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

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Advertencia");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) { a.initOwner(owner); a.initModality(javafx.stage.Modality.WINDOW_MODAL); }
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) { a.initOwner(owner); a.initModality(javafx.stage.Modality.WINDOW_MODAL); }
        a.showAndWait();
    }

    private void error(String msg, String ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setContentText(msg + (ex != null ? "\n\nDetalle: " + ex : ""));
        Stage owner = getStage();
        if (owner != null) { a.initOwner(owner); a.initModality(javafx.stage.Modality.WINDOW_MODAL); }
        a.showAndWait();
    }
}
