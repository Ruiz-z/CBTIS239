package cbtis239.dao;

import cbtis239.model.Periodo;
import cbtis239.util.DB; // Asumiendo que esta es tu clase de conexión
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PeriodoDao {

    public List<Periodo> findAll() {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin FROM Periodo ORDER BY Inicio DESC";
        List<Periodo> out = new ArrayList<>();
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql);
             var rs  = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar periodos: " + e.getMessage(), e);
        }
        return out;
    }
    
    public Periodo findByName(String name) {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin FROM Periodo WHERE Nombre=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar periodo por nombre: " + e.getMessage(), e);
        }
    }

    public long insert(Periodo p) {
        // La sentencia NO incluye idPeriodo porque es AUTO_INCREMENT
        String sql = "INSERT INTO Periodo (Nombre, Inicio, Fin) VALUES (?, ?, ?)";
        try (var con = DB.get();
             // IMPORTANTE: Pedir las claves generadas
             var ps  = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, p.getNombre());
            ps.setDate(2, Date.valueOf(p.getFechaInicio()));
            ps.setDate(3, Date.valueOf(p.getFechaFin()));
            ps.executeUpdate();
            
            try (var keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
             // Este catch ayuda a capturar errores de unicidad o NOT NULL
            throw new RuntimeException("Error al insertar periodo: " + e.getMessage(), e);
        }
    }

    public boolean update(Periodo p) {
        String sql = "UPDATE Periodo SET Nombre=?, Inicio=?, Fin=? WHERE idPeriodo=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDate(2, Date.valueOf(p.getFechaInicio()));
            ps.setDate(3, Date.valueOf(p.getFechaFin()));
            ps.setInt(4, p.getIdPeriodo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar periodo: " + e.getMessage(), e);
        }
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM Periodo WHERE idPeriodo=?";
        try (var con = DB.get();
             var ps  = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar periodo: " + e.getMessage(), e);
        }
    }

    // --- Helpers ---
    private Periodo mapRow(ResultSet rs) throws SQLException {
        Periodo p = new Periodo();
        p.setIdPeriodo(rs.getInt("idPeriodo"));
        p.setNombre(rs.getString("Nombre"));
        
        Date sqlInicio = rs.getDate("Inicio");
        Date sqlFin = rs.getDate("Fin");
        
        p.setFechaInicio(sqlInicio != null ? sqlInicio.toLocalDate() : null);
        p.setFechaFin(sqlFin != null ? sqlFin.toLocalDate() : null);
        
        return p;
    }
    public Periodo getActual() {
        String sql = "SELECT idPeriodo, Nombre, Inicio, Fin " +
                "FROM Periodo WHERE CURDATE() BETWEEN Inicio AND Fin " +
                "ORDER BY Inicio DESC LIMIT 1";
        try (var con = DB.get(); var ps = con.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("No fue posible obtener el periodo actual: " + e.getMessage(), e);
        }
    }

    public Periodo getSiguiente() {
        String sql = "SELECT p.idPeriodo, p.Nombre, p.Inicio, p.Fin " +
                "FROM Periodo p " +
                "WHERE p.Inicio > (SELECT Inicio FROM Periodo WHERE CURDATE() BETWEEN Inicio AND Fin LIMIT 1) " +
                "ORDER BY p.Inicio ASC LIMIT 1";
        try (var con = DB.get(); var ps = con.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("No fue posible obtener el periodo siguiente: " + e.getMessage(), e);
        }
    }

}
