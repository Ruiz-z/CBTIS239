package cbtis239.bo;

import cbtis239.dao.UsuarioDao;
import cbtis239.model.Usuario;
import java.util.List;

public class UsuarioBO {

    private final UsuarioDao dao = new UsuarioDao();

    /* Login (booleano simple, para compatibilidad) */
    public boolean login(String usuario, String contrasena) {
        if (isBlank(usuario) || isBlank(contrasena))
            throw new BusinessException("Debe ingresar usuario y contraseña.");
        return dao.existsByUserAndPlainPassword(usuario.trim(), contrasena.trim());
    }

    /* ✅ Nuevo método: obtiene el usuario con nombre de rol (ignore case listo para comparar) */
    public Usuario validarLogin(String usuario, String contrasena) {
        if (isBlank(usuario) || isBlank(contrasena))
            throw new BusinessException("Debe ingresar usuario y contraseña.");

        Usuario u = dao.findByUserAndPlainPassword(usuario.trim(), contrasena.trim());

        if (u == null)
            throw new BusinessException("Usuario o contraseña incorrectos.");

        // Normaliza el nombre del rol
        if (u.getRolNombre() == null || u.getRolNombre().isBlank()) {
            u.setRolNombre("Desconocido");
        } else {
            u.setRolNombre(u.getRolNombre().trim());
        }

        return u;
    }

    /* Crear */
    public void create(Usuario u) {
        validate(u, true);
        if (dao.existsByUsuario(u.getUsuario()))
            throw new BusinessException("El usuario '" + u.getUsuario() + "' ya existe.");
        dao.insert(u);
    }

    /* Actualizar */
    public void update(Usuario u) {
        validate(u, false);
        if (!dao.existsByUsuario(u.getUsuario()))
            throw new BusinessException("El usuario '" + u.getUsuario() + "' no existe.");
        dao.update(u);
    }

    /* Eliminar */
    public void delete(String usuario) {
        if (isBlank(usuario)) throw new BusinessException("Usuario inválido.");
        dao.delete(usuario.trim());
    }

    /* Listado para tabla (con nombre de rol) */
    public List<Usuario> listAllWithRol() {
        return dao.findAllWithRol();
    }

    // ---------- Validaciones ----------
    private void validate(Usuario u, boolean creating) {
        if (u == null) throw new BusinessException("Usuario nulo.");
        if (isBlank(u.getUsuario())) throw new BusinessException("El campo 'Usuario' es obligatorio.");
        if (isBlank(u.getContrasena())) throw new BusinessException("La contraseña es obligatoria.");
        if (u.getContrasena().length() < 3)
            throw new BusinessException("La contraseña debe tener al menos 3 caracteres.");
        if (isBlank(u.getNombre())) throw new BusinessException("El nombre es obligatorio.");
        if (u.getRolId() <= 0) throw new BusinessException("Debe seleccionar un rol válido.");
    }

    /* Método auxiliar para verificar cadenas vacías */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

