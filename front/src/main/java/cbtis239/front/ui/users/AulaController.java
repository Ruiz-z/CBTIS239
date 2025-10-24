package cbtis239.front.ui.users;

import cbtis239.bo.AulaBO;
import cbtis239.model.Aula;
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
            showError("Error al cargar aulas:\n" + ex.getMessage());
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
            showInfo("Aula agregada correctamente.");
        } catch (NumberFormatException e) {
            showError("La capacidad debe ser un número válido.");
        } catch (Exception ex) {
            showError("Error al agregar aula:\n" + ex.getMessage());
        }
    }

    @FXML private void onEliminar() {
        Aula sel = tblAulas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Selecciona un aula para eliminar.");
            return;
        }
        try {
            bo.eliminar(sel.getClave());
            recargarTabla();
            limpiarFormulario();
            showInfo("Aula eliminada correctamente.");
        } catch (Exception ex) {
            showError("Error al eliminar aula:\n" + ex.getMessage());
        }
    }

    @FXML private void onModificar() {
        Aula sel = tblAulas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Selecciona un aula para modificar.");
            return;
        }
        try {
            int capacidad = Integer.parseInt(txtCapacidad.getText());
            bo.modificar(new Aula(sel.getClave(), capacidad));
            recargarTabla();
            limpiarFormulario();
            showInfo("Aula modificada correctamente.");
        } catch (NumberFormatException e) {
            showError("Capacidad inválida, debe ser un número.");
        } catch (Exception ex) {
            showError("Error al modificar aula:\n" + ex.getMessage());
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
            showError("No se pudo volver al menú:\n" + e.getMessage());
        }
    }

    // ====== Helpers de Alert con owner y modalidad ======
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

    private void showError(String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText("Error");
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void showInfo(String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.setHeaderText(null);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

}
