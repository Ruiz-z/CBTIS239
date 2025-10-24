package cbtis239.front.ui.users;

import cbtis239.bo.AspiranteBO;
import cbtis239.dao.CatalogoDAO;
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
    @FXML private TextField txtFolio, txtNombre, txtPaterno, txtMaterno, txtCurp;
    @FXML private TextField txtCorreo, txtCorreoAspirante, txtTelefono, txtCelAspirante;
    @FXML private TextField txtNss, txtTipoSangre, txtAltura, txtPeso;
    @FXML private TextField txtEstado, txtMunicipio, txtLocalidad, txtCalle, txtNumero, txtColonia;
    @FXML private TextField txtCelPadre, txtCelMadre, txtContactoEmergencia;
    @FXML private TextField txtTutor1, txtTutor2, txtSecundaria, txtEstadoSec, txtMunicipioSec, txtNombreSec;
    @FXML private TextField txtPromedio, txtCalificacion;
    @FXML private ComboBox<String> cmbEstatusPago, cmbEstatusInscripcion;
    @FXML private ComboBox<Catalogo> cmbEdoCivil, cmbGenero, cmbEsp1, cmbEsp2, cmbEsp3, cmbEsp4;
    @FXML private DatePicker dpFechaNac, dpFechaReg;

    // ===== Tabla =====
    @FXML private TableView<Aspirante> tblAspirantes;
    @FXML private TableColumn<Aspirante, Integer> colFolio;
    @FXML private TableColumn<Aspirante, String> colNombre, colPaterno, colMaterno, colEstatus;

    private final AspiranteBO aspiranteBO = new AspiranteBO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO();

    @FXML
    public void initialize() {

        // === Combos fijos ===
        cmbEstatusPago.getItems().addAll("Pendiente", "Pagado");
        cmbEstatusInscripcion.getItems().addAll("Pendiente", "Aceptado", "Rechazado");

        try {
            // === Carga de catálogos desde BD ===
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

        // === Colores de estatus (opcional) ===
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

        // === Cargar lista de aspirantes existentes ===
        recargarTabla();

        // === Doble clic en tabla ===
        tblAspirantes.setRowFactory(tv -> {
            TableRow<Aspirante> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Aspirante seleccionado = row.getItem();
                    if (seleccionado != null) {
                        cargarAspirante(seleccionado.getFolio());
                    }
                }
            });
            return row;
        });

        // === Mensaje si no hay datos ===
        tblAspirantes.setPlaceholder(new Label("No hay aspirantes registrados."));

    }

    private void recargarTabla() {
        try {
            List<Aspirante> data = aspiranteBO.listarBreve();
            tblAspirantes.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            e.printStackTrace();
            tblAspirantes.setItems(FXCollections.observableArrayList());
        }
    }



    // ======== Eventos ========

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


    @FXML
    public void onGuardar() {
        try {
            Aspirante a = buildFromForm();
            aspiranteBO.guardar(a);
            showInfo("Aspirante guardado correctamente");
            recargarTabla();
        } catch (Exception e) {
            showError("No se pudo guardar:\n" + e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        try {
            // Verificar que haya un aspirante seleccionado en la tabla
            Aspirante seleccionado = tblAspirantes.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                showError("Selecciona un aspirante de la tabla antes de modificar.");
                return;
            }

            // Construir objeto desde los campos actuales del formulario
            Aspirante actualizado = buildFromForm();

            // Validar que el folio coincida (PK)
            if (!Objects.equals(actualizado.getFolio(), seleccionado.getFolio())) {
                showError("No puedes cambiar el Folio (clave primaria). Debes modificar los datos del aspirante seleccionado.");
                return;
            }

            // Ejecutar la actualización
            aspiranteBO.guardar(actualizado); // Tu BO ya detecta si existe y hace update
            showInfo("Aspirante modificado correctamente: " + actualizado.getNombre());

            // Refrescar tabla y limpiar formulario
            recargarTabla();
            limpiar(true);

        } catch (SQLException e) {
            showError("Error de base de datos al modificar:\n" + e.getMessage());
        } catch (Exception e) {
            showError("No se pudo modificar el aspirante:\n" + e.getMessage());
        }
    }


    @FXML
    public void onEliminar() {
        String f = txtFolio.getText();
        if (f == null || f.isBlank()) { showError("Indica el folio a eliminar"); return; }
        try {
            aspiranteBO.eliminar(Integer.parseInt(f.trim()));
            showInfo("Aspirante eliminado");
            limpiar(true);
            recargarTabla();
        } catch (SQLException e) {
            showError("No se pudo eliminar:\n" + e.getMessage());
        }
    }

    @FXML public void onCancelar() { limpiar(true); }

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


    // ===== helpers =====

    private Aspirante buildFromForm() {
        Aspirante a = new Aspirante();
        a.setFolio(Integer.parseInt(txtFolio.getText().trim()));
        a.setCurp(v(txtCurp));
        a.setNombre(v(txtNombre));
        a.setPaterno(v(txtPaterno));
        a.setMaterno(v(txtMaterno));
        a.setFechaNacimiento(dpFechaNac.getValue());
        a.setNss(v(txtNss));
        a.setTipoSangre(v(txtTipoSangre));
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

    private void rellenarCampos(Aspirante a) {
        txtFolio.setText(String.valueOf(a.getFolio()));
        txtCurp.setText(a.getCurp());
        txtNombre.setText(a.getNombre());
        txtPaterno.setText(a.getPaterno());
        txtMaterno.setText(a.getMaterno());
        dpFechaNac.setValue(a.getFechaNacimiento());
        txtNss.setText(a.getNss());
        txtTipoSangre.setText(a.getTipoSangre());
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

    private void limpiar(boolean clearFolio) {
        if (clearFolio) txtFolio.clear();
        txtCurp.clear(); txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        txtCorreo.clear(); txtCorreoAspirante.clear(); txtTelefono.clear(); txtCelAspirante.clear();
        txtNss.clear(); txtTipoSangre.clear(); txtAltura.clear(); txtPeso.clear();
        txtEstado.clear(); txtMunicipio.clear(); txtLocalidad.clear(); txtCalle.clear(); txtNumero.clear(); txtColonia.clear();
        txtCelPadre.clear(); txtCelMadre.clear(); txtContactoEmergencia.clear();
        txtTutor1.clear(); txtTutor2.clear(); txtSecundaria.clear(); txtEstadoSec.clear(); txtMunicipioSec.clear(); txtNombreSec.clear();
        txtPromedio.clear(); txtCalificacion.clear();
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
    }

    private String v(TextField t) { return t.getText() == null ? null : t.getText().trim(); }
    private Double parseDouble(TextField t) { return t.getText().isBlank() ? null : Double.valueOf(t.getText()); }
    private Float parseFloat(TextField t) { return t.getText().isBlank() ? null : Float.valueOf(t.getText()); }

    private Integer getId(ComboBox<Catalogo> cb) { return cb.getValue() == null ? null : cb.getValue().getId(); }
    private Catalogo matchId(ComboBox<Catalogo> cb, Integer id) {
        if (id == null) return null;
        for (Catalogo c : cb.getItems()) if (c.getId() == id) return c;
        return null;
    }

    private void showError(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }

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
}
