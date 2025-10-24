package cbtis239.dao;

import cbtis239.model.Docente;
import cbtis239.model.Opcion;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocenteDao {

    private Connection getConnection() throws SQLException { return DB.get(); }

    // ===== Combos =====
    public List<Opcion> listarEdoCivil() throws SQLException {
        String sql = "SELECT idEdoCivil, Nombre FROM EdoCivil ORDER BY Nombre";
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Opcion(rs.getInt(1), rs.getString(2)));
        }
        return out;
    }

    public List<Opcion> listarGeneros() throws SQLException {
        String sql = "SELECT idGenero, Nombre FROM Generos ORDER BY Nombre";
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Opcion(rs.getInt(1), rs.getString(2)));
        }
        return out;
    }

    // ===== CRUD Docente =====
    public List<Docente> listar() throws SQLException {
        String sql = """
            SELECT d.DocenteID, d.CURP, d.Correo, d.NSS, d.Nombre, d.Paterno, d.Materno,
                   d.Telefono, d.Celular, d.EdoCivil_idEdoCivil, d.Generos_idGenero,
                   g.Nombre AS GeneroNombre
            FROM Docente d
            JOIN Generos g ON g.idGenero = d.Generos_idGenero
            ORDER BY d.Nombre, d.Paterno, d.Materno
            """;
        List<Docente> list = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Docente(
                        rs.getInt("DocenteID"),
                        rs.getString("CURP"),
                        rs.getString("Correo"),
                        rs.getString("NSS"),
                        rs.getString("Nombre"),
                        rs.getString("Paterno"),
                        rs.getString("Materno"),
                        rs.getString("Telefono"),
                        rs.getString("Celular"),
                        rs.getInt("EdoCivil_idEdoCivil"),
                        rs.getInt("Generos_idGenero"),
                        rs.getString("GeneroNombre")
                ));
            }
        }
        return list;
    }

    public void insertar(Docente d) throws SQLException {
        String sql = """
            INSERT INTO Docente
              (CURP, Correo, NSS, Nombre, Paterno, Materno, Telefono, Celular,
               EdoCivil_idEdoCivil, Generos_idGenero)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, d.getCurp());
            ps.setString(2, d.getCorreo());
            ps.setString(3, d.getNss());
            ps.setString(4, d.getNombre());
            ps.setString(5, d.getPaterno());
            ps.setString(6, d.getMaterno());
            ps.setString(7, d.getTelefono());
            ps.setString(8, d.getCelular());
            ps.setInt(9, d.getIdEdoCivil());
            ps.setInt(10, d.getIdGenero());
            ps.executeUpdate();
        }
    }

    public void actualizar(Docente d) throws SQLException {
        String sql = """
            UPDATE Docente SET
              CURP=?, Correo=?, NSS=?, Nombre=?, Paterno=?, Materno=?,
              Telefono=?, Celular=?, EdoCivil_idEdoCivil=?, Generos_idGenero=?
            WHERE DocenteID=?
            """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, d.getCurp());
            ps.setString(2, d.getCorreo());
            ps.setString(3, d.getNss());
            ps.setString(4, d.getNombre());
            ps.setString(5, d.getPaterno());
            ps.setString(6, d.getMaterno());
            ps.setString(7, d.getTelefono());
            ps.setString(8, d.getCelular());
            ps.setInt(9, d.getIdEdoCivil());
            ps.setInt(10, d.getIdGenero());
            ps.setInt(11, d.getDocenteId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int docenteId) throws SQLException {
        String sql = "DELETE FROM Docente WHERE DocenteID=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.executeUpdate();
        }
    }

    // Checar duplicados (opcional)
    public boolean existeCurp(String curp) throws SQLException {
        String sql = "SELECT 1 FROM Docente WHERE CURP=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, curp);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
