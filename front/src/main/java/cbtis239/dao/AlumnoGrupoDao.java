package cbtis239.dao;

import cbtis239.util.DB;

import java.sql.*;
import java.util.Objects;

/** DAO para mover alumno de grupo y sincronizar calificaciones (sin triggers). */
public class AlumnoGrupoDao {

    public boolean cambiarGrupoYSincronizar(String matricula, int nuevoGrupoId) throws SQLException {
        Objects.requireNonNull(matricula, "matricula es requerida");

        String selectOld = """
            SELECT GrupoID
            FROM SistemaEscolar.Alumno
            WHERE Matricula = ?
        """;

        String updateGrupo = """
            UPDATE SistemaEscolar.Alumno
            SET GrupoID = ?
            WHERE Matricula = ?
        """;

        String insertCalifNuevas = """
            INSERT INTO SistemaEscolar.Calificacion (CursoID, Alumno_Matricula)
            SELECT hc.Curso_CursoID, ?
            FROM SistemaEscolar.HorarioCurso hc
            LEFT JOIN SistemaEscolar.Calificacion c
              ON c.CursoID = hc.Curso_CursoID
             AND c.Alumno_Matricula = ?
            WHERE hc.Grupo_GrupoID = ?
              AND c.CalificacionID IS NULL
        """;

        String deleteCalifAnteriores = """
            DELETE c
            FROM SistemaEscolar.Calificacion c
            WHERE c.Alumno_Matricula = ?
              AND c.CursoID IN (
                  SELECT hc_old.Curso_CursoID
                  FROM SistemaEscolar.HorarioCurso hc_old
                  WHERE hc_old.Grupo_GrupoID = ?
              )
              AND c.CursoID NOT IN (
                  SELECT hc_new.Curso_CursoID
                  FROM SistemaEscolar.HorarioCurso hc_new
                  WHERE hc_new.Grupo_GrupoID = ?
              )
        """;

        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);

            Integer oldGrupoId = null;
            try (PreparedStatement ps = cn.prepareStatement(selectOld)) {
                ps.setString(1, matricula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldGrupoId = rs.getInt("GrupoID");
                        if (rs.wasNull()) oldGrupoId = null;
                    } else {
                        cn.rollback();
                        return false; // alumno no existe
                    }
                }
            }

            boolean cambio = (oldGrupoId == null) || (oldGrupoId != nuevoGrupoId);

            if (cambio) {
                try (PreparedStatement ps = cn.prepareStatement(updateGrupo)) {
                    ps.setInt(1, nuevoGrupoId);
                    ps.setString(2, matricula);
                    ps.executeUpdate();
                }
            }

            // 1) Insertar calificaciones faltantes para cursos del nuevo grupo
            try (PreparedStatement ps = cn.prepareStatement(insertCalifNuevas)) {
                ps.setString(1, matricula);
                ps.setString(2, matricula);
                ps.setInt(3, nuevoGrupoId);
                ps.executeUpdate();
            }

            // 2) Eliminar calificaciones de cursos del grupo anterior (si no aplican)
            if (oldGrupoId != null && oldGrupoId != nuevoGrupoId) {
                try (PreparedStatement ps = cn.prepareStatement(deleteCalifAnteriores)) {
                    ps.setString(1, matricula);
                    ps.setInt(2, oldGrupoId);
                    ps.setInt(3, nuevoGrupoId);
                    ps.executeUpdate();
                }
            }

            cn.commit();
            return true;
        }
    }
}
