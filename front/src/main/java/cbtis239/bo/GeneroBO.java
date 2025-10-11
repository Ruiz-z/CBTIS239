package cbtis239.bo;


import cbtis239.dao.GeneroDAO;
import cbtis239.model.Genero;

import java.sql.SQLException;
import java.util.List;

public class GeneroBO {
    private final GeneroDAO dao = new GeneroDAO();

    public List<Genero> listar() throws SQLException { return dao.findAll(); }

    public Genero crear(String idStr, String nombre) throws Exception {
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre del género es obligatorio.");

        if (dao.existsByNombre(nombre))
            throw new IllegalArgumentException("Ya existe un género con ese nombre.");

        int newId = dao.insert(nombre); // PK autoincrement
        return new Genero(newId, nombre);
    }

    public void modificar(int id, String nombre) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre del género es obligatorio.");
        dao.update(id, nombre);
    }

    public void eliminar(int id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        dao.delete(id);
    }
}
