package cbtis239.front.ui.users;

import cbtis239.bo.AlumnoBO;
import cbtis239.dao.AlumnoDAO;
import cbtis239.dao.CatalogoDAO;
import cbtis239.dao.GrupoDao;
import cbtis239.model.Alumno;
import cbtis239.model.Catalogo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class AlumnoController {

    // Imágenes
    @FXML private ImageView imgFoto, imgFirma;

    // Campos
    @FXML private TextField txtMatricula, txtNombre, txtPaterno, txtMaterno, txtCurp, txtCorreo, txtNss;
    @FXML private TextField txtCalle, txtNumero, txtColonia, txtMunicipio, txtLocalidad, txtCelPadre, txtCelMadre;
    @FXML private TextField txtEstado, txtTelefono;
    @FXML private ComboBox<String> cmbEstatus, cmbSemestre;
    @FXML private ComboBox<Catalogo> cmbEdoCivil, cmbGenero, cmbPeriodo, cmbEspecialidad, cmbGrupo;
    @FXML private DatePicker dpFechaInscripcion;

    // Tabla
    @FXML private TableView<Alumno> tblAlumnos;
    @FXML private TableColumn<Alumno,String> colMatricula, colNombre;
    @FXML private TableColumn<Alumno,Integer> colSemestre, colGrupo;

    private final CatalogoDAO catalogoDAO = new CatalogoDAO();
    private final GrupoDao grupoDAO = new GrupoDao();
    private final AlumnoBO alumnoBO = new AlumnoBO();

    private String pathFoto, pathFirma;

    // ============================================================
    // ================== VALIDACIONES GENERALES ==================
    // ============================================================

    /** Solo números enteros */
    private void soloEnteros(TextField txt) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) txt.setText(oldValue);
        });
    }

    /** Solo letras (con acentos) y espacios, con longitud máxima */
    private void soloLetras(TextField txt, int max) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]*")) {
                txt.setText(oldValue);
                return;
            }
            if (newValue.length() > max) {
                txt.setText(oldValue);
            }
        });
    }

    /** Limita longitud máxima */
    private void limitarLongitud(TextField txt, int max) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > max) {
                txt.setText(oldValue);
            }
        });
    }

    /** CURP formato oficial */
    private boolean validarCurpFormato(String curp) {
        return curp != null && curp.matches("^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[A-Z0-9][0-9]$");
    }

    /** Reglas de campos obligatorios */
    private void validarObligatorios(Alumno a) {
        if (a.getMatricula() == null || a.getMatricula().isBlank())
            throw new RuntimeException("La matrícula es obligatoria");

        if (a.getCurp() == null || a.getCurp().isBlank())
            throw new RuntimeException("La CURP es obligatoria");

        if (!validarCurpFormato(a.getCurp()))
            throw new RuntimeException("La CURP no tiene un formato válido");

        if (a.getNombre() == null || a.getNombre().isBlank())
            throw new RuntimeException("El nombre es obligatorio");

        if (a.getPaterno() == null || a.getPaterno().isBlank())
            throw new RuntimeException("El apellido paterno es obligatorio");

        if (a.getEstadoInscripcion() == null || a.getEstadoInscripcion().isBlank())
            throw new RuntimeException("El estado de inscripción es obligatorio");

        if (a.getSemestre() == null)
            throw new RuntimeException("El semestre es obligatorio");

        if (a.getTelefono() == null || a.getTelefono().isBlank())
            throw new RuntimeException("El teléfono es obligatorio");

        if (a.getCorreo() == null || a.getCorreo().isBlank())
            throw new RuntimeException("El correo es obligatorio");

        if (a.getFechaInscripcion() == null)
            throw new RuntimeException("La fecha de inscripción es obligatoria");
    }

    /** Validación básica de correo (@) */
    private void validarCorreo(TextField txt) {
        txt.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) { // pierde foco
                String c = txt.getText();
                if (c != null && !c.isBlank() && !c.contains("@")) {
                    showError("El correo \"" + c + "\" no es válido. Debe contener '@'");
                    txt.requestFocus();
                }
            }
        });
    }

    // ============================================================
    // =========================== INIT ============================
    // ============================================================
    @FXML
    public void initialize() {
        try {
            new AlumnoDAO().sincronizarEstadoPorPagoVigente();
        } catch (SQLException ignore) {
            System.err.println("No se pudo sincronizar estado: " + ignore.getMessage());
        }

        // Clip circular foto
        imgFoto.layoutBoundsProperty().addListener((o, ov, nv) -> {
            double cx = nv.getWidth()/2.0, cy = nv.getHeight()/2.0;
            double r = Math.min(cx, cy);
            imgFoto.setClip(new Circle(cx, cy, r));
        });

        // Clip elíptico firma
        imgFirma.layoutBoundsProperty().addListener((o, ov, nv) -> {
            imgFirma.setClip(new Ellipse(
                    nv.getWidth()/2.0,
                    nv.getHeight()/2.0,
                    nv.getWidth()/2.0,
                    nv.getHeight()/2.5));
        });

        // Combos
        cmbEstatus.getItems().addAll("Activo", "Inactivo", "Egresado");
        for (int i=1; i<=6; i++) cmbSemestre.getItems().add(String.valueOf(i));

        try {
            cmbEdoCivil.getItems().setAll(catalogoDAO.edoCivil());
            cmbGenero.getItems().setAll(catalogoDAO.generos());
            cmbPeriodo.getItems().setAll(catalogoDAO.periodos());
            cmbEspecialidad.getItems().setAll(catalogoDAO.especialidades());
        } catch (SQLException e) {
            showError("No se pudieron cargar catálogos:\n" + e.getMessage());
        }

        // Tabla
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupoId"));
        recargarTabla();

        // Buscar por matrícula al perder foco
        txtMatricula.focusedProperty().addListener((obs, ov, nv) -> {
            if (!nv) onBuscarMatricula();
        });

        // Doble clic para cargar alumno
        tblAlumnos.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    cargarAlumno(row.getItem().getMatricula());
                }
            });
            return row;
        });

        // =====================================================
        // ====== RESTRICCIONES / LÍMITES DE CAMPOS ============
        // =====================================================

        // LONGITUDES (según tu tabla)
        limitarLongitud(txtCurp,      18);   // CURP
        limitarLongitud(txtMatricula, 20);   // Matricula
        limitarLongitud(txtTelefono,  15);   // Telefono
        limitarLongitud(txtCorreo,   100);   // Correo
        limitarLongitud(txtNombre,    45);   // Nombre
        limitarLongitud(txtPaterno,   45);   // Paterno
        limitarLongitud(txtMaterno,   45);   // Materno
        limitarLongitud(txtNss,       45);   // NSS
        limitarLongitud(txtCalle,     45);   // Calle
        limitarLongitud(txtNumero,    45);   // Numero
        limitarLongitud(txtColonia,   45);   // Colonia
        limitarLongitud(txtEstado,    45);   // Estado
        limitarLongitud(txtMunicipio, 45);   // Municipio
        limitarLongitud(txtLocalidad, 45);   // Localidad
        limitarLongitud(txtCelPadre,  12);   // CelPadre
        limitarLongitud(txtCelMadre,  12);   // CelMadre

        // SOLO LETRAS (con acentos y espacios)
        soloLetras(txtNombre,   45);
        soloLetras(txtPaterno,  45);
        soloLetras(txtMaterno,  45);
        soloLetras(txtCalle,    45);
        soloLetras(txtColonia,  45);
        soloLetras(txtEstado,   45);
        soloLetras(txtMunicipio,45);
        soloLetras(txtLocalidad,45);

        // SOLO NÚMEROS
        soloEnteros(txtTelefono);
        soloEnteros(txtCelPadre);
        soloEnteros(txtCelMadre);
        soloEnteros(txtNumero);
        soloEnteros(txtNss); // tu campo está como varchar(45) pero lógicamente es numérico

        // CURP siempre mayúsculas
        txtCurp.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String upper = newValue.toUpperCase();
                if (!upper.equals(newValue)) {
                    txtCurp.setText(upper);
                }
            }
        });

        // Validación básica de correo
        validarCorreo(txtCorreo);

        // DatePicker solo mediante calendario
        dpFechaInscripcion.setEditable(false);
        dpFechaInscripcion.getEditor().setDisable(true);
    }

    private void recargarTabla() {
        try {
            tblAlumnos.setItems(FXCollections.observableArrayList(alumnoBO.listarBreve()));
        } catch (SQLException e) {
            tblAlumnos.setItems(FXCollections.observableArrayList());
        }
    }

    // ==============================
    // EVENTOS
    // ==============================

    @FXML
    public void onEspecialidadChange() {
        Catalogo esp = cmbEspecialidad.getValue();
        if (esp == null) {
            cmbGrupo.getItems().clear();
            return;
        }
        try {
            cmbGrupo.getItems().setAll(grupoDAO.gruposPorEspecialidad(esp.getId()));
        } catch (SQLException e) {
            showError("No se pudieron cargar grupos:\n" + e.getMessage());
        }
    }

    @FXML
    public void onBuscarMatricula() {
        String m = txtMatricula.getText();
        if (m == null || m.isBlank()) return;

        try {
            Alumno a = alumnoBO.buscar(m.trim());
            if (a == null) { limpiar(false); return; }

            // Campos generales
            txtCurp.setText(a.getCurp());
            txtNombre.setText(a.getNombre());
            txtPaterno.setText(a.getPaterno());
            txtMaterno.setText(a.getMaterno());
            txtCorreo.setText(a.getCorreo());
            txtNss.setText(a.getNss());
            cmbEstatus.setValue(a.getEstadoInscripcion());
            cmbSemestre.setValue(a.getSemestre()==null? null : String.valueOf(a.getSemestre()));
            cmbPeriodo.getSelectionModel().select(matchId(cmbPeriodo, a.getPeriodoId()));
            cmbEdoCivil.getSelectionModel().select(matchId(cmbEdoCivil, a.getEdoCivilId()));
            cmbGenero.getSelectionModel().select(matchId(cmbGenero, a.getGeneroId()));
            dpFechaInscripcion.setValue(a.getFechaInscripcion());

            // Especialidad
            if (a.getCarrera()!=null) {
                for (Catalogo c : cmbEspecialidad.getItems()) {
                    if (Objects.equals(c.getNombre(), a.getCarrera())) {
                        cmbEspecialidad.getSelectionModel().select(c);
                        break;
                    }
                }
                onEspecialidadChange();
            }

            cmbGrupo.getSelectionModel().select(matchId(cmbGrupo, a.getGrupoId()));

            // Dirección
            txtCalle.setText(a.getCalle());
            txtNumero.setText(a.getNumero());
            txtColonia.setText(a.getColonia());
            txtMunicipio.setText(a.getMunicipio());
            txtLocalidad.setText(a.getLocalidad());
            txtEstado.setText(a.getEstado());
            txtTelefono.setText(a.getTelefono());
            txtCelPadre.setText(a.getCelPadre());
            txtCelMadre.setText(a.getCelMadre());

            // Imágenes
            pathFoto = a.getFoto();
            pathFirma = a.getFirma();
            loadImage(imgFoto, pathFoto);
            loadImage(imgFirma, pathFirma);

        } catch (SQLException e) {
            showError("Error al buscar matrícula:\n" + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            Alumno a = buildFromForm();
            validarObligatorios(a);
            alumnoBO.guardar(a);

            showInfo("Alumno guardado correctamente");

            limpiar(true);
            recargarTabla();

        } catch (Exception e) {
            showError("No se pudo guardar:\n" + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        String m = txtMatricula.getText();
        if (m==null || m.isBlank()) {
            showError("Indica la matrícula a eliminar");
            return;
        }
        try {
            alumnoBO.eliminar(m.trim());
            showInfo("Alumno eliminado");
            limpiar(true);
            recargarTabla();
        } catch (SQLException e) {
            showError("No se pudo eliminar:\n" + e.getMessage());
        }
    }

    @FXML public void onCancelar(){ limpiar(true); }

    @FXML
    public void onVolver() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Stage st = new Stage();
            st.setTitle("Menú");
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setFullScreen(true);
            st.setFullScreenExitHint("");
            st.show();
            ((Stage) imgFoto.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("No se pudo abrir el menú:\n" + e.getMessage());
        }
    }

    // ==============================
    // IMÁGENES
    // ==============================

    @FXML public void onSubirFoto()  { pathFoto  = pickImage(imgFoto); }
    @FXML public void onSubirFirma() { pathFirma = pickImage(imgFirma); }

    private String pickImage(ImageView target) {
        if (target == null) return null;
        if (target.getScene() == null || target.getScene().getWindow() == null) return null;

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg"));

        File f = fc.showOpenDialog(target.getScene().getWindow());
        if (f != null) {
            String ruta = f.getAbsolutePath();
            loadImage(target, ruta);
            return ruta;
        }
        return null;
    }

    private void loadImage(ImageView iv, String path) {
        if (path == null || path.isBlank()) {
            iv.setImage(null);
            return;
        }
        iv.setImage(new Image(new File(path).toURI().toString(), false));
    }

    // ==============================
    // BUILD ALUMNO DESDE FORMULARIO
    // ==============================

    private Alumno buildFromForm() {
        Alumno a = new Alumno();
        a.setMatricula(txtMatricula.getText().trim());

        String curp = v(txtCurp);
        a.setCurp(curp == null ? null : curp.toUpperCase());

        a.setNombre(v(txtNombre));
        a.setPaterno(v(txtPaterno));
        a.setMaterno(v(txtMaterno));
        a.setCorreo(v(txtCorreo));
        a.setNss(v(txtNss));
        a.setEstadoInscripcion(cmbEstatus.getValue());
        a.setSemestre(cmbSemestre.getValue()==null? null : Integer.valueOf(cmbSemestre.getValue()));
        a.setPeriodoId(getId(cmbPeriodo));
        a.setEdoCivilId(getId(cmbEdoCivil));
        a.setGeneroId(getId(cmbGenero));
        a.setCarrera(cmbEspecialidad.getValue()==null? null : cmbEspecialidad.getValue().getNombre());
        a.setGrupoId(getId(cmbGrupo));
        a.setFechaInscripcion(dpFechaInscripcion.getValue());

        a.setCalle(v(txtCalle));
        a.setNumero(v(txtNumero));
        a.setColonia(v(txtColonia));
        a.setEstado(v(txtEstado));
        a.setTelefono(v(txtTelefono));
        a.setMunicipio(v(txtMunicipio));
        a.setLocalidad(v(txtLocalidad));
        a.setCelPadre(v(txtCelPadre));
        a.setCelMadre(v(txtCelMadre));

        a.setFoto(pathFoto);
        a.setFirma(pathFirma);

        return a;
    }

    private Integer getId(ComboBox<Catalogo> cb){
        return cb.getValue()==null? null : cb.getValue().getId();
    }

    private Catalogo matchId(ComboBox<Catalogo> cb, Integer id){
        if (id==null) return null;
        for (Catalogo c: cb.getItems()) if (c.getId()==id) return c;
        return null;
    }

    private String v(TextField t){
        return t.getText()==null? null : t.getText().trim();
    }

    // ==============================
    // LIMPIAR FORMULARIO
    // ==============================

    private void limpiar(boolean clearMatricula){
        if (clearMatricula) txtMatricula.clear();

        txtCurp.clear();
        txtNombre.clear();
        txtPaterno.clear();
        txtMaterno.clear();
        txtCorreo.clear();
        txtNss.clear();

        cmbEstatus.getSelectionModel().clearSelection();
        cmbSemestre.getSelectionModel().clearSelection();
        cmbPeriodo.getSelectionModel().clearSelection();
        cmbEdoCivil.getSelectionModel().clearSelection();
        cmbGenero.getSelectionModel().clearSelection();
        cmbEspecialidad.getSelectionModel().clearSelection();
        cmbGrupo.getItems().clear();
        dpFechaInscripcion.setValue(null);

        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtEstado.clear();
        txtTelefono.clear();
        txtMunicipio.clear();
        txtLocalidad.clear();
        txtCelPadre.clear();
        txtCelMadre.clear();

        imgFoto.setImage(null);
        imgFirma.setImage(null);
        pathFoto=null;
        pathFirma=null;
    }

    // ==============================
    // ALERTAS
    // ==============================

    private void showError(String m){
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }

    private void showInfo(String m){
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }

    // ==============================
    // CARGAR ALUMNO
    // ==============================

    private void cargarAlumno(String matricula) {
        try {
            Alumno a = alumnoBO.buscar(matricula);
            if (a == null) {
                showError("No se encontró el alumno con matrícula: " + matricula);
                return;
            }

            txtMatricula.setText(a.getMatricula());
            txtCurp.setText(a.getCurp());
            txtNombre.setText(a.getNombre());
            txtPaterno.setText(a.getPaterno());
            txtMaterno.setText(a.getMaterno());
            txtCorreo.setText(a.getCorreo());
            txtNss.setText(a.getNss());
            cmbEstatus.setValue(a.getEstadoInscripcion());
            cmbSemestre.setValue(a.getSemestre()==null? null : String.valueOf(a.getSemestre()));
            cmbPeriodo.getSelectionModel().select(matchId(cmbPeriodo, a.getPeriodoId()));
            cmbEdoCivil.getSelectionModel().select(matchId(cmbEdoCivil, a.getEdoCivilId()));
            cmbGenero.getSelectionModel().select(matchId(cmbGenero, a.getGeneroId()));
            dpFechaInscripcion.setValue(a.getFechaInscripcion());

            if (a.getCarrera()!=null) {
                for (Catalogo c : cmbEspecialidad.getItems()) {
                    if (Objects.equals(c.getNombre(), a.getCarrera())) {
                        cmbEspecialidad.getSelectionModel().select(c);
                        break;
                    }
                }

                onEspecialidadChange();

                if (a.getGrupoId() != null) {
                    cmbGrupo.getSelectionModel().select(matchId(cmbGrupo, a.getGrupoId()));
                }
            }

            txtCalle.setText(a.getCalle());
            txtNumero.setText(a.getNumero());
            txtColonia.setText(a.getColonia());
            txtMunicipio.setText(a.getMunicipio());
            txtLocalidad.setText(a.getLocalidad());
            txtEstado.setText(a.getEstado());
            txtTelefono.setText(a.getTelefono());
            txtCelPadre.setText(a.getCelPadre());
            txtCelMadre.setText(a.getCelMadre());

            pathFoto = a.getFoto();
            pathFirma = a.getFirma();
            loadImage(imgFoto, pathFoto);
            loadImage(imgFirma, pathFirma);

        } catch (SQLException e) {
            showError("Error al cargar alumno:\n" + e.getMessage());
        }
    }

}
