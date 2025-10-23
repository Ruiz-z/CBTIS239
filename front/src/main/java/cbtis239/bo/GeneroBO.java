package cbtis239.bo;


import cbtis239.dao.GeneroDAO;
import cbtis239.model.Genero;

import java.sql.SQLException;
import java.util.List;

public class GeneroBO {
    private final GeneroDAO dao = new GeneroDAO();

    // Renombrado para consistencia con el Controller
    public List<Genero> findAll() throws SQLException { return dao.findAll(); }

    // Adaptado el método a cómo lo llama el Controller (solo con nombre)
    public int agregar(String nombre) throws Exception {
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre del género es obligatorio.");

        if (dao.existsByNombre(nombre))
            throw new IllegalArgumentException("Ya existe un género con ese nombre.");

        return dao.insert(nombre); // Devuelve el nuevo ID
    }

    public void modificar(int id, String nombre) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre del género es obligatorio.");
        
        // Nota: Tu BO no valida duplicados en modificación, mantenemos esa lógica.
        dao.update(id, nombre);
    }

    public void eliminar(int id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        dao.delete(id);
    }
}