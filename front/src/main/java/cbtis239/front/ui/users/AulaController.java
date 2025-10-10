package cbtis239.front.ui.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AulaController {

    @FXML
    private TextField txtClave;
    @FXML
    private TextField txtCapacidad;

    @FXML
    private TableView<ObservableList<String>> tablaAulas;
    @FXML
    private TableColumn<ObservableList<String>, String> colClave;
    @FXML
    private TableColumn<ObservableList<String>, String> colCapacidad;

    private ObservableList<ObservableList<String>> listaAulas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colClave.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colCapacidad.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        tablaAulas.setItems(listaAulas);

        // Al seleccionar una fila, se rellenan los campos
        tablaAulas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtClave.setText(newSel.get(0));
                txtCapacidad.setText(newSel.get(1));
            }
        });
    }

    @FXML
    private void onCrear() {
        if (txtClave.getText().isEmpty() || txtCapacidad.getText().isEmpty()) {
            showError("Debes llenar todos los campos");
            return;
        }

        ObservableList<String> fila = FXCollections.observableArrayList(
                txtClave.getText(),
                txtCapacidad.getText()
        );
        listaAulas.add(fila);

        limpiarCampos();
        showInfo("Aula registrada correctamente.");
    }

    @FXML
    private void onModificar() {
        int index = tablaAulas.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            ObservableList<String> fila = FXCollections.observableArrayList(
                    txtClave.getText(),
                    txtCapacidad.getText()
            );
            listaAulas.set(index, fila);
            limpiarCampos();
            showInfo("Aula modificada.");
        } else {
            showError("Selecciona un aula para modificar.");
        }
    }

    @FXML
    private void onCancelar() {
        limpiarCampos();
    }

    @FXML
    private void onVolver(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();

            // cerrar la ventana actual
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo volver al Menú\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
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
