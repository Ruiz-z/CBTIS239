package cbtis239.dao;

import cbtis239.model.Curso;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import cbtis239.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CursoDao {

    private Connection getConnection() throws SQLException { return DB.get(); }

    // ===== Combos base =====
    public List<OpcionStr> listarMaterias() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM Materia ORDER BY Nombre";
        List<OpcionStr> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new OpcionStr(rs.getString(1), rs.getString(2)));
        }
        return out;
    }
    public List<Opcion> listarDocentes() throws SQLException {
        String sql = "SELECT DocenteID, CONCAT(Nombre,' ',IFNULL(Paterno,''),' ',IFNULL(Materno,'')) " +
                     "FROM Docente ORDER BY Nombre, Paterno, Materno";
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Opcion(rs.getInt(1), rs.getString(2).trim().replaceAll(" +"," ")));
        }
        return out;
    }
    public List<Opcion> listarDocentesPorMateria(String claveMateria) throws SQLException {
        String sql = """
            SELECT d.DocenteID,
                   CONCAT(d.Nombre,' ',IFNULL(d.Paterno,''),' ',IFNULL(d.Materno,'')) AS nom
            FROM Docente_has_Materia dm
            JOIN Docente d ON d.DocenteID = dm.Docente_DocenteID
            WHERE dm.Materia_Clave = ?
            ORDER BY d.Nombre, d.Paterno, d.Materno
            """;
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, claveMateria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Opcion(rs.getInt("DocenteID"), rs.getString("nom").trim().replaceAll(" +"," ")));
            }
        }
        return out;
    }
    public List<OpcionStr> listarMateriasPorDocente(int docenteId) throws SQLException {
        String sql = """
            SELECT m.Clave, m.Nombre
            FROM Docente_has_Materia dm
            JOIN Materia m ON m.Clave = dm.Materia_Clave
            WHERE dm.Docente_DocenteID = ?
            ORDER BY m.Nombre
            """;
        List<OpcionStr> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new OpcionStr(rs.getString("Clave"), rs.getString("Nombre")));
            }
        }
        return out;
    }
    public List<OpcionStr> listarAulas() throws SQLException {
        String sql = "SELECT Clave, CONCAT('Aula ',Clave,' (cap ',Capacidad,')') FROM Aula ORDER BY Clave";
        List<OpcionStr> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new OpcionStr(rs.getString(1), rs.getString(2)));
        }
        return out;
    }
    public List<Opcion> listarPeriodos() throws SQLException {
        String sql = "SELECT idPeriodo, Nombre FROM Periodo ORDER BY Inicio DESC";
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Opcion(rs.getInt(1), rs.getString(2)));
        }
        return out;
    }
    public LocalDate[] fechasDePeriodo(int idPeriodo) throws SQLException {
        String sql = "SELECT Inicio, Fin FROM Periodo WHERE idPeriodo=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPeriodo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new LocalDate[]{ rs.getDate(1).toLocalDate(), rs.getDate(2).toLocalDate() };
            }
        }
        throw new SQLException("Periodo no encontrado: " + idPeriodo);
    }

    // ===== DiasSemana =====
    public int getOrCreateDiasSemana(int l, int m, int x, int j, int v) throws SQLException {
        String sel = "SELECT idDiasSemana FROM DiasSemana WHERE " +
                "COALESCE(Lunes,0)=? AND COALESCE(Martes,0)=? AND COALESCE(Miercoles,0)=? AND COALESCE(Jueves,0)=? AND COALESCE(Viernes,0)=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sel)) {
            ps.setInt(1, l); ps.setInt(2, m); ps.setInt(3, x); ps.setInt(4, j); ps.setInt(5, v);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        String ins = "INSERT INTO DiasSemana (Lunes,Martes,Miercoles,Jueves,Viernes) VALUES (?,?,?,?,?)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l); ps.setInt(2, m); ps.setInt(3, x); ps.setInt(4, j); ps.setInt(5, v);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("No fue posible crear DiasSemana personalizado.");
    }

    // ===== Validaciones con INTERSECCIÓN de días =====

/**
 * ¿Hay otro curso en la MISMA AULA que choque en horas y comparta al menos un día,
 * dentro del rango [periodoInicio, periodoFin]?  (excluye c.CursoID=exclCursoId)
 */
