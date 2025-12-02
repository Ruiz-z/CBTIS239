package cbtis239.front.ui.users;

import cbtis239.bo.CalificacionBO;
import cbtis239.model.Calificacion;
import cbtis239.model.Catalogo;
import cbtis239.model.Docente;
import cbtis239.session.SesionActual;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CalificacionesController {

    // ====== Controles ======
    @FXML private ComboBox<Catalogo> cmbCurso;
    @FXML private TextField txtBuscarMatricula;
    @FXML private TextField txtPromedioGeneral;

    @FXML private TableView<Calificacion> tblCalificaciones;
    @FXML private TableColumn<Calificacion, String>  colNombre;
    @FXML private TableColumn<Calificacion, String>  colMatricula;
    @FXML private TableColumn<Calificacion, Double> colParcial1;
    @FXML private TableColumn<Calificacion, Double> colParcial2;
    @FXML private TableColumn<Calificacion, Double> colParcial3;
    @FXML private TableColumn<Calificacion, Double> colExamenFinal; // NUEVA COLUMNA
    @FXML private TableColumn<Calificacion, Double> colFinal;

    private final CalificacionBO califBO = new CalificacionBO();
    private int cursoSeleccionado = -1;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        try {
            configurarTabla();

            // Buscar al presionar Enter
            txtBuscarMatricula.setOnAction(e -> onBuscarPorMatricula());

            // Docente actual desde la sesión global
            Docente doc = SesionActual.getDocente();
            if (doc == null) {
                showError("No hay un docente en sesión. Vuelva a iniciar sesión.");
                deshabilitarPantalla();
                return;
            }

            // Llenar ComboBox de cursos del docente
            var cursos = califBO.cursosDelDocente(doc.getDocenteId());
            cmbCurso.getItems().setAll(cursos);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al inicializar la pantalla de calificaciones:\n" + e.getMessage());
        }
    }

    private void deshabilitarPantalla() {
        cmbCurso.setDisable(true);
        txtBuscarMatricula.setDisable(true);
        tblCalificaciones.setDisable(true);
    }

    // ============================================================
    // CONFIGURAR TABLA
    // ============================================================
    private void configurarTabla() {
        colNombre.setCellValueFactory(c ->
                new SimpleObjectProperty<>(
                        c.getValue().getAlumno() != null
                                ? c.getValue().getAlumno().getNombreCompleto()
                                : ""
                )
        );
        colMatricula.setCellValueFactory(c -> c.getValue().alumnoMatriculaProperty());
        colParcial1.setCellValueFactory(c -> c.getValue().parcial1Property().asObject());
        colParcial2.setCellValueFactory(c -> c.getValue().parcial2Property().asObject());
        colParcial3.setCellValueFactory(c -> c.getValue().parcial3Property().asObject());
        colExamenFinal.setCellValueFactory(c -> c.getValue().examenFinalProperty().asObject()); // NUEVA
        colFinal.setCellValueFactory(c -> c.getValue().promedioProperty().asObject());

        // Columnas editables
        configurarColumnaEditable(colParcial1, "Parcial 1");
        configurarColumnaEditable(colParcial2, "Parcial 2");
        configurarColumnaEditable(colParcial3, "Parcial 3");
        configurarColumnaEditable(colExamenFinal, "Examen Final"); // NUEVA

        tblCalificaciones.setEditable(true);
    }

    private void configurarColumnaEditable(TableColumn<Calificacion, Double> col, String campo) {
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        col.setOnEditCommit(evt -> {
            Calificacion cal = evt.getRowValue();
            Double nuevoValor = evt.getNewValue();

            if (nuevoValor == null || nuevoValor < 0 || nuevoValor > 100) {
                showError("❌ La calificación debe estar entre 0 y 100.");
                tblCalificaciones.refresh();
                return;
            }

            switch (campo) {
                case "Parcial 1" -> cal.setParcial1(nuevoValor);
                case "Parcial 2" -> cal.setParcial2(nuevoValor);
                case "Parcial 3" -> cal.setParcial3(nuevoValor);
                case "Examen Final" -> cal.setExamenFinal(nuevoValor); // NUEVO CASO
            }

            cal.recalcularPromedio();
            tblCalificaciones.refresh();
            actualizarPromedioGeneral();
        });
    }

    // ============================================================
    // EVENTOS
    // ============================================================

    /** Botón Buscar: carga los alumnos del curso seleccionado. */
    @FXML
    private void onBuscar() {
        Catalogo cursoCat = cmbCurso.getValue();
        if (cursoCat == null) {
            showError("Selecciona un curso.");
            return;
        }

        cursoSeleccionado = cursoCat.getId();

        try {
            List<Calificacion> lista = califBO.listarPorCurso(cursoSeleccionado);
            tblCalificaciones.setItems(FXCollections.observableArrayList(lista));
            actualizarPromedioGeneral();
        } catch (SQLException e) {
            showError("Error al cargar las calificaciones:\n" + e.getMessage());
        }
    }

    /** Buscar/filtrar por matrícula (manteniendo el curso actual). */
    @FXML
    private void onBuscarPorMatricula() {
        try {
            if (cursoSeleccionado == -1) {
                showError("Selecciona un curso antes de buscar.");
                return;
            }

            String matricula = txtBuscarMatricula.getText().trim();

            // Campo vacío: restaurar todos los alumnos del curso
            if (matricula.isEmpty()) {
                tblCalificaciones.setItems(FXCollections.observableArrayList(
                        califBO.listarPorCurso(cursoSeleccionado)
                ));
                actualizarPromedioGeneral();
                showInfo("Se ha restaurado la lista completa de alumnos.");
                return;
            }

            // Filtrar sobre lo que ya está cargado
            var listaFiltrada = tblCalificaciones.getItems().stream()
                    .filter(c -> c.getAlumnoMatricula() != null &&
                                 c.getAlumnoMatricula().equalsIgnoreCase(matricula))
                    .toList();

            if (listaFiltrada.isEmpty()) {
                showInfo("No se encontró ningún alumno con matrícula " + matricula);
            } else {
                tblCalificaciones.setItems(FXCollections.observableArrayList(listaFiltrada));
            }

        } catch (SQLException e) {
            showError("Error al filtrar por matrícula:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onGuardar() {
        if (cursoSeleccionado == -1) {
            showError("Selecciona un curso antes de guardar.");
            return;
        }

        try {
            califBO.guardar(tblCalificaciones.getItems());
            actualizarPromedioGeneral();
            showInfo("✅ Calificaciones guardadas correctamente.");
        } catch (Exception e) {
            showError("Error al guardar las calificaciones:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        tblCalificaciones.getItems().clear();
        txtPromedioGeneral.clear();
        txtBuscarMatricula.clear();
        // no reseteo cursoSeleccionado para que el docente pueda volver a buscar
    }

    @FXML
    private void onFinalizar() {
        showInfo("✔️ Calificaciones finalizadas correctamente.");
    }

    @FXML
    private void onVolverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cbtis239/front/views/MenuDocente.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Menú Docente");
            stage.setScene(new Scene(root));
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setMaximized(true);
            stage.show();

            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showError("No se pudo volver al menú docente:\n" + e.getMessage());
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private void actualizarPromedioGeneral() {
        try {
            if (cursoSeleccionado != -1) {
                double prom = califBO.promedioGeneral(cursoSeleccionado);
                txtPromedioGeneral.setText(String.format("%.2f", prom));
            }
        } catch (SQLException e) {
            txtPromedioGeneral.setText("—");
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
