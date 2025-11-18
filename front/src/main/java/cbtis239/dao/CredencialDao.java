package cbtis239.dao;

import cbtis239.model.Alumno;
import cbtis239.util.DB;

import java.sql.*;

public class CredencialDao {

    public Alumno obtenerAlumnoPorMatricula(String matricula) throws SQLException {
        String sql = """
            SELECT Matricula,
                   CURP,
                   Nombre,
                   Paterno,
                   Materno,
                   NSS,
                   Foto,
                   Firma
            FROM alumno
            WHERE Matricula = ?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Alumno a = new Alumno();
                a.setMatricula(rs.getString("Matricula"));
                a.setCurp(rs.getString("CURP"));
                a.setNombre(rs.getString("Nombre"));
                a.setPaterno(rs.getString("Paterno"));
                a.setMaterno(rs.getString("Materno"));
                a.setNss(rs.getString("NSS"));
                a.setFoto(rs.getString("Foto"));   // ruta / URL / Base64
                a.setFirma(rs.getString("Firma")); // <-- AQUÍ SE CARGA LA FIRMA

                return a;
            }
        }
    }

    /** Vigencia simple: nombre del periodo actual. */
    public String vigenciaActual() throws SQLException {
        String sql = """
            SELECT Nombre
            FROM sistemaescolar.periodo
            WHERE CURDATE() BETWEEN Inicio AND Fin
            LIMIT 1
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("Nombre");
        }
        return "Periodo actual no definido";
    }

    public String vigenciaHistorial(String matricula) throws SQLException {
        String sql = """
        SELECT p.Nombre
        FROM sistemaescolar.alumno_periodo ap
        JOIN sistemaescolar.periodo p ON ap.idPeriodo = p.idPeriodo
        WHERE ap.Matricula = ?
        ORDER BY p.Inicio
    """;

        StringBuilder sb = new StringBuilder();

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) {
                        sb.append(" | "); // separador entre periodos
                    }
                    sb.append(rs.getString("Nombre"));
                }
            }
        }

        if (sb.length() == 0) {
            return "Sin periodos registrados";
        }
        return sb.toString();
    }

}