public boolean existeAulaOcupada(String aula, LocalTime hi, LocalTime hf, int diasId,
                                 LocalDate periodoInicio, LocalDate periodoFin, Integer exclCursoId) throws SQLException {
    String sql = """
        SELECT 1
        FROM Curso c
        JOIN DiasSemana dsC ON dsC.idDiasSemana = c.DiasSemana_idDiasSemana
        JOIN DiasSemana dsN ON dsN.idDiasSemana = ?
        WHERE c.Aula_AulaID = ?
          -- horas traslapadas
          AND NOT (c.HoraFin <= ? OR c.HoraInicio >= ?)
          -- fechas traslapadas con el periodo elegido
          AND NOT (c.FechaFin < ? OR c.FechaInicio > ?)
          -- al menos UN día en común
          AND (
               (COALESCE(dsC.Lunes,0)=1     AND COALESCE(dsN.Lunes,0)=1) OR
               (COALESCE(dsC.Martes,0)=1    AND COALESCE(dsN.Martes,0)=1) OR
               (COALESCE(dsC.Miercoles,0)=1 AND COALESCE(dsN.Miercoles,0)=1) OR
               (COALESCE(dsC.Jueves,0)=1    AND COALESCE(dsN.Jueves,0)=1) OR
               (COALESCE(dsC.Viernes,0)=1   AND COALESCE(dsN.Viernes,0)=1)
          )
          AND (? IS NULL OR c.CursoID <> ?)
        LIMIT 1
        """;
    try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, diasId);
        ps.setString(2, aula);
        ps.setTime(3, Time.valueOf(hi));
        ps.setTime(4, Time.valueOf(hf));
        ps.setDate(5, Date.valueOf(periodoInicio));
        ps.setDate(6, Date.valueOf(periodoFin));
        if (exclCursoId == null) { ps.setNull(7, Types.INTEGER); ps.setNull(8, Types.INTEGER); }
        else { ps.setInt(7, exclCursoId); ps.setInt(8, exclCursoId); }
        try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
}

/**
 * ¿El DOCENTE ya tiene otro curso que choque en horas y comparta al menos un día
 * dentro del rango [periodoInicio, periodoFin]? (excluye c.CursoID=exclCursoId)
 */
public boolean existeDocenteOcupado(int docenteId, LocalTime hi, LocalTime hf, int diasId,
                                    LocalDate periodoInicio, LocalDate periodoFin, Integer exclCursoId) throws SQLException {
    String sql = """
        SELECT 1
        FROM Curso c
        JOIN DiasSemana dsC ON dsC.idDiasSemana = c.DiasSemana_idDiasSemana
        JOIN DiasSemana dsN ON dsN.idDiasSemana = ?
        WHERE c.Docente_has_Materia_Docente_DocenteID = ?
          -- horas traslapadas
          AND NOT (c.HoraFin <= ? OR c.HoraInicio >= ?)
          -- fechas traslapadas con el periodo elegido
          AND NOT (c.FechaFin < ? OR c.FechaInicio > ?)
          -- al menos UN día en común
          AND (
               (COALESCE(dsC.Lunes,0)=1     AND COALESCE(dsN.Lunes,0)=1) OR
               (COALESCE(dsC.Martes,0)=1    AND COALESCE(dsN.Martes,0)=1) OR
               (COALESCE(dsC.Miercoles,0)=1 AND COALESCE(dsN.Miercoles,0)=1) OR
               (COALESCE(dsC.Jueves,0)=1    AND COALESCE(dsN.Jueves,0)=1) OR
               (COALESCE(dsC.Viernes,0)=1   AND COALESCE(dsN.Viernes,0)=1)
          )
          AND (? IS NULL OR c.CursoID <> ?)
        LIMIT 1
        """;
    try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, diasId);
        ps.setInt(2, docenteId);
        ps.setTime(3, Time.valueOf(hi));
        ps.setTime(4, Time.valueOf(hf));
        ps.setDate(5, Date.valueOf(periodoInicio));
        ps.setDate(6, Date.valueOf(periodoFin));
        if (exclCursoId == null) { ps.setNull(7, Types.INTEGER); ps.setNull(8, Types.INTEGER); }
        else { ps.setInt(7, exclCursoId); ps.setInt(8, exclCursoId); }
        try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
}

