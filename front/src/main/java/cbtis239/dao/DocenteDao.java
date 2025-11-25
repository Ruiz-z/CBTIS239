package cbtis239.dao;

import cbtis239.model.Docente;
import cbtis239.model.Opcion;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocenteDao {

    // ======================= INSERTS / LINKS =======================

    /** Inserta Docente y regresa el ID generado (usa la conexión de la transacción). */
    public int insertDocente(Connection con, Docente d) throws SQLException {
        String sql = """
            INSERT INTO SistemaEscolar.Docente
            (CURP, Correo, NSS, Nombre, Paterno, Materno, Telefono, Celular,
             EdoCivil_idEdoCivil, Generos_idGenero)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getCurp());
            ps.setString(2, d.getCorreo());
            ps.setString(3, d.getNss());
            ps.setString(4, d.getNombre());
            ps.setString(5, d.getPaterno());
            ps.setString(6, d.getMaterno());
            ps.setString(7, d.getTelefono());
            ps.setString(8, d.getCelular());
            ps.setInt(9, d.getEdoCivilId());
            ps.setInt(10, d.getGeneroId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se obtuvo DocenteID generado.");
    }

    /** Actualiza un docente existente por DocenteID. */
    public void updateDocente(Connection con, Docente d) throws SQLException {
        String sql = """
            UPDATE SistemaEscolar.Docente
               SET CURP = ?,
                   Correo = ?,
                   NSS = ?,
                   Nombre = ?,
                   Paterno = ?,
                   Materno = ?,
                   Telefono = ?,
                   Celular = ?,
                   EdoCivil_idEdoCivil = ?,
                   Generos_idGenero = ?
             WHERE DocenteID = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getCurp());
            ps.setString(2, d.getCorreo());
            ps.setString(3, d.getNss());
            ps.setString(4, d.getNombre());
            ps.setString(5, d.getPaterno());
            ps.setString(6, d.getMaterno());
            ps.setString(7, d.getTelefono());
            ps.setString(8, d.getCelular());
            ps.setInt(9, d.getEdoCivilId());
            ps.setInt(10, d.getGeneroId());
            ps.setInt(11, d.getDocenteId());
            ps.executeUpdate();
        }
    }

    /** ¿Existe el username? */
    public boolean usuarioExiste(Connection con, String usuario) throws SQLException {
        String sql = "SELECT 1 FROM SistemaEscolar.Usuario WHERE Usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Obtiene RolID por nombre (ej. 'Docente'). */
    public int getRolId(Connection con, String nombreRol) throws SQLException {
        String sql = "SELECT RolID FROM SistemaEscolar.Rol WHERE NombreRol = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No existe el rol: " + nombreRol);
    }

    /** Inserta en Usuario. (contraseña tal cual según tu esquema) */
    public void insertUsuario(Connection con, String usuario, String pass, int rolId,
                              String nombre, String paterno, String materno) throws SQLException {
        String sql = """
            INSERT INTO SistemaEscolar.Usuario
            (Usuario, Contrasena, RolID, Nombre, Paterno, Materno)
            VALUES (?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, pass);
            ps.setInt(3, rolId);
            ps.setString(4, nombre);
            ps.setString(5, paterno);
            ps.setString(6, materno);
            ps.executeUpdate();
        }
    }

    /** Vincula Docente ↔ Usuario (DocenteUsuario). */
    public void linkDocenteUsuario(Connection con, int docenteId, String usuario) throws SQLException {
        String sql = """
            INSERT INTO SistemaEscolar.DocenteUsuario (Usuario_Usuario, Docente_DocenteID)
            VALUES (?,?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, docenteId);
            ps.executeUpdate();
        }
    }

    // ======================= ELIMINAR =======================

    /** ¿El docente tiene relaciones (materias/cursos)? */
    public boolean docenteTieneRelaciones(Connection con, int docenteId) throws SQLException {
        // De momento validamos Docente_has_Materia; si tiene filas, lo bloqueamos.
        String sql = "SELECT 1 FROM SistemaEscolar.Docente_has_Materia WHERE Docente_DocenteID = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Obtiene el nombre de usuario asociado a un docente (DocenteUsuario). */
    public String getUsuarioByDocenteId(Connection con, int docenteId) throws SQLException {
        String sql = """
            SELECT Usuario_Usuario
            FROM SistemaEscolar.DocenteUsuario
            WHERE Docente_DocenteID = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("Usuario_Usuario");
            }
        }
        return null;
    }

    /** Elimina la relación DocenteUsuario. */
    public void deleteDocenteUsuario(Connection con, int docenteId) throws SQLException {
        String sql = "DELETE FROM SistemaEscolar.DocenteUsuario WHERE Docente_DocenteID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.executeUpdate();
        }
    }

    /** Elimina el usuario (tabla Usuario). */
    public void deleteUsuario(Connection con, String usuario) throws SQLException {
        String sql = "DELETE FROM SistemaEscolar.Usuario WHERE Usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.executeUpdate();
        }
    }

    /** Elimina el docente (tabla Docente). */
    public void deleteDocente(Connection con, int docenteId) throws SQLException {
        String sql = "DELETE FROM SistemaEscolar.Docente WHERE DocenteID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.executeUpdate();
        }
    }

    // ======================= CATALOGO / LISTADOS =======================

    public List<Opcion> listarEstadosCiviles(Connection con) throws SQLException {
        String sql = "SELECT idEdoCivil, Nombre FROM SistemaEscolar.EdoCivil ORDER BY Nombre";
        List<Opcion> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Opcion(rs.getInt(1), rs.getString(2)));
            }
        }
        return out;
    }

    public List<Opcion> listarGeneros(Connection con) throws SQLException {
        String sql = "SELECT idGenero, Nombre FROM SistemaEscolar.Generos ORDER BY Nombre";
        List<Opcion> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Opcion(rs.getInt(1), rs.getString(2)));
            }
        }
        return out;
    }

    /** Busca el docente asociado a un nombre de usuario (tabla DocenteUsuario). */
    public Docente buscarDocentePorUsuario(Connection con, String usuario) throws SQLException {
        String sql = """
            SELECT d.DocenteID,
                   d.CURP, d.Correo, d.NSS,
                   d.Nombre, d.Paterno, d.Materno,
                   d.Telefono, d.Celular,
                   d.EdoCivil_idEdoCivil, d.Generos_idGenero
            FROM SistemaEscolar.Docente d
            JOIN SistemaEscolar.DocenteUsuario du
              ON du.Docente_DocenteID = d.DocenteID
            WHERE du.Usuario_Usuario = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Docente d = new Docente();
                    d.setDocenteId(rs.getInt("DocenteID"));
                    d.setCurp(rs.getString("CURP"));
                    d.setCorreo(rs.getString("Correo"));
                    d.setNss(rs.getString("NSS"));
                    d.setNombre(rs.getString("Nombre"));
                    d.setPaterno(rs.getString("Paterno"));
                    d.setMaterno(rs.getString("Materno"));
                    d.setTelefono(rs.getString("Telefono"));
                    d.setCelular(rs.getString("Celular"));
                    d.setEdoCivilId(rs.getInt("EdoCivil_idEdoCivil"));
                    d.setGeneroId(rs.getInt("Generos_idGenero"));
                    return d;
                }
            }
        }
        return null; // puede no tener vínculo aún
    }


    /** Lista de docentes con TODA la info para rellenar formulario. */
    public List<Docente> listarDocentesBasico(Connection con) throws SQLException {
        String sql = """
            SELECT DocenteID,
                   CURP, Correo, NSS,
                   Nombre, Paterno, Materno,
                   Telefono, Celular,
                   EdoCivil_idEdoCivil, Generos_idGenero
            FROM SistemaEscolar.Docente
            ORDER BY Nombre, Paterno, Materno
            """;
        List<Docente> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Docente d = new Docente();
                d.setDocenteId(rs.getInt("DocenteID"));
                d.setCurp(rs.getString("CURP"));
                d.setCorreo(rs.getString("Correo"));
                d.setNss(rs.getString("NSS"));
                d.setNombre(rs.getString("Nombre"));
                d.setPaterno(rs.getString("Paterno"));
                d.setMaterno(rs.getString("Materno"));
                d.setTelefono(rs.getString("Telefono"));
                d.setCelular(rs.getString("Celular"));
                d.setEdoCivilId(rs.getInt("EdoCivil_idEdoCivil"));
                d.setGeneroId(rs.getInt("Generos_idGenero"));
                out.add(d);
            }
        }
        return out;
    }

   // ======================= NUEVO: buscar docente por usuario =======================

    /** Busca al docente asociado a un nombre de usuario (tabla DocenteUsuario). */
    public Docente buscarPorUsuario(String usuario) throws SQLException {
        String sql = """
            SELECT d.DocenteID, d.CURP, d.Correo, d.NSS, d.Nombre, d.Paterno, d.Materno,
                   d.Telefono, d.Celular, d.EdoCivil_idEdoCivil, d.Generos_idGenero
            FROM   SistemaEscolar.Docente d
            JOIN   SistemaEscolar.DocenteUsuario du
                   ON du.Docente_DocenteID = d.DocenteID
            WHERE  du.Usuario_Usuario = ?
        """;

        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Docente d = new Docente();
                    d.setDocenteId(rs.getInt("DocenteID"));
                    d.setCurp(rs.getString("CURP"));
                    d.setCorreo(rs.getString("Correo"));
                    d.setNss(rs.getString("NSS"));
                    d.setNombre(rs.getString("Nombre"));
                    d.setPaterno(rs.getString("Paterno"));
                    d.setMaterno(rs.getString("Materno"));
                    d.setTelefono(rs.getString("Telefono"));
                    d.setCelular(rs.getString("Celular"));
                    d.setEdoCivilId(rs.getInt("EdoCivil_idEdoCivil"));
                    d.setGeneroId(rs.getInt("Generos_idGenero"));
                    return d;
                }
            }
        }
        return null;
    }

}
