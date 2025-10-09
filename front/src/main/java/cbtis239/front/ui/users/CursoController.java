package cbtis239.front.ui.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

public class CursoController {

    @FXML private TextField txtClave, txtPeriodo, txtDescripcion;
    @FXML private TableView<ObservableList<String>> tablaCursos;
    @FXML private TableColumn<ObservableList<String>, String> colId, colAlumnos, colMateria, colDocente, colAula;

    private ObservableList<ObservableList<String>> listaCursos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colAlumnos.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colMateria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colDocente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colAula.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));

        tablaCursos.setItems(listaCursos);
    }

    @FXML
    private void onGuardar() {
        String clave = txtClave.getText().trim();
        String periodo = txtPeriodo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (clave.isEmpty() || periodo.isEmpty() || descripcion.isEmpty()) {
            showError("Todos los campos son obligatorios");
            return;
        }

        ObservableList<String> nuevo = FXCollections.observableArrayList(
                clave, "Alumnos X", "Materia X", "Docente X", "Aula X"
        );
        listaCursos.add(nuevo);

        showInfo("Curso guardado correctamente");
        limpiarCampos();
    }

    @FXML
    private void onActualizar() {
        ObservableList<String> seleccionado = tablaCursos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showError("Seleccione un curso para actualizar");
            return;
        }
        seleccionado.set(2, "Materia Actualizada");
        tablaCursos.refresh();
        showInfo("Curso actualizado correctamente");
    }

    @FXML
    private void onEliminar() {
        ObservableList<String> seleccionado = tablaCursos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            listaCursos.remove(seleccionado);
            showInfo("Curso eliminado correctamente");
        } else {
            showError("Seleccione un curso para eliminar");
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarCampos();
    }

    @FXML
    private void onVolver(ActionEvent event) {
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
            e.printStackTrace();
            showError("No se pudo volver al menú\n\n" + e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtClave.clear();
        txtPeriodo.clear();
        txtDescripcion.clear();
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
