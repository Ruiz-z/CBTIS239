package cbtis239.front.ui.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import org.kordamp.ikonli.javafx.FontIcon;

import java.security.SecureRandom;
import java.util.List;

public class RegisterUserController {

    @FXML private TextField txtUsuario, txtNombres, txtApPat, txtApMat;
    @FXML private PasswordField txtPassword, txtConfirm;
    @FXML private TextField txtPasswordPlain, txtConfirmPlain;
    @FXML private ToggleButton tbShowPwd, tbShowConfirm;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnRegistrar;

    @FXML
    private void initialize() {
        // Roles demo
        cbRol.getItems().setAll(List.of("Servicios Escolares", "Docente", "Administrador"));

        // Sincroniza contenido entre visibles/ocultos
        txtPasswordPlain.textProperty().bindBidirectional(txtPassword.textProperty());
        txtConfirmPlain.textProperty().bindBidirectional(txtConfirm.textProperty());

        // Estado inicial: oculto
        setReveal(false, txtPasswordPlain, txtPassword, tbShowPwd);
        setReveal(false, txtConfirmPlain, txtConfirm, tbShowConfirm);

        // Validación reactiva
        txtUsuario.textProperty().addListener((o,a,b)->validateForm());
        txtNombres.textProperty().addListener((o,a,b)->validateForm());
        txtApPat.textProperty().addListener((o,a,b)->validateForm());
        txtApMat.textProperty().addListener((o,a,b)->validateForm());
        txtPassword.textProperty().addListener((o,a,b)->validateForm());
        txtConfirm.textProperty().addListener((o,a,b)->validateForm());
        cbRol.valueProperty().addListener((o,a,b)->validateForm());

        validateForm();
    }

    // Mostrar/ocultar campos (ojito)
    private void setReveal(boolean show, TextField plain, PasswordField hidden, ToggleButton toggle) {
        plain.setVisible(show);  plain.setManaged(show);
        hidden.setVisible(!show); hidden.setManaged(!show);
        toggle.setGraphic(new FontIcon(show ? "fas-eye-slash" : "fas-eye"));
        toggle.setSelected(show);
    }

    @FXML
    private void onTogglePwd() {
        setReveal(tbShowPwd.isSelected(), txtPasswordPlain, txtPassword, tbShowPwd);
    }

    @FXML
    private void onToggleConfirm() {
        setReveal(tbShowConfirm.isSelected(), txtConfirmPlain, txtConfirm, tbShowConfirm);
    }

    @FXML
    private void onGeneratePassword() {
        String g = generateStrongPassword(10);
        txtPassword.setText(g);
        txtConfirm.setText(g);
        validateForm();
    }

    @FXML
    private void onRegistrar(ActionEvent e) {
        // Aquí aún NO guardamos en BD; solo demo.
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Usuario registrado (demo)");
        a.setContentText("Usuario: " + txtUsuario.getText() +
                "\nRol: " + cbRol.getValue());
        a.showAndWait();
        clearForm();
    }

    @FXML
    private void onCancelar() {
        clearForm();
    }

    @FXML
    private void onFinalizar() {
        ((Button)btnRegistrar).getScene().getWindow().hide();
    }

    private void clearForm() {
        txtUsuario.clear(); txtNombres.clear(); txtApPat.clear(); txtApMat.clear();
        txtPassword.clear(); txtConfirm.clear(); cbRol.getSelectionModel().clearSelection();
        // Vuelve a ocultar los “ojos”
        setReveal(false, txtPasswordPlain, txtPassword, tbShowPwd);
        setReveal(false, txtConfirmPlain, txtConfirm, tbShowConfirm);
        validateForm();
    }

    private void validateForm() {
        boolean ok = notBlank(txtUsuario) &&
                notBlank(txtNombres) &&
                notBlank(txtApPat) &&
                notBlank(txtApMat) &&
                notBlank(txtPassword) &&
                txtPassword.getText().equals(txtConfirm.getText()) &&
                cbRol.getValue() != null;

        // feedback visual simple para confirmación
        if (!txtConfirm.getText().isBlank()) {
            String style = txtPassword.getText().equals(txtConfirm.getText())
                    ? "" : "-fx-border-color: #cc0000;";
            txtConfirm.setStyle(style);
            txtConfirmPlain.setStyle(style);
        }
        btnRegistrar.setDisable(!ok);
    }

    private boolean notBlank(TextField tf) {
        return tf.getText() != null && !tf.getText().isBlank();
    }

    private String generateStrongPassword(int len) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@$%*#";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
