package cbtis239.bo;

import cbtis239.dao.EdoCivilDao;
import cbtis239.model.EdoCivil;

import java.sql.SQLException;
import java.util.List;

public class EdoCivilBO {
    private final EdoCivilDao dao = new EdoCivilDao();
    
    // Clase de excepción genérica para el BO
    public static class BusinessException extends Exception {
        public BusinessException(String message) { super(message); }
    }

    public List<EdoCivil> findAll() throws BusinessException {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw new BusinessException("Error de base de datos al listar: " + e.getMessage());
        }
    }

    public int agregar(EdoCivil nuevo) throws BusinessException {
        String nombre = nuevo.getNombre();
        
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new BusinessException("El nombre es obligatorio.");

        try {
            if (dao.existsByNombre(nombre)) 
                throw new BusinessException("Ya existe un estado civil con ese nombre.");

            return dao.insert(nombre);
        } catch (SQLException e) {
            throw new BusinessException("Error de base de datos al agregar: " + e.getMessage());
        }
    }

    public void modificar(EdoCivil modificado) throws BusinessException {
        int id = modificado.getIdEdoCivil();
        String nombre = modificado.getNombre();
        
        if (id <= 0) 
            throw new BusinessException("Selecciona un registro válido.");
            
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new BusinessException("El nombre es obligatorio.");

        try {
            // Validar que el nuevo nombre no pertenezca a otro registro
            if (dao.existsByNombreAndDifferentId(id, nombre))
                throw new BusinessException("Ya existe otro estado civil con ese nombre.");

            dao.update(id, nombre);
        } catch (SQLException e) {
            throw new BusinessException("Error de base de datos al modificar: " + e.getMessage());
        }
    }

    public void eliminar(int id) throws BusinessException {
        if (id <= 0) 
            throw new BusinessException("Selecciona un registro.");
            
        try {
            dao.delete(id);
        } catch (SQLException e) {
            // Manejar si el registro está en uso (llave foránea)
            if (e.getSQLState().startsWith("23")) { 
                throw new BusinessException("No se puede eliminar porque está asociado a un Aspirante o Alumno.");
            }
            throw new BusinessException("Error de base de datos al eliminar: " + e.getMessage());
        }
    }
}