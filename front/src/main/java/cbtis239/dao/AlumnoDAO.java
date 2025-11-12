package cbtis239.dao;

import cbtis239.model.Alumno;
import cbtis239.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlumnoDAO {

    // ====== CRUD ======

    public boolean existe(String matricula) throws SQLException {
        String sql = "SELECT 1 FROM alumno WHERE Matricula=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Alumno a) throws SQLException {
        String sql = """
        INSERT INTO alumno
        (CURP, Matricula, GrupoID, Semestre, EstadoInscripcion, Foto, Firma, Telefono, Correo, FechaInscripcion,
         Nombre, Paterno, Materno, NSS, Carrera, Calle, Numero, Colonia, Estado, Municipio, Localidad,
         CelPadre, CelMadre, EdoCivil_idEdoCivil, Generos_idGenero, Periodo_idPeriodo)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fillPS(ps, a);
            ps.executeUpdate();
        }

    }

    public void update(Alumno a) throws SQLException {
        String sql = """
        UPDATE alumno SET
          CURP=?, Matricula=?, GrupoID=?, Semestre=?, EstadoInscripcion=?, Foto=?, Firma=?, Telefono=?, Correo=?, FechaInscripcion=?,
          Nombre=?, Paterno=?, Materno=?, NSS=?, Carrera=?, Calle=?, Numero=?, Colonia=?, Estado=?, Municipio=?, Localidad=?,
          CelPadre=?, CelMadre=?, EdoCivil_idEdoCivil=?, Generos_idGenero=?, Periodo_idPeriodo=?
        WHERE Matricula=?
    """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = fillPS(ps, a);
            ps.setString(i, a.getMatricula());
            ps.executeUpdate();
        }
    }


    public void deleteByMatricula(String matricula) throws SQLException {
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM alumno WHERE Matricula=?")) {
            ps.setString(1, matricula);
            ps.executeUpdate();
        }
    }

    public Alumno findByMatricula(String mat) throws SQLException {
        String sql = "SELECT * FROM alumno WHERE Matricula=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, mat);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Alumno> listBreve() throws SQLException {
        // Para llenar la tabla (matricula, nombre, semestre, grupo)
        String sql = "SELECT Matricula, Nombre, Semestre, GrupoID FROM alumno ORDER BY Matricula DESC LIMIT 100";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Alumno> out = new ArrayList<>();
            while (rs.next()) {
                Alumno a = new Alumno();
                a.setMatricula(rs.getString("Matricula"));
                a.setNombre(rs.getString("Nombre"));
                int sem = rs.getInt("Semestre");
                a.setSemestre(rs.wasNull() ? null : sem);
                int gid = rs.getInt("GrupoID");
                a.setGrupoId(rs.wasNull() ? null : gid);
                out.add(a);
            }
            return out;
        }
    }

    // ===== helpers =====

    private int fillPS(PreparedStatement ps, Alumno a) throws SQLException {
        // Devuelve el siguiente índice libre (para UPDATE)
        int i = 1;

        ps.setString(i++, a.getCurp());
        ps.setString(i++, a.getMatricula());
        if (a.getGrupoId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getGrupoId());
        if (a.getSemestre() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getSemestre());
        ps.setString(i++, a.getEstadoInscripcion());
        ps.setString(i++, a.getFoto());
        ps.setString(i++, a.getFirma());
        ps.setString(i++, a.getTelefono());
        ps.setString(i++, a.getCorreo());
        if (a.getFechaInscripcion() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(a.getFechaInscripcion()));

        ps.setString(i++, a.getNombre());
        ps.setString(i++, a.getPaterno());
        ps.setString(i++, a.getMaterno());
        ps.setString(i++, a.getNss());
        ps.setString(i++, a.getCarrera());
        ps.setString(i++, a.getCalle());
        ps.setString(i++, a.getNumero());
        ps.setString(i++, a.getColonia());
        ps.setString(i++, a.getEstado());
        ps.setString(i++, a.getMunicipio());
        ps.setString(i++, a.getLocalidad());
        ps.setString(i++, a.getCelPadre());
        ps.setString(i++, a.getCelMadre());

        if (a.getEdoCivilId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getEdoCivilId());
        if (a.getGeneroId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getGeneroId());
        if (a.getPeriodoId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getPeriodoId());

        return i;
    }

    private Alumno map(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setMatricula(rs.getString("Matricula"));
        a.setCurp(rs.getString("CURP"));

        int gid = rs.getInt("GrupoID");  a.setGrupoId(rs.wasNull()? null : gid);
        int sem = rs.getInt("Semestre"); a.setSemestre(rs.wasNull()? null : sem);

        a.setEstadoInscripcion(rs.getString("EstadoInscripcion"));
        a.setFoto(rs.getString("Foto"));
        a.setFirma(rs.getString("Firma"));
        a.setTelefono(rs.getString("Telefono"));
        a.setEstado(rs.getString("Estado"));
        a.setCorreo(rs.getString("Correo"));

        Date d = rs.getDate("FechaInscripcion");
        a.setFechaInscripcion(d == null ? null : d.toLocalDate());

        a.setNombre(rs.getString("Nombre"));
        a.setPaterno(rs.getString("Paterno"));
        a.setMaterno(rs.getString("Materno"));
        a.setNss(rs.getString("NSS"));
        a.setCarrera(rs.getString("Carrera"));
        a.setCalle(rs.getString("Calle"));
        a.setNumero(rs.getString("Numero"));
        a.setColonia(rs.getString("Colonia"));
        a.setLocalidad(rs.getString("Localidad"));
        a.setMunicipio(rs.getString("Municipio"));
        a.setCelPadre(rs.getString("CelPadre"));
        a.setCelMadre(rs.getString("CelMadre"));


        int ec = rs.getInt("EdoCivil_idEdoCivil"); a.setEdoCivilId(rs.wasNull()? null : ec);
        int ge = rs.getInt("Generos_idGenero");    a.setGeneroId(rs.wasNull()? null : ge);
        int pe = rs.getInt("Periodo_idPeriodo");   a.setPeriodoId(rs.wasNull()? null : pe);

        return a;
    }
    public int actualizarEstadoSemestreYPeriodo(String matricula,
                                                String nuevoEstado,
                                                Integer nuevoSemestre,
                                                Integer nuevoPeriodoId) throws SQLException {
        String sql = "UPDATE alumno SET EstadoInscripcion=?, Semestre=?, Periodo_idPeriodo=? WHERE Matricula=?";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            if (nuevoSemestre == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, nuevoSemestre);
            if (nuevoPeriodoId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, nuevoPeriodoId);
            ps.setString(4, matricula);
            return ps.executeUpdate();
        }
    }
    public int actualizarTrasPago(Connection cn, String matricula, int nuevoPeriodoId) throws SQLException {
        String sql =
                """
                UPDATE sistemaescolar.alumno a
                JOIN sistemaescolar.periodo pnuevo ON pnuevo.idPeriodo = ?
                LEFT JOIN sistemaescolar.periodo pactual ON pactual.idPeriodo = a.Periodo_idPeriodo
                /* diff = num de periodos con inicio <= pnuevo - num de periodos con inicio <= pactual */
                SET a.Semestre =
                    CASE
                        WHEN a.Semestre IS NULL THEN 1
                        ELSE a.Semestre + GREATEST(
                            (
                              (SELECT COUNT(*) FROM sistemaescolar.periodo x WHERE x.Inicio <= pnuevo.Inicio)
                              -
                              (SELECT COUNT(*) FROM sistemaescolar.periodo y WHERE y.Inicio <= COALESCE(pactual.Inicio, pnuevo.Inicio))
                            ),
                            0
                        )
                    END,
                    a.Periodo_idPeriodo = ?,
                    a.EstadoInscripcion = 'Activo'
                WHERE a.Matricula = ?
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nuevoPeriodoId);
            ps.setInt(2, nuevoPeriodoId);
            ps.setString(3, matricula);
            return ps.executeUpdate();
        }
    }

    /** Versión cómoda sin transacción externa. */
    public int actualizarTrasPago(String matricula, int nuevoPeriodoId) throws SQLException {
        try (Connection cn = DB.get()) {
            return actualizarTrasPago(cn, matricula, nuevoPeriodoId);
        }
    }

    public int sincronizarEstadoPorPagoVigente(java.sql.Connection cn) throws java.sql.SQLException {
        int total = 0;

        // 1) periodo vigente por fecha
        Integer periodoActual = null;
        try (var ps = cn.prepareStatement(
                "SELECT idPeriodo FROM sistemaescolar.periodo " +
                        "WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1");
             var rs = ps.executeQuery()) {
            if (rs.next()) periodoActual = rs.getInt(1);
        }
        if (periodoActual == null) return 0;

        // 2) Activar a quienes SÍ pagaron el periodo vigente y fijar Periodo_idPeriodo = vigente
        try (var ps = cn.prepareStatement(
                "UPDATE sistemaescolar.alumno a " +
                        "JOIN sistemaescolar.pago p ON p.Alumno_Matricula = a.Matricula " +
                        "  AND p.Periodo_idPeriodo = ? AND p.Estatus = 1 " +
                        "SET a.EstadoInscripcion = 'Activo', a.Periodo_idPeriodo = ?")) {
            ps.setInt(1, periodoActual);
            ps.setInt(2, periodoActual);
            total += ps.executeUpdate();
        }

        // 3) Poner INACTIVO a los que NO pagaron el periodo vigente
        try (var ps = cn.prepareStatement(
                "UPDATE sistemaescolar.alumno a " +
                        "LEFT JOIN sistemaescolar.pago p ON p.Alumno_Matricula = a.Matricula " +
                        "  AND p.Periodo_idPeriodo = ? AND p.Estatus = 1 " +
                        "SET a.EstadoInscripcion = 'Inactivo' " +
                        "WHERE p.idPago IS NULL")) {
            ps.setInt(1, periodoActual);
            total += ps.executeUpdate();
        }

        return total;
    }

    public int sincronizarEstadoPorPagoVigente() throws java.sql.SQLException {
        try (var cn = cbtis239.util.DB.get()) {
            return sincronizarEstadoPorPagoVigente(cn);
        }
    }
    public int sincronizarEstadoConPeriodoVigente(java.sql.Connection cn) throws java.sql.SQLException {
        return sincronizarEstadoPorPagoVigente(cn);
    }

    public int sincronizarEstadoConPeriodoVigente() throws java.sql.SQLException {
        try (var cn = cbtis239.util.DB.get()) {
            return sincronizarEstadoPorPagoVigente(cn);
        }
    }
    public Integer getPeriodoActualId() throws SQLException {
        String sql = "SELECT idPeriodo FROM sistemaescolar.periodo WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1";
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql); var rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }

    // ============================================================
    // 🔹 Obtener consecutivo de matrícula por año y especialidad
    // ============================================================
    public int obtenerConsecutivo(int año, int idEspecialidad) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS total
            FROM sistemaescolar.alumno
            WHERE YEAR(FechaInscripcion) = ? AND Carrera = ?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, 2000 + año); // ejemplo: 25 → 2025
            ps.setString(2, String.valueOf(idEspecialidad)); // usa el campo Carrera como identificador
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        }
        return 0;
    }
    // AlumnoDAO.java
    public Alumno obtenerPorMatricula(String matricula) throws SQLException {
        String sql = """
        SELECT Matricula, Nombre, Paterno, Materno, CURP, NSS, Foto
        FROM alumno
        WHERE Matricula = ?
    """;
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                var a = new Alumno();
                a.setMatricula(rs.getString("Matricula"));
                a.setNombre(rs.getString("Nombre"));
                a.setPaterno(rs.getString("Paterno"));
                a.setMaterno(rs.getString("Materno"));
                a.setCurp(rs.getString("CURP"));
                a.setNss(rs.getString("NSS"));
                a.setFoto(Arrays.toString(rs.getBytes("Foto"))); // puede ser null
                return a;
            }
        }
    }

    /* (opcional) Vigencia textual a partir de los periodos cursados.
       Ajusta el JOIN a tu esquema real (kardex, reinscripcion, alumno_periodo, etc.). */
    public String vigenciaPorMatricula(String matricula) throws SQLException {
        String sql = """
        SELECT MIN(p.Nombre) AS inicio, MAX(p.Nombre) AS fin
        FROM periodo p
        JOIN alumno_periodo ap ON ap.idPeriodo = p.idPeriodo
        WHERE ap.Matricula = ?
    """;
        try (var cn = DB.get(); var ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (var rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("inicio") != null) {
                    return rs.getString("inicio") + " - " + rs.getString("fin");
                }
            }
        }
        // fallback si aún no hay historial
        return "Sin historial";
    }

}

