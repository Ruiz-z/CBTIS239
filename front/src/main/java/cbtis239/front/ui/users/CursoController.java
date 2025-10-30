package cbtis239.front.ui.users;

import cbtis239.bo.CursoBO;
import cbtis239.model.Curso;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CursoController {

    // --------- Form ----------
    @FXML private ComboBox<OpcionStr> cmbMateria;
    @FXML private ComboBox<Opcion>    cmbDocente;
    @FXML private ComboBox<OpcionStr> cmbAula;
    @FXML private ComboBox<Opcion>    cmbPeriodo;
    @FXML private ComboBox<String>    cmbHoraIni, cmbHoraFin;
    @FXML private RadioButton rbLV, rbLJ;
    @FXML private CheckBox chkL, chkM, chkX, chkJ, chkV;
    @FXML private TextArea txtDescripcion;

    // --------- Tabla ----------
    @FXML private TableView<Curso> tblCursos;
    @FXML private TableColumn<Curso, String> colMateria, colDocente, colAula, colHorario;

    private final CursoBO bo = new CursoBO();
    private final ObservableList<Curso> data = FXCollections.observableArrayList();

    private ToggleGroup tgDias;
    private boolean updatingCombos = false;

    // >>> clave para no “brincar” el combo de periodo al autollenar
    private boolean suspendPeriodoListener = false;

    private Curso seleccionado;
    private Integer periodoSeleccionadoId = null;

    @FXML
    private void initialize() {
        // columnas
        colMateria.setCellValueFactory(c -> c.getValue().materiaNombreProperty());
        colDocente.setCellValueFactory(c -> c.getValue().docenteNombreProperty());
        colAula.setCellValueFactory(c -> c.getValue().aulaIdProperty());
        colHorario.setCellValueFactory(c -> c.getValue().horarioTextoProperty());
        tblCursos.setItems(data);

        tgDias = new ToggleGroup();
        rbLV.setToggleGroup(tgDias); rbLJ.setToggleGroup(tgDias);

        cargarCombosBase();

        // filtros dependientes
        cmbMateria.valueProperty().addListener((o, a, n) -> filtrarDocentesPorMateria(n));
        cmbDocente.valueProperty().addListener((o, a, n) -> filtrarMateriasPorDocente(n));

        // horas
        List<String> horas = new ArrayList<>();
        for (int h=7; h<=19; h++) horas.add(String.format("%02d:00", h));
        cmbHoraIni.setItems(FXCollections.observableArrayList(horas.subList(0, horas.size()-1)));
        cmbHoraFin.setItems(FXCollections.observableArrayList(horas));

        cmbHoraIni.valueProperty().addListener((o, a, n) -> {
            if (n != null) {
                int h = Integer.parseInt(n.substring(0,2)) + 1;
                String sug = String.format("%02d:00", Math.min(h, 19));
                cmbHoraFin.getSelectionModel().select(sug);
            }
            actualizarDescripcion();
        });
        cmbHoraFin.valueProperty().addListener((o,a,n) -> actualizarDescripcion());
        cmbAula.valueProperty().addListener((o,a,n) -> actualizarDescripcion());

        // >>> listener del periodo protegido
        cmbPeriodo.valueProperty().addListener((o,a,n) -> {
            if (suspendPeriodoListener) return;
            periodoSeleccionadoId = (n==null?null:n.getId());
            actualizarDescripcion();
        });

        // exclusiones mutuas
        rbLV.selectedProperty().addListener((o,a,n)-> desactivarChecksSiPredef());
        rbLJ.selectedProperty().addListener((o,a,n)-> desactivarChecksSiPredef());
        chkL.selectedProperty().addListener((o,a,n)-> desactivarRadiosSiPersonalizado());
        chkM.selectedProperty().addListener((o,a,n)-> desactivarRadiosSiPersonalizado());
        chkX.selectedProperty().addListener((o,a,n)-> desactivarRadiosSiPersonalizado());
        chkJ.selectedProperty().addListener((o,a,n)-> desactivarRadiosSiPersonalizado());
        chkV.selectedProperty().addListener((o,a,n)-> desactivarRadiosSiPersonalizado());

        // autollenado al seleccionar fila
        tblCursos.getSelectionModel().selectedItemProperty().addListener((o,a,n) -> {
            seleccionado = n;
            if (n != null) cargarEnFormulario(n);
        });

        // >>> renderers para que los combos muestren el nombre
        configComboRenderers();

        recargarTabla();
    }

    private void cargarCombosBase() {
        try {
            cmbMateria.setItems(FXCollections.observableArrayList(bo.listarMaterias()));
            cmbDocente.setItems(FXCollections.observableArrayList(bo.listarDocentes()));
            cmbAula.setItems(FXCollections.observableArrayList(bo.listarAulas()));
            cmbPeriodo.setItems(FXCollections.observableArrayList(bo.listarPeriodos()));
        } catch (SQLException e) { error("Error cargando catálogos", e.getMessage()); }
    }

    // ---------------- Filtros dependientes ----------------
    private void filtrarDocentesPorMateria(OpcionStr materia) {
        if (updatingCombos) return; updatingCombos = true;
        try {
            cmbDocente.hide();
            Opcion sel = cmbDocente.getValue();
            var nueva = (materia!=null)
                    ? FXCollections.observableArrayList(bo.listarDocentesPorMateria(materia.getId()))
                    : FXCollections.observableArrayList(bo.listarDocentes());
            cmbDocente.setItems(nueva);
            if (sel != null && contieneOpcion(nueva, sel.getId())) cmbDocente.setValue(buscarOpcion(nueva, sel.getId()));
            else cmbDocente.setValue(null);
        } catch (SQLException e) { error("BD", e.getMessage());
        } finally { updatingCombos = false; actualizarDescripcion(); }
    }

    private void filtrarMateriasPorDocente(Opcion docente) {
        if (updatingCombos) return; updatingCombos = true;
        try {
            cmbMateria.hide();
            OpcionStr sel = cmbMateria.getValue();
            var nueva = (docente!=null)
                    ? FXCollections.observableArrayList(bo.listarMateriasPorDocente(docente.getId()))
                    : FXCollections.observableArrayList(bo.listarMaterias());
            cmbMateria.setItems(nueva);
            if (sel != null && contieneOpcionStr(nueva, sel.getId())) cmbMateria.setValue(buscarOpcionStr(nueva, sel.getId()));
            else cmbMateria.setValue(null);
        } catch (SQLException e) { error("BD", e.getMessage());
        } finally { updatingCombos = false; actualizarDescripcion(); }
    }

    // ---------------- Autollenado ----------------
    private void cargarEnFormulario(Curso c) {
        try {
            updatingCombos = true;

            if (cmbMateria.getItems()==null || cmbMateria.getItems().isEmpty())
                cmbMateria.setItems(FXCollections.observableArrayList(bo.listarMaterias()));
            if (cmbDocente.getItems()==null || cmbDocente.getItems().isEmpty())
                cmbDocente.setItems(FXCollections.observableArrayList(bo.listarDocentes()));
            if (cmbAula.getItems()==null || cmbAula.getItems().isEmpty())
                cmbAula.setItems(FXCollections.observableArrayList(bo.listarAulas()));
            if (cmbPeriodo.getItems()==null || cmbPeriodo.getItems().isEmpty())
                cmbPeriodo.setItems(FXCollections.observableArrayList(bo.listarPeriodos()));

            // docente / materia
            cmbDocente.setValue(buscarOpcion(cmbDocente.getItems(), c.getDocenteId()));
            filtrarMateriasPorDocente(cmbDocente.getValue());
            cmbMateria.setValue(buscarOpcionStr(cmbMateria.getItems(), c.getMateriaClave()));

            // aula
            cmbAula.setValue(buscarOpcionStr(cmbAula.getItems(), c.getAulaId()));

            // horas
            cmbHoraIni.setValue(String.format("%02d:00", c.getHoraInicio().getHour()));
            cmbHoraFin.setValue(String.format("%02d:00", c.getHoraFin().getHour()));

            // días (desde el texto)
            String diasTxt = c.getHorarioTexto();
            String soloDias = diasTxt.contains(" de ") ? diasTxt.substring(0, diasTxt.indexOf(" de ")).trim() : diasTxt.trim();
            rbLV.setSelected(false); rbLJ.setSelected(false);
            chkL.setSelected(false); chkM.setSelected(false); chkX.setSelected(false); chkJ.setSelected(false); chkV.setSelected(false);
            if ("L, M, X, J, V".equals(soloDias))      rbLV.setSelected(true);
            else if ("L, M, X, J".equals(soloDias))    rbLJ.setSelected(true);
            else {
                if (soloDias.contains("L")) chkL.setSelected(true);
                if (soloDias.contains("M")) chkM.setSelected(true);
                if (soloDias.contains("X")) chkX.setSelected(true);
                if (soloDias.contains("J")) chkJ.setSelected(true);
                if (soloDias.contains("V")) chkV.setSelected(true);
                desactivarRadiosSiPersonalizado();
            }

// AHORA: match exacto por Inicio/Fin del curso
if (c.getFechaInicio()!=null && c.getFechaFin()!=null) {
    selectPeriodoByExactDates(c.getFechaInicio(), c.getFechaFin());
}


        } catch (SQLException e) {
            error("Error al cargar curso", e.getMessage());
        } finally {
            updatingCombos = false;
            actualizarDescripcion();
        }
    }

    // ---------------- Helpers UI ----------------
    private void desactivarChecksSiPredef() {
        boolean predef = rbLV.isSelected() || rbLJ.isSelected();
        chkL.setDisable(predef); chkM.setDisable(predef); chkX.setDisable(predef); chkJ.setDisable(predef); chkV.setDisable(predef);
        if (predef) { chkL.setSelected(false); chkM.setSelected(false); chkX.setSelected(false); chkJ.setSelected(false); chkV.setSelected(false); }
        actualizarDescripcion();
    }
    private void desactivarRadiosSiPersonalizado() {
        boolean per = chkL.isSelected()||chkM.isSelected()||chkX.isSelected()||chkJ.isSelected()||chkV.isSelected();
        rbLV.setDisable(per); rbLJ.setDisable(per);
        if (per) { rbLV.setSelected(false); rbLJ.setSelected(false); }
        actualizarDescripcion();
    }

    private boolean contieneOpcion(ObservableList<Opcion> lista, int id) { for (Opcion o:lista) if (o.getId()==id) return true; return false; }
    private Opcion buscarOpcion(ObservableList<Opcion> lista, int id) { for (Opcion o:lista) if (o.getId()==id) return o; return null; }
    private boolean contieneOpcionStr(ObservableList<OpcionStr> lista, String id) { for (OpcionStr o:lista) if (o.getId().equals(id)) return true; return false; }
    private OpcionStr buscarOpcionStr(ObservableList<OpcionStr> lista, String id) { for (OpcionStr o:lista) if (o.getId().equals(id)) return o; return null; }

    private String diasTextoActual() {
        if (rbLV.isSelected()) return "L, M, X, J, V";
        if (rbLJ.isSelected()) return "L, M, X, J";
        List<String> d = new ArrayList<>();
        if (chkL.isSelected()) d.add("L"); if (chkM.isSelected()) d.add("M");
        if (chkX.isSelected()) d.add("X"); if (chkJ.isSelected()) d.add("J");
        if (chkV.isSelected()) d.add("V");
        return String.join(", ", d);
    }

    private void actualizarDescripcion() {
        var m = cmbMateria.getValue();
        var d = cmbDocente.getValue();
        var a = cmbAula.getValue();
        var hi = cmbHoraIni.getValue();
        var hf = cmbHoraFin.getValue();
        String dias = diasTextoActual();
        if (m!=null && d!=null && a!=null && hi!=null && hf!=null && !dias.isBlank()) {
            txtDescripcion.setText(m.getNombre()+" con "+d.getNombre()+" en el aula "+a.getId()+" de "+hi+" a "+hf+" los días "+dias);
        } else {
            txtDescripcion.clear();
        }
    }

    private void recargarTabla() {
        try { data.setAll(bo.listarCursos()); tblCursos.refresh(); }
        catch (SQLException e) { error("Error cargando cursos", e.getMessage()); }
    }

    // ---------------- Acciones CRUD ----------------
    @FXML
    private void onCrear() {
        try {
            Curso c = buildCursoFromForm(null);
            LocalDate[] fechas = bo.fechasDePeriodo(periodoSeleccionadoId);
            bo.validarChoques(c.getAulaId(), c.getDocenteId(), c.getHoraInicio(), c.getHoraFin(),
                    c.getDiasSemanaId(), fechas[0], fechas[1], null);
            bo.validarCursoIgual(fechas[0], fechas[1], c.getDocenteId(), c.getMateriaClave(), c.getAulaId(),
                    c.getHoraInicio(), c.getHoraFin(), c.getDiasSemanaId(), null);
            bo.crear(c);
            recargarTabla();
            // re-selecciona el periodo mostrado
            if (periodoSeleccionadoId != null) selectPeriodoById(periodoSeleccionadoId);
            info("Éxito", "Curso creado correctamente.");
        } catch (IllegalArgumentException iae) { warn("Validación", iae.getMessage());
        } catch (SQLException e) { error("BD", e.getMessage()); }
    }

    @FXML
    private void onModificar() {
        if (seleccionado == null) { warn("Selección", "Selecciona un curso de la tabla."); return; }
        try {
            Curso c = buildCursoFromForm(seleccionado.getCursoId());
            LocalDate[] fechas = bo.fechasDePeriodo(periodoSeleccionadoId);
            bo.validarChoques(c.getAulaId(), c.getDocenteId(), c.getHoraInicio(), c.getHoraFin(),
                    c.getDiasSemanaId(), fechas[0], fechas[1], c.getCursoId());
            bo.validarCursoIgual(fechas[0], fechas[1], c.getDocenteId(), c.getMateriaClave(), c.getAulaId(),
                    c.getHoraInicio(), c.getHoraFin(), c.getDiasSemanaId(), c.getCursoId());
            bo.actualizar(c);
            recargarTabla();
            if (periodoSeleccionadoId != null) selectPeriodoById(periodoSeleccionadoId);
            info("Éxito", "Curso actualizado.");
        } catch (IllegalArgumentException iae) { warn("Validación", iae.getMessage());
        } catch (SQLException e) { error("BD", e.getMessage()); }
    }

    @FXML
    private void onEliminar() {
        if (seleccionado == null) { warn("Selección", "Selecciona un curso de la tabla."); return; }
        try { bo.eliminar(seleccionado.getCursoId()); recargarTabla(); info("Éxito", "Curso eliminado."); }
        catch (SQLException e) { error("BD", e.getMessage()); }
    }

    @FXML
    private void onLimpiar() {
        cmbMateria.getSelectionModel().clearSelection();
        cmbDocente.getSelectionModel().clearSelection();
        cmbAula.getSelectionModel().clearSelection();
        cmbPeriodo.getSelectionModel().clearSelection();
        cmbHoraIni.getSelectionModel().clearSelection();
        cmbHoraFin.getSelectionModel().clearSelection();
        rbLV.setSelected(false); rbLJ.setSelected(false);
        chkL.setSelected(false); chkM.setSelected(false); chkX.setSelected(false); chkJ.setSelected(false); chkV.setSelected(false);
        txtDescripcion.clear();
        seleccionado = null;
    }

    @FXML
    private void onVolverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Menú 2");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
          throw new IllegalArgumentException("No se pudo volver al menú:\n" + e.getMessage());
        }
    }

    // ---------------- Build/validaciones ----------------
    private Curso buildCursoFromForm(Integer cursoId) throws SQLException {
        var mat = cmbMateria.getValue();
        var doc = cmbDocente.getValue();
        var aula = cmbAula.getValue();
        var per  = cmbPeriodo.getValue();
        var hiStr = cmbHoraIni.getValue();
        var hfStr = cmbHoraFin.getValue();
        if (mat==null || doc==null || aula==null || per==null || hiStr==null || hfStr==null)
            throw new IllegalArgumentException("Completa materia, docente, aula, periodo y horas.");

        LocalTime hi = LocalTime.parse(hiStr);
        LocalTime hf = LocalTime.parse(hfStr);
        if (hi.isBefore(LocalTime.of(7,0)) || hi.isAfter(LocalTime.of(18,0))) throw new IllegalArgumentException("Hora inicio debe estar entre 07:00 y 18:00.");
        if (hf.isBefore(LocalTime.of(8,0)) || hf.isAfter(LocalTime.of(19,0))) throw new IllegalArgumentException("Hora fin debe estar entre 08:00 y 19:00.");
        if (!hf.isAfter(hi)) throw new IllegalArgumentException("Hora fin debe ser después de hora inicio.");

        int diasId;
        if (rbLV.isSelected()) diasId = 1;
        else if (rbLJ.isSelected()) diasId = 2;
        else {
            boolean l=chkL.isSelected(), m=chkM.isSelected(), x=chkX.isSelected(), j=chkJ.isSelected(), v=chkV.isSelected();
            if (!(l||m||x||j||v)) throw new IllegalArgumentException("Selecciona al menos un día.");
            diasId = bo.getOrCreateDiasSemana(l,m,x,j,v);
        }

        LocalDate[] fechas = bo.fechasDePeriodo(per.getId());
        LocalDate fi = fechas[0], ff = fechas[1];
        LocalDate hoy = LocalDate.now();
        String estado = hoy.isBefore(fi) ? "Inactivo" : ( (hoy.isAfter(ff)) ? "Completado" : "Activo");
        int anioAcad = fi.getYear();

        String desc = (txtDescripcion.getText()==null || txtDescripcion.getText().isBlank())
                ? (mat.getNombre()+" con "+doc.getNombre()+" en el aula "+aula.getId()+" de "+hi+" a "+hf+" los días "+diasTextoActual())
                : txtDescripcion.getText();

        Curso c = new Curso();
        if (cursoId != null) c.setCursoId(cursoId);
        c.setAnioAcademico(anioAcad);
        c.setEstado(estado);
        c.setFechaInicio(fi);
        c.setFechaFin(ff);
        c.setDescripcion(desc);
        c.setAulaId(aula.getId());
        c.setHoraInicio(hi);
        c.setHoraFin(hf);
        c.setDiasSemanaId(diasId);
        c.setDocenteId(doc.getId());
        c.setMateriaClave(mat.getId());
        c.setDocenteNombre(doc.getNombre());
        c.setMateriaNombre(mat.getNombre());
        periodoSeleccionadoId = per.getId();
        return c;
    }

    // ---------------- Renderers / Converters ----------------
    private void configComboRenderers() {
        setupComboOpcion(cmbDocente);
        setupComboOpcion(cmbPeriodo);
        setupComboOpcionStr(cmbAula);
        setupComboOpcionStr(cmbMateria);
        cmbPeriodo.setEditable(false);
        cmbDocente.setEditable(false);
        cmbMateria.setEditable(false);
        cmbAula.setEditable(false);
    }
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

    // ---------------- Selección de periodo ----------------
    private void selectPeriodoById(int idPeriodo) {
        for (Opcion p : cmbPeriodo.getItems()) {
            if (p.getId() == idPeriodo) {
                suspendPeriodoListener = true;
                try {
                    cmbPeriodo.setValue(p);
                    periodoSeleccionadoId = p.getId();
                } finally { suspendPeriodoListener = false; }
                return;
            }
        }
    }

    /** Busca el periodo que contiene [fi,ff]; si no hay, toma el que más “encaje” (traslape más corto). */
    private void selectPeriodoByDates(LocalDate fi, LocalDate ff) throws SQLException {
        if (cmbPeriodo.getItems()==null || cmbPeriodo.getItems().isEmpty()) {
            cmbPeriodo.setItems(FXCollections.observableArrayList(bo.listarPeriodos()));
        }
        Opcion mejor = null;
        long mejorSpan = Long.MAX_VALUE;

        for (Opcion p : cmbPeriodo.getItems()) {
            LocalDate[] r = bo.fechasDePeriodo(p.getId());
            LocalDate pi = r[0], pf = r[1];
            boolean contieneAmbos = (!fi.isBefore(pi) && !fi.isAfter(pf)) && (!ff.isBefore(pi) && !ff.isAfter(pf));
            boolean traslapa = !(ff.isBefore(pi) || fi.isAfter(pf));
            if (contieneAmbos || traslapa) {
                long span = java.time.temporal.ChronoUnit.DAYS.between(pi, pf);
                if (span < mejorSpan || (mejor==null && (contieneAmbos || traslapa))) {
                    mejor = p; mejorSpan = span;
                }
            }
        }
        suspendPeriodoListener = true;
        try {
            cmbPeriodo.setValue(mejor);
            periodoSeleccionadoId = (mejor==null ? null : mejor.getId());
        } finally { suspendPeriodoListener = false; }
    }

    /** Selecciona el periodo cuyo Inicio == fi y Fin == ff (match exacto).
 *  Si no lo encuentra, cae al algoritmo de "mejor traslape".
 */
