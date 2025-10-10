package cbtis239.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Carga configuración desde resources (db.properties) y entrega conexiones JDBC.
 * Acepta dos ubicaciones:
 *  - src/main/resources/db.properties
 *  - src/main/resources/cbtis239/front/db.properties
 */
public final class DB {
    private static final Properties PROPS = new Properties();

    static {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("db.properties");

            if (in == null) {
                in = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("cbtis239/front/db.properties");
            }

            if (in == null) {
                throw new IllegalStateException("No se encontró db.properties en el classpath");
            }

            PROPS.load(in);

            // Opcional: registrar driver si tu JDK lo requiere
            // Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // catch (ClassNotFoundException e) {
        //     throw new RuntimeException("No se encontró el driver de MySQL", e);
        // }
    }

    private DB() {}

    public static Connection get() throws SQLException {
        String url  = "jdbc:mysql://127.0.0.1:3306/SistemaEscolar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String pass = "burrospardos1!";
        return DriverManager.getConnection(url, user, pass);
    }

}
