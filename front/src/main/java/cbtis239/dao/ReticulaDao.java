package cbtis239.dao;

import cbtis239.model.MateriaSelectable;
import cbtis239.model.Opcion;
import cbtis239.model.ReticulaAsignadaRow;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO unificado: catálogos + consultas masivas para Retícula. */
public class ReticulaDao {

    // --------- Catálogo de especialidades ---------
    public List<Opcion> listarEspecialidades() throws SQLException {
        String sql = "SELECT Clave, Nombre FROM SistemaEscolar.Especialidad ORDER BY Nombre";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Opcion> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Opcion(rs.getInt("Clave"), rs.getString("Nombre")));
            }
            return list;
        }
    }

    // --------- Listas para la pantalla ----------
    /** Materias NO asignadas a la especialidad (independiente del semestre por la PK). */
    public List<MateriaSelectable> listarDisponibles(int espClave) throws SQLException {
        String sql = """
            SELECT m.Clave, m.Nombre
            FROM SistemaEscolar.Materia m
            WHERE NOT EXISTS (
                SELECT 1 FROM SistemaEscolar.Reticula r
                WHERE r.Especialidad_Clave = ? AND r.Materia_Clave = m.Clave
            )
            ORDER BY m.Nombre
            """;
        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            try (ResultSet rs = ps.executeQuery()) {
                List<MateriaSelectable> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new MateriaSelectable(rs.getString("Clave"), rs.getString("Nombre")));
                }
                return out;
            }
        }
    }

    /** Retícula asignada; si semestre es null devuelve todos los semestres. */
    public List<ReticulaAsignadaRow> listarAsignadas(int espClave, Integer semestre) throws SQLException {
        String base = """
            SELECT r.Materia_Clave, m.Nombre, r.Semestre
            FROM SistemaEscolar.Reticula r
            JOIN SistemaEscolar.Materia m ON m.Clave = r.Materia_Clave
            WHERE r.Especialidad_Clave = ?
            """;
        String order = " ORDER BY r.Semestre, m.Nombre";
        String sql = (semestre == null) ? base + order : base + " AND r.Semestre = ?" + order;

        try (Connection cn = DB.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, espClave);
            if (semestre != null) ps.setInt(2, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                List<ReticulaAsignadaRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new ReticulaAsignadaRow(
                            rs.getString("Materia_Clave"),
                            rs.getString("Nombre"),
                            rs.getInt("Semestre")
                    ));
                }
                return out;
            }
        }
    }

    // --------- Operaciones masivas ----------
    public void insertarMuchas(int espClave, List<String> clavesMateria, int semestre) throws SQLException {
        String sql = "INSERT INTO SistemaEscolar.Reticula(Especialidad_Clave, Materia_Clave, Semestre) VALUES (?,?,?)";
        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                for (String clave : clavesMateria) {
                    ps.setInt(1, espClave);
                    ps.setString(2, clave);
                    ps.setInt(3, semestre);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            cn.commit();
        }
    }

    /** Elimina muchas relaciones (PK ignora semestre). */
    public void eliminarMuchas(int espClave, List<String> clavesMateria) throws SQLException {
        String sql = "DELETE FROM SistemaEscolar.Reticula WHERE Especialidad_Clave=? AND Materia_Clave=?";
        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                for (String clave : clavesMateria) {
                    ps.setInt(1, espClave);
                    ps.setString(2, clave);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            cn.commit();
        }
    }
}
