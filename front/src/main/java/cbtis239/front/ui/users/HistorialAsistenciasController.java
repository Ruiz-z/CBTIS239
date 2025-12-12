package cbtis239.front.ui.users;

import cbtis239.dao.AsistenciaHistorialDAO;
import cbtis239.model.AsistenciaHistorial;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

// Explorador de archivos
import java.awt.Desktop;

public class HistorialAsistenciasController {

    // Campos de búsqueda
    @FXML private TextField txtMatricula;
    @FXML private TextField txtCurp;

    // Tabla
    @FXML private TableView<AsistenciaHistorial> tblAsistencias;
    @FXML private TableColumn<AsistenciaHistorial, LocalDate> colFecha;
    @FXML private TableColumn<AsistenciaHistorial, String>  colEstado;
    @FXML private TableColumn<AsistenciaHistorial, Boolean> colJustificada;
    @FXML private TableColumn<AsistenciaHistorial, String>  colObservaciones;

    // Botones
    @FXML private Button btnBuscar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private Button btnPDF;
    @FXML private Button btnVolverMenu;

    private final ObservableList<AsistenciaHistorial> data = FXCollections.observableArrayList();
    private final AsistenciaHistorialDAO dao = new AsistenciaHistorialDAO();

    @FXML
    private void initialize() {

        // ==== Columnas básicas ====
        colFecha.setCellValueFactory(c -> c.getValue().fechaProperty());
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());

        // ==== Columna JUSTIFICADA (CheckBox) ====
        colJustificada.setCellValueFactory(c -> c.getValue().justificadaProperty());
        colJustificada.setCellFactory(tc -> new CheckBoxTableCell<>());
        colJustificada.setEditable(true);

        // ==== Columna OBSERVACIONES (editable solo si no está "Presente") ====
        colObservaciones.setCellValueFactory(c -> c.getValue().observacionProperty());
        colObservaciones.setEditable(true);

        colObservaciones.setCellFactory(col -> new TextFieldTableCell<AsistenciaHistorial, String>(
                new DefaultStringConverter()) {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setEditable(false);
                    setDisable(false);
                    setStyle("");
                    return;
                }

                setText(item);

                AsistenciaHistorial ah =
                        (AsistenciaHistorial) getTableRow().getItem();

                boolean editable = !"Presente".equalsIgnoreCase(ah.getEstado());

                setEditable(editable);
                setDisable(!editable);

                if (!editable) {
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
                } else {
                    setStyle("");
                }
            }

