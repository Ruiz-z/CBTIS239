package cbtis239.bo;

import cbtis239.dao.AsistenciaBoletaDAO;
import cbtis239.model.AsistenciaBoletaResumen;

import java.sql.SQLException;

public class AsistenciaBoletaBO {

    private final AsistenciaBoletaDAO dao = new AsistenciaBoletaDAO();

    public AsistenciaBoletaResumen resumenPeriodoActual(String matricula) throws SQLException {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("La matrícula no puede estar vacía.");
        }
        return dao.resumenPeriodoActual(matricula.trim());
    }
}
