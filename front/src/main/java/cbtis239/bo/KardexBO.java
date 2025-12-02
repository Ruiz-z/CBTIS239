package cbtis239.bo;

import cbtis239.dao.KardexDAO;
import cbtis239.model.KardexAlumnoInfo;
import cbtis239.model.KardexFila;

import java.sql.SQLException;
import java.util.List;

public class KardexBO {

    private final KardexDAO dao = new KardexDAO();

    public KardexAlumnoInfo infoAlumno(String matricula) throws SQLException {
        if (matricula == null || matricula.isBlank())
            throw new IllegalArgumentException("La matrícula es requerida.");
        return dao.cargarInfoAlumno(matricula.trim());
    }

    public List<KardexFila> kardex(String matricula) throws SQLException {
        if (matricula == null || matricula.isBlank())
            throw new IllegalArgumentException("La matrícula es requerida.");
        return dao.cargarKardex(matricula.trim());
    }

    public int totalCreditosPlan(KardexAlumnoInfo info) throws SQLException {
        if (info == null) return 0;
        return dao.totalCreditosPlan(info.getEspecialidadClave());
    }

    public int creditosAcreditados(List<KardexFila> filas) {
        return filas.stream()
                .filter(f -> f.getCalificacion() >= 6.0)
                .mapToInt(KardexFila::getCreditos)
                .sum();
    }

    public int creditosCursados(List<KardexFila> filas) {
        // Si quieres contar crédito aunque esté reprobado:
        return filas.stream()
                .filter(f -> f.getCalificacion() > 0.0)
                .mapToInt(KardexFila::getCreditos)
                .sum();
    }

    public double promedioGeneral(List<KardexFila> filas) {
        return filas.stream()
                .filter(f -> f.getCalificacion() > 0.0)
                .mapToDouble(KardexFila::getCalificacion)
                .average()
                .orElse(0.0);
    }
}
