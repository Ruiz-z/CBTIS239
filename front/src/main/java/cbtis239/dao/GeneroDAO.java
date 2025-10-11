package cbtis239.dao;

import cbtis239.model.Genero;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GeneroDAO {

    public List<Genero> findAll() throws SQLException {
        String sql = "SELECT idGenero, Nombre FROM generos ORDER BY Nombre";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Genero> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Genero(rs.getInt("idGenero"), rs.getString("Nombre")));
            }
            return out;
        }
    }

    public int insert(String nombre) throws SQLException {
        String sql = "INSERT INTO generos (Nombre) VALUES (?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void update(int id, String nombre) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE generos SET Nombre=? WHERE idGenero=?")) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM generos WHERE idGenero=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByNombre(String nombre) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM generos WHERE Nombre=? LIMIT 1")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
