package cbtis239.front.ui.users;

import cbtis239.bo.ReticulaBO;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import cbtis239.model.Reticula;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReticulaController {

    @FXML private ComboBox<Opcion>    cmbEspecialidad;
    @FXML private ComboBox<OpcionStr> cmbMateria;
    @FXML private ComboBox<Integer>   cmbSemestre;

    @FXML private TableView<Reticula> tblReticula;
    @FXML private TableColumn<Reticula, String>  colEspecialidad;
    @FXML private TableColumn<Reticula, Number>  colSemestre;
    @FXML private TableColumn<Reticula, String>  colMateria;

    private final ReticulaBO bo = new ReticulaBO();
    private final ObservableList<Reticula> data = FXCollections.observableArrayList();

    private boolean suspendUI = false;

    @FXML
    private void initialize() {
        // Configurar columnas
        colEspecialidad.setCellValueFactory(c -> c.getValue().especialidadNombreProperty());
        colSemestre.setCellValueFactory(c -> c.getValue().semestreProperty());
        colMateria.setCellValueFactory(c -> c.getValue().materiaNombreProperty());

        cargarCombos();
        configurarRenderers();

        tblReticula.setItems(data);
        recargarTabla();

        // Listeners protegidos
        cmbEspecialidad.valueProperty().addListener((o, a, n) -> { if (!suspendUI) aplicarFiltro(); });
        cmbSemestre.valueProperty().addListener((o, a, n) -> { if (!suspendUI) aplicarFiltro(); });

        // Selección de fila → autollenado
        tblReticula.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            if (n == null) return;
            suspendUI = true;
            try {
                seleccionarEspecialidad(n.getEspecialidadClave());
                seleccionarMateria(n.getMateriaClave());

                if (!cmbSemestre.getItems().contains(n.getSemestre())) {
                    List<Integer> sems = new ArrayList<>(cmbSemestre.getItems());
                    sems.add(n.getSemestre());
                    cmbSemestre.setItems(FXCollections.observableArrayList(sems));
                }
                cmbSemestre.getSelectionModel().select(Integer.valueOf(n.getSemestre()));
            } finally {
                suspendUI = false;
            }
        });
    }

    private void cargarCombos() {
        try {
            cmbEspecialidad.setItems(FXCollections.observableArrayList(bo.listarEspecialidades()));
            cmbMateria.setItems(FXCollections.observableArrayList(bo.listarMaterias()));

            List<Integer> sems = new ArrayList<>();
            sems.add(null); // opción “Todos”
            for (int i = 1; i <= 6; i++) sems.add(i);
            cmbSemestre.setItems(FXCollections.observableArrayList(sems));
            cmbSemestre.getSelectionModel().select(null);
        } catch (SQLException e) {
            error("No se pudieron cargar catálogos", e.getMessage());
        }
    }

    private void configurarRenderers() {
        setupComboOpcion(cmbEspecialidad);
        setupComboOpcionStr(cmbMateria);

        // Combo Semestre con “Todos”
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
    }

    private void seleccionarEspecialidad(int clave) {
        for (Opcion o : cmbEspecialidad.getItems())
            if (o.getId() == clave) { cmbEspecialidad.getSelectionModel().select(o); return; }
    }
    private void seleccionarMateria(String clave) {
        for (OpcionStr o : cmbMateria.getItems())
            if (o.getId().equals(clave)) { cmbMateria.getSelectionModel().select(o); return; }
    }

    private void aplicarFiltro() {
        Opcion esp = cmbEspecialidad.getValue();
        Integer sem = cmbSemestre.getValue();
        try {
            if (esp == null) {
                data.setAll(bo.listarTodo());
            } else if (sem == null) {
                data.setAll(bo.listarPorEspecialidad(esp.getId()));
            } else {
                data.setAll(bo.listarPorEspecialidadYSemestre(esp.getId(), sem));
            }
            tblReticula.refresh();
        } catch (SQLException e) {
            error("Error al filtrar", e.getMessage());
        }
    }

    private void recargarTabla() {
        try {
            data.setAll(bo.listarTodo());
            tblReticula.refresh();
        } catch (SQLException e) {
            error("Error al cargar retícula", e.getMessage());
        }
    }

    // ---- Botones ----
    @FXML
    private void onAsignar() {
        Opcion esp = cmbEspecialidad.getValue();
        OpcionStr mat = cmbMateria.getValue();
        Integer sem = cmbSemestre.getValue();
        try {
            if (esp == null || mat == null || sem == null)
                throw new IllegalArgumentException("Selecciona especialidad, materia y semestre (no 'Todos').");
            if (sem < 1 || sem > 12) throw new IllegalArgumentException("Semestre inválido.");

            bo.asignar(esp.getId(), mat.getId(), sem);
            aplicarFiltro();
            info("Materia asignada a la especialidad correctamente.");
        } catch (IllegalArgumentException iae) {
            warn("Validación", iae.getMessage());
        } catch (SQLException e) {
            error("Error de base de datos", e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Reticula sel = tblReticula.getSelectionModel().getSelectedItem();
        try {
            int esp; String mat;
            if (sel != null) {
                esp = sel.getEspecialidadClave();
                mat = sel.getMateriaClave();
            } else {
                Opcion e = cmbEspecialidad.getValue();
                OpcionStr m = cmbMateria.getValue();
                if (e == null || m == null)
                    throw new IllegalArgumentException("Selecciona una fila o ambos combos (esp+materia).");
                esp = e.getId(); mat = m.getId();
            }
            bo.eliminar(esp, mat);
            aplicarFiltro();
            info("Relación eliminada correctamente.");
        } catch (IllegalArgumentException iae) {
            warn("Validación", iae.getMessage());
        } catch (SQLException e) {
            error("Error al eliminar", e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            var url = getClass().getResource("/cbtis239/front/views/Menu2.fxml");
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

    // ---- Renderers para ComboBoxes ----
    private void setupComboOpcion(ComboBox<Opcion> cb) {
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Opcion it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Opcion it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });
    }

    private void setupComboOpcionStr(ComboBox<OpcionStr> cb) {
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(OpcionStr it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(OpcionStr it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? "" : it.getNombre());
            }
        });
    }

    // ======================================================
    // === Métodos de Alertas personalizados (tu versión) ===
    // ======================================================

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
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void error(String msg, String ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setContentText(msg + (ex != null ? "\n\nDetalle: " + ex : ""));
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }
}
