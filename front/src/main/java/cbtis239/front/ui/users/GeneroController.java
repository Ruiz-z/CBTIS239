package cbtis239.front.ui.users;

import cbtis239.bo.GeneroBO;
import cbtis239.model.Genero;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GeneroController {

    // ELIMINADO: @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TableView<Genero> tabla;
    // ELIMINADO: @FXML private TableColumn<Genero, Number> colId;
    @FXML private TableColumn<Genero, String> colNombre;
    
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;

    private final GeneroBO bo = new GeneroBO();
    private final ObservableList<Genero> datos = FXCollections.observableArrayList();
    private Integer editingId = null; // Para manejar el ID al modificar

    @FXML
    public void initialize() {
        // ELIMINADO: colId.setCellValueFactory(...)
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        
        reloadTable();

        tabla.getSelectionModel().selectedItemProperty().addListener((obs,o,sel)->{
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
                editingId = sel.getIdGenero(); 
                
                // Modo Edición
                btnModificar.setDisable(false);
                btnEliminar.setDisable(false);
                btnAgregar.setDisable(true);
            } else {
                // Modo Creación
                editingId = null;
                btnModificar.setDisable(true);
                btnEliminar.setDisable(true);
                btnAgregar.setDisable(false);
            }
        });
        
        limpiar(); 
    }

    private void reloadTable() {
        try {
            // El BO fue corregido para tener el método findAll()
            datos.setAll(bo.findAll()); 
            tabla.setItems(datos);
        } catch (Exception e) {
            showError("Error al cargar datos: " + e.getMessage());
        }
    }

    @FXML
    private void onAgregar() {
        try {
            // VERIFICACIÓN DE CAMPO VACÍO
            if (txtNombre.getText().trim().isEmpty()) {
                showError("El nombre del género no puede estar vacío."); return;
            }
            
            // CORRECCIÓN: Llamada al BO usando solo el String
            int newId = bo.agregar(txtNombre.getText());
            
            reloadTable();
            limpiar();
            showInfo("Género guardado con ID: " + newId);
        } catch (Exception e) { 
            // Captura Exception o IllegalArgumentException (del BO)
            showError("Error al guardar: " + e.getMessage()); 
        }
    }

    @FXML
    private void onModificar() {
        if (editingId == null || editingId <= 0) { 
            showError("Debe seleccionar un género de la tabla para modificar."); 
            return; 
        }
        
        try {
            // VERIFICACIÓN DE CAMPO VACÍO
            if (txtNombre.getText().trim().isEmpty()) {
                showError("El nombre del género no puede estar vacío."); return;
            }
            
            // CORRECCIÓN: Llamada al BO usando el ID y el String
            bo.modificar(editingId.intValue(), txtNombre.getText());
            
            reloadTable(); 
            limpiar();
            showInfo("Género modificado.");
        } catch (Exception e) { 
            showError("Error al modificar: " + e.getMessage()); 
        }
    }

    @FXML
    private void onEliminar() {
        Genero sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un registro."); return; }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que quieres eliminar el género '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirmar eliminación");
        
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                bo.eliminar(sel.getIdGenero());
                reloadTable();
                limpiar();
                showInfo("Género eliminado.");
            } catch (Exception e) { 
                showError("Error al eliminar: " + e.getMessage()); 
            }
        }
    }

    @FXML
    private void onCancelar() { 
        limpiar(); 
    }

    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/Menu2.fxml")); 
            Stage st = new Stage();
            st.setTitle("Menú");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();
            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo abrir el menú:\n\n" + e.getMessage());
        }
    }

    private void limpiar() {
        txtNombre.clear();
        tabla.getSelectionModel().clearSelection();
        editingId = null;
        
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        btnAgregar.setDisable(false);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}