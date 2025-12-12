package cbtis239.front.ui.users;

import cbtis239.bo.KardexBO;
import cbtis239.model.KardexAlumnoInfo;
import cbtis239.model.KardexFila;
import cbtis239.report.KardexPDF;
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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

public class KardexController {

    @FXML private TextField txtMatricula;

    // Encabezado alumno
    @FXML private Label lblNombre;
    @FXML private Label lblCurp;
    @FXML private Label lblCarrera;
    @FXML private Label lblPromedio;
    @FXML private Label lblAvanceCreditos;

    // Tabla
    @FXML private TableView<KardexFila> tblKardex;
    @FXML private TableColumn<KardexFila, String>  colPlantel;
    @FXML private TableColumn<KardexFila, String>  colTipoUac;
    @FXML private TableColumn<KardexFila, String>  colClaveUac;
    @FXML private TableColumn<KardexFila, Number>  colSemestre;
    @FXML private TableColumn<KardexFila, String>  colNombreUac;
    @FXML private TableColumn<KardexFila, Number>  colCalif;
    @FXML private TableColumn<KardexFila, Number>  colCreditos;
    @FXML private TableColumn<KardexFila, String>  colPeriodo;

    private final KardexBO bo = new KardexBO();
    private KardexAlumnoInfo infoAlumnoActual;
    private List<KardexFila> filasActuales;
    private int totalCreditosPlan;

    @FXML
    public void initialize() {
        configurarTabla();
        limpiarEncabezado();
    }

    private void configurarTabla() {
        colPlantel.setCellValueFactory(c -> c.getValue().plantelProperty());
        colTipoUac.setCellValueFactory(c -> c.getValue().tipoUacProperty());
        colClaveUac.setCellValueFactory(c -> c.getValue().claveUacProperty());
        colSemestre.setCellValueFactory(c -> c.getValue().semestreProperty());
        colNombreUac.setCellValueFactory(c -> c.getValue().nombreUacProperty());
        colCalif.setCellValueFactory(c -> c.getValue().calificacionProperty());
        colCreditos.setCellValueFactory(c -> c.getValue().creditosProperty());
        colPeriodo.setCellValueFactory(c -> c.getValue().periodoEscolarProperty());
    }

    // ===========================
    // Buscar alumno / kardex
    // ===========================
    @FXML
    private void onBuscar() {
        String matricula = txtMatricula.getText().trim();
        if (matricula.isEmpty()) {
            warn("Dato requerido", "Capture una matrícula.");
            return;
        }

        try {
            infoAlumnoActual = bo.infoAlumno(matricula);
            if (infoAlumnoActual == null) {
                limpiarEncabezado();
                tblKardex.getItems().clear();
                warn("Sin resultados", "No se encontró un alumno con esa matrícula.");
                return;
            }

            filasActuales = bo.kardex(matricula);
            if (filasActuales == null || filasActuales.isEmpty()) {
                limpiarEncabezado();
                tblKardex.getItems().clear();
                warn("Sin datos", "El alumno no tiene materias registradas en el kardex.");
                return;
            }

            totalCreditosPlan = bo.totalCreditosPlan(infoAlumnoActual);
            int credAcred   = bo.creditosAcreditados(filasActuales);
            int credCursado = bo.creditosCursados(filasActuales);
            double avance   = (totalCreditosPlan > 0)
                    ? (credAcred * 100.0 / totalCreditosPlan)
                    : 0.0;
            double promedio = bo.promedioGeneral(filasActuales);

            // Encabezado
            lblNombre.setText(infoAlumnoActual.getNombreCompleto());
            lblCurp.setText(infoAlumnoActual.getCurp());
            lblCarrera.setText(infoAlumnoActual.getCarrera());
            lblPromedio.setText(String.format("%.2f", promedio));
            lblAvanceCreditos.setText(
                    String.format("%d / %d cursados, %.1f%% acreditados",
                            credCursado, totalCreditosPlan, avance)
            );

            // Tabla
            tblKardex.setItems(FXCollections.observableArrayList(filasActuales));

        } catch (SQLException e) {
            e.printStackTrace();
            error("Error al consultar el kardex", e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error", ex.getMessage());
        }
    }

    private void limpiarEncabezado() {
        lblNombre.setText("—");
        lblCurp.setText("—");
        lblCarrera.setText("—");
        lblPromedio.setText("—");
        lblAvanceCreditos.setText("—");
    }

    // ===========================
    // Generar PDF
    // ===========================
    @FXML
    private void onGenerarPDF() {
        String matricula = txtMatricula.getText().trim();
        if (matricula.isEmpty()) {
            warn("Dato requerido", "Capture una matrícula.");
            return;
        }

        if (tblKardex.getItems() == null || tblKardex.getItems().isEmpty()) {
            warn("Sin datos", "Debe buscar primero el kardex del alumno.");
            return;
        }

        try {
            // ============================================================
            // 1) Crear carpeta kardex_pdf dentro del proyecto
            // ============================================================
            String projectPath = System.getProperty("user.dir");  // Ruta base del proyecto
            File carpeta = new File(projectPath + File.separator + "kardex_pdf");

            if (!carpeta.exists()) {
                carpeta.mkdirs(); // Crear si no existe
            }

            // Archivo final
            File archivoPDF = new File(carpeta, "kardex_" + matricula + ".pdf");

            // ============================================================
            // 2) Generar el PDF automáticamente
            // ============================================================
            try (OutputStream out = new FileOutputStream(archivoPDF)) {
                new KardexPDF().generar(matricula, out);
            }

            // ============================================================
            // 3) Abrir explorador mostrando la carpeta
            // ============================================================
            abrirExplorador(carpeta);

            info("Kardex generado correctamente:\n" + archivoPDF.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            error("No se pudo generar el PDF", e.getMessage());
        }
    }
    private void abrirExplorador(File carpeta) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", carpeta.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", carpeta.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", carpeta.getAbsolutePath()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error("No se pudo abrir la carpeta", e.getMessage());
        }
    }

    @FXML
private void onVolverMenu() {
    try {
        var url = getClass().getResource("/cbtis239/front/views/Menu2.fxml");
        if (url == null) {
            throw new IllegalStateException("No se encontró Menu2.fxml");
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


    // ===========================
    // ALERTS (los que tú pasaste)
    // ===========================
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

    @SuppressWarnings("unused")
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
