package cbtis239.bo;

import cbtis239.dao.DocenteDao;
import cbtis239.model.Docente;
import cbtis239.model.Opcion;

import java.sql.SQLException;
import java.util.List;

public class DocenteBO {
    private final DocenteDao dao = new DocenteDao();

    // Combos
    public List<Opcion> listarEdoCivil() throws SQLException { return dao.listarEdoCivil(); }
    public List<Opcion> listarGeneros() throws SQLException { return dao.listarGeneros(); }

    // Tabla
    public List<Docente> listar() throws SQLException { return dao.listar(); }

    public void agregar(Docente d) throws SQLException {
        if (d.getCurp() == null || d.getCurp().isBlank())
            throw new IllegalArgumentException("La CURP es obligatoria.");
        if (d.getCorreo() == null || d.getCorreo().isBlank())
            throw new IllegalArgumentException("El correo es obligatorio.");
        if (d.getNombre() == null || d.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (d.getIdEdoCivil() <= 0) throw new IllegalArgumentException("Selecciona Estado Civil.");
        if (d.getIdGenero() <= 0)   throw new IllegalArgumentException("Selecciona Género.");
        if (dao.existeCurp(d.getCurp()))
            throw new IllegalArgumentException("Ya existe un docente con esa CURP.");
        dao.insertar(d);
    }

    public void modificar(Docente d) throws SQLException {
        if (d.getDocenteId() <= 0) throw new IllegalArgumentException("Selecciona un docente.");
        if (d.getNombre() == null || d.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (d.getIdEdoCivil() <= 0 || d.getIdGenero() <= 0)
            throw new IllegalArgumentException("Selecciona Estado Civil y Género.");
        dao.actualizar(d);
    }

    public void eliminar(int docenteId) throws SQLException {
        if (docenteId <= 0) throw new IllegalArgumentException("Selecciona un docente.");
        dao.eliminar(docenteId);
    }
}
