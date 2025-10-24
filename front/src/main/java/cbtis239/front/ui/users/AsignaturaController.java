package cbtis239.front.ui.users;

import cbtis239.bo.MateriaBO;
import cbtis239.model.Materia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

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
        colClave.setCellValueFactory(c -> c.getValue().claveProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCreditos.setCellValueFactory(c -> c.getValue().creditosProperty());

        // Solo números en créditos (permite vacío)
        txtCreditos.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("\\d*")) {
                txtCreditos.setText(val.replaceAll("[^\\d]", ""));
            }
        });

        // Al seleccionar, llenar formulario
        tblMaterias.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                txtClave.setText(n.getClave());
                txtNombre.setText(n.getNombre());
                txtCreditos.setText(n.getCreditos() == 0 ? "" : String.valueOf(n.getCreditos()));
                txtClave.setDisable(true);
            } else {
                limpiarFormulario();
            }
        });

        tblMaterias.setItems(data);
        recargarTabla();
    }

    // ===== Helpers =====
    private void recargarTabla() {
        try {
            data.clear();                 // limpia lista actual
            data.addAll(bo.listar());     // recarga desde BD
            tblMaterias.refresh();        // fuerza repintado
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
        if (t == null || t.isBlank()) return 0;  // en tu BD es NULL/0 permitido
        try {
            int c = Integer.parseInt(t);
            if (c < 0) throw new NumberFormatException();
            return c;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Créditos debe ser un entero >= 0.");
        }
    }

    // ===== Acciones =====
    @FXML
    private void onAgregar() {
        try {
            String clave = txtClave.getText();
            String nombre = txtNombre.getText();
            int creditos = parseCreditos();

            bo.agregar(new Materia(clave, nombre, creditos));
            recargarTabla();          // << refresca inmediatamente
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
            recargarTabla();          // << refresca inmediatamente
            limpiarFormulario();
            mostrarInfo("Éxito", "Materia eliminada.");
        } catch (Exception ex) {
            mostrarError("Error", ex.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        Materia sel = tblMaterias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAdvertencia("Selección", "Selecciona una materia.");
            return;
        }
        try {
            String nombre = txtNombre.getText();
            int creditos = parseCreditos();

            bo.modificar(new Materia(sel.getClave(), nombre, creditos));
            recargarTabla();          // << refresca inmediatamente
            // (opcionalmente podrías actualizar solo el objeto sel y hacer tblMaterias.refresh())
            limpiarFormulario();
            mostrarInfo("Éxito", "Materia modificada.");
        } catch (IllegalArgumentException iae) {
            mostrarAdvertencia("Validación", iae.getMessage());
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
            newStage.setMaximized(true);
            newStage.show();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            // === Ajuste: usar alerta modal con owner de la ventana actual ===
            mostrarError("No se pudo volver al menú", e.getMessage());
        }
    }

    // ====== Helpers de Alert con owner y modalidad ======
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
