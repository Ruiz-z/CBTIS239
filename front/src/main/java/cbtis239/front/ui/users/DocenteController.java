package cbtis239.front.ui.users;

import cbtis239.bo.DocenteBO;
import cbtis239.model.Docente;
import cbtis239.model.Opcion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class DocenteController {

    // Form
    @FXML private TextField txtCurp, txtCorreo, txtNss, txtNombre, txtPaterno, txtMaterno, txtTelefono, txtCelular;
    @FXML private ComboBox<Opcion> cmbEdoCivil, cmbGenero;

    // Tabla
    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colNombreCompleto, colGenero, colCelular;

    private final DocenteBO bo = new DocenteBO();
    private final ObservableList<Docente> data = FXCollections.observableArrayList();

    private Docente seleccionado; // para modificar/eliminar

    @FXML
    private void initialize() {
        // Columnas
        colNombreCompleto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreCompleto()));
        colGenero.setCellValueFactory(c -> c.getValue().generoNombreProperty());
        colCelular.setCellValueFactory(c -> c.getValue().celularProperty());

        // Cargar combos
        cargarCombos();

        // Selección de tabla → formulario
        tblDocentes.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            seleccionado = n;
            if (n != null) {
                txtCurp.setText(n.getCurp());
                txtCorreo.setText(n.getCorreo());
                txtNss.setText(n.getNss());
                txtNombre.setText(n.getNombre());
                txtPaterno.setText(n.getPaterno());
                txtMaterno.setText(n.getMaterno());
                txtTelefono.setText(n.getTelefono());
                txtCelular.setText(n.getCelular());
                seleccionarEnCombo(cmbEdoCivil, n.getIdEdoCivil());
                seleccionarEnCombo(cmbGenero, n.getIdGenero());
            } else {
                limpiarFormulario();
            }
        });

        tblDocentes.setItems(data);
        recargarTabla();
    }

    private void cargarCombos() {
        try {
            cmbEdoCivil.setItems(FXCollections.observableArrayList(bo.listarEdoCivil()));
            cmbGenero.setItems(FXCollections.observableArrayList(bo.listarGeneros()));
        } catch (SQLException e) {
            mostrarError("Error cargando catálogos", e.getMessage());
        }
    }

    private void seleccionarEnCombo(ComboBox<Opcion> combo, int id) {
        if (combo.getItems() == null) return;
        for (Opcion op : combo.getItems()) {
            if (op.getId() == id) { combo.getSelectionModel().select(op); return; }
        }
        combo.getSelectionModel().clearSelection();
    }

    private void recargarTabla() {
        try {
            data.clear();
            data.addAll(bo.listar());
            tblDocentes.refresh();
        } catch (SQLException e) {
            mostrarError("Error al cargar docentes", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtCurp.clear(); txtCorreo.clear(); txtNss.clear(); txtNombre.clear();
        txtPaterno.clear(); txtMaterno.clear(); txtTelefono.clear(); txtCelular.clear();
        cmbEdoCivil.getSelectionModel().clearSelection();
        cmbGenero.getSelectionModel().clearSelection();
        tblDocentes.getSelectionModel().clearSelection();
        seleccionado = null;
    }

    // ===== Acciones =====
    @FXML
    private void onAgregar() {
        try {
            Docente d = buildFromForm(0);
            bo.agregar(d);
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Docente agregado.");
        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (SQLException e) {
            mostrarError("BD", e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        if (seleccionado == null) { mostrarAdvertencia("Selección", "Selecciona un docente."); return; }
        try {
            Docente d = buildFromForm(seleccionado.getDocenteId());
            bo.modificar(d);
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Docente modificado.");
        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (SQLException e) {
            mostrarError("BD", e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        if (seleccionado == null) { mostrarAdvertencia("Selección", "Selecciona un docente."); return; }
        try {
            bo.eliminar(seleccionado.getDocenteId());
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Docente eliminado.");
        } catch (SQLException e) {
            mostrarError("BD", e.getMessage());
        }
    }

    private Docente buildFromForm(int docenteId) {
        Opcion edo = cmbEdoCivil.getValue();
        Opcion gen = cmbGenero.getValue();
        if (edo == null || gen == null) throw new IllegalArgumentException("Selecciona Estado Civil y Género.");

        Docente d = new Docente();
        d.setDocenteId(docenteId);
        d.setCurp(txtCurp.getText());
        d.setCorreo(txtCorreo.getText());
        d.setNss(txtNss.getText());
        d.setNombre(txtNombre.getText());
        d.setPaterno(txtPaterno.getText());
        d.setMaterno(txtMaterno.getText());
        d.setTelefono(txtTelefono.getText());
        d.setCelular(txtCelular.getText());
        d.setIdEdoCivil(edo.getId());
        d.setIdGenero(gen.getId());
        d.setGeneroNombre(gen.getNombre());
        return d;
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Menú 2");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
        }
    }

    // ===== Alerts =====
    private void mostrarAdvertencia(String h, String c) { new Alert(Alert.AlertType.WARNING, c, ButtonType.OK).showAndWait(); }
    private void mostrarError(String h, String c)       { new Alert(Alert.AlertType.ERROR, c, ButtonType.OK).showAndWait(); }
    private void mostrarInfo(String h, String c)        { new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK).showAndWait(); }
}

