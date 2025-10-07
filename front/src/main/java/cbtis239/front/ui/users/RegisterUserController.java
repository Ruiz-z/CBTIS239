package cbtis239.front.ui.users;

import cbtis239.front.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

public class RegisterUserController {

    @FXML private TextField txtUsuario, txtNombres, txtApPat, txtApMat;
    @FXML private PasswordField txtPassword, txtConfirm;
    @FXML private TextField txtPasswordVisible, txtConfirmVisible; // para mostrar contraseña y confirmar
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnVerPass, btnVerConfirm;

    @FXML private TableView<ObservableList<String>> tablaUsuarios;
    @FXML private TableColumn<ObservableList<String>, String> colUsuario, colNombre, colApPat, colApMat, colRol;

    private ObservableList<ObservableList<String>> listaUsuarios;
    private boolean mostrarPassword = false;
    private boolean mostrarConfirm = false;

    @FXML
    public void initialize() {
        cbRol.setItems(FXCollections.observableArrayList("Administrador", "Empleado", "Invitado"));

        listaUsuarios = FXCollections.observableArrayList();
        colUsuario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        colApPat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colApMat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        colRol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));

        tablaUsuarios.setItems(listaUsuarios);

        // Rellenar campos al seleccionar
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtUsuario.setText(newSel.get(0));
                txtNombres.setText(newSel.get(1));
                txtApPat.setText(newSel.get(2));
                txtApMat.setText(newSel.get(3));
                cbRol.setValue(newSel.get(4));
            }
        });
    }

    // ====== Botón Generar ======
    @FXML
    private void onGeneratePassword() {
        String pass = "P@ssw0rd123";
        txtPassword.setText(pass);
        txtConfirm.setText(pass);
        txtPasswordVisible.setText(pass);
        txtConfirmVisible.setText(pass);
    }

    // ====== Mostrar/Ocultar contraseña ======
    @FXML
    private void onTogglePassword() {
        if (!mostrarPassword) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            btnVerPass.setText("🙈");
            mostrarPassword = true;
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            btnVerPass.setText("👁");
            mostrarPassword = false;
        }
    }

    // ====== Mostrar/Ocultar confirmar contraseña ======
    @FXML
    private void onToggleConfirm() {
        if (!mostrarConfirm) {
            txtConfirmVisible.setText(txtConfirm.getText());
            txtConfirmVisible.setVisible(true);
            txtConfirmVisible.setManaged(true);
            txtConfirm.setVisible(false);
            txtConfirm.setManaged(false);
            btnVerConfirm.setText("🙈");
            mostrarConfirm = true;
        } else {
            txtConfirm.setText(txtConfirmVisible.getText());
            txtConfirm.setVisible(true);
            txtConfirm.setManaged(true);
            txtConfirmVisible.setVisible(false);
            txtConfirmVisible.setManaged(false);
            btnVerConfirm.setText("👁");
            mostrarConfirm = false;
        }
    }

    // ====== Registrar ======
    @FXML
    private void onRegistrar() {
        String passValue = mostrarPassword ? txtPasswordVisible.getText() : txtPassword.getText();
        String confirmValue = mostrarConfirm ? txtConfirmVisible.getText() : txtConfirm.getText();

        if (txtUsuario.getText().isEmpty() ||
                passValue.isEmpty() || confirmValue.isEmpty() ||
                txtNombres.getText().isEmpty() ||
                txtApPat.getText().isEmpty() || txtApMat.getText().isEmpty() ||
                cbRol.getValue() == null) {
            showError("Debes rellenar todos los campos.");
            return;
        }

        if (!passValue.equals(confirmValue)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        ObservableList<String> fila = FXCollections.observableArrayList(
                txtUsuario.getText(),
                txtNombres.getText(),
                txtApPat.getText(),
                txtApMat.getText(),
                cbRol.getValue()
        );
        listaUsuarios.add(fila);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Usuario registrado con éxito.");
        alert.setHeaderText(null);
        alert.showAndWait();

        limpiarCampos();
    }

    @FXML
    private void onModificar() {
        int index = tablaUsuarios.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            ObservableList<String> fila = FXCollections.observableArrayList(
                    txtUsuario.getText(),
                    txtNombres.getText(),
                    txtApPat.getText(),
                    txtApMat.getText(),
                    cbRol.getValue()
            );
            listaUsuarios.set(index, fila);
        }
    }

    @FXML
    private void onEliminar() {
        int index = tablaUsuarios.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            listaUsuarios.remove(index);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarCampos();
    }

    @FXML
    private void onVolver(ActionEvent event) {
        SceneNavigator.switchFromEvent(event, "/cbtis239/front/views/Menu.fxml", "Menú Principal");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void limpiarCampos() {
        txtUsuario.clear();
        txtPassword.clear();
        txtPasswordVisible.clear();
        txtConfirm.clear();
        txtConfirmVisible.clear();
        txtNombres.clear();
        txtApPat.clear();
        txtApMat.clear();
        cbRol.setValue(null);
    }
}
