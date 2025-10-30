package cbtis239.front.ui.users;

import cbtis239.bo.AlumnoBO;
import cbtis239.dao.AlumnoDAO;
import cbtis239.dao.CatalogoDAO;
import cbtis239.dao.GrupoDao;
import cbtis239.model.Alumno;
import cbtis239.model.Catalogo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
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

    @FXML
    public void initialize() {
        try {
            new cbtis239.dao.AlumnoDAO().sincronizarEstadoPorPagoVigente();
            // o si prefieres el nombre "ConPeriodoVigente", ver punto 2 abajo
            // new cbtis239.dao.AlumnoDAO().sincronizarEstadoConPeriodoVigente();
        } catch (java.sql.SQLException ignore) {
            // opcional: loggear
            System.err.println("No se pudo sincronizar estado: " + ignore.getMessage());
        }
        // Clip redondo para foto (se recalcula al cambiar el layout)
        imgFoto.layoutBoundsProperty().addListener((o, ov, nv) -> {
            double cx = nv.getWidth()/2.0, cy = nv.getHeight()/2.0, r = Math.min(cx, cy);
            imgFoto.setClip(new Circle(cx, cy, r));
        });
        imgFirma.layoutBoundsProperty().addListener((o, ov, nv) -> {
            imgFirma.setClip(new Ellipse(nv.getWidth()/2.0, nv.getHeight()/2.0, nv.getWidth()/2.0, nv.getHeight()/2.5));
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

        // Buscar al perder foco
        txtMatricula.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) onBuscarMatricula();
        });
        // Doble clic en tabla para cargar alumno
        tblAlumnos.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Alumno seleccionado = row.getItem();
                    if (seleccionado != null) {
                        cargarAlumno(seleccionado.getMatricula());
                    }
                }
            });
            return row;
        });

    }

    private void recargarTabla() {
        try {
            List<Alumno> data = alumnoBO.listarBreve();
            tblAlumnos.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            // no bloquear pantalla si falla
            tblAlumnos.setItems(FXCollections.observableArrayList());
        }
    }

    // ======== Eventos ========

    @FXML
    public void onEspecialidadChange() {
        Catalogo esp = cmbEspecialidad.getValue();
        if (esp == null) { cmbGrupo.getItems().clear(); return; }
        try {
            List<Catalogo> grupos = grupoDAO.gruposPorEspecialidad(esp.getId());
            cmbGrupo.getItems().setAll(grupos);
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
            // Rellenar
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

            // Especialidad por nombre
            if (a.getCarrera()!=null) {
                for (Catalogo c : cmbEspecialidad.getItems()) {
                    if (Objects.equals(c.getNombre(), a.getCarrera())) { cmbEspecialidad.getSelectionModel().select(c); break; }
                }
                onEspecialidadChange();
            }
            cmbGrupo.getSelectionModel().select(matchId(cmbGrupo, a.getGrupoId()));

            // Dirección/teléfonos
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
            pathFoto = a.getFoto(); pathFirma = a.getFirma();
            loadImage(imgFoto, pathFoto); loadImage(imgFirma, pathFirma);

        } catch (SQLException e) {
            showError("Error al buscar matrícula:\n" + e.getMessage());
        }
    }

    @FXML
    public void onGuardar() {
        try {
            Alumno a = buildFromForm();
            alumnoBO.guardar(a);
            showInfo("Alumno guardado correctamente");
            recargarTabla();
        } catch (Exception e) {
            showError("No se pudo guardar:\n" + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        String m = txtMatricula.getText();
        if (m==null || m.isBlank()) { showError("Indica la matrícula a eliminar"); return; }
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


    @FXML public void onSubirFoto()  { pathFoto  = pickImage(imgFoto); }
    @FXML public void onSubirFirma() { pathFirma = pickImage(imgFirma); }

    // ===== helpers =====

    private Alumno buildFromForm() {
        Alumno a = new Alumno();
        a.setMatricula(txtMatricula.getText().trim());
        a.setCurp(v(txtCurp));
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

    private Integer getId(ComboBox<Catalogo> cb){ return cb.getValue()==null? null : cb.getValue().getId(); }
    private Catalogo matchId(ComboBox<Catalogo> cb, Integer id){
        if (id==null) return null;
        for (Catalogo c: cb.getItems()) if (c.getId()==id) return c;
        return null;
    }
    private String v(TextField t){ return t.getText()==null? null : t.getText().trim(); }

    private void limpiar(boolean clearMatricula){
        if (clearMatricula) txtMatricula.clear();
        txtCurp.clear(); txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        txtCorreo.clear(); txtNss.clear();
        cmbEstatus.setValue("Activo"); cmbSemestre.getSelectionModel().clearSelection();
        cmbPeriodo.getSelectionModel().clearSelection();
        cmbEdoCivil.getSelectionModel().clearSelection();
        cmbGenero.getSelectionModel().clearSelection();
        cmbEspecialidad.getSelectionModel().clearSelection(); cmbGrupo.getItems().clear();
        dpFechaInscripcion.setValue(null);

        txtCalle.clear(); txtNumero.clear(); txtColonia.clear(); txtEstado.clear();
        txtTelefono.clear(); txtMunicipio.clear(); txtLocalidad.clear();
        txtCelPadre.clear(); txtCelMadre.clear();

        imgFoto.setImage(null); imgFirma.setImage(null); pathFoto=null; pathFirma=null;
    }

    private String pickImage(ImageView target){
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg"));
        File f = fc.showOpenDialog(target.getScene().getWindow());
        if (f!=null) { loadImage(target, f.getAbsolutePath()); return f.getAbsolutePath(); }
        return null;
    }

    private void loadImage(ImageView iv, String path){
        if (path==null || path.isBlank()) { iv.setImage(null); return; }
        iv.setImage(new Image(new File(path).toURI().toString(), false));
    }

    private void showError(String m){ new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void showInfo(String m){ new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }

    private void cargarAlumno(String matricula) {
        try {
            Alumno a = alumnoBO.buscar(matricula);
            if (a == null) {
                showError("No se encontró el alumno con matrícula: " + matricula);
                return;
            }

            // 🔹 Rellenar los campos
            txtMatricula.setText(a.getMatricula());
            txtCurp.setText(a.getCurp());
            txtNombre.setText(a.getNombre());
            txtPaterno.setText(a.getPaterno());
            txtMaterno.setText(a.getMaterno());
            txtCorreo.setText(a.getCorreo());
            txtNss.setText(a.getNss());
            cmbEstatus.setValue(a.getEstadoInscripcion());
            cmbSemestre.setValue(a.getSemestre() == null ? null : String.valueOf(a.getSemestre()));
            cmbPeriodo.getSelectionModel().select(matchId(cmbPeriodo, a.getPeriodoId()));
            cmbEdoCivil.getSelectionModel().select(matchId(cmbEdoCivil, a.getEdoCivilId()));
            cmbGenero.getSelectionModel().select(matchId(cmbGenero, a.getGeneroId()));
            dpFechaInscripcion.setValue(a.getFechaInscripcion());

            // Especialidad por nombre
            if (a.getCarrera() != null) {
                for (Catalogo c : cmbEspecialidad.getItems()) {
                    if (Objects.equals(c.getNombre(), a.getCarrera())) {
                        cmbEspecialidad.getSelectionModel().select(c);
                        break;
                    }
                }
                onEspecialidadChange();
            }
            cmbGrupo.getSelectionModel().select(matchId(cmbGrupo, a.getGrupoId()));

            // Dirección/teléfonos
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
            showError("Error al cargar alumno:\n" + e.getMessage());
        }
    }

}

