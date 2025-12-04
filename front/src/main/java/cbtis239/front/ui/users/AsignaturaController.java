package cbtis239.front.ui.users;

import cbtis239.bo.MateriaBO;
import cbtis239.model.Materia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class AsignaturaController {

    @FXML private TextField txtClave;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCreditos;

    @FXML private TableView<Materia> tblMaterias;
    @FXML private TableColumn<Materia, String> colClave;
    @FXML private TableColumn<Materia, String> colNombre;
    @FXML private TableColumn<Materia, Number> colCreditos;

    private final MateriaBO bo = new MateriaBO();
    private final ObservableList<Materia> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        // ============================================
// VALIDACIÓN NUEVA: CLAVE
// Reglas:
// ✔ Letras, números, - y _
// ✔ Siempre mayúsculas
// ✔ Si se usa - o _, DEBE ir seguido de un número
// ============================================
        txtClave.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Permitir solo letras, números, - y _
            if (!newText.matches("[A-Za-z0-9_-]*")) {
                return null; // bloqueo
            }

            // Reglas especiales si contiene - o _
            int len = newText.length();
            if (len >= 2) {
                char ultimo = newText.charAt(len - 1);
                char anterior = newText.charAt(len - 2);

                // Si el anterior es un '-' o '_', el actual DEBE ser número
                if ((anterior == '-' || anterior == '_') && !Character.isDigit(ultimo)) {
                    return null; // bloquear
                }
            }

            // Convertir a mayúsculas siempre
            change.setText(change.getText().toUpperCase());

            return change;
        }));


