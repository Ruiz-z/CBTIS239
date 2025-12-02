package cbtis239.dao;

import cbtis239.model.BoletaCalificacion;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoletaCalificacionDAO {

    private Connection getConnection() throws SQLException {
        return DB.get();
    }

    /**
     * Lista todos los cursos y calificaciones de un alumno por matrícula.
     * Curso = "Materia con Apellidos y Nombre del docente".
     */
    public List<BoletaCalificacion> listarPorAlumno(String matricula) throws SQLException {
        String sql = """
            SELECT 
                m.Nombre AS MateriaNombre,
                CONCAT(d.Paterno, ' ', d.Materno, ' ', d.Nombre) AS DocenteNombre,
                c.Parcial1, c.Parcial2, c.Parcial3, c.ExamenFinal
            FROM calificacion c
            JOIN curso cu 
               ON cu.CursoID = c.CursoID
            JOIN materia m 
               ON m.Clave = cu.Docente_has_Materia_Materia_Clave
            JOIN docente d 
               ON d.DocenteID = cu.Docente_has_Materia_Docente_DocenteID
            WHERE c.Alumno_Matricula = ?
            ORDER BY m.Nombre, DocenteNombre
        """;

        List<BoletaCalificacion> lista = new ArrayList<>();

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BoletaCalificacion fila = new BoletaCalificacion();

                    String materia = rs.getString("MateriaNombre");
                    String docente = rs.getString("DocenteNombre");
                    fila.setCurso(materia + " con " + docente);

                    fila.setParcial1(rs.getDouble("Parcial1"));
                    fila.setParcial2(rs.getDouble("Parcial2"));
                    fila.setParcial3(rs.getDouble("Parcial3"));
                    fila.setExamenFinal(rs.getDouble("ExamenFinal"));

                    lista.add(fila);
                }
            }
        }

        return lista;
    }
}