/**
 * “Curso igual en el mismo periodo”: mismo docente, misma materia, misma aula,
 * MISMAS horas y al menos UN día en común, con fechas traslapadas al periodo.
 */
public boolean existeCursoIgual(LocalDate periodoInicio, LocalDate periodoFin,
                                int docenteId, String materiaClave, String aula,
                                LocalTime hi, LocalTime hf, int diasId, Integer exclCursoId) throws SQLException {
    String sql = """
        SELECT 1
        FROM Curso c
        JOIN DiasSemana dsC ON dsC.idDiasSemana = c.DiasSemana_idDiasSemana
        JOIN DiasSemana dsN ON dsN.idDiasSemana = ?
        WHERE c.Docente_has_Materia_Docente_DocenteID = ?
          AND c.Docente_has_Materia_Materia_Clave = ?
          AND c.Aula_AulaID = ?
          AND c.HoraInicio = ? AND c.HoraFin = ?
          -- fechas traslapadas con el periodo elegido
          AND NOT (c.FechaFin < ? OR c.FechaInicio > ?)
          -- al menos UN día en común
          AND (
               (COALESCE(dsC.Lunes,0)=1     AND COALESCE(dsN.Lunes,0)=1) OR
               (COALESCE(dsC.Martes,0)=1    AND COALESCE(dsN.Martes,0)=1) OR
               (COALESCE(dsC.Miercoles,0)=1 AND COALESCE(dsN.Miercoles,0)=1) OR
               (COALESCE(dsC.Jueves,0)=1    AND COALESCE(dsN.Jueves,0)=1) OR
               (COALESCE(dsC.Viernes,0)=1   AND COALESCE(dsN.Viernes,0)=1)
          )
          AND (? IS NULL OR c.CursoID <> ?)
        LIMIT 1
        """;
    try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, diasId);
        ps.setInt(2, docenteId);
        ps.setString(3, materiaClave);
        ps.setString(4, aula);
        ps.setTime(5, Time.valueOf(hi));
        ps.setTime(6, Time.valueOf(hf));
        ps.setDate(7, Date.valueOf(periodoInicio));
        ps.setDate(8, Date.valueOf(periodoFin));
        if (exclCursoId == null) { ps.setNull(9, Types.INTEGER); ps.setNull(10, Types.INTEGER); }
        else { ps.setInt(9, exclCursoId); ps.setInt(10, exclCursoId); }
        try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
}


    // ===== CRUD =====
    public void insertar(Curso c) throws SQLException {
        String sql = """
            INSERT INTO Curso
              (AnioAcademico, Estado, FechaInicio, FechaFin, Descripcion,
               Aula_AulaID, HoraInicio, HoraFin, DiasSemana_idDiasSemana,
               Docente_has_Materia_Docente_DocenteID, Docente_has_Materia_Materia_Clave)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getAnioAcademico());
            ps.setString(2, c.getEstado());
            ps.setDate(3, Date.valueOf(c.getFechaInicio()));
            ps.setDate(4, Date.valueOf(c.getFechaFin()));
            ps.setString(5, c.getDescripcion());
            ps.setString(6, c.getAulaId());
            ps.setTime(7, Time.valueOf(c.getHoraInicio()));
            ps.setTime(8, Time.valueOf(c.getHoraFin()));
            ps.setInt(9, c.getDiasSemanaId());
            ps.setInt(10, c.getDocenteId());
            ps.setString(11, c.getMateriaClave());
            ps.executeUpdate();
        }
    }

    public void actualizar(Curso c) throws SQLException {
        String sql = """
            UPDATE Curso SET
              AnioAcademico=?, Estado=?, FechaInicio=?, FechaFin=?, Descripcion=?,
              Aula_AulaID=?, HoraInicio=?, HoraFin=?, DiasSemana_idDiasSemana=?,
              Docente_has_Materia_Docente_DocenteID=?, Docente_has_Materia_Materia_Clave=?
            WHERE CursoID=?
            """;
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getAnioAcademico());
            ps.setString(2, c.getEstado());
            ps.setDate(3, Date.valueOf(c.getFechaInicio()));
            ps.setDate(4, Date.valueOf(c.getFechaFin()));
            ps.setString(5, c.getDescripcion());
            ps.setString(6, c.getAulaId());
            ps.setTime(7, Time.valueOf(c.getHoraInicio()));
            ps.setTime(8, Time.valueOf(c.getHoraFin()));
            ps.setInt(9, c.getDiasSemanaId());
            ps.setInt(10, c.getDocenteId());
            ps.setString(11, c.getMateriaClave());
            ps.setInt(12, c.getCursoId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int cursoId) throws SQLException {
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM Curso WHERE CursoID=?")) {
            ps.setInt(1, cursoId);
            ps.executeUpdate();
        }
    }

    public List<Curso> listar() throws SQLException {
  String sql = """
    SELECT c.CursoID,
           m.Nombre AS Materia,
           c.Docente_has_Materia_Materia_Clave AS MateriaClave,
           CONCAT(d.Nombre,' ',IFNULL(d.Paterno,''),' ',IFNULL(d.Materno,'')) AS Docente,
           c.Docente_has_Materia_Docente_DocenteID AS DocenteID,
           c.Aula_AulaID, c.HoraInicio, c.HoraFin,
           c.FechaInicio, c.FechaFin,                     -- <--- AÑADIDO
           ds.Lunes, ds.Martes, ds.Miercoles, ds.Jueves, ds.Viernes
    FROM Curso c
    JOIN Materia m ON m.Clave = c.Docente_has_Materia_Materia_Clave
    JOIN Docente d ON d.DocenteID = c.Docente_has_Materia_Docente_DocenteID
    JOIN DiasSemana ds ON ds.idDiasSemana = c.DiasSemana_idDiasSemana
    ORDER BY m.Nombre, Docente
""";
        List<Curso> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Curso c = new Curso();
                c.setCursoId(rs.getInt("CursoID"));
                c.setMateriaNombre(rs.getString("Materia"));
                c.setMateriaClave(rs.getString("MateriaClave"));
                c.setDocenteNombre(rs.getString("Docente").trim().replaceAll(" +"," "));
                c.setDocenteId(rs.getInt("DocenteID"));
                c.setAulaId(rs.getString("Aula_AulaID"));
                c.setHoraInicio(rs.getTime("HoraInicio").toLocalTime());
                c.setHoraFin(rs.getTime("HoraFin").toLocalTime());
                c.setFechaInicio(rs.getDate("FechaInicio").toLocalDate());   // <--- NUEVO
                c.setFechaFin(rs.getDate("FechaFin").toLocalDate());         // <--- NUEVO
                String dias = (rs.getInt("Lunes")==1?"L,":"") + (rs.getInt("Martes")==1?"M,":"") +
                              (rs.getInt("Miercoles")==1?"X,":"") + (rs.getInt("Jueves")==1?"J,":"") +
                              (rs.getInt("Viernes")==1?"V,":"");
                if (dias.endsWith(",")) dias = dias.substring(0, dias.length()-1);
                c.setHorarioTexto(dias + " de " + c.getHoraInicio() + " a " + c.getHoraFin());
                out.add(c);
            }
        }
        return out;
    }
    public int obtenerCursoPorGrupoMateria(int grupoId, String materiaClave) throws SQLException {
        String sql = """
        SELECT DISTINCT c.CursoID
        FROM Curso c
        JOIN Calificacion cal ON cal.CursoID = c.CursoID
        JOIN Alumno a ON a.Matricula = cal.Alumno_Matricula
        JOIN Grupo g ON g.GrupoID = a.GrupoID
        WHERE a.GrupoID = ?
          AND c.Docente_has_Materia_Materia_Clave = ?
        LIMIT 1
    """;

        System.out.println("[DEBUG] Buscando curso para GrupoID=" + grupoId + " y Materia=" + materiaClave);

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, grupoId);
            ps.setString(2, materiaClave);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("CursoID");
                    System.out.println("[DEBUG] ✅ Curso encontrado: " + id);
                    return id;
                } else {
                    System.out.println("[WARN] ⚠️ No se encontró curso para grupo=" + grupoId + " materia=" + materiaClave);
                }
            }
        }
        return -1;
    }

}
