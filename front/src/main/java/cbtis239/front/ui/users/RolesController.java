package cbtis239.front.ui.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class RolesController {

    @FXML private TextField txtIdRol;
    @FXML private TextField txtNombreRol;
    @FXML private TextField txtPermisos;

    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, Integer> colId;
    @FXML private TableColumn<Rol, String> colNombre;

    private final ObservableList<Rol> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        tblRoles.setItems(data);

        // demo inicial (puedes quitarlo)
        data.addAll(new Rol(1, "Servicios Escolares", "crear,actualizar,eliminar"),
                new Rol(2, "Docente", "consultar,calificar"));
    }

    @FXML
    private void onGuardar() {
        if (txtNombreRol.getText().isBlank()) {
            alert("Valida", "El nombre del rol es obligatorio", Alert.AlertType.WARNING);
            return;
        }
        int id = txtIdRol.getText().isBlank() ? nextId() : Integer.parseInt(txtIdRol.getText());
        data.add(new Rol(id, txtNombreRol.getText().trim(), txtPermisos.getText().trim()));
        onLimpiar();
    }

    @FXML
    private void onActualizar() {
        Rol sel = tblRoles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("Selecciona un rol", "Elige un registro en la tabla para actualizar.", Alert.AlertType.INFORMATION);
            return;
        }
        sel.setNombre(txtNombreRol.getText().trim());
        sel.setPermisos(txtPermisos.getText().trim());
        tblRoles.refresh();
        onLimpiar();
    }

    @FXML
    private void onVolverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setMaximized(true); // 👈 abrir en pantalla completa
            stage.setTitle("Menú Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alert("Error", "No se pudo volver al menú: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void onEliminar() {
        Rol sel = tblRoles.getSelectionModel().getSelectedItem();
        if (sel != null) data.remove(sel);
    }

    @FXML
    private void onLimpiar() {
        txtIdRol.clear();
        txtNombreRol.clear();
        txtPermisos.clear();
        tblRoles.getSelectionModel().clearSelection();
    }


    private int nextId() {
        return data.stream().mapToInt(Rol::getId).max().orElse(0) + 1;
    }

    private void alert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
