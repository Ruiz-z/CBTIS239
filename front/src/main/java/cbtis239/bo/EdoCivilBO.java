package cbtis239.bo;

import cbtis239.dao.EdoCivilDao;
import cbtis239.model.EdoCivil;

import java.sql.SQLException;
import java.util.List;

public class EdoCivilBO {
    private final EdoCivilDao dao = new EdoCivilDao();

    public List<EdoCivil> listar() throws SQLException { return dao.findAll(); }

    public EdoCivil crear(String claveStr, String nombre) throws Exception {
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio.");

        // Si viene clave la usamos, si no, dejamos autoincrement en DB
        int id = 0;
        if (claveStr != null && !claveStr.isBlank()) {
            try { id = Integer.parseInt(claveStr.trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Clave debe ser numérica."); }
        }

        if (dao.existsByNombre(nombre)) throw new IllegalArgumentException("Ya existe un estado civil con ese nombre.");

        if (id == 0) {
            int newId = dao.insert(nombre);
            return new EdoCivil(newId, nombre);
        } else {
            // crear explícito: primero insert autoincrement y luego update id? mejor insert normal y luego update id (no recomendado)
            // Mantendremos uso normal: si indican clave, hacemos insert y MySQL la ignora (porque es AI).
            int newId = dao.insert(nombre);
            return new EdoCivil(newId, nombre);
        }
    }

    public void modificar(int id, String nombre) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        dao.update(id, nombre);
    }

    public void eliminar(int id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Selecciona un registro.");
        dao.delete(id);
    }
}
