package cbtis239.front.ui.users;

import cbtis239.bo.AspiranteBO;
import cbtis239.bo.PagoBO;
import cbtis239.dao.*;
import cbtis239.model.Alumno;
import cbtis239.model.Aspirante;
import cbtis239.model.Catalogo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class AspiranteController {

    // ===== Campos del formulario =====
    @FXML
    private TextField txtFolio, txtNombre, txtPaterno, txtMaterno, txtCurp;
    @FXML
    private TextField txtCorreo, txtCorreoAspirante, txtTelefono, txtCelAspirante;
    @FXML
    private TextField txtNss, txtAltura, txtPeso;
    @FXML
    private TextField txtEstado, txtMunicipio, txtLocalidad, txtCalle, txtNumero, txtColonia;
    @FXML
    private TextField txtCelPadre, txtCelMadre, txtContactoEmergencia;
    @FXML
    private TextField txtTutor1, txtTutor2, txtSecundaria, txtEstadoSec, txtMunicipioSec, txtNombreSec;
    @FXML
    private TextField txtPromedio, txtCalificacion;
    @FXML
    private ComboBox<String> cmbEstatusPago, cmbEstatusInscripcion, cmbTipoSangre;
    @FXML
    private ComboBox<Catalogo> cmbEdoCivil, cmbGenero, cmbEsp1, cmbEsp2, cmbEsp3, cmbEsp4;
    @FXML
    private DatePicker dpFechaNac, dpFechaReg;

    // ===== Tabla =====
    @FXML
    private TableView<Aspirante> tblAspirantes;
    @FXML
    private TableColumn<Aspirante, Integer> colFolio;
    @FXML
    private TableColumn<Aspirante, String> colNombre, colPaterno, colMaterno, colEstatus;

    private final AspiranteBO aspiranteBO = new AspiranteBO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO();

    // ============================================================
    // ===================== VALIDACIONES AGREGADAS ===============
    // ============================================================

    /**
     * Solo números enteros
     */
    private void soloEnteros(TextField txt) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) txt.setText(oldValue);
        });
    }

    /**
     * Solo decimal con X decimales
     */
    private void soloDecimal(TextField txt, int decimales) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            String regex = "^\\d*(\\.\\d{0," + decimales + "})?$";
            if (!newValue.matches(regex)) txt.setText(oldValue);
        });
    }

    /**
     * Solo letras y espacios (para nombre y apellidos)
     */
    private void soloLetras(TextField txt) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÁÉÍÓÚáéíóúÑñ\\s]*")) {
                txt.setText(oldValue);
            }
        });
    }

    /**
     * Limitar longitud máxima según la BD
     */
    private void limitarLongitud(TextField txt, int max) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > max) {
                txt.setText(oldValue);
            }
        });
    }

    private boolean validarCurpFormato(String curp) {
        return curp != null && curp.matches("^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[A-Z0-9][0-9]$");
    }

    private void validarObligatorios(Aspirante a) {

        if (a.getCurp() == null || a.getCurp().isBlank())
            throw new RuntimeException("La CURP es obligatoria");

        if (!validarCurpFormato(a.getCurp()))
            throw new RuntimeException("La CURP no tiene un formato válido");

        if (a.getNombre() == null || a.getNombre().isBlank())
            throw new RuntimeException("El nombre es obligatorio");

        if (a.getPaterno() == null || a.getPaterno().isBlank())
            throw new RuntimeException("El apellido paterno es obligatorio");

        if (a.getFechaNacimiento() == null)
            throw new RuntimeException("La fecha de nacimiento es obligatoria");

        if (a.getTelefono() == null || a.getTelefono().isBlank())
            throw new RuntimeException("El teléfono es obligatorio");

        if (a.getCorreo() == null || a.getCorreo().isBlank())
            throw new RuntimeException("El correo es obligatorio");
    }

    // ============================================================
    // ======================= FOLIO AUTOMÁTICO ===================
    // ============================================================

    /**
     * Calcula el siguiente folio con base en la tabla (max + 1)
     */
    private int obtenerSiguienteFolioLocal() {
        if (tblAspirantes == null || tblAspirantes.getItems() == null || tblAspirantes.getItems().isEmpty()) {
            return 1;
        }
        return tblAspirantes.getItems()
                .stream()
                .mapToInt(Aspirante::getFolio)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Asigna el folio nuevo al textbox, formateado con 3 dígitos (001, 002, ...)
     */
    private void asignarFolioNuevo() {
        int siguiente = obtenerSiguienteFolioLocal();
        txtFolio.setText(String.format("%03d", siguiente));
    }

    // ============================================================
    // =========================== initialize ======================
    // ============================================================
    @FXML
    public void initialize() {
        // =====================================================
// === FORMATO AUTOMÁTICO DECIMALES ====================
// =====================================================
        agregarFormatoDecimal(txtAltura, 2);
        agregarFormatoDecimal(txtPeso, 2);
        agregarFormatoDecimal(txtCalificacion, 2);
        agregarFormatoDecimal(txtPromedio, 2);

// =====================================================
// === VALIDAR QUE EL CORREO TENGA '@' =================
// =====================================================
        validarCorreo(txtCorreo);
        validarCorreo(txtCorreoAspirante);

// SOLO LETRAS en muchos campos
        soloLetras(txtEstado);
        soloLetras(txtMunicipio);
        soloLetras(txtLocalidad);
        soloLetras(txtCalle);
        soloLetras(txtTutor1);
        soloLetras(txtTutor2);
        soloLetras(txtSecundaria);
        soloLetras(txtEstadoSec);
        soloLetras(txtMunicipioSec);
        soloLetras(txtNombreSec);

// =====================================================
// === BLOQUEO DE ESPECIALIDADES REPETIDAS ============
// =====================================================
        cmbEsp1.valueProperty().addListener((o, oldVal, newVal) -> actualizarEspecialidades(oldVal, newVal, cmbEsp1));
        cmbEsp2.valueProperty().addListener((o, oldVal, newVal) -> actualizarEspecialidades(oldVal, newVal, cmbEsp2));
        cmbEsp3.valueProperty().addListener((o, oldVal, newVal) -> actualizarEspecialidades(oldVal, newVal, cmbEsp3));
        cmbEsp4.valueProperty().addListener((o, oldVal, newVal) -> actualizarEspecialidades(oldVal, newVal, cmbEsp4));


        // === Combos fijos ===
        cmbEstatusPago.getItems().addAll("Pendiente", "Pagado");
        cmbEstatusInscripcion.getItems().addAll("Pendiente", "Aceptado", "Rechazado");
        cmbTipoSangre.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        try {
            cmbEdoCivil.getItems().setAll(catalogoDAO.edoCivil());
            cmbGenero.getItems().setAll(catalogoDAO.generos());
            cmbEsp1.getItems().setAll(catalogoDAO.especialidades());
            cmbEsp2.getItems().setAll(catalogoDAO.especialidades());
            cmbEsp3.getItems().setAll(catalogoDAO.especialidades());
            cmbEsp4.getItems().setAll(catalogoDAO.especialidades());
        } catch (SQLException e) {
            showError("No se pudieron cargar catálogos:\n" + e.getMessage());
        }

        // === Configuración de columnas ===
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPaterno.setCellValueFactory(new PropertyValueFactory<>("paterno"));
        colMaterno.setCellValueFactory(new PropertyValueFactory<>("materno"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatusInscripcion"));

        // === Colores de estatus ===
        colEstatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Aceptado" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case "Pendiente" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "Rechazado" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // =====================================================
        // === RESTRICCIONES NUMÉRICAS ========================
        // =====================================================

        // ENTEROS estrictos
        soloEnteros(txtFolio);
        soloEnteros(txtNss);
        soloEnteros(txtTelefono);
        soloEnteros(txtCelAspirante);
        soloEnteros(txtCelPadre);
        soloEnteros(txtCelMadre);
        soloEnteros(txtContactoEmergencia);
        soloEnteros(txtNumero);

        // DECIMALES
        soloDecimal(txtAltura, 2);
        soloDecimal(txtPeso, 2);
        soloDecimal(txtCalificacion, 2);
        soloDecimal(txtPromedio, 2);

        // =====================================================
        // === LÍMITES DE LONGITUD SEGÚN LA TABLA ==============
        // =====================================================

        // clave primaria (int) – límite razonable de dígitos
        limitarLongitud(txtFolio, 11);

        // varchar(18)
        limitarLongitud(txtCurp, 18);

        // varchar(11)
        limitarLongitud(txtNss, 11);

        // varchar(15)
        limitarLongitud(txtTelefono, 15);

        // varchar(100)
        limitarLongitud(txtCorreo, 100);

        // varchar(45)
        limitarLongitud(txtCorreoAspirante, 45);
        limitarLongitud(txtNombre, 45);
        limitarLongitud(txtPaterno, 45);
        limitarLongitud(txtMaterno, 45);
        limitarLongitud(txtCalle, 45);
        limitarLongitud(txtColonia, 45);
        limitarLongitud(txtContactoEmergencia, 45);
        limitarLongitud(txtTutor1, 45);
        limitarLongitud(txtTutor2, 45);
        limitarLongitud(txtSecundaria, 45);
        limitarLongitud(txtEstadoSec, 45);
        limitarLongitud(txtMunicipioSec, 45);
        limitarLongitud(txtNombreSec, 45);

        // varchar(50)
        limitarLongitud(txtEstado, 50);
        limitarLongitud(txtMunicipio, 50);
        limitarLongitud(txtLocalidad, 50);

        // varchar(10)
        limitarLongitud(txtNumero, 10);

        // varchar(12)
        limitarLongitud(txtCelPadre, 12);
        limitarLongitud(txtCelMadre, 12);
        limitarLongitud(txtCelAspirante, 12);

        // decimales: longitud total (enteros + '.' + decimales)
        // decimal(3,2) -> 3 enteros + '.' + 2 decimales = 6
        limitarLongitud(txtAltura, 6);

        // decimal(5,2) y float -> 5 enteros + '.' + 2 decimales = 8
        limitarLongitud(txtPeso, 8);
        limitarLongitud(txtCalificacion, 8);
        limitarLongitud(txtPromedio, 8);

        // =====================================================
        // === SOLO LETRAS EN NOMBRE Y APELLIDOS ===============
        // =====================================================
        soloLetras(txtNombre);
        soloLetras(txtPaterno);
        soloLetras(txtMaterno);

        // =====================================================
        // === CURP SIEMPRE EN MAYÚSCULAS ======================
        // =====================================================
        txtCurp.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                String upper = newValue.toUpperCase();
                if (!upper.equals(newValue)) {
                    txtCurp.setText(upper);
                }
            }
        });

        // =====================================================
        // === FECHAS SOLO MEDIANTE CALENDARIO =================
        // =====================================================
        dpFechaNac.setEditable(false);
        dpFechaNac.getEditor().setDisable(true);
        dpFechaReg.setEditable(false);
        dpFechaReg.getEditor().setDisable(true);

        // === Cargar lista de aspirantes existentes ===
        recargarTabla();

        // === Asignar folio automático para nuevo registro ===
        asignarFolioNuevo();

        // === Doble clic en tabla ===
        tblAspirantes.setRowFactory(tv -> {
            TableRow<Aspirante> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    cargarAspirante(row.getItem().getFolio());
                }
            });
            return row;
        });

        tblAspirantes.setPlaceholder(new Label("No hay aspirantes registrados."));
    }

    // ============================================================
    // ======================== recargar tabla =====================
    // ============================================================
    private void recargarTabla() {
        try {
            List<Aspirante> data = aspiranteBO.listarBreve();
            tblAspirantes.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            e.printStackTrace();
            tblAspirantes.setItems(FXCollections.observableArrayList());
        }
    }

    // ============================================================
    // ======================== onBuscarFolio ======================
    // ============================================================
    @FXML
    public void onBuscarFolio() {
        String f = txtFolio.getText();
        if (f == null || f.isBlank()) return;

        try {
            Aspirante a = aspiranteBO.buscar(Integer.parseInt(f.trim()));
            if (a == null) {
                limpiar(false);
                showInfo("No se encontró el aspirante con folio " + f);
                return;
            }
            rellenarCampos(a);
        } catch (SQLException e) {
            showError("Error al buscar folio:\n" + e.getMessage());
        } catch (NumberFormatException e) {
            showError("El folio debe ser un número válido");
        }
    }

    // ============================================================
    // =========================== onInscribir =====================
    // ============================================================
    @FXML
    private void onInscribir() {
        try {

            Aspirante selTabla = tblAspirantes.getSelectionModel().getSelectedItem();
            if (selTabla == null) {
                showWarning("Selecciona un aspirante de la tabla para inscribir.");
                return;
            }

            AspiranteBO aspiranteBO = new AspiranteBO();
            Aspirante sel = aspiranteBO.buscar(selTabla.getFolio());
            if (sel == null) {
                showError("No se encontró el aspirante en la base de datos.");
                return;
            }

            if (sel.getEstatusPago() == null || !sel.getEstatusPago().equalsIgnoreCase("Pagado")) {
                showWarning("El aspirante NO ha pagado. Solo los aspirantes con pago pueden inscribirse.");
                return;
            }

            GrupoDao grupoDAO = new GrupoDao();
            AlumnoDAO alumnoDAO = new AlumnoDAO();

            Integer[] opciones = {
                    sel.getOpcionEspecialidad1(),
                    sel.getOpcionEspecialidad2(),
                    sel.getOpcionEspecialidad3(),
                    sel.getOpcionEspecialidad4()
            };

            Integer idEspecialidadFinal = null;
            Integer idGrupoAsignado = null;

            for (Integer idEsp : opciones) {
                if (idEsp == null || idEsp == 0) continue;

                Integer grupoDisponible = grupoDAO.grupoDisponible(idEsp);
                if (grupoDisponible != null) {
                    idEspecialidadFinal = idEsp;
                    idGrupoAsignado = grupoDisponible;
                    break;
                }
            }

            if (idGrupoAsignado == null) {
                showError("No hay cupo en ninguna de las especialidades seleccionadas.");
                return;
            }

            AlumnoDAO alumnoDAO2 = new AlumnoDAO();
            int año = LocalDate.now().getYear() % 100;
            int consecutivo = alumnoDAO2.obtenerConsecutivo(año, idEspecialidadFinal) + 1;
            String matricula = String.format("%02d%02d%03d", año, idEspecialidadFinal, consecutivo);

            while (alumnoDAO2.existe(matricula)) {
                consecutivo++;
                matricula = String.format("%02d%02d%03d", año, idEspecialidadFinal, consecutivo);
            }

            // Crear alumno
            Alumno nuevo = new Alumno();
            nuevo.setMatricula(matricula);
            nuevo.setCurp(sel.getCurp());
            nuevo.setNombre(sel.getNombre());
            nuevo.setPaterno(sel.getPaterno());
            nuevo.setMaterno(sel.getMaterno());
            nuevo.setTelefono(sel.getTelefono());
            nuevo.setCorreo(sel.getCorreo());
            nuevo.setNss(sel.getNss());
            nuevo.setCalle(sel.getCalle());
            nuevo.setNumero(sel.getNumero());
            nuevo.setColonia(sel.getColonia());
            nuevo.setEstado(sel.getEstado());
            nuevo.setMunicipio(sel.getMunicipio());
            nuevo.setLocalidad(sel.getLocalidad());
            nuevo.setCelPadre(sel.getCelPadre());
            nuevo.setCelMadre(sel.getCelMadre());
            nuevo.setEdoCivilId(sel.getEdoCivilId());
            nuevo.setGeneroId(sel.getGeneroId());
            nuevo.setSemestre(1);
            nuevo.setEstadoInscripcion("Activo");
            nuevo.setFechaInscripcion(LocalDate.now());
            nuevo.setCarrera(String.valueOf(idEspecialidadFinal));
            nuevo.setGrupoId(idGrupoAsignado);
            int periodoPago = new cbtis239.dao.PagoDAO().getPeriodoActualId();
            nuevo.setPeriodoId(periodoPago);

            alumnoDAO.insert(nuevo);

            try (var cn = cbtis239.util.DB.get()) {
                cn.setAutoCommit(false);
                try {
                    int folio = sel.getFolio();
                    new PagoDAO().deleteByAspiranteFolio(cn, folio);
                    new AspiranteDAO().deleteByFolio(cn, folio);
                    cn.commit();
                } catch (SQLException ex) {
                    cn.rollback();
                    throw ex;
                } finally {
                    cn.setAutoCommit(true);
                }
            }

            try {
                new PagoBO().registrarPago(matricula);
            } catch (Exception ignored) {
            }

            showInfo("Aspirante inscrito correctamente.\nMatrícula: " + matricula);
            recargarTabla();
            limpiar(true);

        } catch (Exception e) {
            showError("Error al inscribir:\n" + e.getMessage());
        }
    }

    // ============================================================
    // ======================== generarMatricula ===================
    // ============================================================
    private String generarMatricula(int idEspecialidad) throws SQLException {
        int año = LocalDate.now().getYear() % 100;
        AlumnoDAO alumnoDAO = new AlumnoDAO();
        int consecutivo = alumnoDAO.obtenerConsecutivo(año, idEspecialidad) + 1;
        return String.format("%02d%02d%03d", año, idEspecialidad, consecutivo);
    }

    // ============================================================
    // ============================= onGuardar =====================
    // ============================================================
    @FXML
    public void onGuardar() {
        try {
            Aspirante a = buildFromForm();
            validarObligatorios(a);

            // Validar que el folio no exista ya (no duplicado)
            Aspirante existente = aspiranteBO.buscar(a.getFolio());
            if (existente != null) {
                showError("Ya existe un aspirante con el folio " + a.getFolio());
                return;
            }

            aspiranteBO.guardar(a);
            showInfo("Aspirante guardado correctamente");
            recargarTabla();
            limpiar(true);

        } catch (Exception e) {
            showError("No se pudo guardar:\n" + e.getMessage());
        }
    }

    // ============================================================
    // =========================== onModificar =====================
    // ============================================================
    @FXML
    private void onModificar() {
        try {
            Aspirante seleccionado = tblAspirantes.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                showError("Selecciona un aspirante de la tabla antes de modificar.");
                return;
            }

            Aspirante actualizado = buildFromForm();

            if (!Objects.equals(actualizado.getFolio(), seleccionado.getFolio())) {
                showError("No puedes cambiar el Folio (clave primaria).");
                return;
            }

            validarObligatorios(actualizado);
            aspiranteBO.guardar(actualizado);

            showInfo("Aspirante modificado correctamente: " + actualizado.getNombre());
            recargarTabla();
            limpiar(true);

        } catch (Exception e) {
            showError("No se pudo modificar:\n" + e.getMessage());
        }
    }

    // ============================================================
    // ============================= onEliminar ====================
    // ============================================================
    @FXML
    public void onEliminar() {
        String f = txtFolio.getText();
        if (f == null || f.isBlank()) {
            showError("Indica el folio a eliminar");
            return;
        }
        try {
            aspiranteBO.eliminar(Integer.parseInt(f.trim()));
            showInfo("Aspirante eliminado");
            limpiar(true);
            recargarTabla();
        } catch (SQLException e) {
            showError("No se pudo eliminar:\n" + e.getMessage());
        }
    }

    // ============================================================
    // ============================= onCancelar ====================
    // ============================================================
    @FXML
    public void onCancelar() {
        limpiar(true);
    }

    // ============================================================
    // =============================== onVolver ====================
    // ============================================================
    @FXML
    public void onVolver() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setMaximized(true);
            st.show();
            ((Stage) tblAspirantes.getScene().getWindow()).close();
        } catch (Exception e) {
            showError("No se pudo abrir el menú:\n" + e.getMessage());
        }
    }

    // ============================================================
    // =========================== buildFromForm ===================
    // ============================================================
    private Aspirante buildFromForm() {
        Aspirante a = new Aspirante();
        a.setFolio(Integer.parseInt(txtFolio.getText().trim()));

        // CURP siempre en mayúsculas
        String curp = v(txtCurp);
        a.setCurp(curp == null ? null : curp.toUpperCase());

        a.setNombre(v(txtNombre));
        a.setPaterno(v(txtPaterno));
        a.setMaterno(v(txtMaterno));
        a.setFechaNacimiento(dpFechaNac.getValue());
        a.setNss(v(txtNss));
        a.setAltura(parseDouble(txtAltura));
        a.setPeso(parseDouble(txtPeso));
        a.setTelefono(v(txtTelefono));
        a.setCorreo(v(txtCorreo));
        a.setCorreoAspirante(v(txtCorreoAspirante));
        a.setCelAspirante(v(txtCelAspirante));
        a.setContactoEmergencia(v(txtContactoEmergencia));
        a.setCalificacionExamenIngreso(parseDouble(txtCalificacion));
        a.setPromedioFinal(parseFloat(txtPromedio));
        a.setEstatusPago(cmbEstatusPago.getValue());
        a.setEstatusInscripcion(cmbEstatusInscripcion.getValue());
        a.setFechaRegistro(dpFechaReg.getValue());
        a.setEdoCivilId(getId(cmbEdoCivil));
        a.setGeneroId(getId(cmbGenero));

        a.setOpcionEspecialidad1(getId(cmbEsp1));
        a.setOpcionEspecialidad2(getId(cmbEsp2));
        a.setOpcionEspecialidad3(getId(cmbEsp3));
        a.setOpcionEspecialidad4(getId(cmbEsp4));

        a.setCalle(v(txtCalle));
        a.setNumero(v(txtNumero));
        a.setColonia(v(txtColonia));
        a.setEstado(v(txtEstado));
        a.setMunicipio(v(txtMunicipio));
        a.setLocalidad(v(txtLocalidad));
        a.setCelPadre(v(txtCelPadre));
        a.setCelMadre(v(txtCelMadre));
        a.setTutor1(v(txtTutor1));
        a.setTutor2(v(txtTutor2));
        a.setSecundaria(v(txtSecundaria));
        a.setEstadoSec(v(txtEstadoSec));
        a.setMunicipioSec(v(txtMunicipioSec));
        a.setNombreSEC(v(txtNombreSec));
        return a;
    }

    // ============================================================
    // =========================== rellenarCampos ==================
    // ============================================================
    private void rellenarCampos(Aspirante a) {
        txtFolio.setText(String.format("%03d", a.getFolio()));
        txtCurp.setText(a.getCurp());
        txtNombre.setText(a.getNombre());
        txtPaterno.setText(a.getPaterno());
        txtMaterno.setText(a.getMaterno());
        dpFechaNac.setValue(a.getFechaNacimiento());
        txtNss.setText(a.getNss());
        txtAltura.setText(String.valueOf(a.getAltura()));
        txtPeso.setText(String.valueOf(a.getPeso()));
        txtTelefono.setText(a.getTelefono());
        txtCorreo.setText(a.getCorreo());
        txtCorreoAspirante.setText(a.getCorreoAspirante());
        txtCelAspirante.setText(a.getCelAspirante());
        txtContactoEmergencia.setText(a.getContactoEmergencia());
        txtCalificacion.setText(String.valueOf(a.getCalificacionExamenIngreso()));
        txtPromedio.setText(String.valueOf(a.getPromedioFinal()));
        cmbEstatusPago.setValue(a.getEstatusPago());
        cmbEstatusInscripcion.setValue(a.getEstatusInscripcion());
        dpFechaReg.setValue(a.getFechaRegistro());

        cmbEdoCivil.getSelectionModel().select(matchId(cmbEdoCivil, a.getEdoCivilId()));
        cmbGenero.getSelectionModel().select(matchId(cmbGenero, a.getGeneroId()));

        cmbEsp1.getSelectionModel().select(matchId(cmbEsp1, a.getOpcionEspecialidad1()));
        cmbEsp2.getSelectionModel().select(matchId(cmbEsp2, a.getOpcionEspecialidad2()));
        cmbEsp3.getSelectionModel().select(matchId(cmbEsp3, a.getOpcionEspecialidad3()));
        cmbEsp4.getSelectionModel().select(matchId(cmbEsp4, a.getOpcionEspecialidad4()));

        txtCalle.setText(a.getCalle());
        txtNumero.setText(a.getNumero());
        txtColonia.setText(a.getColonia());
        txtEstado.setText(a.getEstado());
        txtMunicipio.setText(a.getMunicipio());
        txtLocalidad.setText(a.getLocalidad());
        txtCelPadre.setText(a.getCelPadre());
        txtCelMadre.setText(a.getCelMadre());
        txtTutor1.setText(a.getTutor1());
        txtTutor2.setText(a.getTutor2());
        txtSecundaria.setText(a.getSecundaria());
        txtEstadoSec.setText(a.getEstadoSec());
        txtMunicipioSec.setText(a.getMunicipioSec());
        txtNombreSec.setText(a.getNombreSEC());
    }

    // ============================================================
    // =============================== limpiar =====================
    // ============================================================
    private void limpiar(boolean clearFolio) {
        if (clearFolio) txtFolio.clear();
        txtCurp.clear();
        txtNombre.clear();
        txtPaterno.clear();
        txtMaterno.clear();
        txtCorreo.clear();
        txtCorreoAspirante.clear();
        txtTelefono.clear();
        txtCelAspirante.clear();
        txtNss.clear();
        txtAltura.clear();
        txtPeso.clear();
        txtEstado.clear();
        txtMunicipio.clear();
        txtLocalidad.clear();
        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtCelPadre.clear();
        txtCelMadre.clear();
        txtContactoEmergencia.clear();
        txtTutor1.clear();
        txtTutor2.clear();
        txtSecundaria.clear();
        txtEstadoSec.clear();
        txtMunicipioSec.clear();
        txtNombreSec.clear();
        txtPromedio.clear();
        txtCalificacion.clear();
        cmbEstatusPago.getSelectionModel().clearSelection();
        cmbEstatusInscripcion.getSelectionModel().clearSelection();
        cmbEdoCivil.getSelectionModel().clearSelection();
        cmbGenero.getSelectionModel().clearSelection();
        cmbEsp1.getSelectionModel().clearSelection();
        cmbEsp2.getSelectionModel().clearSelection();
        cmbEsp3.getSelectionModel().clearSelection();
        cmbEsp4.getSelectionModel().clearSelection();
        dpFechaNac.setValue(null);
        dpFechaReg.setValue(null);

        // Si se limpia todo para un nuevo registro, asignar folio automático
        if (clearFolio) {
            asignarFolioNuevo();
        }
    }

    // ============================================================
    // =============================== helpers =====================
    // ============================================================
    private String v(TextField t) {
        return t.getText() == null ? null : t.getText().trim();
    }

    private Double parseDouble(TextField t) {
        return t.getText().isBlank() ? null : Double.valueOf(t.getText());
    }

    private Float parseFloat(TextField t) {
        return t.getText().isBlank() ? null : Float.valueOf(t.getText());
    }

    private Integer getId(ComboBox<Catalogo> cb) {
        return cb.getValue() == null ? null : cb.getValue().getId();
    }

    private Catalogo matchId(ComboBox<Catalogo> cb, Integer id) {
        if (id == null) return null;
        for (Catalogo c : cb.getItems()) if (c.getId() == id) return c;
        return null;
    }

    private void showError(String m) {
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }

    private void showInfo(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }

    private void showWarning(String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }

    private void cargarAspirante(int folio) {
        try {
            Aspirante a = aspiranteBO.buscar(folio);
            if (a == null) {
                showError("No se encontró el aspirante con folio: " + folio);
                return;
            }
            rellenarCampos(a);
        } catch (SQLException e) {
            showError("Error al cargar aspirante:\n" + e.getMessage());
        }
    }

    private void agregarFormatoDecimal(TextField txt, int decimales) {
        txt.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // cuando pierde el foco
                if (!txt.getText().isBlank()) {
                    try {
                        double d = Double.parseDouble(txt.getText());
                        txt.setText(String.format("%." + decimales + "f", d));
                    } catch (Exception e) {
                        txt.setText(String.format("%." + decimales + "f", 0.0));
                    }
                }
            }
        });
    }

    private void validarCorreo(TextField txt) {
        txt.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) { // pierde foco
                String c = txt.getText();
                if (c != null && !c.isBlank() && !c.contains("@")) {
                    showWarning("El correo \"" + c + "\" no es válido. Debe contener '@'");
                    txt.requestFocus();
                }
            }
        });
    }

    // =============================================
