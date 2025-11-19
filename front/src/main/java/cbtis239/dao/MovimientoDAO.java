package cbtis239.dao;

import cbtis239.model.Movimiento;
import cbtis239.util.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    // Registrar un movimiento (entrada o salida)
    public void registrarMovimiento(String matricula, String tipo) throws SQLException {
        String sql = "INSERT INTO movimientos (Alumno_Matricula, Fecha, Hora, TipoMovimiento) " +
                "VALUES (?, CURDATE(), CURTIME(), ?)";

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setString(2, tipo);
            ps.executeUpdate();
        }
    }

    // Verificar si ya hubo una entrada hoy
    public boolean tieneEntradaHoy(String matricula) throws SQLException {
        String sql = "SELECT COUNT(*) FROM movimientos " +
                "WHERE Alumno_Matricula = ? AND Fecha = CURDATE() AND TipoMovimiento = 'Entrada'";

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // Verificar si el último movimiento fue salida
    public boolean ultimaEsSalida(String matricula) throws SQLException {
        String sql = "SELECT TipoMovimiento FROM movimientos " +
                "WHERE Alumno_Matricula = ? AND Fecha = CURDATE() ORDER BY Hora DESC LIMIT 1";

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1).equals("Salida");
            }
        }
        return false; // si no hay registros hoy, no fue salida
    }
}
