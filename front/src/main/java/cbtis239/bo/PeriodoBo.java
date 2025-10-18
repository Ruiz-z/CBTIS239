package cbtis239.bo;

import cbtis239.dao.PeriodoDao;
import cbtis239.model.Periodo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PeriodoBo {

    private final PeriodoDao dao = new PeriodoDao();

    public List<Periodo> listar() throws SQLException {
        return dao.findAll();
    }

    public void guardar(Periodo p) throws SQLException {
        validar(p);
        if (p.getIdPeriodo() == 0) {
            int id = dao.insert(p);
            p.setIdPeriodo(id);
        } else {
            dao.update(p);
        }
    }

    public void eliminar(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Seleccione un periodo válido.");
        dao.delete(id);
    }

    private void validar(Periodo p) {
        if (p.getNombre() == null || p.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del periodo es obligatorio.");
        LocalDate i = p.getInicio(), f = p.getFin();
        if (i == null || f == null)
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        if (f.isBefore(i))
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio.");
    }
}
