package cbtis239.dao;

import cbtis239.model.Opcion;
import cbtis239.util.DB;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

/**
 * Consultas para Horario de Grupos (sin triggers).
 */
public class HorarioGruposDao {

    // ---------- Tipos auxiliares ----------
    public static class MateriaRet {
        public final String clave;
        public final String nombre;
        public MateriaRet(String clave, String nombre) { this.clave = clave; this.nombre = nombre; }
    }

    public static class CursoAsignado {
        public final int cursoId;
        public final String materiaClave;
        public final String materiaNombre;
        public final String docenteNombre;
        public final String aula;
        public final LocalTime hi, hf;
        public final boolean lu, ma, mi, ju, vi;

        public CursoAsignado(int cursoId, String materiaClave, String materiaNombre, String docenteNombre,
                             String aula, LocalTime hi, LocalTime hf,
                             boolean lu, boolean ma, boolean mi, boolean ju, boolean vi) {
            this.cursoId = cursoId;
            this.materiaClave = materiaClave;
            this.materiaNombre = materiaNombre;
            this.docenteNombre = docenteNombre;
            this.aula = aula;
            this.hi = hi; this.hf = hf;
            this.lu = lu; this.ma = ma; this.mi = mi; this.ju = ju; this.vi = vi;
        }
    }

