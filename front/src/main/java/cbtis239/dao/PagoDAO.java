package cbtis239.dao;

import cbtis239.model.Pago;
import cbtis239.model.PagoHist;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    // ================== PERIODO ACTUAL ==================

    public Integer getPeriodoActualId() throws SQLException {
        String sql = "SELECT idPeriodo FROM sistemaescolar.periodo " +
                "WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }

    public String getPeriodoActualNombre() throws SQLException {
        String sql = "SELECT Nombre FROM sistemaescolar.periodo " +
                "WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
        return null;
    }

    // ================== EXISTENCIA BÁSICA ==================

    public boolean existsAspiranteFolio(int folio) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.aspirante WHERE Folio = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsAlumnoMatricula(String mat) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.alumno WHERE Matricula = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, mat);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================== NOMBRES COMPLETOS ==================

    public String nombreCompletoAlumno(String matricula) {
        String sql =
                "SELECT COALESCE(CONCAT_WS(' ', NULLIF(Nombre,''), NULLIF(Paterno,''), NULLIF(Materno,'')), " +
                        "               CONCAT('Alumno ', Matricula)) AS nom " +
                        "FROM sistemaescolar.alumno WHERE Matricula = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ignore) {}
        return "Alumno " + matricula;
    }

    public String nombreCompletoAspirante(int folio) {
        String sql =
                "SELECT COALESCE(CONCAT_WS(' ', NULLIF(Nombre,''), NULLIF(Paterno,''), NULLIF(Materno,'')), " +
                        "               CONCAT('Aspirante ', Folio)) AS nom " +
                        "FROM sistemaescolar.aspirante WHERE Folio = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ignore) {}
        return "Aspirante " + folio;
    }

    // ================== LISTAS ==================

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
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, periodoId);
            ps.setInt(2, periodoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("NombreCompleto");
                    double monto  = rs.getBigDecimal("Monto") != null
                            ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                    int est       = rs.getInt("Estatus");
                    int idPago    = rs.getInt("idPago");
                    String mat    = rs.getString("Matricula");
                    int folio     = rs.getInt("Folio");
                    boolean folioNull = rs.wasNull();
                    lista.add(new Pago(idPago, est, monto, mat,
                            folioNull ? null : folio, periodoId, nombre));
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
        try (Connection cn = DB.get()) {

            if (!esNumero) {
                // matrícula alfanumérica
                try (PreparedStatement ps = cn.prepareStatement(sqlAlumnos)) {
                    ps.setInt(1, periodoId);
                    ps.setString(2, entrada);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto  = rs.getBigDecimal("Monto") != null
                                    ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            int est       = rs.getInt("Estatus");
                            int idPago    = rs.getInt("idPago");
                            String mat    = rs.getString("Matricula");
                            lista.add(new Pago(idPago, est, monto, mat, null, periodoId, nombre));
                        }
                    }
                }
                return lista;
            }

            // es número -> primero folio aspirante
            try (PreparedStatement ps = cn.prepareStatement(sqlAspirantes)) {
                ps.setInt(1, periodoId);
                ps.setInt(2, Integer.parseInt(entrada));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto  = rs.getBigDecimal("Monto") != null
                                ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        int est       = rs.getInt("Estatus");
                        int idPago    = rs.getInt("idPago");
                        int folio     = rs.getInt("Folio");
                        lista.add(new Pago(idPago, est, monto, null, folio, periodoId, nombre));
                    }
                }
            }

            // ...y también matrícula numérica
            try (PreparedStatement ps = cn.prepareStatement(sqlAlumnos)) {
                ps.setInt(1, periodoId);
                ps.setString(2, entrada);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto  = rs.getBigDecimal("Monto") != null
                                ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        int est       = rs.getInt("Estatus");
                        int idPago    = rs.getInt("idPago");
                        String mat    = rs.getString("Matricula");
                        lista.add(new Pago(idPago, est, monto, mat, null, periodoId, nombre));
                    }
                }
            }
        }
        return lista;
    }

    // ================== INSERTS EN TRANSACCIÓN ==================

    public int insertPagoAlumnoTx(Connection cn, String matricula, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago " +
                "(Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, ?, NULL, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, matricula);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            // UNIQUE uq_pago_alumno_periodo
            throw new SQLException("DUPLICATE_PAGO_ALUMNO");
        }
    }

    public int insertPagoAspiranteTx(Connection cn, int folio, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago " +
                "(Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, NULL, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setInt(2, folio);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            // UNIQUE uq_pago_aspirante_periodo
            throw new SQLException("DUPLICATE_PAGO_ASPIRANTE");
        }
    }

    // ================== HISTORIAL alumno_periodo ==================

    /** Inserta en alumno_periodo sólo si no existe ya ese periodo para esa matrícula. */
    public void insertAlumnoPeriodoTx(Connection cn, String matricula, int periodoId) throws SQLException {
        String sql = """
            INSERT INTO sistemaescolar.alumno_periodo (Matricula, idPeriodo, FechaRegistro)
            SELECT ?, ?, CURDATE()
            WHERE NOT EXISTS (
                SELECT 1 FROM sistemaescolar.alumno_periodo
                WHERE Matricula = ? AND idPeriodo = ?
            )
            """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setInt(2, periodoId);
            ps.setString(3, matricula);
            ps.setInt(4, periodoId);
            ps.executeUpdate();
        }
    }

    // ================== VIGENCIA POR ÚLTIMO PERIODO ==================

    public String vigenciaUltimoPeriodoAlumno(String matricula) throws SQLException {
        String sql = """
            SELECT per.Nombre
            FROM sistemaescolar.alumno_periodo ap
            JOIN sistemaescolar.periodo per ON per.idPeriodo = ap.idPeriodo
            WHERE ap.Matricula = ?
            ORDER BY per.Fin DESC, per.Inicio DESC, ap.FechaRegistro DESC
            LIMIT 1
            """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    // ================== INSERTS “sencillos” (ya los tenías) ==================

    public int insertPagoAlumno(String matricula, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, ?, NULL, ?)";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, matricula);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        return 0;
    }

    public int insertPagoAspirante(int folio, double monto, int periodoId) throws SQLException {
        String sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, NULL, ?, ?)";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setInt(2, folio);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        return 0;
    }

    // ================== UPDATES ==================

    public boolean setAspiranteEstatusPagado(int folio) throws SQLException {
        String sql = "UPDATE sistemaescolar.aspirante SET EstatusPago = 'Pagado' WHERE Folio = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean setAlumnoEstadoActivo(String matricula) throws SQLException {
        String sql = "UPDATE sistemaescolar.alumno SET EstadoInscripcion = 'Activo' WHERE Matricula = ?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate() == 1;
        }
    }

    // ================== EXISTS PAGO ==================

    public boolean existsPagoAlumnoEnPeriodo(String matricula, int periodoId) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.pago WHERE Alumno_Matricula = ? AND Periodo_idPeriodo = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setInt(2, periodoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsPagoAspiranteEnPeriodo(int folio, int periodoId) throws SQLException {
        String sql = "SELECT 1 FROM sistemaescolar.pago WHERE Aspirante_Folio = ? AND Periodo_idPeriodo = ? LIMIT 1";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            ps.setInt(2, periodoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================== DELETE POR ASPIRANTE ==================

    public int deleteByAspiranteFolio(Connection cn, int folio) throws SQLException {
        String sql = "DELETE FROM sistemaescolar.pago WHERE Aspirante_Folio = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            return ps.executeUpdate();
        }
    }

    public int deleteByAspiranteFolio(int folio) throws SQLException {
        try (Connection cn = DB.get()) {
            return deleteByAspiranteFolio(cn, folio);
        }
    }

    // ================== HISTORIAL DE PAGOS (YA LO TENÍAS) ==================

    public List<PagoHist> buscarHistorialTodosPeriodos(String entrada) throws SQLException {
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

        List<PagoHist> lista = new ArrayList<>();
        try (Connection cn = DB.get()) {

            if (!esNumero) {
                // matrícula
                try (PreparedStatement ps = cn.prepareStatement(sqlAlumno)) {
                    ps.setString(1, entrada);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String nombre = rs.getString("NombreCompleto");
                            double monto = rs.getBigDecimal("Monto") != null
                                    ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                            String periodo = rs.getString("Periodo");
                            int idPago = rs.getInt("idPago");
                            String mat = rs.getString("Matricula");
                            lista.add(new PagoHist(nombre, monto, periodo, idPago, mat, null));
                        }
                    }
                }
                return lista;
            }

            // número -> primero folio aspirante
            try (PreparedStatement ps = cn.prepareStatement(sqlAspirante)) {
                ps.setInt(1, Integer.parseInt(entrada));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto = rs.getBigDecimal("Monto") != null
                                ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        String periodo = rs.getString("Periodo");
                        int idPago = rs.getInt("idPago");
                        Integer folio = rs.getInt("Folio");
                        lista.add(new PagoHist(nombre, monto, periodo, idPago, null, folio));
                    }
                }
            }

            // y también matrícula numérica
            try (PreparedStatement ps = cn.prepareStatement(sqlAlumno)) {
                ps.setString(1, entrada);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nombre = rs.getString("NombreCompleto");
                        double monto = rs.getBigDecimal("Monto") != null
                                ? rs.getBigDecimal("Monto").doubleValue() : 0.0;
                        String periodo = rs.getString("Periodo");
                        int idPago = rs.getInt("idPago");
                        String mat = rs.getString("Matricula");
                        lista.add(new PagoHist(nombre, monto, periodo, idPago, mat, null));
                    }
                }
            }
        }
        return lista;
    }
}
