package cbtis239.dao;

import cbtis239.model.Director;
import cbtis239.util.DB;

import java.sql.*;

public class DirectorDAO {

    // Obtiene el único director (id = 1), o null si no existe
    public Director getUnico() throws SQLException {
        String sql = "SELECT idDirector, nombre, paterno, materno, firma " +
                "FROM director WHERE idDirector = 1";

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Director d = new Director();
                d.setIdDirector(rs.getInt("idDirector"));
                d.setNombre(rs.getString("nombre"));
                d.setPaterno(rs.getString("paterno"));
                d.setMaterno(rs.getString("materno"));
                d.setFirma(rs.getBytes("firma"));
                return d;
            }
        }
        return null;
    }

    /**
     * Guarda el director único:
     *  - Si ya existe el id = 1 -> UPDATE
     *  - Si no existe           -> INSERT con id = 1
     */
    public void guardar(Director d) throws SQLException {
        try (Connection cn = DB.get()) {

            // Intentar UPDATE
            String sqlUpdate = "UPDATE director " +
                    "SET nombre = ?, paterno = ?, materno = ?, firma = ? " +
                    "WHERE idDirector = 1";
            int updated;
            try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                ps.setString(1, d.getNombre());
                ps.setString(2, d.getPaterno());
                ps.setString(3, d.getMaterno());
                ps.setBytes(4, d.getFirma());
                updated = ps.executeUpdate();
            }

            // Si no actualizó ninguna fila, insertar el primer director
            if (updated == 0) {
                String sqlInsert = "INSERT INTO director " +
                        "(idDirector, nombre, paterno, materno, firma) " +
                        "VALUES (1, ?, ?, ?, ?)";
                try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
                    ps.setString(1, d.getNombre());
                    ps.setString(2, d.getPaterno());
                    ps.setString(3, d.getMaterno());
                    ps.setBytes(4, d.getFirma());
                    ps.executeUpdate();
                }
            }
        }
    }
}
