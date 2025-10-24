package cbtis239.front.ui.users;

import cbtis239.bo.PagoBO;
import cbtis239.model.Pago;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class pagosController {

    @FXML private TextField txtBusqueda;
    @FXML private TextField txtMonto;
    @FXML private TextField txtPeriodo;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> colNombre;
    @FXML private TableColumn<Pago, Double> colMonto;
    @FXML private TableColumn<Pago, String> colPagado;

    private final PagoBO pagoBO = new PagoBO();

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colMonto.setCellValueFactory(data -> data.getValue().montoProperty().asObject());
        colPagado.setCellValueFactory(data -> data.getValue().pagadoProperty());

        txtMonto.setText(String.format("%.2f", PagoBO.MONTO_FIJO));
        txtMonto.setDisable(true);

        try {
            txtPeriodo.setText(pagoBO.periodoActualNombre());
        } catch (Exception e) {
            txtPeriodo.setText("Sin periodo vigente");
        }
        txtPeriodo.setDisable(true);

        // Cargar TODOS (alumnos + aspirantes) con su estado de pago actual
        tablaPagos.setItems(pagoBO.listarTodos());

        // Enter en buscar
        txtBusqueda.setOnAction(e -> onBuscar());
    }

    @FXML
    private void onBuscar() {
        ObservableList<Pago> datos = pagoBO.buscar(txtBusqueda.getText());
        tablaPagos.setItems(datos);
    }

    @FXML
    private void onRegistrarPago(ActionEvent e) {
        Button btn = (Button) e.getSource();
        btn.setDisable(true);
        try {
            String entrada = txtBusqueda.getText().trim();
            if (entrada.isEmpty()) { info("Ingrese una matrícula o folio."); return; }
            pagoBO.registrarPago(entrada);
            onBuscar();
            txtBusqueda.clear();
            info("Pago registrado correctamente.");
        } catch (Exception ex) {
            error("No se pudo registrar el pago:\n" + ex.getMessage());
        } finally {
            btn.setDisable(false);
        }
    }


    @FXML
    private void onCancelar() {
        txtBusqueda.clear();
        tablaPagos.setItems(pagoBO.listarTodos());
    }

    @FXML
    private void onVolver(ActionEvent event) {
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
            error("No se pudo abrir el menú:\n" + e.getMessage());
        }
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
