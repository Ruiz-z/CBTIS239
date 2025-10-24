package cbtis239.bo;

import cbtis239.dao.AlumnoDAO;
import cbtis239.dao.PagoDAO;
import cbtis239.dao.PeriodoDao;
import cbtis239.model.Alumno;
import cbtis239.model.Periodo;

public class ReinscripcionBO {
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final PeriodoDao periodoDao = new PeriodoDao();
    private static final int MAX_SEM = 6; // ajusta si tu plan es distinto

    /** Si pagó en el periodo ACTUAL -> pasa a SIGUIENTE, semestre+1, Activo.
     *  Si NO pagó -> queda Inactivo en el periodo actual (sin avanzar). */
    public void reinscribir(String matricula) throws Exception {
        Periodo actual = periodoDao.getActual();
        Periodo siguiente = periodoDao.getSiguiente();
        if (actual == null)   throw new Exception("No hay periodo vigente.");
        if (siguiente == null) throw new Exception("Configura el siguiente periodo en la tabla Periodo.");

        boolean pagado = pagoDAO.existsPagoAlumnoEnPeriodo(matricula, actual.getIdPeriodo());
        Alumno a = alumnoDAO.findByMatricula(matricula);
        if (a == null) throw new Exception("Matrícula no encontrada: " + matricula);

        if (pagado) {
            int sem = a.getSemestre() == null ? 1 : a.getSemestre();
            int nuevoSem = Math.min(sem + 1, MAX_SEM);
            alumnoDAO.actualizarEstadoSemestreYPeriodo(matricula, "Activo", nuevoSem, siguiente.getIdPeriodo());
        } else {
            alumnoDAO.actualizarEstadoSemestreYPeriodo(matricula, "Inactivo", a.getSemestre(), actual.getIdPeriodo());
        }
    }
}