private void selectPeriodoByExactDates(LocalDate fi, LocalDate ff) throws SQLException {
    if (fi == null || ff == null) {
        // sin fechas no podemos decidir; limpiar selección
        suspendPeriodoListener = true;
        try {
            cmbPeriodo.getSelectionModel().clearSelection();
            periodoSeleccionadoId = null;
        } finally { suspendPeriodoListener = false; }
        return;
    }

    if (cmbPeriodo.getItems() == null || cmbPeriodo.getItems().isEmpty()) {
        cmbPeriodo.setItems(FXCollections.observableArrayList(bo.listarPeriodos()));
    }

    Opcion exacto = null;
    for (Opcion p : cmbPeriodo.getItems()) {
        LocalDate[] r = bo.fechasDePeriodo(p.getId()); // [Inicio, Fin]
        if (fi.equals(r[0]) && ff.equals(r[1])) {
            exacto = p;
            break;
        }
    }

    suspendPeriodoListener = true;
    try {
        if (exacto != null) {
            cmbPeriodo.setValue(exacto);
            periodoSeleccionadoId = exacto.getId();
        } else {
            // fallback: mejor traslape/contención
            selectPeriodoByDates(fi, ff);
        }
    } finally {
        suspendPeriodoListener = false;
    }
}

    // ---------------- Alerts ----------------
    private void warn(String t, String m) { show(Alert.AlertType.WARNING, t, m); }
    private void info(String t, String m) { show(Alert.AlertType.INFORMATION, t, m); }
    private void error(String t, String m) { show(Alert.AlertType.ERROR, t, m); }
    private void show(Alert.AlertType type, String t, String m) {
        Alert a = new Alert(type, m, ButtonType.OK);
        a.setHeaderText(t);
        a.initOwner(tblCursos.getScene().getWindow());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }
}
