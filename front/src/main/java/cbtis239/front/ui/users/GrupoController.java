package cbtis239.front.ui.users;

import cbtis239.bo.GrupoBo;
import cbtis239.model.Especialidad;
import cbtis239.model.Grupo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;

public class GrupoController {

    @FXML private TextField txtNombreGrupo;
    @FXML private TextField txtCapacidad;
    @FXML private ComboBox<Especialidad> cbEspecialidad;

    @FXML private TableView<Grupo> tablaGrupos;
    @FXML private TableColumn<Grupo, String> colNombre;
    @FXML private TableColumn<Grupo, Number> colCapacidad;
    @FXML private TableColumn<Grupo, String> colEspecialidad;

    private final ObservableList<Grupo> listaGrupos = FXCollections.observableArrayList();
    private final ObservableList<Especialidad> listaEsp = FXCollections.observableArrayList();
    private final GrupoBo bo = new GrupoBo();

    @FXML
    public void initialize() {

        // ============================================================
        // VALIDACIÓN MEJORADA — NOMBRE GRUPO
        // Letras, números, espacios, guion, guion bajo, mayúsculas,
        // y si se usa "-" o "_" DEBE ir seguido de un número.
        // ============================================================
        txtNombreGrupo.setTextFormatter(new TextFormatter<>(change -> {

            String newText = change.getControlNewText();

            // Permitir solo letras, números, espacios, _ y -
            if (!newText.matches("[A-Za-z0-9ÁÉÍÓÚÑáéíóúñ _-]*")) {
                return null;
            }

            int len = newText.length();
            if (len >= 2) {
                char anterior = newText.charAt(len - 2);
                char ultimo = newText.charAt(len - 1);

                // Si el anterior es "-" o "_" → el actual debe ser número
                if ((anterior == '-' || anterior == '_') && !Character.isDigit(ultimo)) {
                    return null;
                }
            }

            // Convertir a mayúsculas
            change.setText(change.getText().toUpperCase());

            return change;
        }));


        // Capacidad: números, max 3 dígitos
        txtCapacidad.setTextFormatter(createFormatter(3, "\\d*", false));

        colNombre.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreGrupo()));
        colCapacidad.setCellValueFactory(d ->
                new javafx.beans.property.SimpleIntegerProperty(d.getValue().getCapacidad()));
        colEspecialidad.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEspecialidadNombre()));

        tablaGrupos.setItems(listaGrupos);
        cbEspecialidad.setItems(listaEsp);

        tablaGrupos.getSelectionModel().selectedItemProperty().addListener((obs, a, sel) -> {
            if (sel != null) {
                txtNombreGrupo.setText(sel.getNombreGrupo());
                txtCapacidad.setText(String.valueOf(sel.getCapacidad()));
                listaEsp.stream()
                        .filter(e -> e.getClave() == sel.getEspecialidadClave())
                        .findFirst()
                        .ifPresent(cbEspecialidad::setValue);
            }
        });

        cargarInicial();
    }

    private void cargarInicial() {
        try {
            listaEsp.setAll(bo.listarEspecialidades());
            listaGrupos.setAll(bo.listarGrupos());
        } catch (Exception e) {
            showError("No se pudieron cargar datos:\n\n" + e.getMessage());
        }
    }

    // ==========================================
    // REGISTRAR
    // ==========================================
    @FXML
    private void onRegistrar() {
        try {
            String nombre = txtNombreGrupo.getText().trim();
            String capStr = txtCapacidad.getText().trim();
            Especialidad esp = cbEspecialidad.getValue();

            if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre no puede estar vacío.");
            if (capStr.isEmpty()) throw new IllegalArgumentException("La capacidad no puede estar vacía.");
            if (esp == null) throw new IllegalArgumentException("Selecciona una especialidad.");

            // Validar que NO termine en - o _
            if (nombre.matches(".*[-_]$")) {
                throw new IllegalArgumentException("El nombre no puede terminar con '-' o '_'.");
            }

            // Validar regla: después de - o _ debe ir número
            if (nombre.matches(".*[-_][A-ZÁÉÍÓÚÑ ]+.*")) {
                throw new IllegalArgumentException("Después de '-' o '_' debe ir un número.");
            }

            // Validar duplicados
            boolean existe = listaGrupos.stream()
                    .anyMatch(g -> g.getNombreGrupo().equalsIgnoreCase(nombre));
            if (existe) {
                throw new IllegalArgumentException("El nombre del grupo ya existe.");
            }

            Grupo g = bo.crear(nombre, capStr, esp.getClave());
            listaGrupos.add(0, g);
            limpiar();
            showInfo("Grupo registrado.");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    // ==========================================
    // MODIFICAR
    // ==========================================
    @FXML
    private void onModificar() {
        Grupo sel = tablaGrupos.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un grupo en la tabla."); return; }

        try {
            String nombreNuevo = txtNombreGrupo.getText().trim();

            if (nombreNuevo.isEmpty())
                throw new IllegalArgumentException("El nombre no puede estar vacío.");

            if (nombreNuevo.matches(".*[-_]$"))
                throw new IllegalArgumentException("El nombre no puede terminar con '-' o '_'.");

            if (nombreNuevo.matches(".*[-_][A-ZÁÉÍÓÚÑ ]+.*"))
                throw new IllegalArgumentException("Después de '-' o '_' debe ir un número.");

            // Validación duplicado
            boolean existe = listaGrupos.stream()
                    .anyMatch(g -> g != sel && g.getNombreGrupo().equalsIgnoreCase(nombreNuevo));
            if (existe) {
                throw new IllegalArgumentException("El nombre del grupo ya existe.");
            }

            sel.setNombreGrupo(nombreNuevo);
            sel.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));

            Especialidad esp = cbEspecialidad.getValue();
            if (esp == null) throw new IllegalArgumentException("Selecciona una especialidad.");

            sel.setEspecialidadClave(esp.getClave());
            sel.setEspecialidadNombre(esp.getNombre());

            bo.modificar(sel);

            tablaGrupos.refresh();
            showInfo("Grupo actualizado.");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        limpiar();
    }

    private void limpiar() {
        txtNombreGrupo.clear();
        txtCapacidad.clear();
        cbEspecialidad.getSelectionModel().clearSelection();
        tablaGrupos.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Menú Principal");
            newStage.setScene(new Scene(root));
            newStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("");
            newStage.show();
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showError("No se pudo volver al menú:\n" + e.getMessage());
        }
    }

    private Stage getStage() {
        return (Stage) tablaGrupos.getScene().getWindow();
    }

    private void showError(String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText("Error");
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    private void showInfo(String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
        a.initOwner(getStage());
        a.initModality(javafx.stage.Modality.WINDOW_MODAL);
        a.showAndWait();
    }

    // ============================================================
    //          GENERADOR DE FORMATTERS
    // ============================================================
    private TextFormatter<String> createFormatter(int maxLen, String regex, boolean upper) {
        UnaryOperator<TextFormatter.Change> filter = change -> {

            String newText = change.getControlNewText();

            if (newText.length() > maxLen) return null;
            if (!newText.matches(regex)) return null;

            if (upper)
                change.setText(change.getText().toUpperCase());

            return change;
        };
        return new TextFormatter<>(filter);
    }

}
