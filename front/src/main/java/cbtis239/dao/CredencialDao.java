package cbtis239.dao;

import cbtis239.model.Alumno;
import cbtis239.util.DB;

import java.sql.*;

public class CredencialDao {

    public Alumno obtenerAlumnoPorMatricula(String matricula) throws SQLException {
        String sql = """
            SELECT Matricula, CURP, Nombre, Paterno, Materno, NSS, Foto
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
                a.setFoto(rs.getString("Foto")); // String: ruta/URL/Base64
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
}
