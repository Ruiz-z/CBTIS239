package cbtis239.bo;

import cbtis239.dao.DirectorDAO;
import cbtis239.model.Director;

import java.sql.SQLException;

public class DirectorBO {

    private final DirectorDAO dao = new DirectorDAO();

    // Puede devolver null si aún no hay registro
    public Director obtenerDirector() throws SQLException {
        return dao.getUnico();
    }

    public void guardarDirector(Director d) throws SQLException {
        if (d.getNombre() == null || d.getNombre().isBlank()) {
            throw new SQLException("El nombre del director es obligatorio.");
        }
        if (d.getPaterno() == null || d.getPaterno().isBlank()) {
            throw new SQLException("El apellido paterno del director es obligatorio.");
        }
        if (d.getMaterno() == null || d.getMaterno().isBlank()) {
            throw new SQLException("El apellido materno del director es obligatorio.");
        }

        // Siempre se guarda sobre id=1
        d.setIdDirector(1);
        dao.guardar(d);
    }
}
