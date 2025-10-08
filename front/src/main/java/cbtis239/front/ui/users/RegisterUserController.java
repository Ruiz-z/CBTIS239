package cbtis239.front.ui.users;

import cbtis239.front.api.ApiException;
import cbtis239.front.api.UserApiClient;
import cbtis239.front.api.dto.RoleDto;
import cbtis239.front.api.dto.UserCreateRequest;
import cbtis239.front.api.dto.UserDto;
import cbtis239.front.api.dto.UserUpdateRequest;
import cbtis239.front.util.SceneNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class RegisterUserController {

    @FXML private TextField txtUsuario, txtNombres, txtApPat, txtApMat;
    @FXML private PasswordField txtPassword, txtConfirm;
    @FXML private TextField txtPasswordVisible, txtConfirmVisible; // para mostrar contraseña y confirmar
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnVerPass, btnVerConfirm;

    @FXML private TableView<UserFxModel> tablaUsuarios;
    @FXML private TableColumn<UserFxModel, String> colUsuario, colNombre, colApPat, colApMat, colRol;

    private final UserApiClient apiClient = new UserApiClient();
    private final ObservableList<UserFxModel> listaUsuarios = FXCollections.observableArrayList();
    private final ObservableList<String> rolesDisponibles = FXCollections.observableArrayList();

    private boolean mostrarPassword = false;
    private boolean mostrarConfirm = false;

    @FXML
    public void initialize() {
        cbRol.setItems(rolesDisponibles);

        colUsuario.setCellValueFactory(data -> data.getValue().emailProperty());
        colNombre.setCellValueFactory(data -> data.getValue().nombresProperty());
        colApPat.setCellValueFactory(data -> data.getValue().apellidoPatProperty());
        colApMat.setCellValueFactory(data -> data.getValue().apellidoMatProperty());
        colRol.setCellValueFactory(data -> data.getValue().rolesProperty());

        tablaUsuarios.setItems(listaUsuarios);

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtUsuario.setText(newSel.getEmail());
                txtNombres.setText(newSel.getNombres());
                txtApPat.setText(newSel.getApellidoPat());
                txtApMat.setText(newSel.getApellidoMat());
                String role = newSel.getPrimaryRole();
                if (role != null && !rolesDisponibles.contains(role)) {
                    rolesDisponibles.add(role);
                }
                cbRol.setValue(role);
            }
        });

        loadRoles();
        loadUsers();
    }

    private void loadRoles() {
        CompletableFuture.supplyAsync(apiClient::listRoles)
                .thenAccept(roles -> Platform.runLater(() -> {
                    List<String> names = roles.stream()
                            .map(RoleDto::name)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(name -> !name.isBlank())
                            .distinct()
                            .sorted(String::compareToIgnoreCase)
                            .toList();
                    rolesDisponibles.setAll(names);
                }))
                .exceptionally(ex -> {
                    showAsyncError("No se pudieron cargar los roles disponibles.", ex);
                    return null;
                });
    }

    private void loadUsers() {
        CompletableFuture.supplyAsync(apiClient::listUsers)
                .thenAccept(users -> Platform.runLater(() -> {
                    List<UserFxModel> models = users.stream()
                            .map(UserFxModel::fromDto)
                            .collect(Collectors.toList());
                    models.sort((a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail()));
                    listaUsuarios.setAll(models);
                }))
                .exceptionally(ex -> {
                    showAsyncError("No se pudieron cargar los usuarios.", ex);
                    return null;
                });
    }

    private void showAsyncError(String msg, Throwable throwable) {
        Platform.runLater(() -> showError(msg + "\n\n" + extractErrorMessage(throwable)));
    }

    private String extractErrorMessage(Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
        if (cause instanceof ApiException apiEx) {
            return "Código " + apiEx.getStatusCode() + ": " + apiEx.getMessage();
        }
        return cause != null && cause.getMessage() != null ? cause.getMessage() : throwable.toString();
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

        if (!validateRequiredFields(true, passValue, confirmValue)) {
            return;
        }

        UserCreateRequest request = new UserCreateRequest(
                buildFullName(),
                txtUsuario.getText().trim(),
                passValue.trim(),
                gatherRoles()
        );

        CompletableFuture.supplyAsync(() -> apiClient.createUser(request))
                .thenAccept(dto -> Platform.runLater(() -> {
                    showInfo("Usuario registrado con éxito.");
                    limpiarCampos();
                    tablaUsuarios.getSelectionModel().clearSelection();
                    mergeDtoIntoTable(dto);
                }))
                .exceptionally(ex -> {
                    showAsyncError("No se pudo registrar el usuario.", ex);
                    return null;
                });
    }

    @FXML
    private void onModificar() {
        UserFxModel selected = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona un usuario de la tabla para modificar.");
            return;
        }

        if (!txtUsuario.getText().trim().equalsIgnoreCase(selected.getEmail())) {
            showError("El usuario/correo no se puede modificar desde esta pantalla.");
            return;
        }

        if (!validateRequiredFields(false, null, null)) {
            return;
        }

        UserUpdateRequest request = new UserUpdateRequest(
                buildFullName(),
                null,
                gatherRoles()
        );

        CompletableFuture.supplyAsync(() -> apiClient.updateUser(selected.getId(), request))
                .thenAccept(dto -> Platform.runLater(() -> {
                    showInfo("Usuario actualizado correctamente.");
                    mergeDtoIntoTable(dto);
                    tablaUsuarios.refresh();
                }))
                .exceptionally(ex -> {
                    showAsyncError("No se pudo actualizar el usuario.", ex);
                    return null;
                });
    }

    @FXML
    private void onEliminar() {
        UserFxModel selected = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona un usuario para eliminar.");
            return;
        }

        CompletableFuture.runAsync(() -> apiClient.deleteUser(selected.getId()))
                .thenRun(() -> Platform.runLater(() -> {
                    listaUsuarios.remove(selected);
                    limpiarCampos();
                    showInfo("Usuario eliminado.");
                }))
                .exceptionally(ex -> {
                    showAsyncError("No se pudo eliminar el usuario.", ex);
                    return null;
                });
    }

    @FXML
    private void onCancelar() {
        limpiarCampos();
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver(ActionEvent event) {
        SceneNavigator.switchFromEvent(event, "/cbtis239/front/views/Menu.fxml", "Menú Principal");
    }

    private boolean validateRequiredFields(boolean requirePasswords, String passValue, String confirmValue) {
        if (txtUsuario.getText().isBlank() || txtNombres.getText().isBlank() || txtApPat.getText().isBlank()) {
            showError("Debes rellenar al menos usuario, nombres y apellido paterno.");
            return false;
        }

        if (cbRol.getValue() == null || cbRol.getValue().isBlank()) {
            showError("Selecciona un rol para el usuario.");
            return false;
        }

        if (requirePasswords) {
            if (passValue == null || passValue.isBlank() || confirmValue == null || confirmValue.isBlank()) {
                showError("Debes capturar y confirmar la contraseña.");
                return false;
            }
            if (!passValue.equals(confirmValue)) {
                showError("Las contraseñas no coinciden.");
                return false;
            }
        }
        return true;
    }

    private Set<String> gatherRoles() {
        String selected = cbRol.getValue();
        return selected == null || selected.isBlank() ? Set.of() : Set.of(selected);
    }

    private String buildFullName() {
        return List.of(txtNombres.getText(), txtApPat.getText(), txtApMat.getText()).stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private void mergeDtoIntoTable(UserDto dto) {
        UserFxModel existing = listaUsuarios.stream()
                .filter(model -> Objects.equals(model.getId(), dto.id()))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            listaUsuarios.add(UserFxModel.fromDto(dto));
        } else {
            existing.updateFromDto(dto);
        }
        listaUsuarios.sort((a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail()));
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void limpiarCampos() {
        txtUsuario.clear();
        if (mostrarPassword) {
            onTogglePassword();
        }
        if (mostrarConfirm) {
            onToggleConfirm();
        }
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