// ======================================================
// VALIDACIÓN NOMBRE: permite números, espacios y letras;
// ======================================================
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            // Evita recursión cuando setText se ejecuta
            if (!txtNombre.isFocused()) return;

            // Convertir a mayúsculas
            String mayus = newVal.toUpperCase();

            // Solo permitir letras, números y espacios
            if (!mayus.matches("[A-ZÁÉÍÓÚÑ0-9 ]*")) {
                // Rechaza el cambio
                txtNombre.setText(oldVal);
                txtNombre.positionCaret(oldVal.length());
                return;
            }

            // Si cambió, actualizar
            if (!mayus.equals(newVal)) {
                int pos = txtNombre.getCaretPosition();
                txtNombre.setText(mayus);

                // Restaurar posición del cursor
                if (pos <= mayus.length()) {
                    txtNombre.positionCaret(pos);
                } else {
                    txtNombre.positionCaret(mayus.length());
                }
            }
        });

        // Créditos solo números
        txtCreditos.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("\\d*")) {
                txtCreditos.setText(val.replaceAll("[^\\d]", ""));
            }
        });

        // Tabla
        colClave.setCellValueFactory(c -> c.getValue().claveProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCreditos.setCellValueFactory(c -> c.getValue().creditosProperty());

        tblMaterias.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                txtClave.setText(n.getClave());
                txtNombre.setText(n.getNombre());
                txtCreditos.setText(String.valueOf(n.getCreditos()));
                txtClave.setDisable(true);
            } else {
                limpiarFormulario();
            }
        });

        tblMaterias.setItems(data);
        recargarTabla();
    }

    // ======================================
    // CONVERSIÓN DE NÚMEROS A ROMANOS
    // ======================================
    private String convertirNumerosARomanos(String texto) {
        Map<String, String> map = new HashMap<>();
        map.put("1", "I");
        map.put("2", "II");
        map.put("3", "III");
        map.put("4", "IV");
        map.put("5", "V");
        map.put("6", "VI");
        map.put("7", "VII");
        map.put("8", "VIII");
        map.put("9", "IX");
        map.put("10", "X");

        String[] parts = texto.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String p : parts) {
            if (p.matches("\\d+")) {
                if (map.containsKey(p)) sb.append(map.get(p)).append(" ");
            } else {
                sb.append(p).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // ===========================================================
    // TEXT FORMATTER — para clave
    // ===========================================================
    private TextFormatter<String> createFormatter(int maxLen, String regex, boolean upper) {
        UnaryOperator<TextFormatter.Change> filter = change -> {

            String newText = change.getControlNewText();

            if (newText.length() > maxLen)
                return null;

            if (!newText.matches(regex))
                return null;

            if (upper)
                change.setText(change.getText().toUpperCase());

            return change;
        };
        return new TextFormatter<>(filter);
    }

    private void recargarTabla() {
        try {
            data.clear();
            data.addAll(bo.listar());
            tblMaterias.refresh();
        } catch (SQLException ex) {
            mostrarError("Error al cargar materias", ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtClave.clear();
        txtNombre.clear();
        txtCreditos.clear();
        txtClave.setDisable(false);
        tblMaterias.getSelectionModel().clearSelection();
    }

    private int parseCreditos() {
        String t = txtCreditos.getText();
        if (t == null || t.isBlank()) return 0;
        try {
            int c = Integer.parseInt(t);
            if (c < 0) throw new NumberFormatException();
            return c;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Créditos debe ser un entero >= 0.");
        }
    }

    @FXML
    private void onAgregar() {
        try {
            String clave = txtClave.getText().trim();
            String nombre = txtNombre.getText().trim();
            int creditos = parseCreditos();

            if (clave.isEmpty()) throw new IllegalArgumentException("La clave no puede estar vacía.");
            if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre no puede estar vacío.");

            // ✅ Validación extra: no terminar en - o _
            if (clave.matches(".*[-_]$")) {
                throw new IllegalArgumentException("La clave no puede terminar en '-' o '_' (falta un número).");
            }

            // ✅ Validación extra: después de - o _ debe venir un número
            if (clave.matches(".*[-_][A-Z]+.*")) {
                throw new IllegalArgumentException("Después de '-' o '_' debe venir un número.");
            }

            boolean claveExiste = data.stream().anyMatch(m -> m.getClave().equalsIgnoreCase(clave));
            if (claveExiste)
                throw new IllegalArgumentException("Ya existe una materia con esa clave.");

            boolean nombreExiste = data.stream().anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre));
            if (nombreExiste)
                throw new IllegalArgumentException("Ya existe una materia con ese nombre.");

            bo.agregar(new Materia(clave, nombre, creditos));
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Materia agregada correctamente.");

        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }


    @FXML
    private void onEliminar() {
        Materia sel = tblMaterias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAdvertencia("Selección", "Selecciona una materia.");
            return;
        }
        try {
            bo.eliminar(sel.getClave());
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Materia eliminada.");
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
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

            // Cerrar ventana actual
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showError("No se pudo volver al menú:\n" + e.getMessage());
        }
    }
    private void showError(String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText("Error");

        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }

        a.showAndWait();
    }


    @FXML
    private void onModificar() {
        Materia sel = tblMaterias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAdvertencia("Selección", "Selecciona una materia.");
            return;
        }

        try {
            String nombre = txtNombre.getText().trim();
            int creditos = parseCreditos();
            String clave = sel.getClave().trim(); // clave original

            if (nombre.isEmpty())
                throw new IllegalArgumentException("El nombre no puede estar vacío.");

            // (Clave no se modifica, pero de todos modos validamos por seguridad)
            if (clave.matches(".*[-_]$")) {
                throw new IllegalArgumentException("La clave no puede terminar en '-' o '_' (falta un número).");
            }

            if (clave.matches(".*[-_][A-Z]+.*")) {
                throw new IllegalArgumentException("Después de '-' o '_' debe venir un número.");
            }

            boolean nombreExiste = data.stream()
                    .anyMatch(m -> m != sel && m.getNombre().equalsIgnoreCase(nombre));

            if (nombreExiste)
                throw new IllegalArgumentException("Ya existe una materia con ese nombre.");

            bo.modificar(new Materia(sel.getClave(), nombre, creditos));
            recargarTabla();
            limpiarFormulario();
            mostrarInfo("Éxito", "Materia modificada.");

        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }

    // ALERTS
    private Stage getStage() {
        javafx.stage.Window w = javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isFocused)
                .findFirst()
                .orElse(null);
        return (w instanceof Stage) ? (Stage) w : null;
    }

    private void mostrarAdvertencia(String h, String c) {
        Alert a = new Alert(Alert.AlertType.WARNING, c, ButtonType.OK);
        a.setHeaderText(h);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void mostrarError(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText(h);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void mostrarInfo(String h, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.setHeaderText(h);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }
}
