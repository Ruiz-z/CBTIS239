package cbtis239.dao;

import cbtis239.model.AsistenciaBoletaResumen;
import cbtis239.util.DB;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class AsistenciaBoletaDAO {

    private Connection getConnection() throws SQLException {
        return DB.get();
    }

    public AsistenciaBoletaResumen resumenPeriodoActual(String matricula) throws SQLException {

        LocalDate inicio;
        LocalDate fin;

        // 1) Rango del periodo actual
        String sqlPeriodo = "SELECT Inicio, Fin FROM v_periodo_actual";

        try (Connection cn = getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sqlPeriodo)) {

            if (!rs.next()) {
                return new AsistenciaBoletaResumen(0, 0, 0.0);
            }

            inicio = rs.getDate("Inicio").toLocalDate();
            fin = rs.getDate("Fin").toLocalDate();
        }

        // 2) Días inhábiles dentro del rango
        Set<LocalDate> diasInhabiles = new HashSet<>();

        String sqlInhabiles = """
            SELECT DiaInhabil 
            FROM diasinhabiles 
            WHERE DiaInhabil BETWEEN ? AND ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sqlInhabiles)) {

            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    diasInhabiles.add(rs.getDate("DiaInhabil").toLocalDate());
                }
            }
        }

        // 3) Días escolares (L-V sin inhábiles)
        int diasEscolares = 0;
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) continue;
            if (diasInhabiles.contains(d)) continue;
            diasEscolares++;
        }

        // 4) Días asistidos (asistencia_diaria)
        int diasAsistidos = 0;

        String sqlAsistencia = """
            SELECT COUNT(*) AS Total
            FROM asistencia_diaria
            WHERE Alumno_Matricula = ?
              AND Fecha BETWEEN ? AND ?
              AND EstadoAsistencia IN ('Presente', 'Justificada')
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sqlAsistencia)) {

            ps.setString(1, matricula);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    diasAsistidos = rs.getInt("Total");
                }
            }
        }

        double porcentaje = (diasEscolares > 0)
                ? diasAsistidos * 100.0 / diasEscolares
                : 0.0;

        return new AsistenciaBoletaResumen(diasEscolares, diasAsistidos, porcentaje);
    }
}
