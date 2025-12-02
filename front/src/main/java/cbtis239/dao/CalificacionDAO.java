package cbtis239.dao;

import cbtis239.model.Alumno;
import cbtis239.model.Calificacion;
import cbtis239.model.Catalogo;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalificacionDAO {

    private Connection getConnection() throws SQLException {
        return DB.get(); // conexión unificada
    }

    // ============================================================
    // LISTAR CURSOS DEL DOCENTE (para ComboBox)
    // ============================================================
    public List<Catalogo> listarCursosDeDocente(int docenteId) throws SQLException {
        String sql = """
            SELECT DISTINCT 
                   c.CursoID,
                   m.Nombre       AS MateriaNombre,
                   g.NombreGrupo  AS GrupoNombre,
                   TIME_FORMAT(c.HoraInicio, '%H:%i') AS Hi,
                   TIME_FORMAT(c.HoraFin,   '%H:%i') AS Hf
            FROM   Curso c
            JOIN   HorarioCurso hc 
                     ON hc.Curso_CursoID = c.CursoID
            JOIN   Grupo g 
                     ON g.GrupoID = hc.Grupo_GrupoID
            JOIN   Materia m 
                     ON m.Clave = c.Docente_has_Materia_Materia_Clave
            WHERE  c.Docente_has_Materia_Docente_DocenteID = ?
            ORDER BY MateriaNombre, GrupoNombre, Hi
            """;

        List<Catalogo> lista = new ArrayList<>();

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, docenteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idCurso = rs.getInt("CursoID");
                    String materia = rs.getString("MateriaNombre");
                    String grupo = rs.getString("GrupoNombre");
                    String hi = rs.getString("Hi");
                    String hf = rs.getString("Hf");

                    String etiqueta = materia + " - " + grupo + " - " + hi + " a " + hf;
                    lista.add(new Catalogo(idCurso, etiqueta));
                }
            }
        }

        return lista;
    }

    // ============================================================
    // LISTAR CALIFICACIONES DE UN CURSO (con Alumno completo)
    // ============================================================
    public List<Calificacion> listarPorCurso(int cursoId) throws SQLException {
        String sql = """
            SELECT c.CalificacionID, c.CursoID, c.Parcial1, c.Parcial2, c.Parcial3,
                   c.ExamenFinal, c.Alumno_Matricula,
                   a.Nombre, a.Paterno, a.Materno
            FROM calificacion c
            INNER JOIN alumno a ON a.Matricula = c.Alumno_Matricula
            WHERE c.CursoID = ?
            ORDER BY a.Paterno, a.Materno, a.Nombre
        """;

        List<Calificacion> lista = new ArrayList<>();

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, cursoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // ==== Crear objeto Alumno ====
                    Alumno alumno = new Alumno();
                    alumno.setMatricula(rs.getString("Alumno_Matricula"));
                    alumno.setNombre(rs.getString("Nombre"));
                    alumno.setPaterno(rs.getString("Paterno"));
                    alumno.setMaterno(rs.getString("Materno"));

                    // ==== Crear objeto Calificacion ====
                    Calificacion c = new Calificacion();
                    c.setCalificacionId(rs.getInt("CalificacionID"));
                    c.setCursoId(rs.getInt("CursoID"));
                    c.setAlumnoMatricula(alumno.getMatricula());
                    c.setAlumno(alumno);

                    c.setParcial1(rs.getDouble("Parcial1"));
                    c.setParcial2(rs.getDouble("Parcial2"));
                    c.setParcial3(rs.getDouble("Parcial3"));
                    c.setExamenFinal(rs.getDouble("ExamenFinal"));
                    c.recalcularPromedio();

                    lista.add(c);
                }
            }
        }
        return lista;
    }

    // ============================================================
    // ACTUALIZAR UNA CALIFICACIÓN
    // ============================================================
    public void actualizar(Calificacion c) throws SQLException {
        String sql = """
            UPDATE calificacion
               SET Parcial1 = ?, 
                   Parcial2 = ?, 
                   Parcial3 = ?, 
                   ExamenFinal = ?, 
                   FechaActualizacion = CURRENT_TIMESTAMP
             WHERE CalificacionID = ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, c.getParcial1() == 0 ? null : c.getParcial1(), Types.DECIMAL);
            ps.setObject(2, c.getParcial2() == 0 ? null : c.getParcial2(), Types.DECIMAL);
            ps.setObject(3, c.getParcial3() == 0 ? null : c.getParcial3(), Types.DECIMAL);
            ps.setObject(4, c.getExamenFinal() == 0 ? null : c.getExamenFinal(), Types.DECIMAL); // FIX
            ps.setInt(5, c.getCalificacionId());
            ps.executeUpdate();
        }
    }

    // ============================================================
    // CALCULAR PROMEDIO GENERAL DE UN CURSO
    //  (sigue usando solo parciales como lo tenías)
    // ============================================================
    public double promedioGeneral(int cursoId) throws SQLException {
        String sql = """
            SELECT AVG(
                (IFNULL(Parcial1,0) + IFNULL(Parcial2,0) + IFNULL(Parcial3,0)) /
                NULLIF((IF(Parcial1 IS NULL,0,1) + IF(Parcial2 IS NULL,0,1) + IF(Parcial3 IS NULL,0,1)),0)
            ) AS PromedioGeneral
            FROM calificacion
            WHERE CursoID = ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cursoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble("PromedioGeneral");
            }
        }
        return 0.0;
    }

    // ============================================================
    // INSERTAR REGISTROS VACÍOS SI NO EXISTEN PARA UN CURSO
    // ============================================================
    public void insertarSiNoExiste(int cursoId, String matricula) throws SQLException {
        String checkSql = "SELECT 1 FROM calificacion WHERE CursoID = ? AND Alumno_Matricula = ?";
        String insertSql = "INSERT INTO calificacion (CursoID, Alumno_Matricula) VALUES (?, ?)";

        try (Connection cn = getConnection();
             PreparedStatement check = cn.prepareStatement(checkSql)) {
            check.setInt(1, cursoId);
            check.setString(2, matricula);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = cn.prepareStatement(insertSql)) {
                        ins.setInt(1, cursoId);
                        ins.setString(2, matricula);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }
}
