package cbtis239.dao;
import cbtis239.model.Periodo;
import cbtis239.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PeriodoDao {

    public List<Periodo> findAll() throws SQLException {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin FROM periodo ORDER BY idPeriodo DESC";
        List<Periodo> list = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Periodo p = new Periodo();
                p.setIdPeriodo(rs.getInt("idPeriodo"));
                p.setNombre(rs.getString("Nombre"));
                p.setInicio(rs.getDate("Inicio").toLocalDate());
                p.setFin(rs.getDate("Fin").toLocalDate());
                list.add(p);
            }
        }
        return list;
    }

    public Periodo findById(int id) throws SQLException {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin FROM periodo WHERE idPeriodo=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Periodo(
                            rs.getInt("idPeriodo"),
                            rs.getString("Nombre"),
                            rs.getDate("Inicio").toLocalDate(),
                            rs.getDate("Fin").toLocalDate()
                    );
                }
                return null;
            }
        }
    }

    public Periodo findByNombre(String nombre) throws SQLException {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin FROM periodo WHERE Nombre=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Periodo(
                            rs.getInt("idPeriodo"),
                            rs.getString("Nombre"),
                            rs.getDate("Inicio").toLocalDate(),
                            rs.getDate("Fin").toLocalDate()
                    );
                }
                return null;
            }
        }
    }

    public int insert(Periodo p) throws SQLException {
        String sql = "INSERT INTO periodo (Nombre, Inicio, Fin) VALUES (?, ?, ?)";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setDate(2, Date.valueOf(p.getInicio()));
            ps.setDate(3, Date.valueOf(p.getFin()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        }
    }

    public void update(Periodo p) throws SQLException {
        String sql = "UPDATE periodo SET Nombre=?, Inicio=?, Fin=? WHERE idPeriodo=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDate(2, Date.valueOf(p.getInicio()));
            ps.setDate(3, Date.valueOf(p.getFin()));
            ps.setInt(4, p.getIdPeriodo());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM periodo WHERE idPeriodo=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
