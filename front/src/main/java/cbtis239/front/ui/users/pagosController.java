package cbtis239.front.ui.users;

import cbtis239.bo.PagoBO;
import cbtis239.model.Pago;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class pagosController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> colNombre;
    @FXML private TableColumn<Pago, Double> colMonto;
    @FXML private TableColumn<Pago, String> colPagado;

    private final ObservableList<Pago> listaPagos = FXCollections.observableArrayList();
    private final PagoBO pagoBO = new PagoBO();

    @FXML
    private void initialize() {
        // Configurar columnas contra las properties del Model
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colMonto.setCellValueFactory(data -> data.getValue().montoProperty().asObject());
        colPagado.setCellValueFactory(data -> data.getValue().pagadoProperty());

        tablaPagos.setItems(listaPagos);

        // (Opcional) Enter en el TextField hace búsqueda
        txtBusqueda.setOnAction(e -> onBuscar());
    }

    // Botón "Registrar Pago" (usa monto fijo 250.00 y periodo 1; cambia lo que necesites)
    @FXML
    private void onRegistrarPago(ActionEvent e) {
        String folioOMat = txtBusqueda.getText().trim();
        if (folioOMat.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingrese una matrícula o folio para registrar un pago.");
            return;
        }
        try {
            Pago p = pagoBO.registrarPago(folioOMat, 250.00, 1); // <-- ajusta monto/periodo si quieres
            listaPagos.add(p);
            txtBusqueda.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("No se pudo registrar el pago:\n\n" + ex.getMessage());
        }
    }

    // (Opcional) Búsqueda para rellenar la tabla con pagos existentes de esa matrícula/folio
    private void onBuscar() {
        String q = txtBusqueda.getText().trim();
        if (q.isEmpty()) {
            tablaPagos.setItems(listaPagos);
            return;
        }
        ObservableList<Pago> datos = pagoBO.buscarPagos(q);
        tablaPagos.setItems(datos);
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
            showError("No se pudo abrir el menú:\n\n" + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Error");
        a.setContentText(msg);
        a.show();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
