package cbtis239.front.ui.users;

import cbtis239.bo.AsistenciaBO;
import cbtis239.dao.AlumnoDAO;
import cbtis239.model.Alumno;
import cbtis239.util.SceneNavigator;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
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

    private final DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Temporizador para limpiar la pantalla si no hay más escaneos
    private PauseTransition autoClearTimer;

    @FXML
    private void initialize() {
        lblMensaje.setText("");
        txtNombre.setEditable(false);
        txtFecha.setEditable(false);
        txtHora.setEditable(false);

        // El lector manda ENTER → procesar escaneo
        txtMatricula.setOnAction(e -> procesarEscaneo());

        // Timer de 10 segundos para limpiar pantalla
        autoClearTimer = new PauseTransition(Duration.seconds(10));
        autoClearTimer.setOnFinished(e -> limpiarPantalla());

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
                "/cbtis239/front/views/login.fxml",
                "Inicio de sesión"
        );
    }

    private void procesarEscaneo() {
        String matricula = txtMatricula.getText().trim();

        if (matricula.isEmpty()) {
            lblMensaje.setText("Capture o escanee una matrícula.");
            volverAFoco();
            return;
        }

        try {
            Alumno a = alumnoDAO.buscarPorMatricula(matricula);

            if (a == null) {
                lblMensaje.setText("Credencial no válida.");
                txtNombre.clear();
                // dejamos fecha/hora si quieres ver el momento del intento
                imgAlumno.setImage(null);
                reiniciarTimerAutoClear();
                volverAFoco();
                return;
            }

            // Registrar entrada / salida
            String mensaje = asistenciaBO.registrarEscaneo(matricula);
            lblMensaje.setText(mensaje);

            // Fecha y hora actuales
            LocalDate hoy  = LocalDate.now();
            LocalTime ahora = LocalTime.now();
            txtFecha.setText(fmtFecha.format(hoy));
            txtHora.setText(fmtHora.format(ahora));

            // Nombre completo
            String nombreCompleto = a.getNombreCompleto();
            if (nombreCompleto == null || nombreCompleto.isBlank()) {
                nombreCompleto = "Sin nombre";
            }
            txtNombre.setText(nombreCompleto);

            // Foto
            cargarFoto(a.getFoto());

            // Preparar siguiente escaneo
            reiniciarTimerAutoClear();
            volverAFoco();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMensaje.setText("Error al registrar: " + ex.getMessage());
            reiniciarTimerAutoClear();
            volverAFoco();
        }
    }

    private void cargarFoto(String fotoField) {
        if (fotoField == null || fotoField.isBlank()) {
            System.out.println("Alumno sin foto en BD.");
            imgAlumno.setImage(null);
            return;
        }

        System.out.println("FOTO EN BD = " + fotoField);

        try {
            String ruta;

            if (fotoField.startsWith("[") && fotoField.endsWith("]")) {
                // Lista de códigos: [67, 58, 92, ...]
                String inner = fotoField.substring(1, fotoField.length() - 1);
                String[] partes = inner.split(",\\s*");

                byte[] bytes = new byte[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    int val = Integer.parseInt(partes[i].trim());
                    bytes[i] = (byte) val; // valores -128..127 → byte real
                }

                // Decodificamos como UTF-8 → soporta ñ y acentos
                ruta = new String(bytes, StandardCharsets.UTF_8);
            } else {
                // Por si ya viniera como ruta normal
                ruta = fotoField;
            }

            System.out.println("Ruta reconstruida = " + ruta);

            // Normalizar \ → / y formar URL
            String normalizada = ruta.replace("\\", "/");
            String url = "file:/" + normalizada;

            System.out.println("URL generada = " + url);

            Image imagen = new Image(url);
            if (!imagen.isError()) {
                imgAlumno.setImage(imagen);
                System.out.println("✔ Foto cargada correctamente.");
            } else {
                System.out.println("✖ Error al cargar imagen (Image.isError = true).");
                if (imagen.getException() != null) {
                    imagen.getException().printStackTrace();
                }
                imgAlumno.setImage(null);
            }

        } catch (Exception ex) {
            System.out.println("✖ Excepción al reconstruir/cargar la foto:");
            ex.printStackTrace();
            imgAlumno.setImage(null);
        }
    }

    private void reiniciarTimerAutoClear() {
        autoClearTimer.stop();
        autoClearTimer.playFromStart();
    }

    private void limpiarPantalla() {
        txtMatricula.clear();
        txtNombre.clear();
        txtFecha.clear();
        txtHora.clear();
        lblMensaje.setText("");
        imgAlumno.setImage(null);
        txtMatricula.requestFocus();
    }

    private void volverAFoco() {
        txtMatricula.requestFocus();
        txtMatricula.selectAll();
    }
}
