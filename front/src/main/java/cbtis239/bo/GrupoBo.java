package cbtis239.bo;

import cbtis239.dao.EspecialidadDao;
import cbtis239.dao.GrupoDao;
import cbtis239.model.Especialidad;
import cbtis239.model.Grupo;

import java.sql.SQLException;
import java.util.List;

public class GrupoBo {
    private final GrupoDao grupoDAO = new GrupoDao();
    private final EspecialidadDao espDAO = new EspecialidadDao();

    public List<Grupo> listarGrupos() throws SQLException {
        return grupoDAO.findAll();
    }

    public List<Especialidad> listarEspecialidades() throws SQLException {
        return espDAO.findAll();
    }

    public Grupo crear(String nombre, String capacidadStr, Integer especialidadClave) throws Exception {
        if (nombre == null || (nombre = nombre.trim()).isEmpty())
            throw new IllegalArgumentException("El nombre del grupo es obligatorio.");
        if (capacidadStr == null || capacidadStr.isBlank())
            throw new IllegalArgumentException("La capacidad es obligatoria.");

        int capacidad;
        try { capacidad = Integer.parseInt(capacidadStr.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("La capacidad debe ser numérica."); }

        if (capacidad < 0) throw new IllegalArgumentException("La capacidad no puede ser negativa.");
        if (especialidadClave == null) throw new IllegalArgumentException("Selecciona una especialidad.");

        Grupo g = new Grupo(0, nombre, capacidad, especialidadClave, null);
        int id = grupoDAO.insert(g);
        g.setGrupoId(id);

        // resolver nombre de especialidad para tabla
        espDAO.findAll().stream()
                .filter(e -> e.getClave() == especialidadClave)
                .findFirst().ifPresent(e -> g.setEspecialidadNombre(e.getNombre()));

        return g;
    }

    public void modificar(Grupo g) throws Exception {
        if (g.getNombreGrupo() == null || g.getNombreGrupo().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del grupo es obligatorio.");
        if (g.getCapacidad() < 0) throw new IllegalArgumentException("La capacidad no puede ser negativa.");
        if (g.getEspecialidadClave() <= 0) throw new IllegalArgumentException("Selecciona una especialidad.");
        grupoDAO.update(g);
    }

    public void eliminar(int grupoId) throws Exception {
        grupoDAO.deleteById(grupoId);
    }
}