    // ---------- Catálogos ----------
    public List<Opcion> listarEspecialidades() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM SistemaEscolar.Especialidad ORDER BY Nombre";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Opcion> out = new ArrayList<>();
            while (rs.next()) out.add(new Opcion(rs.getInt("Clave"), rs.getString("Nombre")));
            return out;
        }
    }

    public List<Opcion> listarGruposPorEspecialidad(int espClave) throws SQLException {
        String sql = """
            SELECT g.GrupoID, g.NombreGrupo
            FROM SistemaEscolar.Grupo g
            WHERE g.Especialidad_Clave = ?
            ORDER BY g.NombreGrupo
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            try (ResultSet rs = ps.executeQuery()) {
                List<Opcion> out = new ArrayList<>();
                while (rs.next()) out.add(new Opcion(rs.getInt("GrupoID"), rs.getString("NombreGrupo")));
                return out;
            }
        }
    }

    public Integer obtenerSemestreGrupo(int grupoId) throws SQLException {
        String sql = """
            SELECT MAX(a.Semestre) AS SemestreGrupo
            FROM SistemaEscolar.Grupo g
            JOIN SistemaEscolar.Alumno a ON a.GrupoID = g.GrupoID
            WHERE g.GrupoID = ?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt("SemestreGrupo");
                    return rs.wasNull() ? null : v;
                }
                return null;
            }
        }
    }

    // ---------- Retícula ----------
    public List<MateriaRet> listarMateriasReticula(int espClave, int semestre) throws SQLException {
        String sql = """
            SELECT r.Materia_Clave, m.Nombre
            FROM SistemaEscolar.Reticula r
            JOIN SistemaEscolar.Materia m ON m.Clave = r.Materia_Clave
            WHERE r.Especialidad_Clave = ? AND r.Semestre = ?
            ORDER BY m.Nombre
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            ps.setInt(2, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                List<MateriaRet> out = new ArrayList<>();
                while (rs.next()) out.add(new MateriaRet(rs.getString(1), rs.getString(2)));
                return out;
            }
        }
    }

    /** Materias (clave) ya cubiertas por el grupo (al menos un curso inscrito). */
    public Set<String> materiasAsignadasGrupo(int grupoId) throws SQLException {
        String sql = """
            SELECT DISTINCT m.Clave
            FROM SistemaEscolar.HorarioCurso hc
            JOIN SistemaEscolar.Curso c ON c.CursoID = hc.Curso_CursoID
            JOIN SistemaEscolar.Docente_has_Materia dhm
              ON dhm.Docente_DocenteID = c.Docente_has_Materia_Docente_DocenteID
             AND dhm.Materia_Clave     = c.Docente_has_Materia_Materia_Clave
            JOIN SistemaEscolar.Materia m ON m.Clave = dhm.Materia_Clave
            WHERE hc.Grupo_GrupoID = ?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> out = new HashSet<>();
                while (rs.next()) out.add(rs.getString(1));
                return out;
            }
        }
    }

    // ---------- Cursos del grupo (seleccionados) ----------
    public List<CursoAsignado> listarCursosDelGrupo(int grupoId) throws SQLException {
        String sql = """
            SELECT
                c.CursoID,
                m.Clave      AS MateriaClave,
                m.Nombre     AS MateriaNombre,
                CONCAT(d.Nombre, ' ', d.Paterno, ' ', d.Materno) AS DocenteNombre,
                c.Aula_AulaID AS Aula,
                c.HoraInicio,
                c.HoraFin,
                ds.Lunes, ds.Martes, ds.Miercoles, ds.Jueves, ds.Viernes
            FROM SistemaEscolar.HorarioCurso hc
            JOIN SistemaEscolar.Curso c         ON c.CursoID = hc.Curso_CursoID
            JOIN SistemaEscolar.DiasSemana ds   ON ds.idDiasSemana = c.DiasSemana_idDiasSemana
            JOIN SistemaEscolar.Docente_has_Materia dhm
                 ON dhm.Docente_DocenteID = c.Docente_has_Materia_Docente_DocenteID
                AND dhm.Materia_Clave     = c.Docente_has_Materia_Materia_Clave
            JOIN SistemaEscolar.Materia m       ON m.Clave = dhm.Materia_Clave
            JOIN SistemaEscolar.Docente d       ON d.DocenteID = dhm.Docente_DocenteID
            WHERE hc.Grupo_GrupoID = ?
            ORDER BY m.Nombre, c.HoraInicio
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CursoAsignado> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new CursoAsignado(
                        rs.getInt("CursoID"),
                        rs.getString("MateriaClave"),
                        rs.getString("MateriaNombre"),
                        rs.getString("DocenteNombre"),
                        rs.getString("Aula"),
                        rs.getTime("HoraInicio").toLocalTime(),
                        rs.getTime("HoraFin").toLocalTime(),
                        rs.getObject("Lunes")     != null && rs.getInt("Lunes")     == 1,
                        rs.getObject("Martes")    != null && rs.getInt("Martes")    == 1,
                        rs.getObject("Miercoles") != null && rs.getInt("Miercoles") == 1,
                        rs.getObject("Jueves")    != null && rs.getInt("Jueves")    == 1,
                        rs.getObject("Viernes")   != null && rs.getInt("Viernes")   == 1
                    ));
                }
                return out;
            }
        }
    }

    // ---------- Cursos por materias (disponibles) ----------
    public List<CursoAsignado> listarCursosPorMaterias(Set<String> clavesMaterias) throws SQLException {
        if (clavesMaterias == null || clavesMaterias.isEmpty()) return Collections.emptyList();

        String placeholders = String.join(",", Collections.nCopies(clavesMaterias.size(), "?"));
        String sql = """
            SELECT
                c.CursoID,
                m.Clave      AS MateriaClave,
                m.Nombre     AS MateriaNombre,
                CONCAT(d.Nombre, ' ', d.Paterno, ' ', d.Materno) AS DocenteNombre,
                c.Aula_AulaID AS Aula,
                c.HoraInicio,
                c.HoraFin,
                ds.Lunes, ds.Martes, ds.Miercoles, ds.Jueves, ds.Viernes
            FROM SistemaEscolar.Curso c
            JOIN SistemaEscolar.DiasSemana ds
              ON ds.idDiasSemana = c.DiasSemana_idDiasSemana
            JOIN SistemaEscolar.Docente_has_Materia dhm
              ON dhm.Docente_DocenteID = c.Docente_has_Materia_Docente_DocenteID
             AND dhm.Materia_Clave     = c.Docente_has_Materia_Materia_Clave
            JOIN SistemaEscolar.Materia m  ON m.Clave = dhm.Materia_Clave
            JOIN SistemaEscolar.Docente d  ON d.DocenteID = dhm.Docente_DocenteID
            WHERE m.Clave IN ( %s )
            ORDER BY m.Nombre, c.HoraInicio
        """.formatted(placeholders);

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            for (String cve : clavesMaterias) ps.setString(i++, cve);

            try (ResultSet rs = ps.executeQuery()) {
                List<CursoAsignado> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new CursoAsignado(
                        rs.getInt("CursoID"),
                        rs.getString("MateriaClave"),
                        rs.getString("MateriaNombre"),
                        rs.getString("DocenteNombre"),
                        rs.getString("Aula"),
                        rs.getTime("HoraInicio").toLocalTime(),
                        rs.getTime("HoraFin").toLocalTime(),
                        rs.getObject("Lunes")     != null && rs.getInt("Lunes")     == 1,
                        rs.getObject("Martes")    != null && rs.getInt("Martes")    == 1,
                        rs.getObject("Miercoles") != null && rs.getInt("Miercoles") == 1,
                        rs.getObject("Jueves")    != null && rs.getInt("Jueves")    == 1,
                        rs.getObject("Viernes")   != null && rs.getInt("Viernes")   == 1
                    ));
                }
                return out;
            }
        }
    }

    // ---------- Mutaciones (con sincronización de Calificacion) ----------
    public int agregarCursosGrupo(int grupoId, List<Integer> cursoIds) throws SQLException {
        if (cursoIds == null || cursoIds.isEmpty()) return 0;

        String existsHC = """
            SELECT 1 FROM SistemaEscolar.HorarioCurso
            WHERE Grupo_GrupoID = ? AND Curso_CursoID = ?
        """;
        String insertHC = """
            INSERT INTO SistemaEscolar.HorarioCurso (Curso_CursoID, Grupo_GrupoID)
            VALUES (?, ?)
        """;
        String insertCalifIfMissing = """
            INSERT INTO SistemaEscolar.Calificacion (CursoID, Alumno_Matricula)
            SELECT ?, a.Matricula
            FROM SistemaEscolar.Alumno a
            LEFT JOIN SistemaEscolar.Calificacion c
              ON c.CursoID = ? AND c.Alumno_Matricula = a.Matricula
            WHERE a.GrupoID = ?
              AND c.CalificacionID IS NULL
        """;

        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);
            int total = 0;

            try (PreparedStatement psEx = cn.prepareStatement(existsHC);
                 PreparedStatement psIn = cn.prepareStatement(insertHC);
                 PreparedStatement psCal = cn.prepareStatement(insertCalifIfMissing)) {

                for (Integer cursoId : cursoIds) {
                    // 1) HorarioCurso (evita duplicados)
                    psEx.setInt(1, grupoId);
                    psEx.setInt(2, cursoId);
                    boolean yaExiste;
                    try (ResultSet rs = psEx.executeQuery()) { yaExiste = rs.next(); }

                    if (!yaExiste) {
                        psIn.setInt(1, cursoId);
                        psIn.setInt(2, grupoId);
                        total += psIn.executeUpdate();
                    }

                    // 2) Calificacion (crea renglones para cada alumno del grupo si no existen)
                    psCal.setInt(1, cursoId);
                    psCal.setInt(2, cursoId);
                    psCal.setInt(3, grupoId);
                    psCal.executeUpdate();
                }
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }

            cn.commit();
            return total;
        }
    }

    public int eliminarCursosGrupo(int grupoId, List<Integer> cursoIds) throws SQLException {
        if (cursoIds == null || cursoIds.isEmpty()) return 0;

        String placeholders = String.join(",", Collections.nCopies(cursoIds.size(), "?"));

        String deleteCalif = """
            DELETE c FROM SistemaEscolar.Calificacion c
            JOIN SistemaEscolar.Alumno a ON a.Matricula = c.Alumno_Matricula
            WHERE a.GrupoID = ?
              AND c.CursoID IN ( %s )
        """.formatted(placeholders);

        String deleteHC = """
            DELETE FROM SistemaEscolar.HorarioCurso
            WHERE Grupo_GrupoID = ?
              AND Curso_CursoID IN ( %s )
        """.formatted(placeholders);

        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);
            int total;
            try (PreparedStatement ps1 = cn.prepareStatement(deleteCalif);
                 PreparedStatement ps2 = cn.prepareStatement(deleteHC)) {

                // 1) Borrar Calificacion de TODOS los alumnos del grupo para esos cursos
                int idx = 1;
                ps1.setInt(idx++, grupoId);
                for (Integer id : cursoIds) ps1.setInt(idx++, id);
                ps1.executeUpdate();

                // 2) Borrar las relaciones HorarioCurso
                idx = 1;
                ps2.setInt(idx++, grupoId);
                for (Integer id : cursoIds) ps2.setInt(idx++, id);
                total = ps2.executeUpdate();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
            cn.commit();
            return total;
        }
    }
}
