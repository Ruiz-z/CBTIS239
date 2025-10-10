package cbtis239.front;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    // AJUSTA esta ruta si tu Login.fxml está en otro lugar.
    private static final String LOGIN_FXML = "/cbtis239/front/views/Login.fxml";

    @Override
    public void start(Stage stage) {
        try {
            // Para capturar cualquier excepción no manejada en el hilo FX
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> showCrash(e));

            final URL url = Objects.requireNonNull(
                    MainApp.class.getResource(LOGIN_FXML),
                    "No se encontró el FXML: " + LOGIN_FXML + " (revisa ruta en resources)"
            );

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load(); // <- si truena aquí, casi siempre es: fx:controller mal, falta 'opens' del paquete, o error en <fx:.../>

            Scene scene = new Scene(root);
            stage.setTitle("Inicio de sesión");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();

        } catch (Throwable ex) {
            // Cae aquí lo que provoca “Exception in Application start method”
            showCrash(ex);
            // re-lanzamos para que termine la JVM si quieres
            throw new RuntimeException(ex);
        }
    }

    private void showCrash(Throwable ex) {
        ex.printStackTrace(); // para consola
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error al iniciar");
        a.setHeaderText(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()));
        a.setContentText(sw.toString().substring(0, Math.min(2000, sw.toString().length()))); // evita alert infinito
        a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
