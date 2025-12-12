package cbtis239.dao;

import cbtis239.model.AsistenciaHistorial;
import cbtis239.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaHistorialDAO {

    public List<AsistenciaHistorial> buscar(String matricula, String curp) throws SQLException {

        StringBuilder sb = new StringBuilder(
                "SELECT a.AsistenciaID, a.Alumno_Matricula, a.Fecha, " +
                        "       a.EstadoAsistencia, a.Observaciones " +
                        "FROM asistencia_diaria a " +
                        "JOIN alumno al ON al.Matricula = a.Alumno_Matricula "
        );

        List<Object> params = new ArrayList<>();
        boolean where = false;

        if (matricula != null && !matricula.isBlank()) {
            sb.append(where ? " AND " : " WHERE ").append("a.Alumno_Matricula = ?");
            params.add(matricula.trim());
            where = true;
        }

        if (curp != null && !curp.isBlank()) {
            sb.append(where ? " AND " : " WHERE ").append("al.CURP = ?");
            params.add(curp.trim());
            where = true;
        }

        sb.append(" ORDER BY a.Fecha");

        List<AsistenciaHistorial> lista = new ArrayList<>();

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    int id = rs.getInt("AsistenciaID");
                    String mat = rs.getString("Alumno_Matricula");
                    LocalDate fecha = rs.getDate("Fecha").toLocalDate();
                    String estado = rs.getString("EstadoAsistencia");
                    String obs = rs.getString("Observaciones");

                    AsistenciaHistorial ah =
                            new AsistenciaHistorial(id, mat, fecha, estado, obs);

                    lista.add(ah);
                }
            }
        }

        return lista;
    }

    public void guardarCambios(List<AsistenciaHistorial> lista) throws SQLException {

        String sql = "UPDATE asistencia_diaria " +
                "SET EstadoAsistencia = ?, Observaciones = ? " +
                "WHERE AsistenciaID = ?";

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (AsistenciaHistorial a : lista) {
                ps.setString(1, a.getEstado());
                ps.setString(2, a.getObservacion());
                ps.setInt(3, a.getId());
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }
}
