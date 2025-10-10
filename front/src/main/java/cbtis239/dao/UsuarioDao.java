package cbtis239.dao;

import cbtis239.model.Usuario;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    /** Login ya existente (por compatibilidad) */
    public boolean existsByUserAndPlainPassword(String usuario, String contrasena) {
        final String sql = "SELECT 1 FROM `usuario` WHERE `Usuario`=? AND `Contrasena`=? LIMIT 1";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar usuario: " + e.getMessage(), e);
        }
    }

    /** ¿Existe el usuario (por PK)? */
    public boolean existsByUsuario(String usuario) {
        final String sql = "SELECT 1 FROM `usuario` WHERE `Usuario`=? LIMIT 1";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia: " + e.getMessage(), e);
        }
    }

    /** Insertar nuevo usuario */
    public void insert(Usuario u) {
        final String sql = "INSERT INTO `usuario`(`Usuario`,`Contrasena`,`RolID`,`Nombre`,`Paterno`,`Materno`) " +
                "VALUES(?,?,?,?,?,?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getContrasena());
            ps.setInt(3, u.getRolId());
            ps.setString(4, u.getNombre());
            ps.setString(5, u.getPaterno());
            ps.setString(6, u.getMaterno());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar usuario: " + e.getMessage(), e);
        }
    }

    /** Actualizar (por PK Usuario) */
    public boolean update(Usuario u) {
        final String sql = "UPDATE `usuario` SET `Contrasena`=?,`RolID`=?,`Nombre`=?,`Paterno`=?,`Materno`=? " +
                "WHERE `Usuario`=?";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getContrasena());
            ps.setInt(2, u.getRolId());
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getPaterno());
            ps.setString(5, u.getMaterno());
            ps.setString(6, u.getUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    /** Eliminar por PK */
    public boolean delete(String usuario) {
        final String sql = "DELETE FROM `usuario` WHERE `Usuario`=?";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    /** Lista de usuarios + nombre del rol (para mostrar en tabla) */
    public List<Usuario> findAllWithRol() {
        final String sql =
                "SELECT u.`Usuario`,u.`Contrasena`,u.`RolID`,u.`Nombre`,u.`Paterno`,u.`Materno`, r.`NombreRol` " +
                        "FROM `usuario` u LEFT JOIN `rol` r ON r.`RolID` = u.`RolID` " +
                        "ORDER BY u.`Usuario`";
        List<Usuario> out = new ArrayList<>();
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setUsuario(rs.getString("Usuario"));
                u.setContrasena(rs.getString("Contrasena"));
                u.setRolId(rs.getInt("RolID"));
                u.setNombre(rs.getString("Nombre"));
                u.setPaterno(rs.getString("Paterno"));
                u.setMaterno(rs.getString("Materno"));
                u.setRolNombre(rs.getString("NombreRol")); // para la tabla
                out.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
        return out;
    }
}

