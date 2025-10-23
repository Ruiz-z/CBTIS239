package cbtis239.dao;

import cbtis239.model.Pago;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {


    // Busca pagos por matrícula (texto) o folio (número). Si el parámetro es numérico, intenta por folio.
    public List<Pago> findByMatriculaOrFolio(String matriculaOrFolio) throws SQLException {
        boolean esNumero = matriculaOrFolio != null && matriculaOrFolio.matches("\\d+");

        String sql = esNumero
                ? "SELECT idPago, Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo " +
                "FROM pago WHERE Aspirante_Folio = ? ORDER BY idPago DESC"
                : "SELECT idPago, Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo " +
                "FROM pago WHERE Alumno_Matricula = ? ORDER BY idPago DESC";

        List<Pago> lista = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            if (esNumero) {
                ps.setInt(1, Integer.parseInt(matriculaOrFolio));
            } else {
                ps.setString(1, matriculaOrFolio);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPago = rs.getInt("idPago");
                    int estatus = rs.getInt("Estatus");
                    double monto = rs.getBigDecimal("Monto").doubleValue();
                    String matricula = rs.getString("Alumno_Matricula");
                    int folio = rs.getInt("Aspirante_Folio");
                    int periodo = rs.getInt("Periodo_idPeriodo");

                    Pago p = new Pago(idPago, estatus, monto, matricula, folio, periodo);
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // Inserta un pago. Si 'matriculaOrFolio' es numérico lo guarda como folio, si no, como matrícula.
    // Devuelve el id generado.
    public int insertPago(String matriculaOrFolio, double monto, int periodoId, int estatus) throws SQLException {
        boolean esNumero = matriculaOrFolio != null && matriculaOrFolio.matches("\\d+");

        String sql = esNumero
                ? "INSERT INTO pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (?, ?, NULL, ?, ?)"
                : "INSERT INTO pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (?, ?, ?, NULL, ?)";

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, estatus);
            ps.setBigDecimal(2, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            if (esNumero) {
                ps.setInt(3, Integer.parseInt(matriculaOrFolio));
                ps.setInt(4, periodoId);
            } else {
                ps.setString(3, matriculaOrFolio);
                ps.setInt(4, periodoId);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    // Cambia estatus (1 pagado / 0 pendiente)
    public boolean updateEstatus(int idPago, int estatus) throws SQLException {
        String sql = "UPDATE pago SET Estatus = ? WHERE idPago = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, estatus);
            ps.setInt(2, idPago);
            return ps.executeUpdate() == 1;
        }
    }

    // Borra un pago (opcional)
    public boolean deleteById(int idPago) throws SQLException {
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM pago WHERE idPago = ?")) {
            ps.setInt(1, idPago);
            return ps.executeUpdate() == 1;
        }
    }
}
