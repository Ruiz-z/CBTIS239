package cbtis239.dao;

import cbtis239.model.Aula;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AulaDao {

    private Connection getConnection() throws SQLException {
        return DB.get(); // usa la misma config que el resto de pantallas
    }

    public List<Aula> listar() throws SQLException {
        String sql = "SELECT Clave, Capacidad FROM Aula ORDER BY Clave";
        List<Aula> list = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Aula(rs.getString("Clave"), rs.getInt("Capacidad")));
            }
        }
        return list;
    }

    public boolean existeClave(String clave) throws SQLException {
        String sql = "SELECT 1 FROM Aula WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertar(Aula a) throws SQLException {
        String sql = "INSERT INTO Aula (Clave, Capacidad) VALUES (?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getClave());
            ps.setInt(2, a.getCapacidad());
            ps.executeUpdate();
        }
    }

    public void actualizar(Aula a) throws SQLException {
        String sql = "UPDATE Aula SET Capacidad = ? WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getCapacidad());
            ps.setString(2, a.getClave());
            ps.executeUpdate();
        }
    }

    public void eliminarPorClave(String clave) throws SQLException {
        String sql = "DELETE FROM Aula WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.executeUpdate();
        }
    }
}
