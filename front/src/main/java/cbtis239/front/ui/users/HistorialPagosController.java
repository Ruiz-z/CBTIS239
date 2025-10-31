package cbtis239.front.ui.users;

import cbtis239.bo.PagoBO;
import cbtis239.model.PagoHist;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Callback;



public class HistorialPagosController {

    @FXML private TextField txtBusqueda;      // Matrícula o folio
    @FXML private Button btnBuscar;
    @FXML private TableView<PagoHist> tblHistorial;
    @FXML private TableColumn<PagoHist, String> colNombre;
    @FXML private TableColumn<PagoHist, Number> colMonto;
    @FXML private TableColumn<PagoHist, String> colPeriodo;

    private final PagoBO bo = new PagoBO();

    @FXML
    private void initialize() {
        // Placeholders y UX
        tblHistorial.setPlaceholder(new Label("Ingrese una matrícula o folio y presione Buscar"));
        txtBusqueda.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onBuscar(); });

        // Columnas -> getters del POJO PagoHist (nombreCompleto, monto, periodo)
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        // Formateo de monto como moneda y alineado a la derecha
        colMonto.setCellFactory(formateoMoneda());
    }

    private Callback<TableColumn<PagoHist, Number>, TableCell<PagoHist, Number>> formateoMoneda() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", value.doubleValue()));
                }
                setAlignment(Pos.CENTER_RIGHT);
            }
        };
    }

    @FXML
    private void onBuscar() {
        String entrada = txtBusqueda.getText() != null ? txtBusqueda.getText().trim() : "";
        if (entrada.isEmpty()) {
            tblHistorial.getItems().clear();
            Alert a = new Alert(Alert.AlertType.WARNING, "Escribe una matrícula o un folio.");
            a.showAndWait();
            return;
        }

        try {
            ObservableList<PagoHist> data = bo.buscarHistorialTodosPeriodos(entrada);
            tblHistorial.setItems(data);
            if (data.isEmpty()) {
                tblHistorial.setPlaceholder(new Label("Sin pagos registrados para esa matrícula/folio."));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo consultar el historial:\n" + ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void volverAlMenu(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/cbtis239/front/views/MenuSF.fxml"));
            Stage st = new Stage();
            st.setTitle("MenúSF");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();
            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
