package cbtis239.dao;

import cbtis239.model.Rol;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDao {

    public List<Rol> findAll() {
        String sql = "SELECT `RolID`,`NombreRol`,`Descripcion` FROM `rol` ORDER BY `RolID`";
        List<Rol> out = new ArrayList<>();
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql);
             var rs  = ps.executeQuery()) {
            while (rs.next()) {
                Rol r = new Rol();
                r.setIdRol(rs.getInt("RolID"));
                r.setNombre(rs.getString("NombreRol"));
                r.setDescripcion(rs.getString("Descripcion"));
                out.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles: " + e.getMessage(), e);
        }
        return out;
    }

    public Rol findById(int id) {
        String sql = "SELECT `RolID`,`NombreRol`,`Descripcion` FROM `rol` WHERE `RolID`=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = new Rol();
                    r.setIdRol(rs.getInt("RolID"));
                    r.setNombre(rs.getString("NombreRol"));
                    r.setDescripcion(rs.getString("Descripcion"));
                    return r;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar rol: " + e.getMessage(), e);
        }
        return null;
    }

    public Rol findByName(String nombre) {
        String sql = "SELECT `RolID`,`NombreRol`,`Descripcion` FROM `rol` WHERE `NombreRol`=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = new Rol();
                    r.setIdRol(rs.getInt("RolID"));
                    r.setNombre(rs.getString("NombreRol"));
                    r.setDescripcion(rs.getString("Descripcion"));
                    return r;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por nombre: " + e.getMessage(), e);
        }
        return null;
    }

    public long insert(Rol r) {
        String sql = "INSERT INTO `rol`(`NombreRol`,`Descripcion`) VALUES(?,?)";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getDescripcion());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar rol: " + e.getMessage(), e);
        }
    }

    public boolean update(Rol r) {
        String sql = "UPDATE `rol` SET `NombreRol`=?, `Descripcion`=? WHERE `RolID`=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getDescripcion());
            ps.setInt(3, r.getIdRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM `rol` WHERE `RolID`=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar rol: " + e.getMessage(), e);
        }
    }
}

