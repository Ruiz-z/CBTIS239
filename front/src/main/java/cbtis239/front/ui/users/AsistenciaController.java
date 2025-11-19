package cbtis239.front.ui.users;

import cbtis239.bo.AsistenciaBO;
import cbtis239.dao.AlumnoDAO;
import cbtis239.model.Alumno;
import cbtis239.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AsistenciaController {

    @FXML private TextField txtMatricula;
    @FXML private TextField txtNombre;
    @FXML private TextField txtFecha;
    @FXML private TextField txtHora;
    @FXML private Label lblMensaje;
    @FXML private ImageView imgAlumno;
    @FXML private Button btnRegistrar;
    @FXML private Button btnSalir;

    private final AsistenciaBO asistenciaBO = new AsistenciaBO();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    // AJUSTA ESTA RUTA A DONDE TENGAS LAS FOTOS
    // Ejemplo: C:/CBTIS239/fotos/22050727.jpg  →  en BD guardas "22050727.jpg"
    private static final String BASE_FOTOS = "file:/C:/CBTIS239/fotos/";

    private final DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private void initialize() {
        lblMensaje.setText("");
        txtNombre.setEditable(false);
        txtFecha.setEditable(false);
        txtHora.setEditable(false);

        // El lector de código de barras manda ENTER → procesar
        txtMatricula.setOnAction(e -> procesarEscaneo());

        txtMatricula.requestFocus();
    }

    @FXML
    private void onRegistrar() {
        procesarEscaneo();
    }

    @FXML
    private void onSalir(ActionEvent e) {
        // AJUSTA LA RUTA DEL LOGIN SI ES DISTINTA
        SceneNavigator.switchFromEvent(
                e,
                "/cbtis239/front/views/login.fxml",
                "Acceso al sistema"
        );
    }

    private void procesarEscaneo() {
        String matricula = txtMatricula.getText().trim();

        if (matricula.isEmpty()) {
            lblMensaje.setText("Capture o escanee una matrícula.");
            return;
        }

        try {
            // 1) Registrar entrada / salida
            String mensaje = asistenciaBO.registrarEscaneo(matricula);
            lblMensaje.setText(mensaje);

            // 2) Fecha y hora actuales
            LocalDate hoy  = LocalDate.now();
            LocalTime ahora = LocalTime.now();
            txtFecha.setText(fmtFecha.format(hoy));
            txtHora.setText(fmtHora.format(ahora));

            // 3) Buscar alumno en BD
            Alumno a = alumnoDAO.buscarPorMatricula(matricula);

            if (a != null) {
                String nombreCompleto = a.getNombreCompleto();
                if (nombreCompleto == null || nombreCompleto.isBlank()) {
                    nombreCompleto = "Sin nombre";
                }
                txtNombre.setText(nombreCompleto);

                // FOTO (String con nombre de archivo o ruta relativa)
                String archivoFoto = a.getFoto();  // ej. "22050727.jpg"

                if (archivoFoto != null && !archivoFoto.isBlank()) {
                    String url = BASE_FOTOS + archivoFoto;
                    System.out.println("Cargando foto desde: " + url);

                    try {
                        imgAlumno.setImage(new Image(url, true));
                    } catch (Exception exImg) {
                        exImg.printStackTrace();
                        imgAlumno.setImage(null);
                    }
                } else {
                    imgAlumno.setImage(null);
                }

            } else {
                txtNombre.setText("Alumno no encontrado");
                imgAlumno.setImage(null);
            }

            // Preparar para el siguiente escaneo
            txtMatricula.requestFocus();
            txtMatricula.selectAll();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMensaje.setText("Error al registrar: " + ex.getMessage());
        }
    }
}

