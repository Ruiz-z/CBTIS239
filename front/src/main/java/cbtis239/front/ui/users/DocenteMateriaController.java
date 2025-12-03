package cbtis239.front.ui.users;

import cbtis239.bo.DocenteMateriaBO;
import cbtis239.model.DocenteMateria;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class DocenteMateriaController {

    @FXML private ComboBox<Opcion> cmbDocente;      // id int, nombre completo
    @FXML private ComboBox<OpcionStr> cmbMateria;   // clave String, nombre

    @FXML private TableView<DocenteMateria> tblRelaciones;
    @FXML private TableColumn<DocenteMateria, String> colDocente;
    @FXML private TableColumn<DocenteMateria, String> colMateria;

    private final DocenteMateriaBO bo = new DocenteMateriaBO();
    private final ObservableList<DocenteMateria> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colDocente.setCellValueFactory(c -> c.getValue().docenteNombreProperty());
        colMateria.setCellValueFactory(c -> c.getValue().materiaNombreProperty());

        cargarCombos();
        tblRelaciones.setItems(data);
        recargarTabla();

        // Selección de fila → seleccionar en combos
        tblRelaciones.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                seleccionarDocente(n.getDocenteId());
                seleccionarMateria(n.getMateriaClave());
            }
        });
    }

    private void cargarCombos() {
        try {
            cmbDocente.setItems(FXCollections.observableArrayList(bo.listarDocentes()));
            cmbMateria.setItems(FXCollections.observableArrayList(bo.listarMaterias()));
        } catch (SQLException e) {
            mostrarError("Error cargando listas", e.getMessage());
        }
    }

    private void seleccionarDocente(int docenteId) {
        for (Opcion op : cmbDocente.getItems())
            if (op.getId() == docenteId) { cmbDocente.getSelectionModel().select(op); return; }
    }

    private void seleccionarMateria(String clave) {
        for (OpcionStr op : cmbMateria.getItems())
            if (op.getId().equals(clave)) { cmbMateria.getSelectionModel().select(op); return; }
    }

    private void recargarTabla() {
        try {
            data.clear();
            data.addAll(bo.listarRelaciones());
            tblRelaciones.refresh();
        } catch (SQLException e) {
            mostrarError("Error al cargar relaciones", e.getMessage());
        }
    }

    // ============================
    //  ASIGNAR
    // ============================
    @FXML
    private void onAsignar() {
        Opcion d = cmbDocente.getValue();
        OpcionStr m = cmbMateria.getValue();

        try {
            if (d == null || m == null)
                throw new IllegalArgumentException("Selecciona docente y materia.");

            bo.asignar(d.getId(), m.getId());
            recargarTabla();
            limpiarCampos();   // 🔥 LIMPIA LOS COMBOS

            mostrarInfo("Éxito", "Materia asignada al docente.");

        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (SQLException e) {
            mostrarError("BD", e.getMessage());
        }
    }

    // ============================
    //  ELIMINAR
    // ============================
    @FXML
    private void onEliminar() {
        DocenteMateria sel = tblRelaciones.getSelectionModel().getSelectedItem();

        try {
            if (sel == null) {
                // intentar con combos
                Opcion d = cmbDocente.getValue();
                OpcionStr m = cmbMateria.getValue();

                if (d == null || m == null)
                    throw new IllegalArgumentException("Selecciona una fila o docente y materia.");

                bo.eliminar(d.getId(), m.getId());
            } else {
                bo.eliminar(sel.getDocenteId(), sel.getMateriaClave());
            }

            recargarTabla();
            limpiarCampos();  // 🔥 LIMPIA DESPUÉS DE ELIMINAR
            mostrarInfo("Éxito", "Relación eliminada.");

        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (SQLException e) {
            mostrarError("BD", e.getMessage());
        }
    }

    // ============================
    //  LIMPIAR CAMPOS
    // ============================
    private void limpiarCampos() {
        cmbDocente.getSelectionModel().clearSelection();
        cmbMateria.getSelectionModel().clearSelection();
        tblRelaciones.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu3.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    // ============================
    //  ALERTAS
    // ============================
    private Stage getStage() {
        return (Stage) tblRelaciones.getScene().getWindow();
    }

    private void mostrarAdvertencia(String h, String c) {
        Alert a = new Alert(Alert.AlertType.WARNING, c, ButtonType.OK);
        a.setHeaderText(h);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    private void mostrarError(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText(h);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    private void mostrarInfo(String h, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.setHeaderText(h);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }
}
