package cbtis239.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError;

    @FXML
    private void onLogin(ActionEvent e) {
        String u = txtUser.getText() == null ? "" : txtUser.getText().trim();
        String p = txtPass.getText() == null ? "" : txtPass.getText().trim();

        if (u.equals("admin") && p.equals("admin")) {
            lblError.setVisible(false);
            System.out.println("Login OK → ir al menú");

        } else {
            lblError.setText("Usuario o contraseña incorrectos");
            lblError.setVisible(true);
        }
    }

    @FXML
    private void onExit(ActionEvent e) {
        System.exit(0);
    }
}
