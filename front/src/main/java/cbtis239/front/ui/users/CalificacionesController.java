package cbtis239.front.ui.users;

import cbtis239.bo.CalificacionBO;
import cbtis239.dao.*;
import cbtis239.model.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalificacionesController {

    private final Map<String, String> materiaClaveMap = new HashMap<>(); // nombre → clave real
    @FXML private ComboBox<Catalogo> cmbGrupo, cmbMateria;
    @FXML private TextField txtPromedioGeneral;
    @FXML private TableView<Calificacion> tblCalificaciones;
    @FXML private TableColumn<Calificacion, String> colNombre, colMatricula;
    @FXML private TableColumn<Calificacion, Double> colParcial1, colParcial2, colParcial3, colFinal;

    private final CalificacionBO califBO = new CalificacionBO();
    private final GrupoDao grupoDAO = new GrupoDao();
    private final MateriaDao materiaDAO = new MateriaDao();
    private final CursoDao cursoDAO = new CursoDao();

    private int cursoSeleccionado = -1;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        try {
            // 🔹 Cargar grupos
            cmbGrupo.getItems().setAll(
                    grupoDAO.findAll().stream()
                            .map(g -> new Catalogo(g.getGrupoId(), g.getNombreGrupo()))
                            .toList()
            );

            // 🔹 Llenar mapa nombre → clave real de materia
            materiaDAO.listar().forEach(m -> materiaClaveMap.put(m.getNombre(), m.getClave()));

            // 🔹 Llenar ComboBox con nombres visibles de materia
            cmbMateria.getItems().setAll(
                    materiaDAO.listar().stream()
                            .map(m -> new Catalogo(m.getClave().hashCode(), m.getNombre()))
                            .toList()
            );

            // 🔹 Configurar la tabla editable
            configurarTabla();

            // 🔹 Permitir buscar al presionar Enter en el campo de matrícula
            txtBuscarMatricula.setOnAction(event -> {
                try {
                    onBuscarPorMatricula();
                } catch (Exception e) {
                    showError("Error al buscar por matrícula:\n" + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al inicializar la pantalla de calificaciones:\n" + e.getMessage());
        }
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
        colFinal.setCellValueFactory(c -> c.getValue().promedioProperty().asObject());

        configurarColumnaEditable(colParcial1, "Parcial 1");
        configurarColumnaEditable(colParcial2, "Parcial 2");
        configurarColumnaEditable(colParcial3, "Parcial 3");

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
            }

            // 🔹 Recalcular promedio inmediato
            cal.recalcularPromedio();
            tblCalificaciones.refresh();
            actualizarPromedioGeneral();
        });
    }


    // ============================================================
    // EVENTOS DE BOTONES
    // ============================================================
    @FXML
    private void onBuscar() {
        Catalogo grupo = cmbGrupo.getValue();
        Catalogo materia = cmbMateria.getValue();

        if (grupo == null || materia == null) {
            showError("Selecciona grupo y materia antes de buscar.");
            return;
        }

        try {
            // Recuperar la clave real usando el nombre mostrado
            String materiaClave = materiaClaveMap.get(materia.getNombre());

            cursoSeleccionado = cursoDAO.obtenerCursoPorGrupoMateria(
                    grupo.getId(),
                    materiaClave
            );


            if (cursoSeleccionado == -1) {
                showError("No se encontró un curso activo para esa combinación.");
                return;
            }

            List<Calificacion> lista = califBO.listarPorCurso(cursoSeleccionado);
            tblCalificaciones.setItems(FXCollections.observableArrayList(lista));
            actualizarPromedioGeneral();

        } catch (SQLException e) {
            showError("Error al cargar las calificaciones:\n" + e.getMessage());
        }
    }

    @FXML
    private TextField txtBuscarMatricula;
    @FXML
    private void onBuscarPorMatricula() {
        try {
            String matricula = txtBuscarMatricula.getText().trim();

            // 🔹 Si el campo está vacío, mostrar todo el curso
            if (matricula.isEmpty()) {
                if (cursoSeleccionado != -1) {
                    tblCalificaciones.setItems(FXCollections.observableArrayList(
                            califBO.listarPorCurso(cursoSeleccionado)
                    ));
                    showInfo("Se ha restaurado la lista completa de alumnos.");
                } else {
                    showError("Selecciona un curso antes de buscar.");
                }
                return;
            }

            // 🔹 Filtrar por matrícula
            List<Calificacion> listaFiltrada = tblCalificaciones.getItems().stream()
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
        cursoSeleccionado = -1;
    }

    @FXML
    private void onFinalizar() {
        showInfo("✔️ Calificaciones finalizadas correctamente.");
    }

    // ============================================================
    // 🔙 BOTÓN: VOLVER AL MENÚ DOCENTE
    // ============================================================
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

            // cerrar ventana actual
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
