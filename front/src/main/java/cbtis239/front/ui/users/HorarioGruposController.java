package cbtis239.front.ui.users;

import cbtis239.bo.AlumnoGrupoBO;
import cbtis239.bo.HorarioGruposBO;
import cbtis239.model.CursoRow;
import cbtis239.model.Opcion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HorarioGruposController {

    @FXML private ComboBox<Opcion> cmbEspecialidad;
    @FXML private ComboBox<Opcion> cmbGrupo;
    @FXML private Label lblSemestre;
    @FXML private FlowPane paneReticula;

    // Seleccionados (inscritos)
    @FXML private TableView<CursoRow> tblSeleccionados;
    @FXML private TableColumn<CursoRow, Boolean> colSelCheck;
    @FXML private TableColumn<CursoRow, String>  colSelMateria, colSelDocente, colSelHorario;

    // Disponibles
    @FXML private TableView<CursoRow> tblDisponibles;
    @FXML private TableColumn<CursoRow, Boolean> colDispCheck;
    @FXML private TableColumn<CursoRow, String>  colDispMateria, colDispDocente, colDispHorario;

    private final HorarioGruposBO bo = new HorarioGruposBO();
    private final AlumnoGrupoBO alumnoGrupoBO = new AlumnoGrupoBO();

    private final ObservableList<CursoRow> dataSel  = FXCollections.observableArrayList();
    private final ObservableList<CursoRow> dataDisp = FXCollections.observableArrayList();

    /** Claves de materias de retícula para la combinación especialidad+semestre actual */
    private Set<String> reticulaClaves = new HashSet<>();

    @FXML
    private void initialize() {
        // Resize policy
        if (tblSeleccionados != null) tblSeleccionados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (tblDisponibles   != null) tblDisponibles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ---- MUY IMPORTANTE: habilitar edición para que el CheckBoxTableCell escriba en la propiedad ----
        tblSeleccionados.setEditable(true);
        tblDisponibles.setEditable(true);

        // ===== SELECCIONADOS =====
        // Columna de checkbox enlazada a la BooleanProperty del row
        colSelCheck.setEditable(true);
        colSelCheck.setSortable(false);
        colSelCheck.setReorderable(false);
        colSelCheck.setResizable(false);
        colSelCheck.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelCheck.setCellFactory(col -> {
            CheckBoxTableCell<CursoRow, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true);
            return cell;
        });

        colSelMateria.setCellValueFactory(c -> c.getValue().materiaProperty());
        colSelDocente.setCellValueFactory(c -> c.getValue().docenteProperty());
        colSelHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        tblSeleccionados.setItems(dataSel);

        // ===== DISPONIBLES =====
        colDispCheck.setEditable(true);
        colDispCheck.setSortable(false);
        colDispCheck.setReorderable(false);
        colDispCheck.setResizable(false);
        colDispCheck.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colDispCheck.setCellFactory(col -> {
            CheckBoxTableCell<CursoRow, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true);
            return cell;
        });

        colDispMateria.setCellValueFactory(c -> c.getValue().materiaProperty());
        colDispDocente.setCellValueFactory(c -> c.getValue().docenteProperty());
        colDispHorario.setCellValueFactory(c -> c.getValue().horarioProperty());
        tblDisponibles.setItems(dataDisp);

        // Cargar catálogos
        cargarEspecialidades();

        // Listeners de cambio
        cmbEspecialidad.valueProperty().addListener((o, a, n) -> {
            if (n != null) {
                cargarGrupos(n.getId());
                limpiarReticula();
                dataSel.clear(); dataDisp.clear();
            }
        });
        cmbGrupo.valueProperty().addListener((o, a, n) -> intentarPintarYLlenar());
        cmbEspecialidad.valueProperty().addListener((o, a, n) -> intentarPintarYLlenar());
    }

    private void intentarPintarYLlenar() {
        Opcion esp = cmbEspecialidad.getValue();
        Opcion grp = cmbGrupo.getValue();
        if (esp == null || grp == null) return;

        try {
            Integer semestre = bo.obtenerSemestreGrupo(grp.getId());
            if (semestre == null) {
                lblSemestre.setText("(sin alumnos)");
                limpiarReticula();
                dataSel.clear(); dataDisp.clear();
                return;
            }
            lblSemestre.setText("#" + semestre);

            // 1) Retícula
            var materias = bo.listarReticulaMaterias(esp.getId(), semestre);
            reticulaClaves = materias.stream().map(m -> m.clave).collect(Collectors.toSet());

            // 2) Ya cubiertas por el grupo
            Set<String> cubiertas = bo.materiasAsignadasGrupo(grp.getId());

            // 3) Pintar retícula
            pintarReticula(materias, cubiertas);

            // 4) Tabla Seleccionados
            dataSel.setAll(bo.cursosSeleccionadosTabla(grp.getId()));

            // 5) Tabla Disponibles (todos los cursos de las materias de retícula)
            dataDisp.setAll(bo.cursosDisponiblesPorMaterias(reticulaClaves));

        } catch (SQLException e) {
            error("Error consultando datos:\n" + e.getMessage());
        }
    }

    private void cargarEspecialidades() {
        try {
            var items = FXCollections.observableArrayList(bo.listarEspecialidades());
            cmbEspecialidad.setItems(items);
            if (!items.isEmpty()) cmbEspecialidad.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            error("No se pudieron cargar especialidades:\n" + e.getMessage());
        }
    }

    private void cargarGrupos(int espClave) {
        try {
            var items = FXCollections.observableArrayList(bo.listarGruposPorEspecialidad(espClave));
            cmbGrupo.setItems(items);
            if (!items.isEmpty()) cmbGrupo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            error("No se pudieron cargar grupos:\n" + e.getMessage());
        }
    }

    private void pintarReticula(List<HorarioGruposBO.MateriaRet> materias, Set<String> cubiertas) {
        paneReticula.getChildren().clear();
        int total = Math.max(materias.size(), 8);
        for (int i = 0; i < total; i++) {
            Label pill = new Label();
            pill.setMinWidth(140);
            pill.setMinHeight(46);
            pill.setWrapText(true);
            pill.setStyle("-fx-alignment:center; -fx-padding:10; -fx-background-radius:10; -fx-border-radius:10;");

            if (i < materias.size()) {
                var mr = materias.get(i);
                pill.setText(mr.nombre);
                boolean cubierta = cubiertas != null && cubiertas.contains(mr.clave);
                pill.setStyle(pill.getStyle() + (cubierta
                        ? "-fx-background-color:#b7f0a5;"    // verde
                        : "-fx-background-color:#ffa3a3;")); // rojo
            } else {
                pill.setText(" ");
                pill.setStyle(pill.getStyle() + "-fx-background-color:#ffffff;"); // blanco
            }
            paneReticula.getChildren().add(pill);
        }
        Region spacer = new Region(); spacer.setPrefWidth(1);
        paneReticula.getChildren().add(spacer);
    }

    private void limpiarReticula() { paneReticula.getChildren().clear(); }

    // ================= Acciones (Agregar / Quitar) =================

    @FXML
    private void onAgregar() {
        Opcion grp = cmbGrupo.getValue();
        if (grp == null) { info("Selecciona un grupo primero."); return; }

        var marcar = dataDisp.stream().filter(CursoRow::isSelected).collect(Collectors.toList());
        if (marcar.isEmpty()) { info("Marca al menos un curso en 'Cursos disponibles'."); return; }

        // 1) No duplicar materia (ya inscritas + dentro del mismo lote)
        Set<String> materiasYa = dataSel.stream().map(CursoRow::getMateria).collect(Collectors.toSet());
        Set<String> loteMaterias = new HashSet<>();
        for (CursoRow r : marcar) {
            if (materiasYa.contains(r.getMateria())) {
                warn("Materia repetida", "Ya existe un curso inscrito de '" + r.getMateria() + "'.");
                return;
            }
            if (!loteMaterias.add(r.getMateria())) {
                warn("Materia repetida", "Seleccionaste más de un curso de '" + r.getMateria() + "'.");
                return;
            }
        }

        // 2) Choques de horario contra YA inscritos
        for (CursoRow nuevo : marcar) {
            for (CursoRow ya : dataSel) {
                if (hayTraslape(nuevo, ya)) {
                    warn("Choque de horario",
                            "El curso '" + nuevo.getMateria() + "' choca con '" + ya.getMateria() + "'.");
                    return;
                }
            }
        }

        // 3) Choques dentro del mismo lote
        for (int i = 0; i < marcar.size(); i++) {
            for (int j = i + 1; j < marcar.size(); j++) {
                if (hayTraslape(marcar.get(i), marcar.get(j))) {
                    warn("Choque dentro de la selección",
                            "Los cursos seleccionados '" + marcar.get(i).getMateria() +
                                    "' y '" + marcar.get(j).getMateria() + "' se traslapan.");
                    return;
                }
            }
        }

        // Insertar (también generará Calificacion para alumnos del grupo)
        List<Integer> ids = marcar.stream().map(CursoRow::getId).collect(Collectors.toList());
        try {
            int ins = bo.agregarCursosGrupo(grp.getId(), ids);
            info("Se agregaron " + ins + " curso(s) al grupo.");
            dataDisp.forEach(r -> r.setSelected(false));
            intentarPintarYLlenar();
        } catch (SQLException e) {
            error("No se pudieron agregar cursos:\n" + e.getMessage());
        }
    }

    @FXML
    private void onQuitar() {
        Opcion grp = cmbGrupo.getValue();
        if (grp == null) { info("Selecciona un grupo primero."); return; }

        var ids = dataSel.stream().filter(CursoRow::isSelected).map(CursoRow::getId).collect(Collectors.toList());
        if (ids.isEmpty()) { info("Marca al menos un curso para quitar."); return; }

        try {
            int borrados = bo.eliminarCursosGrupo(grp.getId(), ids);
            info("Se eliminaron " + borrados + " curso(s) del grupo.");
            intentarPintarYLlenar();
        } catch (SQLException e) {
            error("No se pudieron eliminar cursos:\n" + e.getMessage());
        }
    }

    // ================= Generador automático (preferir bloques contiguos) =================

    @FXML
    private void onGenerarAuto() {
        Opcion grp = cmbGrupo.getValue();
        if (grp == null) { info("Selecciona un grupo primero."); return; }

        try {
            if (dataDisp.isEmpty() || dataSel.isEmpty()) intentarPintarYLlenar();

            // Materias objetivo (por nombre) y ya cubiertas
            Set<String> materiasObjetivo = dataDisp.stream()
                    .map(CursoRow::getMateria)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Set<String> materiasCubiertas = dataSel.stream()
                    .map(CursoRow::getMateria)
                    .collect(Collectors.toSet());

            List<CursoRow> ocupacion = new ArrayList<>(dataSel);
            Map<String, List<CursoRow>> candPorMateria = dataDisp.stream()
                    .collect(Collectors.groupingBy(CursoRow::getMateria));

            List<CursoRow> aAgregar = new ArrayList<>();
            List<String> noCubiertas = new ArrayList<>();

            for (String materia : materiasObjetivo) {
                if (materiasCubiertas.contains(materia)) continue;

                List<CursoRow> candis = candPorMateria.getOrDefault(materia, Collections.emptyList())
                        .stream()
                        .filter(c -> !chocaConLista(c, ocupacion))
                        .sorted(
                                java.util.Comparator
                                        .comparingDouble((CursoRow c) -> scoreContiguidad(c, ocupacion))
                                        .thenComparing(java.util.Comparator.comparing(
                                                (CursoRow c) -> c.getHi(),
                                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                                        ))
                                        .thenComparingLong(this::duracionMin)
                        )
                        .collect(Collectors.toList());

                if (candis.isEmpty()) {
                    noCubiertas.add(materia);
                    continue;
                }

                CursoRow elegido = null;
                for (CursoRow c : candis) {
                    if (!chocaConLista(c, aAgregar)) { elegido = c; break; }
                }

                if (elegido != null) {
                    aAgregar.add(elegido);
                    ocupacion.add(elegido);
                    materiasCubiertas.add(materia);
                } else {
                    noCubiertas.add(materia);
                }
            }

            if (aAgregar.isEmpty()) {
                info(noCubiertas.isEmpty()
                        ? "No hay cursos disponibles nuevos para agregar."
                        : "No se pudo cubrir ninguna materia adicional.\nSin cubrir: " + String.join(", ", noCubiertas));
                return;
            }

            List<Integer> ids = aAgregar.stream().map(CursoRow::getId).collect(Collectors.toList());
            int ins = bo.agregarCursosGrupo(grp.getId(), ids);

            intentarPintarYLlenar();

            String msg = "Se agregaron automáticamente " + ins + " curso(s).";
            if (!noCubiertas.isEmpty()) msg += "\nNo se pudieron cubrir: " + String.join(", ", noCubiertas);
            info(msg);

        } catch (SQLException e) {
            error("Error al generar automáticamente:\n" + e.getMessage());
        }
    }

    // ===== Heurística / Validaciones =====

    private boolean chocaConLista(CursoRow c, List<CursoRow> lista) {
        for (CursoRow x : lista) if (hayTraslape(c, x)) return true;
        return false;
    }

    /** Hay traslape si comparten al menos un día y los rangos de hora se cruzan. */
    private boolean hayTraslape(CursoRow a, CursoRow b) {
        if (a == null || b == null) return false;

        // 1) Si no comparten días, no hay choque
        int diasComunes = a.getDiasMask() & b.getDiasMask();
        if (diasComunes == 0) return false;

        // 2) Validar horas (si alguna viene null, no se detecta choque)
        LocalTime a1 = a.getHi(), a2 = a.getHf();
        LocalTime b1 = b.getHi(), b2 = b.getHf();
        if (a1 == null || a2 == null || b1 == null || b2 == null) return false;

        // 3) Traslape de intervalos: [a1,a2] vs [b1,b2]
        return !(a2.isBefore(b1) || b2.isBefore(a1));
    }

    /** Puntaje de contigüidad: menor es mejor. */
    private double scoreContiguidad(CursoRow cand, List<CursoRow> ocupacion) {
        int occMask = 0;
        for (CursoRow r : ocupacion) occMask |= r.getDiasMask();

        int candMask = cand.getDiasMask();
        int newDaysMask = candMask & (~occMask);

        int newDays = bitCount(newDaysMask);
        double gap = avgMinGapMinutes(cand, ocupacion); // 0 si toca/solapa

        return newDays * 120.0 + gap;
    }

    private int bitCount(int m) {
        int c = 0;
        for (int i = 0; i < 5; i++) if ((m & (1<<i)) != 0) c++;
        return c;
    }

    private double avgMinGapMinutes(CursoRow cand, List<CursoRow> ocupacion) {
        List<Integer> days = daysOf(cand.getDiasMask());
        if (days.isEmpty()) return 9999;

        double sum = 0;
        for (int d : days) {
            List<CursoRow> sameDay = ocupacion.stream()
                    .filter(r -> (r.getDiasMask() & (1<<d)) != 0)
                    .collect(Collectors.toList());
            if (sameDay.isEmpty()) {
                sum += 180;
                continue;
            }
            long best = Long.MAX_VALUE;
            for (CursoRow r : sameDay) {
                long g = gapMinutes(cand.getHi(), cand.getHf(), r.getHi(), r.getHf());
                if (g < best) best = g;
                if (best == 0) break;
            }
            sum += best;
        }
        return sum / days.size();
    }

    private List<Integer> daysOf(int mask) {
        List<Integer> d = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) if ((mask & (1<<i)) != 0) d.add(i);
        return d;
    }

    private long gapMinutes(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {
        if (a1 == null || a2 == null || b1 == null || b2 == null) return 180;
        if (!a2.isBefore(b1) && !b2.isBefore(a1)) return 0;
        if (a2.isBefore(b1)) return Duration.between(a2, b1).toMinutes();
        return Duration.between(b2, a1).toMinutes();
    }

    private long duracionMin(CursoRow c) {
        LocalTime hi = c.getHi();
        LocalTime hf = c.getHf();
        if (hi == null || hf == null) return Long.MAX_VALUE / 4;
        return Duration.between(hi, hf).toMinutes();
    }

    // ========= Mover alumno (opcional, integrado al mismo controller) =========
    @FXML
    private void onMoverAlumnoAlGrupoActual() {
        Opcion grp = cmbGrupo.getValue();
        if (grp == null) { info("Selecciona primero un grupo destino."); return; }

        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Mover alumno");
        dlg.setHeaderText("Mover alumno al grupo " + grp.getNombre());
        dlg.setContentText("Matrícula del alumno:");
        dlg.getEditor().setPromptText("A001...");
        dlg.showAndWait().ifPresent(m -> {
            String mat = m == null ? "" : m.trim();
            if (mat.isEmpty()) { warn("Validación", "Ingresa una matrícula."); return; }
            moverAlumnoYSincronizar(mat, grp.getId());
        });
    }

    private void moverAlumnoYSincronizar(String matricula, int nuevoGrupoId) {
        try {
            boolean ok = alumnoGrupoBO.cambiarGrupoYSincronizar(matricula, nuevoGrupoId);
            if (ok) {
                info("Alumno " + matricula + " movido y calificaciones sincronizadas.");
                intentarPintarYLlenar();
            } else {
                warn("Alumno no encontrado", "Verifica la matrícula: " + matricula);
            }
        } catch (Exception e) {
            error("Error al mover alumno:\n" + e.getMessage());
        }
    }

    // ========= Navegación =========
    @FXML
    private void onVolverMenu() {
        try {
            var url = getClass().getResource("/cbtis239/front/views/menu.fxml");
            if (url == null) throw new IllegalStateException("No se encontró menu.fxml");
            Parent root = FXMLLoader.load(url);
            Stage st = new Stage();
            st.setTitle("Menú");
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setFullScreen(true);
            st.setFullScreenExitHint("");
            st.show();
            ((Stage) paneReticula.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            error("No se pudo abrir el menú:\n" + e.getMessage());
        }
    }

    // ========= Alerts =========
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
