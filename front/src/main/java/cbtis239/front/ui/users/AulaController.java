package cbtis239.front.ui.users;

import cbtis239.bo.AulaBO;
import cbtis239.model.Aula;
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

public class AulaController {

    @FXML private TextField txtClave;
    @FXML private TextField txtCapacidad;
    @FXML private TableView<Aula> tblAulas;
    @FXML private TableColumn<Aula, String> colClave;
    @FXML private TableColumn<Aula, Number> colCapacidad;

    private final AulaBO bo = new AulaBO();
    private final ObservableList<Aula> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colClave.setCellValueFactory(c -> c.getValue().claveProperty());
        colCapacidad.setCellValueFactory(c -> c.getValue().capacidadProperty());

        txtCapacidad.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("\\d*")) {
                txtCapacidad.setText(val.replaceAll("[^\\d]", ""));
            }
        });

        tblAulas.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                txtClave.setText(n.getClave());
                txtCapacidad.setText(String.valueOf(n.getCapacidad()));
                txtClave.setDisable(true);
            } else limpiarFormulario();
        });

        tblAulas.setItems(data);
        recargarTabla();
    }

    private void recargarTabla() {
        try {
            data.setAll(bo.listar());
        } catch (SQLException ex) {
            mostrarError("Error al cargar aulas", ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtClave.clear();
        txtCapacidad.clear();
        txtClave.setDisable(false);
        tblAulas.getSelectionModel().clearSelection();
    }

    @FXML private void onAgregar() {
        try {
            String clave = txtClave.getText();
            int capacidad = Integer.parseInt(txtCapacidad.getText());
            bo.agregar(new Aula(clave, capacidad));
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Aula agregada correctamente.");
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Validación", "La capacidad debe ser un número válido.");
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }

    @FXML private void onEliminar() {
        Aula sel = tblAulas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAdvertencia("Selección", "Selecciona un aula."); return; }
        try {
            bo.eliminar(sel.getClave());
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Aula eliminada.");
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }

    @FXML private void onModificar() {
        Aula sel = tblAulas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAdvertencia("Selección", "Selecciona un aula."); return; }
        try {
            int capacidad = Integer.parseInt(txtCapacidad.getText());
            bo.modificar(new Aula(sel.getClave(), capacidad));
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Aula modificada.");
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Validación", "Capacidad inválida.");
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
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
        }
    }


    private void limpiarCampos() {
        txtClave.clear();
        txtCapacidad.clear();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
