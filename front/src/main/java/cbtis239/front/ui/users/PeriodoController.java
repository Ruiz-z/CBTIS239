package cbtis239.front.ui.users;

import cbtis239.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class PeriodoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;

    @FXML private TableView<?> tablaPeriodos;
    @FXML private TableColumn<?, ?> colId, colNombre, colInicio, colFin;

    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;

    @FXML
    private void initialize() {
        // Aquí puedes inicializar tabla con Periodo si tienes modelo
    }

    @FXML
    private void onAgregar() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Agregar periodo (TODO implementar)");
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void onModificar() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Modificar periodo (TODO implementar)");
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void onEliminar() {
        Alert a = new Alert(Alert.AlertType.WARNING, "Eliminar periodo (TODO implementar)");
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void onCancelar() {
        txtId.clear();
        txtNombre.clear();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        tablaPeriodos.getSelectionModel().clearSelection();
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
}

