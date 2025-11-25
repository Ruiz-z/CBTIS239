package cbtis239.bo;

import cbtis239.dao.AlumnoDAO;
import cbtis239.dao.CredencialDao;
import cbtis239.model.Alumno;

import java.sql.SQLException;

public class CredencialBO {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();   // ya tienes existe(matricula)
    private final CredencialDao credDAO = new CredencialDao();

    public Alumno cargarAlumnoParaCredencial(String matricula) throws SQLException {
        if (matricula == null || matricula.isBlank()) throw new SQLException("Matrícula vacía.");
        if (!alumnoDAO.existe(matricula)) throw new SQLException("La matrícula " + matricula + " no existe.");
        return credDAO.obtenerAlumnoPorMatricula(matricula);
    }

    public String calcularVigencia(String matricula) throws SQLException {
        // Ahora devuelve el HISTORIAL de periodos del alumno
        return credDAO.vigenciaHistorial(matricula);
    }

}
