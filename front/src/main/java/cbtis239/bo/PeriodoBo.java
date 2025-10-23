package cbtis239.bo;

import cbtis239.dao.PeriodoDao;
import cbtis239.model.Periodo;
import java.time.LocalDate;
import java.util.List;

// Asumiendo la existencia de BusinessException para manejo de errores de negocio

public class PeriodoBo {

    private final PeriodoDao dao = new PeriodoDao();

    public long create(Periodo p) throws BusinessException {
        validate(p);

        // Validar unicidad del nombre
        var existente = dao.findByName(p.getNombre());
        if (existente != null) {
            throw new BusinessException("Ya existe un periodo con ese nombre: " + p.getNombre());
        }
        
        // Ejecutar inserción
        try {
            long id = dao.insert(p);
            if (id <= 0) throw new BusinessException("No se pudo crear el periodo. Intente de nuevo.");
            return id;
        } catch (RuntimeException e) {
            throw new BusinessException("Error de base de datos al crear periodo: " + e.getMessage());
        }
    }

    public void update(Periodo p) throws BusinessException {
        if (p.getIdPeriodo() <= 0) throw new BusinessException("ID de periodo inválido para actualizar.");
        validate(p);
        
        // Validar que el nombre no pertenezca a otro ID
        var existente = dao.findByName(p.getNombre());
        if (existente != null && existente.getIdPeriodo() != p.getIdPeriodo()) {
            throw new BusinessException("Ese nombre de periodo ya está tomado por otro registro.");
        }
        
        try {
            if (!dao.update(p)) throw new BusinessException("No se pudo actualizar el periodo.");
        } catch (RuntimeException e) {
             throw new BusinessException("Error de base de datos al actualizar periodo: " + e.getMessage());
        }
    }

    public void delete(int idPeriodo) throws BusinessException {
        if (idPeriodo <= 0) throw new BusinessException("ID de periodo inválido.");
        
        try {
            if (!dao.delete(idPeriodo)) throw new BusinessException("No se pudo eliminar el periodo.");
        } catch (RuntimeException e) {
             throw new BusinessException("Error de base de datos al eliminar periodo: " + e.getMessage());
        }
    }
    
    public List<Periodo> findAll() { return dao.findAll(); }

    // --------------- Validación ----------------
    private void validate(Periodo p) throws BusinessException {
        if (p == null) throw new BusinessException("Periodo nulo.");
        
        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            throw new BusinessException("El nombre del periodo no puede ir vacío.");
        
        if (p.getNombre().length() > 40)
            throw new BusinessException("El nombre del periodo excede 40 caracteres.");

        if (p.getFechaInicio() == null)
            throw new BusinessException("La fecha de inicio es obligatoria.");

        if (p.getFechaFin() == null)
            throw new BusinessException("La fecha de fin es obligatoria.");
            
        if (p.getFechaFin().isBefore(p.getFechaInicio()))
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
    }
}
