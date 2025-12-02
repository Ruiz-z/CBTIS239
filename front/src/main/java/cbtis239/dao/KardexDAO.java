package cbtis239.dao;

import cbtis239.model.KardexAlumnoInfo;
import cbtis239.model.KardexFila;
import cbtis239.util.DB;

import java.sql.*;
import java.util.*;

public class KardexDAO {

    private Connection getConnection() throws SQLException {
        return DB.get();
    }

    // ============================================================
    // INFO DEL ALUMNO PARA ENCABEZADO DEL KARDEX
    // ============================================================
    public KardexAlumnoInfo cargarInfoAlumno(String matricula) throws SQLException {
        String sql = """
            SELECT a.Matricula,
                   a.CURP,
                   a.Nombre,
                   a.Paterno,
                   a.Materno,
                   COALESCE(a.Carrera, e.Nombre) AS Carrera,
                   COALESCE(e.Clave, 0)         AS EspecialidadClave
            FROM alumno a
            LEFT JOIN grupo g        ON g.GrupoID = a.GrupoID
            LEFT JOIN especialidad e ON e.Clave   = g.Especialidad_Clave
            WHERE a.Matricula = ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KardexAlumnoInfo info = new KardexAlumnoInfo();
                    info.setMatricula(rs.getString("Matricula"));
                    info.setCurp(rs.getString("CURP"));

                    String nombreCompleto = String.format("%s %s %s",
                            rs.getString("Paterno"),
                            rs.getString("Materno"),
                            rs.getString("Nombre")).trim();
                    info.setNombreCompleto(nombreCompleto);

                    info.setCarrera(rs.getString("Carrera"));
                    info.setEspecialidadClave(rs.getInt("EspecialidadClave"));
                    return info;
                }
            }
        }
        return null;
    }

    // ============================================================
    // DETALLE DEL KARDEX (MATERIAS) – SIN REPETIR MATERIAS
    // ============================================================
    public List<KardexFila> cargarKardex(String matricula) throws SQLException {
        String sql = """
            SELECT 
                'CBTIS NO. 239'           AS Plantel,
                m.Clave                   AS ClaveUAC,
                COALESCE(r.Semestre, 0)   AS Semestre,
                m.Nombre                  AS NombreUAC,
                COALESCE(m.Creditos, 0)   AS Creditos,
                c.Parcial1,
                c.Parcial2,
                c.Parcial3,
                c.ExamenFinal,
                cu.AnioAcademico
            FROM calificacion c
            JOIN curso cu 
                 ON cu.CursoID = c.CursoID
            JOIN materia m 
                 ON m.Clave = cu.Docente_has_Materia_Materia_Clave
            LEFT JOIN reticula r 
                 ON r.Materia_Clave = m.Clave
            WHERE c.Alumno_Matricula = ?
            ORDER BY m.Clave,
                     cu.AnioAcademico DESC,
                     cu.CursoID DESC
        """;

        // Usamos LinkedHashMap para quedarnos SOLO con la PRIMERA fila
        // de cada materia (claveUac), que será la más reciente
        // gracias al ORDER BY anterior.
        Map<String, KardexFila> mapa = new LinkedHashMap<>();

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String clave = rs.getString("ClaveUAC");

                    // Si ya guardamos esa materia, ignoramos intentos anteriores
                    if (mapa.containsKey(clave)) {
                        continue;
                    }

                    KardexFila k = new KardexFila();
                    k.setPlantel(rs.getString("Plantel"));

                    int semestre = rs.getInt("Semestre");
                    k.setSemestre(semestre);

                    // Regla simple: 1–4 Básica, 5+ Profesional
                    String tipoUac = (semestre >= 5) ? "Profesional" : "Básica";
                    k.setTipoUac(tipoUac);

                    k.setClaveUac(clave);
                    k.setNombreUac(rs.getString("NombreUAC"));
                    k.setCreditos(rs.getInt("Creditos"));

                    double p1 = rs.getDouble("Parcial1");
                    double p2 = rs.getDouble("Parcial2");
                    double p3 = rs.getDouble("Parcial3");
                    double ex = rs.getDouble("ExamenFinal"); // por si luego lo quieres usar

                    // ===== CF: PROMEDIO DE LAS 3 UNIDADES (SIN EXAMEN FINAL) =====
                    double suma = 0;
                    int n = 0;
                    if (p1 > 0) { suma += p1; n++; }
                    if (p2 > 0) { suma += p2; n++; }
                    if (p3 > 0) { suma += p3; n++; }
                    double cf = (n > 0) ? (suma / n) : 0.0;
                    k.setCalificacion(cf);

                    // Periodo escolar aproximado: "Semestral X - AAAA"
                    short anio = rs.getShort("AnioAcademico");
                    String periodo = (semestre > 0 && anio > 0)
                            ? String.format("Semestral %d - %d", semestre, (int) anio)
                            : "";
                    k.setPeriodoEscolar(periodo);

                    mapa.put(clave, k);
                }
            }
        }

        return new ArrayList<>(mapa.values());
    }

    // ============================================================
    // CRÉDITOS TOTALES DEL PLAN (RETÍCULA)
    // ============================================================
    public int totalCreditosPlan(int especialidadClave) throws SQLException {
        if (especialidadClave <= 0) return 0;

        String sql = """
            SELECT COALESCE(SUM(m.Creditos), 0) AS TotalCred
            FROM reticula r
            JOIN materia m ON m.Clave = r.Materia_Clave
            WHERE r.Especialidad_Clave = ?
        """;

        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, especialidadClave);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalCred");
                }
            }
        }
        return 0;
    }
}
