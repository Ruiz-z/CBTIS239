package cbtis239.front.ui.users;

import cbtis239.bo.BusinessException;
import cbtis239.bo.RolBO;
import cbtis239.bo.UsuarioBO;
import cbtis239.model.Rol;
import cbtis239.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.security.SecureRandom;
import java.util.List;

public class RegisterUserController {

    // ====== FXML ids ======
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private PasswordField txtConfirm;
    @FXML
    private TextField txtConfirmVisible;
    @FXML
    private Button btnVerPass;
    @FXML
    private Button btnVerConfirm;

    @FXML
    private TextField txtNombres;
    @FXML
    private TextField txtApPat;
    @FXML
    private TextField txtApMat;

    @FXML
    private ComboBox<Rol> cbRol;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> colUsuario;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colApPat;
    @FXML
    private TableColumn<Usuario, String> colApMat;
    @FXML
    private TableColumn<Usuario, String> colRol;

    private final UsuarioBO usuarioBO = new UsuarioBO();
    private final RolBO rolBO = new RolBO();

    private final ObservableList<Rol> roles = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    private boolean showingPass = false;
    private boolean showingConfirm = false;

    @FXML
    private void initialize() {

        // =============== PASSWORD VISIBILITY ===============
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtConfirmVisible.textProperty().bindBidirectional(txtConfirm.textProperty());
        setPasswordVisible(false);
        setConfirmVisible(false);

        // =============== VALIDACIONES DE INPUT ===============

        // SOLO LETRAS (100%)
        soloLetras(txtUsuario, 20);
        soloLetras(txtNombres, 100);
        soloLetras(txtApPat, 45);
        soloLetras(txtApMat, 45);

        // Cargar roles
        cargarRoles();

        // ---- Tabla usuarios ----
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApPat.setCellValueFactory(new PropertyValueFactory<>("paterno"));
        colApMat.setCellValueFactory(new PropertyValueFactory<>("materno"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolNombre"));

        tablaUsuarios.setItems(usuarios);
        reloadTable();

        tablaUsuarios.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Usuario u = tablaUsuarios.getSelectionModel().getSelectedItem();
                if (u != null) loadToForm(u);
            }
        });
    }

    // ========================================================================
    // =========================== VALIDADORES ================================
    // ========================================================================

    /**
     * Solo letras y espacios, y limitar longitud máxima
     */
    private void soloLetras(TextField txt, int maxLength) {
        txt.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]*")) {
                txt.setText(oldValue);
                return;
            }
            if (newValue.length() > maxLength) {
                txt.setText(oldValue);
            }
        });
    }

    // ========================================================================
    // ======================= CARGA DE ROLES =================================
    // ========================================================================
    private void cargarRoles() {
        try {
            List<Rol> list = rolBO.findAll();

            list.removeIf(r ->
                    r.getNombre() != null &&
                            r.getNombre().equalsIgnoreCase("Docente"));

            roles.setAll(list);
            cbRol.setItems(roles);

            cbRol.setConverter(new StringConverter<>() {
                @Override
                public String toString(Rol r) {
                    return r == null ? "" : r.getNombre();
                }

                @Override
                public Rol fromString(String s) {
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            error("Error", "No se pudieron cargar los roles.", e);
        }
    }

    private void reloadTable() {
        usuarios.setAll(usuarioBO.listAllWithRol());
    }

    // ========================================================================
    // ======================= HANDLERS UI ====================================
    // ========================================================================
    @FXML
    private void onTogglePassword() {
        showingPass = !showingPass;
        setPasswordVisible(showingPass);
    }

    @FXML
    private void onToggleConfirm() {
        showingConfirm = !showingConfirm;
        setConfirmVisible(showingConfirm);
    }

    private void setPasswordVisible(boolean visible) {
        txtPasswordVisible.setVisible(visible);
        txtPasswordVisible.setManaged(visible);
        txtPassword.setVisible(!visible);
        txtPassword.setManaged(!visible);
        btnVerPass.setText(visible ? "🙈" : "👁");
    }

    private void setConfirmVisible(boolean visible) {
        txtConfirmVisible.setVisible(visible);
        txtConfirmVisible.setManaged(visible);
        txtConfirm.setVisible(!visible);
        txtConfirm.setManaged(!visible);
        btnVerConfirm.setText(visible ? "🙈" : "👁");
    }

    @FXML
    private void onGeneratePassword() {
        txtPassword.setText(generatePassword(8));
        txtConfirm.setText(txtPassword.getText());
    }

    @FXML
    private void onRegistrar() {
        try {
            Rol r = cbRol.getValue();
            if (r == null) throw new BusinessException("Selecciona un rol.");

            String user = trim(txtUsuario.getText());
            String pass = trim(showingPass ? txtPasswordVisible.getText() : txtPassword.getText());
            String conf = trim(showingConfirm ? txtConfirmVisible.getText() : txtConfirm.getText());

            String nombre = trim(txtNombres.getText());
            String apPat = trim(txtApPat.getText());
            String apMat = trim(txtApMat.getText());

            if (!pass.equals(conf))
                throw new BusinessException("La contraseña y la confirmación no coinciden.");

            // Validación de vacíos
            if (user.isEmpty()) throw new BusinessException("El usuario es obligatorio.");
            if (nombre.isEmpty()) throw new BusinessException("El nombre es obligatorio.");
            if (apPat.isEmpty()) throw new BusinessException("El apellido paterno es obligatorio.");
            if (pass.isEmpty()) throw new BusinessException("La contraseña es obligatoria.");

            Usuario u = new Usuario();
            u.setUsuario(user);
            u.setContrasena(pass);
            u.setRolId(r.getIdRol());
            u.setNombre(nombre);
            u.setPaterno(apPat);
            u.setMaterno(apMat);

            usuarioBO.create(u);
            info("Usuario registrado", "Se registró el usuario: " + user);

            reloadTable();
            clearForm();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "No se pudo registrar el usuario.", ex);
        }
    }

    @FXML
    private void onModificar() {
        try {
            Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (sel == null) {
                warn("Atención", "Selecciona un usuario.");
                return;
            }

            Rol r = cbRol.getValue();
            if (r == null) throw new BusinessException("Selecciona un rol.");

            String pass = trim(showingPass ? txtPasswordVisible.getText() : txtPassword.getText());
            String conf = trim(showingConfirm ? txtConfirmVisible.getText() : txtConfirm.getText());

            if (!pass.equals(conf))
                throw new BusinessException("La contraseña no coincide.");

            sel.setContrasena(pass);
            sel.setRolId(r.getIdRol());
            sel.setNombre(trim(txtNombres.getText()));
            sel.setPaterno(trim(txtApPat.getText()));
            sel.setMaterno(trim(txtApMat.getText()));

            usuarioBO.update(sel);

            info("Usuario modificado", "Se actualizó: " + sel.getUsuario());
            reloadTable();
            clearForm();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "No se pudo modificar.", ex);
        }
    }

    @FXML
    private void onEliminar() {
        try {
            Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (sel == null) {
                warn("Atención", "Selecciona un usuario.");
                return;
            }

            if (confirm("Eliminar",
                    "¿Eliminar definitivamente el usuario '" + sel.getUsuario() + "'?").getButtonData()
                    != ButtonBar.ButtonData.OK_DONE) return;

            usuarioBO.delete(sel.getUsuario());

            info("Eliminado", "Usuario eliminado.");
            reloadTable();
            clearForm();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error", "No se pudo eliminar.", ex);
        }
    }

    // ========================================================================
    // ============================ HELPERS ===================================
    // ========================================================================
    private void clearForm() {

        // ---- Habilitar PK otra vez ----
        txtUsuario.setDisable(false);

        // ---- Limpiar todos los textos ----
        txtUsuario.clear();
        txtPassword.clear();
        txtPasswordVisible.clear();
        txtConfirm.clear();
        txtConfirmVisible.clear();
        txtNombres.clear();
        txtApPat.clear();
        txtApMat.clear();

        // ---- Resetear ComboBox Roles ----
        cbRol.getSelectionModel().clearSelection();

        // ---- Deseleccionar tabla ----
        tablaUsuarios.getSelectionModel().clearSelection();

        // ---- Reset de visibilidad de passwords ----
        showingPass = false;
        showingConfirm = false;

        setPasswordVisible(false);
        setConfirmVisible(false);

        // ---- Romper el contenido visible del toggle ----
        txtPasswordVisible.setManaged(false);
        txtConfirmVisible.setManaged(false);

        // ---- Enfocar primer campo ----
        txtUsuario.requestFocus();
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String generatePassword(int len) {
        String abc = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#%*";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(abc.charAt(r.nextInt(abc.length())));
        return sb.toString();
    }

    private void loadToForm(Usuario u) {
        txtUsuario.setText(u.getUsuario());
        txtUsuario.setDisable(true);

        txtPassword.setText(u.getContrasena());
        txtConfirm.setText(u.getContrasena());

        txtNombres.setText(u.getNombre());
        txtApPat.setText(u.getPaterno());
        txtApMat.setText(u.getMaterno());

        cbRol.getSelectionModel().clearSelection();
        roles.stream()
                .filter(r -> r.getIdRol() == u.getRolId())
                .findFirst()
                .ifPresent(r -> cbRol.getSelectionModel().select(r));
    }

    // Alerts

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void error(String title, String header, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.showAndWait();
        ex.printStackTrace();
    }

    private ButtonType confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.CANCEL);
    }

    @FXML
    public void onVolver() {
        try {
            // Cargar menú
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu.fxml"));
            Stage st = new Stage();
            st.setTitle("Menú");
            st.setScene(new Scene(root));
            st.initStyle(javafx.stage.StageStyle.UNDECORATED);
            st.setFullScreen(true);
            st.setFullScreenExitHint("");
            st.show();

            // 🔥 Cerrar la ventana actual
            Stage actual = (Stage) txtUsuario.getScene().getWindow();
            actual.close();

        } catch (Exception e) {
            showError("No se pudo abrir el menú:\n" + e.getMessage());
        }
    }

    private void showError(String m){
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }

}