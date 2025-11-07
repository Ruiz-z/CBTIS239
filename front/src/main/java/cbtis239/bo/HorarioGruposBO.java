package cbtis239.bo;

import cbtis239.dao.HorarioGruposDao;
import cbtis239.model.CursoRow;
import cbtis239.model.Opcion;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HorarioGruposBO {

    private final HorarioGruposDao dao = new HorarioGruposDao();

    // Tipos espejo del DAO (para no exponer clases internas del DAO)
    public static class MateriaRet {
        public final String clave;
        public final String nombre;
        public MateriaRet(String clave, String nombre) { this.clave = clave; this.nombre = nombre; }
    }

    public List<Opcion> listarEspecialidades() throws SQLException { return dao.listarEspecialidades(); }
    public List<Opcion> listarGruposPorEspecialidad(int espClave) throws SQLException { return dao.listarGruposPorEspecialidad(espClave); }
    public Integer obtenerSemestreGrupo(int grupoId) throws SQLException { return dao.obtenerSemestreGrupo(grupoId); }

    public List<MateriaRet> listarReticulaMaterias(int espClave, int semestre) throws SQLException {
        return dao.listarMateriasReticula(espClave, semestre).stream()
                .map(m -> new MateriaRet(m.clave, m.nombre))
                .collect(Collectors.toList());
    }

    public Set<String> materiasAsignadasGrupo(int grupoId) throws SQLException {
        return dao.materiasAsignadasGrupo(grupoId);
    }

    public List<CursoRow> cursosSeleccionadosTabla(int grupoId) throws SQLException {
        return dao.listarCursosDelGrupo(grupoId).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    public List<CursoRow> cursosDisponiblesPorMaterias(Set<String> clavesMaterias) throws SQLException {
        return dao.listarCursosPorMaterias(clavesMaterias).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    public int agregarCursosGrupo(int grupoId, List<Integer> cursoIds) throws SQLException {
        return dao.agregarCursosGrupo(grupoId, cursoIds);
    }

    public int eliminarCursosGrupo(int grupoId, List<Integer> cursoIds) throws SQLException {
        return dao.eliminarCursosGrupo(grupoId, cursoIds);
    }

    // ---------- Helpers de mapeo ----------
    private CursoRow toRow(HorarioGruposDao.CursoAsignado c) {
        int mask = maskDias(c.lu, c.ma, c.mi, c.ju, c.vi);
        String dias = diasCorto(mask);
        String hi = safe(c.hi);
        String hf = safe(c.hf);
        String horario = dias + " de " + hi + " a " + hf + " en " + (c.aula == null ? "-" : c.aula);
        return new CursoRow(c.cursoId, c.materiaNombre, c.docenteNombre, horario, mask, c.hi, c.hf, c.aula);
    }

    private String safe(LocalTime t) { return t == null ? "--:--" : t.toString(); }

    private int maskDias(boolean lu, boolean ma, boolean mi, boolean ju, boolean vi) {
        int m = 0;
        if (lu) m |= 1<<0;
        if (ma) m |= 1<<1;
        if (mi) m |= 1<<2;
        if (ju) m |= 1<<3;
        if (vi) m |= 1<<4;
        return m;
    }

    private String diasCorto(int mask) {
        StringBuilder sb = new StringBuilder();
        if ((mask & 1) != 0) sb.append("L,");
        if ((mask & 2) != 0) sb.append("M,");
        if ((mask & 4) != 0) sb.append("X,");
        if ((mask & 8) != 0) sb.append("J,");
        if ((mask & 16)!= 0) sb.append("V,");
        if (sb.length() == 0) return "-";
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
