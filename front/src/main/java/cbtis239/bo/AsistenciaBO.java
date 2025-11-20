package cbtis239.bo;

import cbtis239.dao.AsistenciaDiariaDAO;
import cbtis239.dao.MovimientoDAO;
import java.sql.SQLException;

public class AsistenciaBO {

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final AsistenciaDiariaDAO asistenciaDAO = new AsistenciaDiariaDAO();

    // Procesa un escaneo de credencial
    public String registrarEscaneo(String matricula) throws SQLException {

        boolean tieneEntrada = movimientoDAO.tieneEntradaHoy(matricula);
        boolean ultimaSalida = movimientoDAO.ultimaEsSalida(matricula);

        if (!tieneEntrada) {
            movimientoDAO.registrarMovimiento(matricula, "Entrada");
            asistenciaDAO.marcarPresente(matricula);
            return "Entrada registrada (Presente).";
        }

        if (ultimaSalida) {
            movimientoDAO.registrarMovimiento(matricula, "Entrada");
            return "Reingreso registrado.";
        } else {
            movimientoDAO.registrarMovimiento(matricula, "Salida");
            return "Salida registrada.";
        }
    }

    // Cerrar día → poner faltas a quienes no entraron
    public void cerrarDia() throws SQLException {
        asistenciaDAO.marcarFaltaSiNoEntro();
    }
}
