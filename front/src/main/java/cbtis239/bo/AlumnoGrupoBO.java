package cbtis239.bo;

import cbtis239.dao.AlumnoGrupoDao;

/** BO auxiliar para mover alumno de grupo y sincronizar calificaciones (sin triggers). */
public class AlumnoGrupoBO {
    private final AlumnoGrupoDao dao = new AlumnoGrupoDao();
    public boolean cambiarGrupoYSincronizar(String matricula, int nuevoGrupoId) throws Exception {
        return dao.cambiarGrupoYSincronizar(matricula, nuevoGrupoId);
    }
}
