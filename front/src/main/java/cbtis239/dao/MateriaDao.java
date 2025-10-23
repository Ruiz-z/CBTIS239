package cbtis239.dao;

import cbtis239.model.Materia;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaDao {

    private Connection getConnection() throws SQLException { return DB.get(); }

    public List<Materia> listar() throws SQLException {
        String sql = "SELECT Clave, Nombre, Creditos FROM Materia ORDER BY Clave";
        List<Materia> list = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Materia(
                        rs.getString("Clave"),
                        rs.getString("Nombre"),
                        (Integer) rs.getObject("Creditos") == null ? 0 : rs.getInt("Creditos")
                ));
            }
        }
        return list;
    }

    public boolean existeClave(String clave) throws SQLException {
        String sql = "SELECT 1 FROM Materia WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertar(Materia m) throws SQLException {
        String sql = "INSERT INTO Materia (Clave, Nombre, Creditos) VALUES (?, ?, ?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, m.getClave());
            ps.setString(2, m.getNombre());
            if (m.getCreditos() == 0) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, m.getCreditos());
            ps.executeUpdate();
        }
    }

    public void actualizar(Materia m) throws SQLException {
        String sql = "UPDATE Materia SET Nombre = ?, Creditos = ? WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            if (m.getCreditos() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, m.getCreditos());
            ps.setString(3, m.getClave());
            ps.executeUpdate();
        }
    }

    public void eliminarPorClave(String clave) throws SQLException {
        String sql = "DELETE FROM Materia WHERE Clave = ?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.executeUpdate();
        }
    }
}
