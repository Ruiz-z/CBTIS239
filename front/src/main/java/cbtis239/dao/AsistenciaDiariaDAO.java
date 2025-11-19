package cbtis239.dao;

import cbtis239.util.DB;
import java.sql.*;

public class AsistenciaDiariaDAO {

    // Crear o actualizar asistencia como presente
    public void marcarPresente(String matricula) throws SQLException {
        String sql = "INSERT INTO asistencia_diaria (Alumno_Matricula, Fecha, EstadoAsistencia) " +
                "VALUES (?, CURDATE(), 'Presente') " +
                "ON DUPLICATE KEY UPDATE EstadoAsistencia = 'Presente'";

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.executeUpdate();
        }
    }

    // Registrar falta automática (si no entró)
    public void marcarFaltaSiNoEntro() throws SQLException {
        String sql = """
            INSERT INTO asistencia_diaria (Alumno_Matricula, Fecha, EstadoAsistencia)
            SELECT a.Matricula, CURDATE(), 'Falta'
            FROM alumno a
            WHERE a.Matricula NOT IN (
                SELECT Alumno_Matricula FROM movimientos
                WHERE Fecha = CURDATE() AND TipoMovimiento = 'Entrada'
            );
        """;

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
