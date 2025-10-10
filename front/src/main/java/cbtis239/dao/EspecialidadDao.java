package cbtis239.dao;

import cbtis239.util.DB;
import cbtis239.model.Especialidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadDao {

    public List<Especialidad> findAll() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM Especialidad ORDER BY Nombre";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Especialidad> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Especialidad(
                        rs.getInt("Clave"),
                        rs.getString("Nombre")
                ));
            }
            return list;
        }
    }

    public boolean existsByNombre(String nombre) throws SQLException {
        String sql = "SELECT 1 FROM Especialidad WHERE LOWER(Nombre)=LOWER(?) LIMIT 1";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Inserta con clave manual (si NO es auto_increment). */
    public void insert(Especialidad e) throws SQLException {
        String sql = "INSERT INTO Especialidad(Clave, Nombre) VALUES(?, ?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getClave());
            ps.setString(2, e.getNombre());
            ps.executeUpdate();
        }
    }

    /** Inserta con clave auto_increment (si lo es). */
    public void insertAuto(Especialidad e) throws SQLException {
        String sql = "INSERT INTO Especialidad(Nombre) VALUES(?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setClave(keys.getInt(1));
            }
        }
    }

    public void updateNombre(int clave, String nuevoNombre) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Especialidad SET Nombre=? WHERE Clave=?")) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, clave);
            ps.executeUpdate();
        }
    }

    public void deleteByClave(int clave) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM Especialidad WHERE Clave=?")) {
            ps.setInt(1, clave);
            ps.executeUpdate();
        }
    }
}
