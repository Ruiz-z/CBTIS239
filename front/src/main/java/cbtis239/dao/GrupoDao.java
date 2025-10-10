package cbtis239.dao;

import cbtis239.util.DB;
import cbtis239.model.Grupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoDao {

    public List<Grupo> findAll() throws SQLException {
        String sql = """
                SELECT g.GrupoID, g.NombreGrupo, g.Capacidad, g.Especialidad_Clave,
                       e.Nombre AS Especialidad
                FROM Grupo g
                LEFT JOIN Especialidad e ON e.Clave = g.Especialidad_Clave
                ORDER BY g.NombreGrupo
                """;
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Grupo> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Grupo(
                        rs.getInt("GrupoID"),
                        rs.getString("NombreGrupo"),
                        rs.getInt("Capacidad"),
                        rs.getInt("Especialidad_Clave"),
                        rs.getString("Especialidad")
                ));
            }
            return list;
        }
    }

    public int insert(Grupo g) throws SQLException {
        String sql = "INSERT INTO Grupo (NombreGrupo, Capacidad, Especialidad_Clave) VALUES (?, ?, ?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getNombreGrupo());
            ps.setInt(2, g.getCapacidad());
            ps.setInt(3, g.getEspecialidadClave());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void update(Grupo g) throws SQLException {
        String sql = """
                UPDATE Grupo
                   SET NombreGrupo = ?, Capacidad = ?, Especialidad_Clave = ?
                 WHERE GrupoID = ?
                """;
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, g.getNombreGrupo());
            ps.setInt(2, g.getCapacidad());
            ps.setInt(3, g.getEspecialidadClave());
            ps.setInt(4, g.getGrupoId());
            ps.executeUpdate();
        }
    }

    public void deleteById(int grupoId) throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Grupo WHERE GrupoID=?")) {
            ps.setInt(1, grupoId);
            ps.executeUpdate();
        }
    }
}

