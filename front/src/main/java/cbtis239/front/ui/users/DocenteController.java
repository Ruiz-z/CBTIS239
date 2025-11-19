package cbtis239.front.ui.users;

import cbtis239.bo.BusinessException;
import cbtis239.bo.DocenteBO;
import cbtis239.model.Docente;
import cbtis239.model.Opcion;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.function.UnaryOperator;

public class DocenteController {

    // --------- Formulario Docente ---------
    @FXML private TextField txtCurp, txtCorreo, txtNss, txtNombre, txtPaterno, txtMaterno,
            txtTelefono, txtCelular;

    @FXML private ComboBox<Opcion> cmbEdoCivil, cmbGenero;

    // --------- Usuario / Contraseña (solo alta) ---------
    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPass, txtPass2;

    // --------- Tabla (solo nombre completo) ---------
    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colNombreCompleto;

    private final DocenteBO bo = new DocenteBO();
    private final ObservableList<Docente> data = FXCollections.observableArrayList();

    // Docente actualmente seleccionado
    private Docente seleccionado;

    @FXML
    private void initialize() {
        configurarValidacionesTexto();

        // Columna: nombre completo
        colNombreCompleto.setCellValueFactory(c ->
                Bindings.createStringBinding(() -> nombreCompleto(c.getValue()))
        );

        tblDocentes.setItems(data);
        tblDocentes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Al cambiar selección en la tabla -> llenar formulario
        tblDocentes.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                seleccionado = n;
                cargarEnFormulario(n);
            }
        });

        // Doble clic también
        tblDocentes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Docente d = tblDocentes.getSelectionModel().getSelectedItem();
                if (d != null) {
                    seleccionado = d;
                    cargarEnFormulario(d);
                }
            }
        });

        cargarCombos();
        recargarTabla();
    }

    // ====================== Acciones ======================

    @FXML
    private void onAgregar() {
        try {
            Docente d = buildDocenteFromForm();

            Opcion edo = cmbEdoCivil.getValue();
            Opcion gen = cmbGenero.getValue();
            if (edo == null) throw new BusinessException("Selecciona Estado Civil.");
            if (gen == null) throw new BusinessException("Selecciona Género.");
            d.setEdoCivilId(edo.getId());
            d.setGeneroId(gen.getId());

            String usuario = get(txtUsuario);
            String pass    = txtPass.getText();
            String pass2   = txtPass2.getText();

            int id = bo.crearDocenteConUsuario(d, usuario, pass, pass2);
            info("Docente creado (ID " + id + ") con usuario '" + usuario + "'.");

            limpiar();
            recargarTabla();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error al crear docente", ex.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        try {
            if (seleccionado == null) {
                warn("Selección", "Selecciona un docente de la tabla para modificar.");
                return;
            }

            Docente d = buildDocenteFromForm();
            d.setDocenteId(seleccionado.getDocenteId());

            Opcion edo = cmbEdoCivil.getValue();
            Opcion gen = cmbGenero.getValue();
            if (edo == null) throw new BusinessException("Selecciona Estado Civil.");
            if (gen == null) throw new BusinessException("Selecciona Género.");
            d.setEdoCivilId(edo.getId());
            d.setGeneroId(gen.getId());

            bo.actualizarDocente(d);
            info("Docente actualizado correctamente.");

            limpiar();
            recargarTabla();

        } catch (BusinessException be) {
            warn("Validación", be.getMessage());
        } catch (Exception ex) {
            error("Error al actualizar docente", ex.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        try {
            if (seleccionado == null) {
                warn("Selección", "Selecciona un docente de la tabla para eliminar.");
                return;
            }

            String nombre = nombreCompleto(seleccionado);
            boolean ok = confirm(
                    "Confirmar eliminación",
                    "¿Seguro que deseas eliminar al docente:\n\n" + nombre +
                    "\n\nEsto también eliminará su usuario vinculado (si existe)."
            );
            if (!ok) return;

            bo.eliminarDocente(seleccionado.getDocenteId());
            info("Docente eliminado correctamente.");

            limpiar();
            recargarTabla();

        } catch (BusinessException be) {
            warn("No se puede eliminar", be.getMessage());
        } catch (Exception ex) {
            error("Error al eliminar docente", ex.getMessage());
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Menú 2");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
          throw new IllegalArgumentException("No se pudo volver al menú:\n" + e.getMessage());
        }
    }

    // ====================== Helpers UI ======================

    private void cargarCombos() {
        try {
            cmbEdoCivil.setItems(FXCollections.observableArrayList(bo.listarEstadosCiviles()));
            cmbGenero.setItems(FXCollections.observableArrayList(bo.listarGeneros()));
        } catch (SQLException e) {
            error("Error cargando catálogos", e.getMessage());
        }
    }

    private void recargarTabla() {
        try {
            data.setAll(bo.listarDocentesBasico());
            tblDocentes.refresh();
        } catch (SQLException e) {
            error("Error cargando docentes", e.getMessage());
        }
    }

    private Docente buildDocenteFromForm() {
        Docente d = new Docente();
        d.setCurp(get(txtCurp));
        d.setCorreo(get(txtCorreo));
        d.setNss(get(txtNss));
        d.setNombre(get(txtNombre));
        d.setPaterno(get(txtPaterno));
        d.setMaterno(get(txtMaterno));
        d.setTelefono(get(txtTelefono));
        d.setCelular(get(txtCelular));
        return d;
    }

    private void cargarEnFormulario(Docente d) {
        if (d == null) return;

        txtCurp.setText(nullSafe(d.getCurp()));
        txtCorreo.setText(nullSafe(d.getCorreo()));
        txtNss.setText(nullSafe(d.getNss()));
        txtNombre.setText(nullSafe(d.getNombre()));
        txtPaterno.setText(nullSafe(d.getPaterno()));
        txtMaterno.setText(nullSafe(d.getMaterno()));
        txtTelefono.setText(nullSafe(d.getTelefono()));
        txtCelular.setText(nullSafe(d.getCelular()));

        // Seleccionar estado civil
        if (d.getEdoCivilId() > 0) {
            for (Opcion op : cmbEdoCivil.getItems()) {
                if (op.getId() == d.getEdoCivilId()) {
                    cmbEdoCivil.getSelectionModel().select(op);
                    break;
                }
            }
        } else {
            cmbEdoCivil.getSelectionModel().clearSelection();
        }

        // Seleccionar género
        if (d.getGeneroId() > 0) {
            for (Opcion op : cmbGenero.getItems()) {
                if (op.getId() == d.getGeneroId()) {
                    cmbGenero.getSelectionModel().select(op);
                    break;
                }
            }
        } else {
            cmbGenero.getSelectionModel().clearSelection();
        }

        // Usuario/contraseñas solo para alta
        txtUsuario.clear();
        txtPass.clear();
        txtPass2.clear();
    }

    private String get(TextField t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private String nombreCompleto(Docente d) {
        if (d == null) return "";
        String n = d.getNombre()  == null ? "" : d.getNombre().trim();
        String p = d.getPaterno() == null ? "" : d.getPaterno().trim();
        String m = d.getMaterno() == null ? "" : d.getMaterno().trim();
        return (n + " " + p + " " + m).replaceAll("\\s+", " ").trim();
    }

    private void limpiar() {
        txtCurp.clear(); txtCorreo.clear(); txtNss.clear();
        txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        txtTelefono.clear(); txtCelular.clear();
        txtUsuario.clear(); txtPass.clear(); txtPass2.clear();
        cmbEdoCivil.getSelectionModel().clearSelection();
        cmbGenero.getSelectionModel().clearSelection();
        tblDocentes.getSelectionModel().clearSelection();
        seleccionado = null;
    }

    // ====================== Validaciones con TextFormatter ======================

    private void configurarValidacionesTexto() {
        // CURP: 18 chars, mayúsculas y dígitos
        txtCurp.setTextFormatter(createFormatter(18, "[A-Z0-9]*", true));

        // Correo: hasta 100 caracteres, cualquier cosa
        txtCorreo.setTextFormatter(createFormatter(100, ".*", false));

        // NSS: solo dígitos, 11
        txtNss.setTextFormatter(createFormatter(11, "\\d*", false));

        // Nombre / Apellidos: letras y espacios, 45
        String letras = "[A-Za-zÁÉÍÓÚÑáéíóúñ ]*";
        txtNombre.setTextFormatter(createFormatter(45, letras, false));
        txtPaterno.setTextFormatter(createFormatter(45, letras, false));
        txtMaterno.setTextFormatter(createFormatter(45, letras, false));

        // Teléfono: dígitos, 15
        txtTelefono.setTextFormatter(createFormatter(15, "\\d*", false));

        // Celular: dígitos, 12
        txtCelular.setTextFormatter(createFormatter(12, "\\d*", false));

        // Usuario: letras, números, ._- , 20
        txtUsuario.setTextFormatter(createFormatter(20, "[A-Za-z0-9._-]*", false));
    }

    private TextFormatter<String> createFormatter(int maxLen, String regex, boolean upper) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.length() > maxLen) return null;
            if (!newText.matches(regex)) return null;
            if (upper) {
                change.setText(change.getText().toUpperCase());
            }
            return change;
        };
        return new TextFormatter<>(filter);
    }

    // ========= Alerts con owner =========
    private Stage getStage() {
        javafx.stage.Window w = javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isFocused)
                .findFirst()
                .orElseGet(() ->
                        javafx.stage.Window.getWindows().stream()
                                .filter(javafx.stage.Window::isShowing)
                                .findFirst()
                                .orElse(null));
        return (w instanceof Stage) ? (Stage) w : null;
    }

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Advertencia");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
