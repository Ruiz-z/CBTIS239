package cbtis239.front.ui.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterUserController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordPlain;
    @FXML private PasswordField txtConfirm;
    @FXML private TextField txtConfirmPlain;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApPat;
    @FXML private TextField txtApMat;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnRegistrar;
    @FXML private ToggleButton tbShowPwd;
    @FXML private ToggleButton tbShowConfirm;

    @FXML
    private void initialize() {
        // Demo roles
        cbRol.getItems().addAll("Administrador", "Docente", "Alumno");

        // Desactivar el botón hasta que se llenen campos
        btnRegistrar.setDisable(true);

        // Listener básico
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
        txtConfirm.textProperty().addListener((obs, oldVal, newVal) -> validarFormulario());
    }

    private void validarFormulario() {
        boolean completo = !txtUsuario.getText().isEmpty()
                && !txtPassword.getText().isEmpty()
                && txtPassword.getText().equals(txtConfirm.getText());
        btnRegistrar.setDisable(!completo);
    }

    @FXML
    private void onRegistrar() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Usuario registrado: " + txtUsuario.getText(),
                ButtonType.OK);
        a.showAndWait();
    }

    @FXML
    private void onCancelar() {
        // Aquí podrías limpiar los campos o cerrar ventana
        txtUsuario.clear();
        txtPassword.clear();
        txtConfirm.clear();
        txtNombres.clear();
        txtApPat.clear();
        txtApMat.clear();
        cbRol.getSelectionModel().clearSelection();
    }

    @FXML
    private void onFinalizar() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Registro finalizado.",
                ButtonType.OK);
        a.showAndWait();
    }

    @FXML
    private void onGeneratePassword() {
        String random = "Usr" + (int)(Math.random()*9999);
        txtPassword.setText(random);
        txtConfirm.setText(random);
    }

    @FXML
    private void onTogglePwd() {
        boolean show = tbShowPwd.isSelected();
        txtPasswordPlain.setText(txtPassword.getText());
        txtPassword.setVisible(!show);
        txtPasswordPlain.setVisible(show);
    }

    @FXML
    private void onToggleConfirm() {
        boolean show = tbShowConfirm.isSelected();
        txtConfirmPlain.setText(txtConfirm.getText());
        txtConfirm.setVisible(!show);
        txtConfirmPlain.setVisible(show);
    }
}
