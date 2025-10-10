package cbtis239.bo;

import cbtis239.dao.EspecialidadDao;
import cbtis239.model.Especialidad;

import java.sql.SQLException;
import java.util.List;

public class EspecialidadBO {
    private final EspecialidadDao dao = new EspecialidadDao();

    public List<Especialidad> listar() throws SQLException { return dao.findAll(); }

    public Especialidad crear(String claveStr, String nombre) throws Exception {
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (dao.existsByNombre(nombre))
            throw new IllegalStateException("Ya existe una especialidad con ese nombre.");

        // Si clave se deja vacía -> auto_increment
        if (claveStr == null || claveStr.isBlank()) {
            Especialidad e = new Especialidad(0, nombre);
            dao.insertAuto(e);
            return e;
        } else {
            int clave;
            try { clave = Integer.parseInt(claveStr.trim()); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Clave debe ser numérica."); }
            Especialidad e = new Especialidad(clave, nombre);
            dao.insert(e);
            return e;
        }
    }

    public void modificar(int clave, String nuevoNombre) throws Exception {
        if (nuevoNombre == null || (nuevoNombre = nuevoNombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        dao.updateNombre(clave, nuevoNombre);
    }

    public void eliminar(int clave) throws Exception { dao.deleteByClave(clave); }
}
