package cbtis239.bo;

import cbtis239.dao.CursoDao;
import cbtis239.model.Curso;
import cbtis239.model.Opcion;
import cbtis239.model.OpcionStr;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CursoBO {
    private final CursoDao dao = new CursoDao();

    // Combos y datos
    public List<OpcionStr> listarMaterias() throws SQLException { return dao.listarMaterias(); }
    public List<Opcion> listarDocentes() throws SQLException { return dao.listarDocentes(); }
    public List<Opcion> listarDocentesPorMateria(String clave) throws SQLException { return dao.listarDocentesPorMateria(clave); }
    public List<OpcionStr> listarMateriasPorDocente(int docenteId) throws SQLException { return dao.listarMateriasPorDocente(docenteId); }
    public List<OpcionStr> listarAulas() throws SQLException { return dao.listarAulas(); }
    public List<Opcion> listarPeriodos() throws SQLException { return dao.listarPeriodos(); }
    public LocalDate[] fechasDePeriodo(int idPeriodo) throws SQLException { return dao.fechasDePeriodo(idPeriodo); }
    public List<Curso> listarCursos() throws SQLException { return dao.listar(); }

    public int getOrCreateDiasSemana(boolean l, boolean m, boolean x, boolean j, boolean v) throws SQLException {
        return dao.getOrCreateDiasSemana(l?1:0, m?1:0, x?1:0, j?1:0, v?1:0);
    }

    // Validaciones
    public void validarChoques(String aula, int docenteId, LocalTime hi, LocalTime hf,
                               int diasId, LocalDate periodoInicio, LocalDate periodoFin, Integer exclCursoId) throws SQLException {
        if (dao.existeAulaOcupada(aula, hi, hf, diasId, periodoInicio, periodoFin, exclCursoId))
            throw new IllegalArgumentException("El salón está ocupado a esa hora.");
        if (dao.existeDocenteOcupado(docenteId, hi, hf, diasId, periodoInicio, periodoFin, exclCursoId))
            throw new IllegalArgumentException("El docente ya tiene un curso a esa hora.");
    }

    public void validarCursoIgual(LocalDate periodoInicio, LocalDate periodoFin,
                                  int docenteId, String materiaClave, String aula,
                                  LocalTime hi, LocalTime hf, int diasId, Integer excl) throws SQLException {
        if (dao.existeCursoIgual(periodoInicio, periodoFin, docenteId, materiaClave, aula, hi, hf, diasId, excl))
            throw new IllegalArgumentException("Ya existe un curso igual en el mismo periodo.");
    }

    // Persistencia
    public void crear(Curso c) throws SQLException { dao.insertar(c); }
    public void actualizar(Curso c) throws SQLException { dao.actualizar(c); }
    public void eliminar(int cursoId) throws SQLException { dao.eliminar(cursoId); }
}
