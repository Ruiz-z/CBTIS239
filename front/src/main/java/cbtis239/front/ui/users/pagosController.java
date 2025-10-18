package cbtis239.front.ui.users;

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

    @FXML
    private void initialize() {
        // Configurar columnas
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colMonto.setCellValueFactory(data -> data.getValue().montoProperty().asObject());
        colPagado.setCellValueFactory(data -> data.getValue().pagadoProperty());

        // Asignar lista vacía por defecto
        tablaPagos.setItems(listaPagos);
    }

    @FXML
    private void onRegistrarPago(ActionEvent e) {
        String folio = txtBusqueda.getText().trim();

        if (folio.isEmpty()) {
            mostrarAlerta("Campo vacío", "Ingrese una matrícula o folio para registrar un pago.");
            return;
        }

        // Ejemplo de agregar registro simulado
        listaPagos.add(new Pago("Alumno " + folio, 250.00, "Sí"));
        txtBusqueda.clear();
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

    // 🔹 Clase interna modelo de Pago
    public static class Pago {
        private final javafx.beans.property.SimpleStringProperty nombre;
        private final javafx.beans.property.SimpleDoubleProperty monto;
        private final javafx.beans.property.SimpleStringProperty pagado;

        public Pago(String nombre, double monto, String pagado) {
            this.nombre = new javafx.beans.property.SimpleStringProperty(nombre);
            this.monto = new javafx.beans.property.SimpleDoubleProperty(monto);
            this.pagado = new javafx.beans.property.SimpleStringProperty(pagado);
        }

        public javafx.beans.property.StringProperty nombreProperty() { return nombre; }
        public javafx.beans.property.DoubleProperty montoProperty() { return monto; }
        public javafx.beans.property.StringProperty pagadoProperty() { return pagado; }
    }
}
