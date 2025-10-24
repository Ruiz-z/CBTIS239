package cbtis239.bo;

import cbtis239.dao.DocenteMateriaDao;
import cbtis239.model.DocenteMateria;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;

import java.sql.SQLException;
import java.util.List;

public class DocenteMateriaBO {
    private final DocenteMateriaDao dao = new DocenteMateriaDao();

    public List<Opcion> listarDocentes() throws SQLException { return dao.listarDocentes(); }
    public List<OpcionStr> listarMaterias() throws SQLException { return dao.listarMaterias(); }
    public List<DocenteMateria> listarRelaciones() throws SQLException { return dao.listarRelaciones(); }

    public void asignar(int docenteId, String materiaClave) throws SQLException {
        if (docenteId <= 0) throw new IllegalArgumentException("Selecciona un docente.");
        if (materiaClave == null || materiaClave.isBlank()) throw new IllegalArgumentException("Selecciona una materia.");
        if (dao.existeRelacion(docenteId, materiaClave))
            throw new IllegalArgumentException("Ese docente ya tiene asignada esa materia.");
        dao.insertar(docenteId, materiaClave);
    }

    public void eliminar(int docenteId, String materiaClave) throws SQLException {
        if (docenteId <= 0 || materiaClave == null || materiaClave.isBlank())
            throw new IllegalArgumentException("Selecciona una relación válida.");
        dao.eliminar(docenteId, materiaClave);
    }
}
