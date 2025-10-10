package cbtis239.front.ui.users;

import cbtis239.bo.UsuarioBO;
import cbtis239.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoginController {

    @FXML private TextField txtUser;

    // Dos campos para mostrar/ocultar contraseña
    @FXML private PasswordField txtPass;       // oculto
    @FXML private TextField txtPassVisible;    // visible

    @FXML private CheckBox chkMostrar;
    @FXML private Button btnEntrar;
    @FXML private Button btnSalir;
    @FXML private Label lblEstado;

    private final UsuarioBO usuarioBO = new UsuarioBO();

    public void attachWindow(javafx.stage.Stage stage) {
        stage.setOnCloseRequest(e -> { e.consume(); System.exit(0); });
    }

    @FXML
    private void initialize() {
        lblEstado.setText("");

        // Sincroniza ambos campos de contraseña
        txtPassVisible.managedProperty().bind(chkMostrar.selectedProperty());
        txtPassVisible.visibleProperty().bind(chkMostrar.selectedProperty());

        txtPass.managedProperty().bind(chkMostrar.selectedProperty().not());
        txtPass.visibleProperty().bind(chkMostrar.selectedProperty().not());

        // Enlaza el texto de ambos (bidireccional)
        txtPassVisible.textProperty().bindBidirectional(txtPass.textProperty());
    }

    @FXML
    private void toggleShowPassword(ActionEvent e) {
        // No hace falta lógica adicional porque ya está enlazado en initialize(),
        // pero mantenemos el handler porque el FXML lo invoca.
    }


    @FXML
    private void onEntrar(ActionEvent e) {
        lblEstado.setText("Conectando...");
        btnEntrar.setDisable(true);

        String user = txtUser.getText().trim();
        String pass = (chkMostrar.isSelected() ? txtPassVisible.getText() : txtPass.getText()).trim();

        try {
            boolean ok = usuarioBO.login(user, pass);
            if (ok) {
                SceneNavigator.switchFromEvent(
                        e,
                        "/cbtis239/front/views/menu.fxml",
                        "Menú Principal"
                );
            } else {
                lblEstado.setText("Usuario o contraseña incorrectos");
                btnEntrar.setDisable(false);
            }
        } catch (Exception ex) {
            btnEntrar.setDisable(false);
            lblEstado.setText("Error al iniciar sesión");
            showError("Fallo en login", ex);
        }
    }

    private void showError(String title, Throwable ex) {
        ex.printStackTrace();
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()));
        String msg = sw.toString();
        a.setContentText(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
        a.showAndWait();
    }


    @FXML
    private void onSalir(ActionEvent e) { System.exit(0); }
}


