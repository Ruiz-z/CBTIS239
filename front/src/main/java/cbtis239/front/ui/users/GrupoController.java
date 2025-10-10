package cbtis239.front.ui.users;

import cbtis239.bo.GrupoBo;
import cbtis239.model.Especialidad;
import cbtis239.model.Grupo;
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
                // seleccionar especialidad en el combo
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
            e.printStackTrace();
            showError("No se pudieron cargar datos:\n\n" + e.getMessage());
        }
    }

    @FXML
    private void onRegistrar() {
        try {
            var esp = cbEspecialidad.getValue();
            Grupo g = bo.crear(txtNombreGrupo.getText(), txtCapacidad.getText(),
                    esp == null ? null : esp.getClave());
            listaGrupos.add(0, g);
            limpiar();
            showInfo("Grupo registrado.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        Grupo sel = tablaGrupos.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Selecciona un grupo en la tabla."); return; }

        try {
            sel.setNombreGrupo(txtNombreGrupo.getText());
            try {
                sel.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            } catch (NumberFormatException n) {
                throw new IllegalArgumentException("La capacidad debe ser numérica.");
            }
            var esp = cbEspecialidad.getValue();
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
    private void onCancelar() { limpiar(); }

    private void limpiar() {
        txtNombreGrupo.clear();
        txtCapacidad.clear();
        cbEspecialidad.getSelectionModel().clearSelection();
        tablaGrupos.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver(ActionEvent event) {
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

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
}
