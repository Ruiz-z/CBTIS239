package cbtis239.bo;

import cbtis239.dao.ReticulaDao;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import cbtis239.model.Reticula;

import java.sql.SQLException;
import java.util.List;

public class ReticulaBO {
    private final ReticulaDao dao = new ReticulaDao();

    public List<Opcion> listarEspecialidades() throws SQLException { return dao.listarEspecialidades(); }
    public List<OpcionStr> listarMaterias() throws SQLException { return dao.listarMaterias(); }

    public List<Reticula> listarTodo() throws SQLException { return dao.listarTodo(); }
    public List<Reticula> listarPorEspecialidad(int espClave) throws SQLException { return dao.listarPorEspecialidad(espClave); }
    public List<Reticula> listarPorEspecialidadYSemestre(int espClave, int semestre) throws SQLException {
        return dao.listarPorEspecialidadYSemestre(espClave, semestre);
    }

    public void asignar(int espClave, String matClave, int semestre) throws SQLException {
        // Como tu PK es (Especialidad_Clave, Materia_Clave), solo puede existir 1 semestre por relación
        if (dao.existeRelacion(espClave, matClave))
            throw new SQLException("La especialidad ya tiene asignada esa materia (relación única).");
        dao.insertar(espClave, matClave, semestre);
    }

    public void eliminar(int espClave, String matClave) throws SQLException {
        dao.eliminar(espClave, matClave);
    }
}