// === EVITAR ESPECIALIDADES REPETIDAS CORREGIDO
// =============================================
    private List<Catalogo> especialidadesOriginales;

    // =============================================
// === EVITAR ESPECIALIDADES REPETIDAS CORREGIDO
// =============================================
    private void actualizarEspecialidades(Catalogo oldVal, Catalogo newVal, ComboBox<Catalogo> origen) {

        // Si no eligió nada, no hacemos nada.
        if (newVal == null) return;

        int id = newVal.getId();

        // Verificamos si está repetida
        boolean repetida =
                (cmbEsp1 != origen && cmbEsp1.getValue() != null && cmbEsp1.getValue().getId() == id) ||
                        (cmbEsp2 != origen && cmbEsp2.getValue() != null && cmbEsp2.getValue().getId() == id) ||
                        (cmbEsp3 != origen && cmbEsp3.getValue() != null && cmbEsp3.getValue().getId() == id) ||
                        (cmbEsp4 != origen && cmbEsp4.getValue() != null && cmbEsp4.getValue().getId() == id);

        if (repetida) {

            showWarning("La especialidad seleccionada ya fue elegida en otra opción.");

            // 🔥 Solución: limpiar el ComboBox pero SIN gatillar conflicto interno
            javafx.application.Platform.runLater(() -> {
                origen.getSelectionModel().clearSelection();
                origen.setValue(null);
            });
        }
    }

}
