package cbtis239.dao;

import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;
import cbtis239.model.Reticula;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReticulaDao {

    // ---- Catálogos ----
    public List<Opcion> listarEspecialidades() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM SistemaEscolar.Especialidad ORDER BY Nombre";
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Opcion> list = new ArrayList<>();
            while (rs.next()) list.add(new Opcion(rs.getInt("Clave"), rs.getString("Nombre")));
            return list;
        }
    }

    public List<OpcionStr> listarMaterias() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM SistemaEscolar.Materia ORDER BY Nombre";
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<OpcionStr> list = new ArrayList<>();
            while (rs.next()) list.add(new OpcionStr(rs.getString("Clave"), rs.getString("Nombre")));
            return list;
        }
    }

    // ---- Retícula ----
    public List<Reticula> listarTodo() throws SQLException {
        String sql = """
            SELECT r.Especialidad_Clave,
                   e.Nombre AS Especialidad,
                   r.Semestre,
                   r.Materia_Clave,
                   m.Nombre AS Materia
            FROM SistemaEscolar.Reticula r
            JOIN SistemaEscolar.Especialidad e ON e.Clave = r.Especialidad_Clave
            JOIN SistemaEscolar.Materia      m ON m.Clave = r.Materia_Clave
            ORDER BY e.Nombre, r.Semestre, m.Nombre
            """;
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Reticula> list = new ArrayList<>();
            while (rs.next()) {
                Reticula r = new Reticula();
                r.setEspecialidadClave(rs.getInt("Especialidad_Clave"));
                r.setEspecialidadNombre(rs.getString("Especialidad"));
                r.setSemestre(rs.getInt("Semestre"));
                r.setMateriaClave(rs.getString("Materia_Clave"));
                r.setMateriaNombre(rs.getString("Materia"));
                list.add(r);
            }
            return list;
        }
    }

    public List<Reticula> listarPorEspecialidad(int espClave) throws SQLException {
        String sql = """
            SELECT r.Especialidad_Clave,
                   e.Nombre AS Especialidad,
                   r.Semestre,
                   r.Materia_Clave,
                   m.Nombre AS Materia
            FROM SistemaEscolar.Reticula r
            JOIN SistemaEscolar.Especialidad e ON e.Clave = r.Especialidad_Clave
            JOIN SistemaEscolar.Materia      m ON m.Clave = r.Materia_Clave
            WHERE r.Especialidad_Clave = ?
            ORDER BY r.Semestre, m.Nombre
            """;
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reticula> list = new ArrayList<>();
                while (rs.next()) {
                    Reticula r = new Reticula();
                    r.setEspecialidadClave(rs.getInt("Especialidad_Clave"));
                    r.setEspecialidadNombre(rs.getString("Especialidad"));
                    r.setSemestre(rs.getInt("Semestre"));
                    r.setMateriaClave(rs.getString("Materia_Clave"));
                    r.setMateriaNombre(rs.getString("Materia"));
                    list.add(r);
                }
                return list;
            }
        }
    }

    public List<Reticula> listarPorEspecialidadYSemestre(int espClave, int semestre) throws SQLException {
        String sql = """
            SELECT r.Especialidad_Clave,
                   e.Nombre AS Especialidad,
                   r.Semestre,
                   r.Materia_Clave,
                   m.Nombre AS Materia
            FROM SistemaEscolar.Reticula r
            JOIN SistemaEscolar.Especialidad e ON e.Clave = r.Especialidad_Clave
            JOIN SistemaEscolar.Materia      m ON m.Clave = r.Materia_Clave
            WHERE r.Especialidad_Clave = ? AND r.Semestre = ?
            ORDER BY m.Nombre
            """;
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            ps.setInt(2, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reticula> list = new ArrayList<>();
                while (rs.next()) {
                    Reticula r = new Reticula();
                    r.setEspecialidadClave(rs.getInt("Especialidad_Clave"));
                    r.setEspecialidadNombre(rs.getString("Especialidad"));
                    r.setSemestre(rs.getInt("Semestre"));
                    r.setMateriaClave(rs.getString("Materia_Clave"));
                    r.setMateriaNombre(rs.getString("Materia"));
                    list.add(r);
                }
                return list;
            }
        }
    }

    public boolean existeRelacion(int espClave, String matClave) throws SQLException {
        String sql = "SELECT 1 FROM SistemaEscolar.Reticula WHERE Especialidad_Clave=? AND Materia_Clave=? LIMIT 1";
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            ps.setString(2, matClave);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insertar(int espClave, String matClave, int semestre) throws SQLException {
        String sql = "INSERT INTO SistemaEscolar.Reticula(Especialidad_Clave, Materia_Clave, Semestre) VALUES (?,?,?)";
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            ps.setString(2, matClave);
            ps.setInt(3, semestre);
            ps.executeUpdate();
        }
    }

    public void eliminar(int espClave, String matClave) throws SQLException {
        String sql = "DELETE FROM SistemaEscolar.Reticula WHERE Especialidad_Clave=? AND Materia_Clave=?";
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            ps.setString(2, matClave);
            ps.executeUpdate();
        }
    }
}
