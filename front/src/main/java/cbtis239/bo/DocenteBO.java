package cbtis239.bo;

import cbtis239.dao.DocenteDao;
import cbtis239.model.Docente;
import cbtis239.model.Opcion;
import cbtis239.util.DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DocenteBO {

    private final DocenteDao dao = new DocenteDao();

    // -------------------- Crear Docente + Usuario + Link (transacción) --------------------
    public int crearDocenteConUsuario(Docente d, String usuario, String pass, String pass2) throws Exception {
        if (usuario == null || usuario.isBlank())
            throw new BusinessException("Debes capturar el usuario.");
        if (pass == null || pass.isBlank())
            throw new BusinessException("Debes capturar la contraseña.");
        if (!pass.equals(pass2))
            throw new BusinessException("Las contraseñas no coinciden.");
        if (pass.length() < 6)
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres.");

        validarDocenteBasico(d);

        try (Connection con = DB.get()) {
            con.setAutoCommit(false);
            try {
                // usuario único
                if (dao.usuarioExiste(con, usuario)) {
                    throw new BusinessException("El usuario '" + usuario + "' ya existe.");
                }

                // 1) docente
                int docenteId = dao.insertDocente(con, d);

                // 2) rol docente
                int rolDocente = dao.getRolId(con, "Docente");

                // 3) usuario
                dao.insertUsuario(con, usuario, pass, rolDocente,
                        d.getNombre(), d.getPaterno(), d.getMaterno());

                // 4) link
                dao.linkDocenteUsuario(con, docenteId, usuario);

                con.commit();
                return docenteId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // -------------------- Actualizar docente (sin tocar usuario) --------------------
    public void actualizarDocente(Docente d) {
        if (d == null) throw new BusinessException("Docente nulo.");
        if (d.getDocenteId() <= 0)
            throw new BusinessException("Selecciona un docente de la tabla.");

        validarDocenteBasico(d);

        try (Connection con = DB.get()) {
            dao.updateDocente(con, d);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el docente: " + e.getMessage(), e);
        }
    }

    // -------------------- Eliminar docente + usuario + vínculo --------------------
    public void eliminarDocente(int docenteId) {
        if (docenteId <= 0)
            throw new BusinessException("Selecciona un docente válido.");

        try (Connection con = DB.get()) {
            con.setAutoCommit(false);
            try {
                // ¿Tiene materias/cursos?
                if (dao.docenteTieneRelaciones(con, docenteId)) {
                    throw new BusinessException(
                            "No se puede eliminar el docente porque tiene materias o cursos asignados.\n" +
                            "Primero elimina o reasigna sus materias/cursos."
                    );
                }

                // Usuario vinculado (DocenteUsuario)
                String usuario = dao.getUsuarioByDocenteId(con, docenteId);

                if (usuario != null) {
                    dao.deleteDocenteUsuario(con, docenteId);
                    dao.deleteUsuario(con, usuario);
                }

                dao.deleteDocente(con, docenteId);

                con.commit();
            } catch (BusinessException be) {
                con.rollback();
                throw be;
            } catch (Exception ex) {
                con.rollback();
                throw new RuntimeException("Error al eliminar el docente: " + ex.getMessage(), ex);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión al eliminar docente: " + e.getMessage(), e);
        }
    }

    // -------------------- Validaciones comunes --------------------
    private void validarDocenteBasico(Docente d) {
        if (isBlank(d.getCurp()))
            throw new BusinessException("La CURP es obligatoria.");
        if (d.getCurp().length() != 18)
            throw new BusinessException("La CURP debe tener 18 caracteres.");

        if (isBlank(d.getCorreo()))
            throw new BusinessException("El correo es obligatorio.");

        if (isBlank(d.getNss()))
            throw new BusinessException("El NSS es obligatorio.");

        if (isBlank(d.getNombre()))
            throw new BusinessException("El nombre es obligatorio.");
        if (isBlank(d.getPaterno()))
            throw new BusinessException("El apellido paterno es obligatorio.");
        if (isBlank(d.getMaterno()))
            throw new BusinessException("El apellido materno es obligatorio.");

        if (isBlank(d.getTelefono()))
            throw new BusinessException("El teléfono es obligatorio.");
        if (isBlank(d.getCelular()))
            throw new BusinessException("El celular es obligatorio.");

        if (d.getEdoCivilId() <= 0)
            throw new BusinessException("Debes seleccionar estado civil.");
        if (d.getGeneroId() <= 0)
            throw new BusinessException("Debes seleccionar género.");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // -------------------- Catálogos / listados para UI --------------------
    public List<Opcion> listarEstadosCiviles() throws SQLException {
        try (Connection con = DB.get()) {
            return dao.listarEstadosCiviles(con);
        }
    }

    public List<Opcion> listarGeneros() throws SQLException {
        try (Connection con = DB.get()) {
            return dao.listarGeneros(con);
        }
    }

    public List<Docente> listarDocentesBasico() throws SQLException {
        try (Connection con = DB.get()) {
            return dao.listarDocentesBasico(con);
        }
    }

        /** Obtiene el docente asociado a un usuario (por nombre de usuario). */
    public Docente buscarDocentePorUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new BusinessException("Usuario inválido para buscar docente.");
        }

        try (Connection con = DB.get()) {
            return dao.buscarDocentePorUsuario(con, usuario.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar docente por usuario: " + e.getMessage(), e);
        }
    }


    // -------------------- NUEVO: obtener docente por usuario --------------------
    public Docente obtenerDocentePorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.isBlank())
            throw new BusinessException("Usuario para búsqueda de docente inválido.");
        return dao.buscarPorUsuario(usuario.trim());
    }

}
