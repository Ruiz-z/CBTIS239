package cbtis239.bo;

import cbtis239.dao.AulaDao;
import cbtis239.model.Aula;

import java.sql.SQLException;
import java.util.List;

public class AulaBO {
    private final AulaDao dao = new AulaDao();

    public List<Aula> listar() throws SQLException {
        return dao.listar();
    }

    public void agregar(Aula a) throws SQLException {
        if (a.getClave() == null || a.getClave().isBlank())
            throw new IllegalArgumentException("La clave del aula es obligatoria.");
        if (a.getCapacidad() <= 0)
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0.");
        if (dao.existeClave(a.getClave()))
            throw new IllegalArgumentException("Ya existe un aula con esa clave.");
        dao.insertar(a);
    }

    public void modificar(Aula a) throws SQLException {
        if (a.getCapacidad() <= 0)
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0.");
        dao.actualizar(a);
    }

    public void eliminar(String clave) throws SQLException {
        if (clave == null || clave.isBlank())
            throw new IllegalArgumentException("Selecciona una clave válida.");
        dao.eliminarPorClave(clave);
    }
}
