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

public class GrupoController {

    @FXML private TextField txtNombreGrupo;
    @FXML private TableView<ObservableList<String>> tablaGrupos;
    @FXML private TableColumn<ObservableList<String>, String> colNombre;

    private ObservableList<ObservableList<String>> listaGrupos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        tablaGrupos.setItems(listaGrupos);
    }

    @FXML
    private void onRegistrar() {
        String nombre = txtNombreGrupo.getText().trim();
        if (nombre.isEmpty()) {
            showError("El nombre del grupo no puede estar vacío.");
            return;
        }

        listaGrupos.add(FXCollections.observableArrayList(nombre));
        txtNombreGrupo.clear();
        showInfo("Grupo registrado correctamente.");
    }

    @FXML
    private void onCancelar() {
        txtNombreGrupo.clear();
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
            showError("No se pudo abrir el menú principal\n\n" + e.getMessage());
        }
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
