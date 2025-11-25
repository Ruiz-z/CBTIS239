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
        SceneNavigator.switchFromEvent(
                e,
                "/cbtis239/front/views/login.fxml",   // ajusta si tu login tiene otro nombre/ruta
                "Inicio de sesión"
        );
    }

    private void procesarEscaneo() {
        String matricula = txtMatricula.getText().trim();

        if (matricula.isEmpty()) {
            lblMensaje.setText("Capture o escanee una matrícula.");
            volverAFocoMatricula();
            return;
        }

        try {
            // 1) Buscar primero al alumno
            Alumno a = alumnoDAO.buscarPorMatricula(matricula);

            if (a == null) {
                // 👉 Credencial no corresponde a ningún alumno
                lblMensaje.setText("Credencial no válida. Verifique la credencial del alumno.");
                // No movemos fecha/hora, no cambiamos escena
                txtNombre.clear();
                imgAlumno.setImage(null);
                volverAFocoMatricula();
                return;
            }

            // 2) Si el alumno existe, registrar entrada / salida
            String mensaje = asistenciaBO.registrarEscaneo(matricula);
            lblMensaje.setText(mensaje);

            // 3) Actualizar fecha y hora actuales
            LocalDate hoy  = LocalDate.now();
            LocalTime ahora = LocalTime.now();
            txtFecha.setText(fmtFecha.format(hoy));
            txtHora.setText(fmtHora.format(ahora));

            // 4) Mostrar nombre completo
            String nombreCompleto = a.getNombreCompleto();
            if (nombreCompleto == null || nombreCompleto.isBlank()) {
                nombreCompleto = "Sin nombre";
            }
            txtNombre.setText(nombreCompleto);

            // 5) Mostrar foto si existe
            String archivoFoto = a.getFoto();  // ej. "22050727.jpg"
            if (archivoFoto != null && !archivoFoto.isBlank()) {
                String url = BASE_FOTOS + archivoFoto;
                try {
                    imgAlumno.setImage(new Image(url, true));
                } catch (Exception exImg) {
                    exImg.printStackTrace();
                    imgAlumno.setImage(null);
                }
            } else {
                imgAlumno.setImage(null);
            }

            // 6) Preparar para el siguiente escaneo
            volverAFocoMatricula();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMensaje.setText("Error al registrar: " + ex.getMessage());
            volverAFocoMatricula();
        }
    }

    private void volverAFocoMatricula() {
        txtMatricula.requestFocus();
        txtMatricula.selectAll();
    }
}
