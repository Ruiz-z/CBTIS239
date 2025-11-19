package cbtis239.bo;

import cbtis239.dao.CalificacionDAO;
import cbtis239.model.Calificacion;
import cbtis239.model.Catalogo;

import java.sql.SQLException;
import java.util.List;

public class CalificacionBO {

    private final CalificacionDAO dao = new CalificacionDAO();

    // ============================================================
    // LISTAR CURSOS DEL DOCENTE (para el ComboBox)
    // ============================================================
    public List<Catalogo> cursosDelDocente(int docenteId) throws SQLException {
        if (docenteId <= 0)
            throw new IllegalArgumentException("El ID del docente no es válido.");
        return dao.listarCursosDeDocente(docenteId);
    }

    // ============================================================
    // LISTAR CALIFICACIONES POR CURSO
    // ============================================================
    public List<Calificacion> listarPorCurso(int cursoId) throws SQLException {
        if (cursoId <= 0)
            throw new IllegalArgumentException("El ID del curso no es válido.");
        return dao.listarPorCurso(cursoId);
    }

    // ============================================================
    // GUARDAR CALIFICACIONES EDITADAS
    // ============================================================
    public void guardar(List<Calificacion> lista) throws SQLException {
        if (lista == null || lista.isEmpty())
            throw new IllegalArgumentException("No hay registros para guardar.");

        for (Calificacion c : lista) {
            if (c.getCalificacionId() <= 0)
                continue; // evitar errores si hay registros sin ID todavía

            c.recalcularPromedio();
            dao.actualizar(c);
        }
    }

    // ============================================================
    // PROMEDIO GENERAL DEL CURSO
    // ============================================================
    public double promedioGeneral(int cursoId) throws SQLException {
        if (cursoId <= 0)
            return 0.0;
        return dao.promedioGeneral(cursoId);
    }
}
