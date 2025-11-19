package cbtis239.front.ui.users;

import cbtis239.bo.DocenteBO;
import cbtis239.bo.UsuarioBO;
import cbtis239.model.Docente;
import cbtis239.model.Usuario;
import cbtis239.session.SesionActual;
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
    private final DocenteBO docenteBO = new DocenteBO();

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
        // Lógica ya manejada con las bindings en initialize()
    }

    @FXML
    private void onEntrar(ActionEvent e) {
        lblEstado.setText("Conectando...");
        btnEntrar.setDisable(true);

        String user = txtUser.getText().trim();
        String pass = (chkMostrar.isSelected() ? txtPassVisible.getText() : txtPass.getText()).trim();

        try {
            // ✅ Se obtiene el objeto Usuario con su rol desde UsuarioBO
            Usuario usuario = usuarioBO.validarLogin(user, pass);

            if (usuario != null) {

                // Guardar usuario en sesión global
                SesionActual.setUsuario(usuario);

                String rolNombre = usuario.getRolNombre();
                lblEstado.setText("Acceso concedido");

                // --- Servicios Escolares ---
                if (rolNombre.equalsIgnoreCase("Servicios Escolares")) {
                    SceneNavigator.switchFromEvent(
                            e,
                            "/cbtis239/front/views/menu.fxml",
                            "Servicios Escolares"
                    );
                    System.out.println("✅ Acceso: Servicios Escolares");
                }
                // --- Docente ---
                else if (rolNombre.equalsIgnoreCase("Docente")) {

                    // Buscar docente asociado a este usuario
                    Docente d = docenteBO.obtenerDocentePorUsuario(usuario.getUsuario());
                    if (d == null) {
                        lblEstado.setText("No se encontró un docente vinculado a este usuario.");
                        btnEntrar.setDisable(false);
                        return;
                    }
                    // Guardar docente en sesión
                    SesionActual.setDocente(d);

                    SceneNavigator.switchFromEvent(
                            e,
                            "/cbtis239/front/views/MenuDocente.fxml",
                            "Menú Docente"
                    );
                    System.out.println("✅ Acceso: Docente (ID " + d.getDocenteId() + ")");
                }
                // --- Servicios Financieros ---
                else if (rolNombre.equalsIgnoreCase("Servicios Financieros")) {
                    SceneNavigator.switchFromEvent(
                            e,
                            "/cbtis239/front/views/MenuSF.fxml",
                            "Servicios Financieros"
                    );
                    System.out.println("✅ Acceso: Servicios Financieros");
                }
                else {
                    lblEstado.setText("Rol no reconocido: " + rolNombre);
                    btnEntrar.setDisable(false);
                    System.out.println("⚠️ Rol desconocido: " + rolNombre);
                }

            } else {
                lblEstado.setText("Usuario o contraseña incorrectos");
                btnEntrar.setDisable(false);
            }

        } catch (Exception ex) {
            btnEntrar.setDisable(false);

            if (ex instanceof cbtis239.bo.BusinessException) {
                lblEstado.setText("❌ " + ex.getMessage());
            } else {
                lblEstado.setText("⚠️ Error interno, contacte al administrador");
                ex.printStackTrace();
            }
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