            @Override
            public void startEdit() {
                AsistenciaHistorial ah =
                        (AsistenciaHistorial) getTableRow().getItem();

                if (ah != null && !"Presente".equalsIgnoreCase(ah.getEstado())) {
                    super.startEdit();   // permite editar
                }
            }
        });

        // CLAVE: cuando se termina de editar, actualizar el modelo
        colObservaciones.setOnEditCommit(event -> {
            AsistenciaHistorial ah = event.getRowValue();
            if (ah != null) {
                ah.setObservacion(event.getNewValue());
            }
        });

        tblAsistencias.setItems(data);
        tblAsistencias.setEditable(true);

        // ==== Botones ====
        btnBuscar.setOnAction(e -> buscar());
        btnGuardar.setOnAction(e -> guardar());
        btnCancelar.setOnAction(e -> limpiar());

        if (btnPDF != null) {
            btnPDF.setOnAction(e -> generarPdf());
        }
        if (btnVolverMenu != null) {
            btnVolverMenu.setOnAction(this::volverAlMenu);
        }
    }

    // ======================= LÓGICA PRINCIPAL =======================

    private void buscar() {
        String mat = txtMatricula.getText();
        String curp = txtCurp.getText();

        if ((mat == null || mat.isBlank()) && (curp == null || curp.isBlank())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Debe capturar al menos Matrícula o CURP.");
            return;
        }

        try {
            List<AsistenciaHistorial> lista = dao.buscar(mat, curp);
            data.setAll(lista);

            if (lista.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados",
                        "No se encontraron asistencias para los datos proporcionados.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Ocurrió un error al consultar el historial:\n" + ex.getMessage());
        }
    }

    private void guardar() {
        try {
            dao.guardarCambios(data);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    "Los cambios se guardaron correctamente.");
            limpiar();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Error al guardar los cambios:\n" + ex.getMessage());
        }
    }

    private void limpiar() {
        txtMatricula.clear();
        txtCurp.clear();
        data.clear();
    }

    // ======================= VOLVER AL MENÚ SF =======================

    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/cbtis239/front/views/menu.fxml"));
            Stage st = new Stage();
            st.setTitle("MenúSF");
            st.setScene(new Scene(root));
            st.setMaximized(true);
            st.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir el menú de Servicios Financieros.");
        }
    }

    // ======================= GENERAR PDF =======================

    private void generarPdf() {
        String matricula = txtMatricula.getText();

        if (matricula == null || matricula.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Captura la matrícula para nombrar el archivo.");
            return;
        }

        if (data.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso",
                    "No hay datos en la tabla para generar el PDF.");
            return;
        }

        try {
            File carpeta = new File("historial_asistencia");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            String nombreArchivo = "historial_asistencia_" + matricula + ".pdf";
            File archivoPdf = new File(carpeta, nombreArchivo);

            try (PDDocument doc = new PDDocument()) {

                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                try (PDPageContentStream cs =
                             new PDPageContentStream(doc, page)) {

                    float margin = 50;
                    float y = page.getMediaBox().getHeight() - margin;

                    // Título
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Historial de Asistencias");
                    cs.endText();

                    y -= 25;

                    // Datos encabezado
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 12);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Matrícula: " + matricula);
                    cs.endText();

                    y -= 15;

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 12);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("CURP: " + (txtCurp.getText() == null ? "" : txtCurp.getText()));
                    cs.endText();

                    y -= 30;

                    // Encabezados tabla
                    float xFecha = margin;
                    float xEstado = xFecha + 110;
                    float xJust = xEstado + 110;
                    float xObs = xJust + 110;

                    cs.setFont(PDType1Font.HELVETICA_BOLD, 11);

                    cs.beginText(); cs.newLineAtOffset(xFecha, y); cs.showText("FECHA"); cs.endText();
                    cs.beginText(); cs.newLineAtOffset(xEstado, y); cs.showText("ESTADO"); cs.endText();
                    cs.beginText(); cs.newLineAtOffset(xJust,  y); cs.showText("JUSTIFICADA"); cs.endText();
                    cs.beginText(); cs.newLineAtOffset(xObs,   y); cs.showText("OBSERVACIONES"); cs.endText();

                    y -= 15;
                    cs.setFont(PDType1Font.HELVETICA, 10);

                    // Filas
                    for (AsistenciaHistorial a : data) {
                        if (y < 60) {
                            break;
                        }

                        String fechaStr = a.getFecha() != null ? a.getFecha().toString() : "";
                        String estadoStr = a.getEstado();
                        String justStr = a.isJustificada() ? "Sí" : "No";
                        String obsStr = a.getObservacion();
                        if (obsStr == null) obsStr = "";

                        cs.beginText(); cs.newLineAtOffset(xFecha, y); cs.showText(fechaStr); cs.endText();
                        cs.beginText(); cs.newLineAtOffset(xEstado, y); cs.showText(estadoStr); cs.endText();
                        cs.beginText(); cs.newLineAtOffset(xJust,  y); cs.showText(justStr); cs.endText();
                        cs.beginText(); cs.newLineAtOffset(xObs,   y); cs.showText(recortar(obsStr, 60)); cs.endText();

                        y -= 12;
                    }
                }

                doc.save(archivoPdf);
            }

            abrirEnExplorador(carpeta);

            mostrarAlerta(Alert.AlertType.INFORMATION, "PDF generado",
                    "Se generó el archivo:\n" + archivoPdf.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo generar el PDF:\n" + e.getMessage());
        }
    }

    private void abrirEnExplorador(File carpeta) {
        try {
            if (carpeta != null && carpeta.exists()
                    && Desktop.isDesktopSupported()) {

                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(carpeta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String recortar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }

    private void mostrarAlerta(Alert.AlertType type, String titulo, String mensaje) {
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

