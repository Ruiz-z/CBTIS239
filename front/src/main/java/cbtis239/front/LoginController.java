package cbtis239.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError;

    @FXML
    private void onLogin(ActionEvent e) {
        String u = txtUser.getText().trim();
        String p = txtPass.getText().trim();

        if (u.equals("admin") && p.equals("admin")) {
            lblError.setVisible(false);
            System.out.println("Login correcto → aquí abres el menú");
        } else {
            lblError.setText("Usuario o contraseña incorrectos");
            lblError.setVisible(true);
        }
    }
}
