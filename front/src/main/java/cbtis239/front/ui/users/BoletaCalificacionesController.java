package cbtis239.front.ui.users;

import cbtis239.bo.AsistenciaBoletaBO;
import cbtis239.bo.BoletaCalificacionBO;
import cbtis239.model.AsistenciaBoletaResumen;
import cbtis239.model.BoletaCalificacion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

public class BoletaCalificacionesController {

    @FXML private TextField txtMatricula;

    @FXML private TableView<BoletaCalificacion> tblBoleta;
    @FXML private TableColumn<BoletaCalificacion, String>  colCurso;
    @FXML private TableColumn<BoletaCalificacion, Double> colParcial1;
    @FXML private TableColumn<BoletaCalificacion, Double> colParcial2;
    @FXML private TableColumn<BoletaCalificacion, Double> colParcial3;
    @FXML private TableColumn<BoletaCalificacion, Double> colExamenFinal;

    // Labels para resumen de asistencia
    @FXML private Label lblDiasEscolares;
    @FXML private Label lblDiasAsistidos;
    @FXML private Label lblPorcentajeAsistencia;

    private final BoletaCalificacionBO boletaBO = new BoletaCalificacionBO();
    private final AsistenciaBoletaBO asistenciaBoletaBO = new AsistenciaBoletaBO();

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        configurarTabla();
        limpiarResumenAsistencia();
    }

    private void configurarTabla() {
        colCurso.setCellValueFactory(c -> c.getValue().cursoProperty());
        colParcial1.setCellValueFactory(c -> c.getValue().parcial1Property().asObject());
        colParcial2.setCellValueFactory(c -> c.getValue().parcial2Property().asObject());
        colParcial3.setCellValueFactory(c -> c.getValue().parcial3Property().asObject());
        colExamenFinal.setCellValueFactory(c -> c.getValue().examenFinalProperty().asObject());
    }

    // ============================================================
    // EVENTOS
    // ============================================================
    @FXML
    private void onBuscar() {
        String matricula = txtMatricula.getText().trim();

        if (matricula.isEmpty()) {
            warn("Dato requerido", "Capture una matrícula.");
            return;
        }

        try {
            // 1) Boleta de calificaciones
            List<BoletaCalificacion> lista = boletaBO.boletaDeAlumno(matricula);

            if (lista.isEmpty()) {
                tblBoleta.getItems().clear();
                limpiarResumenAsistencia();
                info("No se encontraron calificaciones para la matrícula " + matricula);
                return;
            }

            tblBoleta.setItems(FXCollections.observableArrayList(lista));

            // 2) Resumen de asistencia
            actualizarResumenAsistencia(matricula);

        } catch (SQLException e) {
            e.printStackTrace();
            error("Error al consultar la boleta", e.getMessage());
        } catch (IllegalArgumentException ex) {
            error("Error", ex.getMessage());
        }
    }

    @FXML
    private void onGenerarPDF() {
        if (tblBoleta.getItems() == null || tblBoleta.getItems().isEmpty()) {
            warn("Sin datos", "No hay datos para generar la boleta.");
            return;
        }

        String matricula = txtMatricula.getText().trim();
        if (matricula.isEmpty()) {
            warn("Dato requerido", "Capture una matrícula.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar boleta en PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf")
        );
        chooser.setInitialFileName("boleta_" + matricula + ".pdf");

        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;

        try (OutputStream out = new java.io.FileOutputStream(file)) {
            cbtis239.report.BoletaCalificacionesPDF pdf =
                    new cbtis239.report.BoletaCalificacionesPDF();
            pdf.generar(matricula, out);
            info("Boleta generada correctamente:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            error("No se pudo generar el PDF", e.getMessage());
        }
    }

    @FXML
    private void onVolverMenu() {
        try {
            var url = getClass().getResource("/cbtis239/front/views/Menu3.fxml");
            if (url == null) {
                throw new IllegalStateException("No se encontró Menu3.fxml");
            }

            Parent root = FXMLLoader.load(url);

            // Reusar el mismo Stage de la pantalla actual
            Stage stage = (Stage) txtMatricula.getScene().getWindow();
            stage.setTitle("Menú Principal");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);              // como tus demás pantallas
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            error("Error al abrir el menú", e.getMessage());
        }
    }


    // ============================================================
    // RESUMEN ASISTENCIA
    // ============================================================
    private void actualizarResumenAsistencia(String matricula) {
        try {
            AsistenciaBoletaResumen r = asistenciaBoletaBO.resumenPeriodoActual(matricula);

            lblDiasEscolares.setText(String.valueOf(r.getDiasEscolares()));
            lblDiasAsistidos.setText(String.valueOf(r.getDiasAsistidos()));
            lblPorcentajeAsistencia.setText(String.format("%.2f %%", r.getPorcentaje()));

            double p = r.getPorcentaje();
            String color;
            if (p >= 90) {
                color = "green";
            } else if (p >= 75) {
                color = "orange";
            } else {
                color = "red";
            }
            lblPorcentajeAsistencia.setStyle("-fx-font-weight:bold; -fx-text-fill:" + color + ";");

        } catch (SQLException e) {
            e.printStackTrace();
            limpiarResumenAsistencia();
        }
    }

    private void limpiarResumenAsistencia() {
        lblDiasEscolares.setText("—");
        lblDiasAsistidos.setText("—");
        lblPorcentajeAsistencia.setText("—");
        lblPorcentajeAsistencia.setStyle("-fx-text-fill:black;");
    }

    // ============================================================
    // UTILIDADES (VERSION ADAPTADA)
    // ============================================================
    // ========= Alerts con owner =========
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

    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Advertencia");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(title);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        Stage owner = getStage();
        if (owner != null) {
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
