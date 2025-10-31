package cbtis239.dao;

import cbtis239.model.Pago;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {


    public Integer getPeriodoActualId() throws SQLException {
        String sql = "SELECT idPeriodo FROM sistemaescolar.periodo WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql); var rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }


    public String getPeriodoActualNombre() throws SQLException {
        String sql = "SELECT Nombre FROM sistemaescolar.periodo WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql); var rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
        return null;
    }


    public boolean existsAspiranteFolio(int folio) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.aspirante WHERE Folio = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (var rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existsAlumnoMatricula(String mat) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.alumno WHERE Matricula = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, mat);
            try (var rs = ps.executeQuery()) { return rs.next(); }
        }
    }


    public String nombreCompletoAlumno(String matricula) {
        String sql =
                "SELECT COALESCE(CONCAT_WS(' ', NULLIF(Nombre,''), NULLIF(Paterno,''), NULLIF(Materno,'')), " +
                        "               CONCAT('Alumno ', Matricula)) AS nom " +
                        "FROM sistemaescolar.alumno WHERE Matricula = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException ignore) {}
        return "Alumno " + matricula;
    }


    public String nombreCompletoAspirante(int folio) {
        String sql =
                "SELECT COALESCE(CONCAT_WS(' ', NULLIF(Nombre,''), NULLIF(Paterno,''), NULLIF(Materno,'')), " +
                        "               CONCAT('Aspirante ', Folio)) AS nom " +
                        "FROM sistemaescolar.aspirante WHERE Folio = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (var rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException ignore) {}
        return "Aspirante " + folio;
    }



    public List<Pago> listarTodosConEstado(int periodoId, double montoFijo) throws SQLException {
        String sql =
                // ALUMNOS
                "SELECT " +
                        "  COALESCE(CONCAT_WS(' ', a.Nombre, a.Paterno, a.Materno), CONCAT('Alumno ', a.Matricula)) AS NombreCompleto, " +
                        "  COALESCE(p.Monto, 0)   AS Monto, " +
                        "  COALESCE(p.Estatus, 0) AS Estatus, " +
                        "  p.idPago               AS idPago, " +
                        "  a.Matricula            AS Matricula, " +
                        "  NULL                   AS Folio " +
                        "FROM sistemaescolar.alumno a " +
                        "LEFT JOIN sistemaescolar.pago p " +
                        "  ON p.Alumno_Matricula = a.Matricula AND p.Periodo_idPeriodo = ? " +
                        "UNION ALL " +
                        // ASPIRANTES
                        "SELECT " +
                        "  COALESCE(CONCAT_WS(' ', s.Nombre, s.Paterno, s.Materno), CONCAT('Aspirante ', s.Folio)) AS NombreCompleto, " +
                        "  COALESCE(p.Monto, 0)   AS Monto, " +
                        "  COALESCE(p.Estatus, 0) AS Estatus, " +
                        "  p.idPago               AS idPago, " +
                        "  NULL                   AS Matricula, " +
                        "  s.Folio                AS Folio " +
                        "FROM sistemaescolar.aspirante s " +
                        "LEFT JOIN sistemaescolar.pago p " +
                        "  ON p.Aspirante_Folio = s.Folio AND p.Periodo_idPeriodo = ? " +
                        "ORDER BY 1";

        List<Pago> lista = new ArrayList<>();
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setInt(1, periodoId);
            ps.setInt(2, periodoId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("NombreCompleto");
                    double monto  = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                    int est       = rs.getInt("Estatus");
                    int idPago    = rs.getInt("idPago");
                    String mat    = rs.getString("Matricula");
                    int folio     = rs.getInt("Folio");
                    boolean folioNull = rs.wasNull();
                    lista.add(new Pago(idPago, est, monto, mat, folioNull ? null : folio, periodoId, nombre));
                }
            }
        }
        return lista;
    }





    public List<Pago> buscarTodosConEstado(String entrada, int periodoId, double montoFijo) throws SQLException {
        boolean esNumero = entrada != null && entrada.matches("\\d+");

        String sqlAlumnos =
                "SELECT COALESCE(CONCAT_WS(' ', a.Nombre, a.Paterno, a.Materno), CONCAT('Alumno ', a.Matricula)) AS NombreCompleto, " +
                        "       COALESCE(p.Monto, 0)   AS Monto, " +
                        "       COALESCE(p.Estatus, 0) AS Estatus, " +
                        "       p.idPago               AS idPago, " +
                        "       a.Matricula            AS Matricula, " +
                        "       NULL                   AS Folio " +
                        "FROM sistemaescolar.alumno a " +
                        "LEFT JOIN sistemaescolar.pago p " +
                        "  ON p.Alumno_Matricula = a.Matricula AND p.Periodo_idPeriodo = ? " +
                        "WHERE a.Matricula = ?";

        String sqlAspirantes =
                "SELECT COALESCE(CONCAT_WS(' ', s.Nombre, s.Paterno, s.Materno), CONCAT('Aspirante ', s.Folio)) AS NombreCompleto, " +
                        "       COALESCE(p.Monto, 0)   AS Monto, " +
                        "       COALESCE(p.Estatus, 0) AS Estatus, " +
                        "       p.idPago               AS idPago, " +
                        "       NULL                   AS Matricula, " +
                        "       s.Folio                AS Folio " +
                        "FROM sistemaescolar.aspirante s " +
                        "LEFT JOIN sistemaescolar.pago p " +
                        "  ON p.Aspirante_Folio = s.Folio AND p.Periodo_idPeriodo = ? " +
                        "WHERE s.Folio = ?";

        List<Pago> lista = new ArrayList<>();
        try (var cn = DB.get()) {
            if (!esNumero) {
                try (var ps = cn.prepareStatement(sqlAlumnos)) {
                    ps.setInt(1, periodoId);
                    ps.setString(2, entrada);
                    try (var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto  = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            int est       = rs.getInt("Estatus");
                            int idPago    = rs.getInt("idPago");
                            String mat    = rs.getString("Matricula");
                            lista.add(new Pago(idPago, est, monto, mat, null, periodoId, nombre));
                        }
                    }
                }
            } else {
                try (var ps = cn.prepareStatement(sqlAspirantes)) {
                    ps.setInt(1, periodoId);
                    ps.setInt(2, Integer.parseInt(entrada));
                    try (var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto  = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            int est       = rs.getInt("Estatus");
                            int idPago    = rs.getInt("idPago");
                            int folio     = rs.getInt("Folio");
                            lista.add(new Pago(idPago, est, monto, null, folio, periodoId, nombre));
                        }
                    }
                }
                // matricula numérica
                try (var ps = cn.prepareStatement(sqlAlumnos)) {
                    ps.setInt(1, periodoId);
                    ps.setString(2, entrada);
                    try (var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto  = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            int est       = rs.getInt("Estatus");
                            int idPago    = rs.getInt("idPago");
                            String mat    = rs.getString("Matricula");
                            lista.add(new Pago(idPago, est, monto, mat, null, periodoId, nombre));
                        }
                    }
                }
            }
        }
        return lista;
    }

    public int insertPagoAlumnoTx(Connection cn, String matricula, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, ?, NULL, ?)";
        try (var ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, matricula);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            // Proviene del UNIQUE uq_pago_alumno_periodo
            throw new SQLException("DUPLICATE_PAGO_ALUMNO");
        }
    }

    public int insertPagoAspiranteTx(Connection cn, int folio, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, NULL, ?, ?)";
        try (var ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setInt(2, folio);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            // Proviene del UNIQUE uq_pago_aspirante_periodo
            throw new SQLException("DUPLICATE_PAGO_ASPIRANTE");
        }
    }



    public int insertPagoAlumno(String matricula, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, ?, NULL, ?)";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, matricula);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        }
        return 0;
    }

    public int insertPagoAspirante(int folio, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, NULL, ?, ?)";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setInt(2, folio);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        }
        return 0;
    }

    public boolean setAspiranteEstatusPagado(int folio) throws SQLException {
        String sql = "UPDATE sistemaescolar.aspirante SET EstatusPago = 'Pagado' WHERE Folio = ?";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean setAlumnoEstadoActivo(String matricula) throws SQLException {
        String sql = "UPDATE sistemaescolar.alumno SET EstadoInscripcion = 'Activo' WHERE Matricula = ?";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate() == 1;
        }
    }
    public boolean existsPagoAlumnoEnPeriodo(String matricula, int periodoId) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.pago WHERE Alumno_Matricula = ? AND Periodo_idPeriodo = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setInt(2, periodoId);
            try (var rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existsPagoAspiranteEnPeriodo(int folio, int periodoId) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.pago WHERE Aspirante_Folio = ? AND Periodo_idPeriodo = ? LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            ps.setInt(2, periodoId);
            try (var rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    /** Elimina todos los pagos ligados al aspirante (para liberar la FK). */
    public int deleteByAspiranteFolio(Connection cn, int folio) throws SQLException {
        String sql = "DELETE FROM sistemaescolar.pago WHERE Aspirante_Folio = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            return ps.executeUpdate(); // número de pagos borrados
        }
    }

    /** Versión cómoda sin transacción externa. */
    public int deleteByAspiranteFolio(int folio) throws SQLException {
        try (Connection cn = DB.get()) {
            return deleteByAspiranteFolio(cn, folio);
        }
    }
    public List<cbtis239.model.PagoHist> buscarHistorialTodosPeriodos(String entrada) throws SQLException {
        boolean esNumero = entrada != null && entrada.matches("\\d+");

        String sqlAlumno =
                "SELECT " +
                        "  COALESCE(CONCAT_WS(' ', a.Nombre, a.Paterno, a.Materno), CONCAT('Alumno ', a.Matricula)) AS NombreCompleto, " +
                        "  p.Monto, " +
                        "  per.Nombre AS Periodo, " +
                        "  p.idPago AS idPago, " +
                        "  a.Matricula AS Matricula, " +
                        "  NULL AS Folio " +
                        "FROM sistemaescolar.pago p " +
                        "JOIN sistemaescolar.alumno a   ON a.Matricula = p.Alumno_Matricula " +
                        "JOIN sistemaescolar.periodo per ON per.idPeriodo = p.Periodo_idPeriodo " +
                        "WHERE a.Matricula = ? " +
                        "ORDER BY per.Inicio DESC, p.idPago DESC";

        String sqlAspirante =
                "SELECT " +
                        "  COALESCE(CONCAT_WS(' ', s.Nombre, s.Paterno, s.Materno), CONCAT('Aspirante ', s.Folio)) AS NombreCompleto, " +
                        "  p.Monto, " +
                        "  per.Nombre AS Periodo, " +
                        "  p.idPago AS idPago, " +
                        "  NULL AS Matricula, " +
                        "  s.Folio AS Folio " +
                        "FROM sistemaescolar.pago p " +
                        "JOIN sistemaescolar.aspirante s ON s.Folio = p.Aspirante_Folio " +
                        "JOIN sistemaescolar.periodo per ON per.idPeriodo = p.Periodo_idPeriodo " +
                        "WHERE s.Folio = ? " +
                        "ORDER BY per.Inicio DESC, p.idPago DESC";

        List<cbtis239.model.PagoHist> lista = new ArrayList<>();
        try (Connection cn = DB.get()) {

            // Si NO es número: es matrícula segura
            if (!esNumero) {
                try (PreparedStatement ps = cn.prepareStatement(sqlAlumno)) {
                    ps.setString(1, entrada);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            String periodo = rs.getString("Periodo");
                            int idPago = rs.getInt("idPago");
                            String mat = rs.getString("Matricula");
                            lista.add(new cbtis239.model.PagoHist(nombre, monto, periodo, idPago, mat, null));
                        }
                    }
                }
                return lista;
            }

            // Es número: intenta por folio (aspirante)...
            try (PreparedStatement ps = cn.prepareStatement(sqlAspirante)) {
                ps.setInt(1, Integer.parseInt(entrada));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        String periodo = rs.getString("Periodo");
                        int idPago = rs.getInt("idPago");
                        Integer folio = rs.getInt("Folio");
                        lista.add(new cbtis239.model.PagoHist(nombre, monto, periodo, idPago, null, folio));
                    }
                }
            }

            // ...y también por matrícula numérica.
            try (PreparedStatement ps = cn.prepareStatement(sqlAlumno)) {
                ps.setString(1, entrada);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto = rs.getBigDecimal("Monto") != null ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        String periodo = rs.getString("Periodo");
                        int idPago = rs.getInt("idPago");
                        String mat = rs.getString("Matricula");
                        lista.add(new cbtis239.model.PagoHist(nombre, monto, periodo, idPago, mat, null));
                    }
                }
            }
        }
        return lista;
    }

}