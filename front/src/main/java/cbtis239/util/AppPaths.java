package cbtis239.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class AppPaths {

    private static final String CREDENCIALES_DIR = "credenciales";

    private AppPaths() {
    }


    public static Path getAppBaseDir() {
        try {
            URL location = AppPaths.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            Path path = Paths.get(location.toURI());

            // Si es un jar, usamos la carpeta del jar
            if (path.toString().endsWith(".jar")) {
                return path.getParent();
            } else {
                // En desarrollo suele ser .../build/classes/java/main o similar -> subimos 2 niveles
                Path parent = path.getParent();
                if (parent != null && parent.getParent() != null) {
                    return parent.getParent();
                }
                // Fallback
                return Paths.get("").toAbsolutePath();
            }
        } catch (URISyntaxException e) {
            // Fallback: carpeta de trabajo actual
            return Paths.get("").toAbsolutePath();
        }
    }

    
    public static Path getCredencialesDir() throws IOException {
        Path base = getAppBaseDir();
        Path dir = base.resolve(CREDENCIALES_DIR);
        Files.createDirectories(dir);  // crea la carpeta si no existe
        return dir;
    }
}
