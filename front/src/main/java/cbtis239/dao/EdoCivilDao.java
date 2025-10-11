package cbtis239.dao;
import cbtis239.model.EdoCivil;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EdoCivilDao {

    public List<EdoCivil> findAll() throws SQLException {
        String sql = "SELECT idEdoCivil, Nombre FROM EdoCivil ORDER BY Nombre";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<EdoCivil> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new EdoCivil(
                        rs.getInt("idEdoCivil"),
                        rs.getString("Nombre")
                ));
            }
            return list;
        }
    }

    public int insert(String nombre) throws SQLException {
        String sql = "INSERT INTO EdoCivil (Nombre) VALUES (?)";
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
        String sql = "UPDATE EdoCivil SET Nombre=? WHERE idEdoCivil=?";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM EdoCivil WHERE idEdoCivil=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByNombre(String nombre) throws SQLException {
        String sql = "SELECT 1 FROM EdoCivil WHERE Nombre = ? LIMIT 1";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
