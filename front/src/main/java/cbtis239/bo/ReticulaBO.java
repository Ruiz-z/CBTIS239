package cbtis239.bo;

import cbtis239.dao.ReticulaDao;
import cbtis239.model.MateriaSelectable;
import cbtis239.model.Opcion;
import cbtis239.model.ReticulaAsignadaRow;

import java.sql.SQLException;
import java.util.List;

/** BO unificado: catálogos + operaciones masivas de retícula. */
public class ReticulaBO {

    private final ReticulaDao dao = new ReticulaDao();

    // Catálogo
    public List<Opcion> listarEspecialidades() throws SQLException {
        return dao.listarEspecialidades();
    }

    // Listas
    public List<MateriaSelectable> listarDisponibles(int espClave) throws SQLException {
        return dao.listarDisponibles(espClave);
    }

    public List<ReticulaAsignadaRow> listarAsignadas(int espClave, Integer semestre) throws SQLException {
        return dao.listarAsignadas(espClave, semestre);
    }

    // Masivas
    public void insertarMuchas(int espClave, List<String> clavesMateria, int semestre) throws SQLException {
        dao.insertarMuchas(espClave, clavesMateria, semestre);
    }

    public void eliminarMuchas(int espClave, List<String> clavesMateria) throws SQLException {
        dao.eliminarMuchas(espClave, clavesMateria);
    }
}
