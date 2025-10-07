package cbtis239.front;

import cbtis239.front.util.SceneNavigator;
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
        String u = txtUser.getText();
        String p = txtPass.getText();

        // Simulación de validación
        if ("admin".equals(u) && "1234".equals(p)) {
            lblError.setVisible(false);

            // Aquí usas SceneNavigator
            SceneNavigator.switchFromEvent(
                    e,
                    "/cbtis239/front/views/menu.fxml",
                    "Menú Principal"
            );

            System.out.println("Login OK → menú abierto en pantalla completa");
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
