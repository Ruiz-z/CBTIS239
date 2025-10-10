package cbtis239.bo;

import cbtis239.dao.RolDao;
import cbtis239.model.Rol;

import java.util.List;

public class RolBO {

    private final RolDao dao = new RolDao();

    public long create(Rol r) {
        validate(r, true);
        var existente = dao.findByName(r.getNombre());
        if (existente != null) {
            throw new BusinessException("Ya existe un rol con ese nombre: " + r.getNombre());
        }
        long id = dao.insert(r);
        if (id <= 0) throw new BusinessException("No se pudo crear el rol.");
        return id;
    }

    public void update(Rol r) {
        if (r.getIdRol() <= 0) throw new BusinessException("ID de rol inválido.");
        validate(r, false);
        var existente = dao.findByName(r.getNombre());
        if (existente != null && existente.getIdRol() != r.getIdRol()) {
            throw new BusinessException("Ese nombre de rol ya está tomado por otro registro.");
        }
        if (!dao.update(r)) throw new BusinessException("No se pudo actualizar el rol.");
    }

    public void delete(int idRol) {
        if (idRol <= 0) throw new BusinessException("ID de rol inválido.");
        if (!dao.delete(idRol)) throw new BusinessException("No se pudo eliminar el rol.");
    }

    public Rol findById(int idRol) { return dao.findById(idRol); }

    public Rol findByName(String name) {
        if (name == null || name.isBlank()) return null;
        return dao.findByName(name.trim());
    }

    public List<Rol> findAll() { return dao.findAll(); }

    // --------------- helpers ----------------
    private void validate(Rol r, boolean isCreate) {
        if (r == null) throw new BusinessException("Rol nulo.");
        if (r.getNombre() == null || r.getNombre().trim().isEmpty())
            throw new BusinessException("El nombre del rol no puede ir vacío.");
        if (r.getNombre().length() > 50)
            throw new BusinessException("El nombre del rol excede 50 caracteres.");
        if (r.getDescripcion() != null && r.getDescripcion().length() > 500)
            throw new BusinessException("La descripción excede 500 caracteres.");
    }
}
