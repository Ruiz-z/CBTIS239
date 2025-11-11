package cbtis239.dao;

import cbtis239.model.Alumno;
import cbtis239.model.Calificacion;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalificacionDAO {

    private Connection getConnection() throws SQLException {
        return DB.get(); // conexión unificada
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
               SET Parcial1 = ?, Parcial2 = ?, Parcial3 = ?, ExamenFinal = ?, 
                   FechaActualizacion = CURRENT_TIMESTAMP
             WHERE CalificacionID = ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, c.getParcial1() == 0 ? null : c.getParcial1(), Types.DECIMAL);
            ps.setObject(2, c.getParcial2() == 0 ? null : c.getParcial2(), Types.DECIMAL);
            ps.setObject(3, c.getParcial3() == 0 ? null : c.getParcial3(), Types.DECIMAL);
            ps.setObject(4, c.getPromedio() == 0 ? null : c.getPromedio(), Types.DECIMAL);
            ps.setInt(5, c.getCalificacionId());
            ps.executeUpdate();
        }
    }

    // ============================================================
    // CALCULAR PROMEDIO GENERAL DE UN CURSO
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
