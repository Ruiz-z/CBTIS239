package cbtis239.bo;

import cbtis239.dao.AlumnoDAO;
import cbtis239.model.Alumno;

import java.sql.SQLException;
import java.util.List;

public class AlumnoBO {

    private final AlumnoDAO dao = new AlumnoDAO();

    public void guardar(Alumno a) throws SQLException {
        if (a.getMatricula()==null || a.getMatricula().isBlank())
            throw new IllegalArgumentException("La matrícula es obligatoria");
        if (dao.existe(a.getMatricula())) dao.update(a); else dao.insert(a);
    }

    public void eliminar(String matricula) throws SQLException {
        dao.deleteByMatricula(matricula);
    }

    public Alumno buscar(String matricula) throws SQLException {
        return dao.findByMatricula(matricula);
    }

    public List<Alumno> listarBreve() throws SQLException {
        return dao.listBreve();
    }
}
