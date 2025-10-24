package cbtis239.dao;

import cbtis239.model.DocenteMateria;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocenteMateriaDao {

    private Connection getConnection() throws SQLException { return DB.get(); }

    // ===== Combos =====
    public List<Opcion> listarDocentes() throws SQLException {
        String sql = "SELECT DocenteID, CONCAT(Nombre,' ',IFNULL(Paterno,''),' ',IFNULL(Materno,'')) AS Nom " +
                     "FROM Docente ORDER BY Nombre, Paterno, Materno";
        List<Opcion> out = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Opcion(rs.getInt(1), rs.getString(2).trim().replaceAll(" +"," ")));
        }
        return out;
    }

    public List<OpcionStr> listarMaterias() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM Materia ORDER BY Nombre";
        List<OpcionStr> out = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new OpcionStr(rs.getString(1), rs.getString(2)));
        }
        return out;
    }

    // ===== Tabla (JOIN de relación) =====
    public List<DocenteMateria> listarRelaciones() throws SQLException {
        String sql = """
            SELECT d.DocenteID,
                   CONCAT(d.Nombre,' ',IFNULL(d.Paterno,''),' ',IFNULL(d.Materno,'')) AS Docente,
                   m.Clave, m.Nombre AS Materia
            FROM Docente_has_Materia dm
            JOIN Docente d ON d.DocenteID = dm.Docente_DocenteID
            JOIN Materia m ON m.Clave = dm.Materia_Clave
            ORDER BY Docente, Materia
            """;
        List<DocenteMateria> out = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new DocenteMateria(
                        rs.getInt("DocenteID"),
                        rs.getString("Docente").trim().replaceAll(" +"," "),
                        rs.getString("Clave"),
                        rs.getString("Materia")
                ));
            }
        }
        return out;
    }

    public boolean existeRelacion(int docenteId, String materiaClave) throws SQLException {
        String sql = "SELECT 1 FROM Docente_has_Materia WHERE Docente_DocenteID=? AND Materia_Clave=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.setString(2, materiaClave);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insertar(int docenteId, String materiaClave) throws SQLException {
        String sql = "INSERT INTO Docente_has_Materia (Docente_DocenteID, Materia_Clave) VALUES (?,?)";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.setString(2, materiaClave);
            ps.executeUpdate();
        }
    }

    public void eliminar(int docenteId, String materiaClave) throws SQLException {
        String sql = "DELETE FROM Docente_has_Materia WHERE Docente_DocenteID=? AND Materia_Clave=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, docenteId);
            ps.setString(2, materiaClave);
            ps.executeUpdate();
        }
    }
}
