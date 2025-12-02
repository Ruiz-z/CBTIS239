package cbtis239.bo;

import cbtis239.dao.BoletaCalificacionDAO;
import cbtis239.model.BoletaCalificacion;

import java.sql.SQLException;
import java.util.List;

public class BoletaCalificacionBO {

    private final BoletaCalificacionDAO dao = new BoletaCalificacionDAO();

    /**
     * Obtiene la boleta de un alumno (valida matricula vacía).
     */
    public List<BoletaCalificacion> boletaDeAlumno(String matricula) throws SQLException {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("La matrícula no puede estar vacía.");
        }
        return dao.listarPorAlumno(matricula.trim());
    }
}
