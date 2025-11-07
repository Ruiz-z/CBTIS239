package cbtis239.front.ui.users;

import cbtis239.bo.AlumnoGrupoBO;
import cbtis239.dao.HorarioGruposDao;
import cbtis239.model.Opcion;
import cbtis239.util.DB;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controller para mover un alumno a otro grupo y sincronizar sus calificaciones (sin triggers).
 * - Selecciona Especialidad -> carga Grupos
 * - Ingresa Matrícula -> muestra grupo actual (si existe)
 * - Botón "Cambiar de Grupo" -> ejecuta AlumnoGrupoBO.cambiarGrupoYSincronizar(...)
 */
public class AlumnoGrupoController {

    @FXML private TextField txtMatricula;
    @FXML private ComboBox<Opcion> cmbEspecialidad;
    @FXML private ComboBox<Opcion> cmbGrupo;
    @FXML private Label lblGrupoActual;

    @FXML private Button btnCambiar;
    @FXML private Button btnVolver;

    private final HorarioGruposDao catDao = new HorarioGruposDao();
    private final AlumnoGrupoBO     bo    = new AlumnoGrupoBO();

    @FXML
    private void initialize() {
        // Llenar especialidades
        try {
            cmbEspecialidad.setItems(FXCollections.observableArrayList(catDao.listarEspecialidades()));
            if (!cmbEspecialidad.getItems().isEmpty()) {
                cmbEspecialidad.getSelectionModel().selectFirst();
                cargarGruposPorEspecialidad();
            }
        } catch (Exception e) {
            error("No se pudieron cargar las especialidades.\n" + e.getMessage());
        }

        // Cuando cambie la especialidad, recargar grupos
        cmbEspecialidad.valueProperty().addListener((o, a, n) -> cargarGruposPorEspecialidad());

        // Cuando escriba la matrícula, intenta mostrar grupo actual
        txtMatricula.textProperty().addListener((o, a, n) -> mostrarGrupoActual());
    }

    private void cargarGruposPorEspecialidad() {
        var esp = cmbEspecialidad.getValue();
        if (esp == null) {
            cmbGrupo.getItems().clear();
            return;
        }
        try {
            cmbGrupo.setItems(FXCollections.observableArrayList(
                    catDao.listarGruposPorEspecialidad(esp.getId())
            ));
            if (!cmbGrupo.getItems().isEmpty()) {
                cmbGrupo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            error("No se pudieron cargar los grupos de la especialidad.\n" + e.getMessage());
        }
    }

    private void mostrarGrupoActual() {
        String m = txtMatricula.getText() == null ? "" : txtMatricula.getText().trim();
        if (m.isEmpty()) {
            lblGrupoActual.setText("-");
            return;
        }
        try {
            String nombre = consultarNombreGrupoActual(m);
            lblGrupoActual.setText(nombre == null ? "(no encontrado)" : nombre);
        } catch (Exception e) {
            lblGrupoActual.setText("(error)");
        }
    }

    /** Consulta sencilla para mostrar el nombre del grupo actual del alumno (si existe). */
    private String consultarNombreGrupoActual(String matricula) throws Exception {
        String sql = """
            SELECT g.NombreGrupo
            FROM SistemaEscolar.Alumno a
            JOIN SistemaEscolar.Grupo g ON g.GrupoID = a.GrupoID
            WHERE a.Matricula = ?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("NombreGrupo");
                return null;
            }
        }
    }

    // ================= Acciones UI =================

    @FXML
    private void onCambiarGrupo() {
        String matricula = txtMatricula.getText() == null ? "" : txtMatricula.getText().trim();
        if (matricula.isEmpty()) {
            warn("Validación", "Ingresa la matrícula del alumno.");
            return;
        }
        Opcion selGrupo = cmbGrupo.getValue();
        if (selGrupo == null) {
            warn("Validación", "Selecciona un grupo de destino.");
            return;
        }

        try {
            boolean ok = bo.cambiarGrupoYSincronizar(matricula, selGrupo.getId());
            if (ok) {
                info("Grupo actualizado y calificaciones sincronizadas.");
                mostrarGrupoActual(); // refrescar etiqueta
            } else {
                warn("Alumno no encontrado", "Verifica que la matrícula exista.");
            }
        } catch (Exception e) {
            error("Ocurrió un error al cambiar de grupo.\n" + e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu() {
        try {
            var url = getClass().getResource("/cbtis239/front/views/Menu2.fxml");
            if (url == null) throw new IllegalStateException("No se encontró /cbtis239/front/views/Menu2.fxml");
            Parent root = FXMLLoader.load(url);
            Stage st = new Stage();
            st.setTitle("Menú Principal");
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setFullScreen(true);
            st.setFullScreenExitHint("");
            st.show();
            ((Stage) btnVolver.getScene().getWindow()).close();
        } catch (Exception e) {
            error("No se pudo abrir el menú.\n" + e.getMessage());
        }
    }

    // ================== Helpers de Alertas ==================
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

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }
}
