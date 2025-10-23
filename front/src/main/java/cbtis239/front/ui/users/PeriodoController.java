package cbtis239.front.ui.users;

import cbtis239.bo.PeriodoBo;
import cbtis239.model.Periodo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.time.LocalDate;

public class PeriodoController {

    // Campos (sin ID de captura)
    @FXML private TextField txtNombre;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;

    // Tabla
    @FXML private TableView<Periodo> tablaPeriodos;
    @FXML private TableColumn<Periodo, Integer> colId;
    @FXML private TableColumn<Periodo, String> colNombre;
    @FXML private TableColumn<Periodo, LocalDate> colInicio;
    @FXML private TableColumn<Periodo, LocalDate> colFin;

    private final PeriodoBo bo = new PeriodoBo();
    private final ObservableList<Periodo> data = FXCollections.observableArrayList();

    // id seleccionado (para modificar/eliminar)
    private int seleccionadoId = 0;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPeriodo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInicio.setCellValueFactory(new PropertyValueFactory<>("inicio"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("fin"));

        tablaPeriodos.setItems(data);

        // Cargar datos
        recargar();

        // Al seleccionar, pasar a formulario
        tablaPeriodos.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            if (b != null) {
                seleccionadoId = b.getIdPeriodo();
                txtNombre.setText(b.getNombre());
                dpInicio.setValue(b.getInicio());
                dpFin.setValue(b.getFin());
            }
        });
    }

    private void recargar() {
        try {
            data.setAll(bo.listar());
        } catch (Exception e) {
            showError("No se pudieron cargar periodos\n\n" + e.getMessage());
        }
    }

    @FXML
    private void onAgregar() {
        try {
            Periodo p = new Periodo(0,
                    txtNombre.getText().trim(),
                    dpInicio.getValue(),
                    dpFin.getValue());
            bo.guardar(p);
            recargar();
            limpiar();
            showInfo("Periodo agregado.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onModificar() {
        if (seleccionadoId <= 0) { showError("Seleccione un periodo de la tabla."); return; }
        try {
            Periodo p = new Periodo(seleccionadoId,
                    txtNombre.getText().trim(),
                    dpInicio.getValue(),
                    dpFin.getValue());
            bo.guardar(p);
            recargar();
            limpiar();
            showInfo("Periodo modificado.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        if (seleccionadoId <= 0) { showError("Seleccione un periodo de la tabla."); return; }
        if (new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el periodo seleccionado?")
                .showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            bo.eliminar(seleccionadoId);
            recargar();
            limpiar();
            showInfo("Periodo eliminado.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML private void onCancelar() { limpiar(); }

    private void limpiar() {
        seleccionadoId = 0;
        txtNombre.clear();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        tablaPeriodos.getSelectionModel().clearSelection();
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
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo volver al Menú\n\n" + e.getMessage());
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
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
