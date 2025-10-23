package cbtis239.bo;

import cbtis239.dao.MateriaDao;
import cbtis239.model.Materia;

import java.sql.SQLException;
import java.util.List;

public class MateriaBO {
    private final MateriaDao dao = new MateriaDao();

    public List<Materia> listar() throws SQLException { return dao.listar(); }

    public void agregar(Materia m) throws SQLException {
        if (m.getClave() == null || m.getClave().isBlank())
            throw new IllegalArgumentException("La clave es obligatoria.");
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (dao.existeClave(m.getClave()))
            throw new IllegalArgumentException("Ya existe una materia con esa clave.");
        if (m.getCreditos() < 0)
            throw new IllegalArgumentException("Los créditos no pueden ser negativos.");
        dao.insertar(m);
    }

    public void modificar(Materia m) throws SQLException {
        if (m.getNombre() == null || m.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (m.getCreditos() < 0)
            throw new IllegalArgumentException("Los créditos no pueden ser negativos.");
        dao.actualizar(m);
    }

    public void eliminar(String clave) throws SQLException {
        if (clave == null || clave.isBlank())
            throw new IllegalArgumentException("Selecciona una clave válida.");
        dao.eliminarPorClave(clave);
    }
}
