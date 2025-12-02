package cbtis239.report;

import cbtis239.bo.AsistenciaBoletaBO;
import cbtis239.bo.BoletaCalificacionBO;
import cbtis239.model.AsistenciaBoletaResumen;
import cbtis239.model.BoletaCalificacion;
import cbtis239.util.DB;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Genera el PDF de Boleta de Calificaciones para un alumno.
 * Usa periodo actual, calificaciones y asistencia global al plantel.
 * CF = promedio de Parcial1, Parcial2, Parcial3 (ignorando ExamenFinal).
 */
public class BoletaCalificacionesPDF {

    private final BoletaCalificacionBO boletaBO = new BoletaCalificacionBO();
    private final AsistenciaBoletaBO asistenciaBO = new AsistenciaBoletaBO();

    public void generar(String matricula, OutputStream out) throws Exception {

        // ===== 1. Cargar datos de BD =====
        AlumnoInfo infoAlumno = cargarInfoAlumno(matricula);
        if (infoAlumno == null) {
            throw new IllegalArgumentException("No se encontró alumno con matrícula " + matricula);
        }

        List<BoletaCalificacion> califs = boletaBO.boletaDeAlumno(matricula);
        if (califs == null || califs.isEmpty()) {
            throw new IllegalArgumentException("El alumno no tiene calificaciones registradas.");
        }

        AsistenciaBoletaResumen asistencia = asistenciaBO.resumenPeriodoActual(matricula);
        String nombrePeriodo = obtenerNombrePeriodoActual();

        // ===== 2. Crear documento PDF =====
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fuentes
        Font fontTitulo   = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fontSub      = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fontNormal   = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fontNormalB  = new Font(Font.HELVETICA, 9, Font.BOLD);

        // ===== 3. Encabezado =====
        Paragraph pTitulo = new Paragraph("BOLETA DE CALIFICACIONES", fontTitulo);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTitulo);
        doc.add(Chunk.NEWLINE);

        // Datos generales en tabla de 4 columnas
        PdfPTable tInfo = new PdfPTable(4);
        tInfo.setWidthPercentage(100);
        tInfo.setWidths(new float[]{18, 32, 18, 32});

        addPar(tInfo, "SUBSISTEMA:", fontSub,
                "UNIDAD DE EDUCACIÓN MEDIA SUPERIOR TECNOLÓGICA INDUSTRIAL Y DE SERVICIOS", fontNormal);
        addPar(tInfo, "PLANTEL:",    fontSub, "CBTIS No. 239", fontNormal);
        addPar(tInfo, "SEMESTRE:",   fontSub, String.valueOf(infoAlumno.semestre), fontNormal);
        addPar(tInfo, "GRUPO:",      fontSub, infoAlumno.grupo, fontNormal);
        addPar(tInfo, "NO. CONTROL:",fontSub, infoAlumno.matricula, fontNormal);
        addPar(tInfo, "NOMBRE:",     fontSub, infoAlumno.nombreCompleto, fontNormal);
        addPar(tInfo, "CARRERA:",    fontSub, infoAlumno.carrera, fontNormal);
        addPar(tInfo, "PERIODO:",    fontSub,
                nombrePeriodo != null ? nombrePeriodo : "(Periodo actual)", fontNormal);

        doc.add(tInfo);
        doc.add(Chunk.NEWLINE);

        // ===== 4. Tabla principal de calificaciones =====
        // Columnas: ASIGNATURA / MÓDULO | 1o | 2o | 3o | CF | TAS | TAC
        PdfPTable tCalif = new PdfPTable(7);
        tCalif.setWidthPercentage(100);
        tCalif.setWidths(new float[]{45, 7, 7, 7, 7, 10, 10});

        PdfPCell c;

        // Cabecera
        c = new PdfPCell(new Phrase("ASIGNATURA / MÓDULO", fontSub));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(new Color(200, 230, 255));
        tCalif.addCell(c);

        String[] headers = {"1o", "2o", "3o", "CF", "TAS", "TAC"};
        for (String h : headers) {
            c = new PdfPCell(new Phrase(h, fontSub));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(new Color(200, 230, 255));
            tCalif.addCell(c);
        }

        // Datos por materia
        for (BoletaCalificacion b : califs) {

            double p1 = b.getParcial1();
            double p2 = b.getParcial2();
            double p3 = b.getParcial3();
            // ExamenFinal se IGNORA para CF en esta boleta

            // === CF: promedio de p1, p2, p3 (solo los > 0) ===
            double suma = 0;
            int n = 0;
            if (p1 > 0) { suma += p1; n++; }
            if (p2 > 0) { suma += p2; n++; }
            if (p3 > 0) { suma += p3; n++; }

            double cf = (n > 0) ? (suma / n) : 0;

            // TAS: total de asistencias globales del periodo
            int tas = asistencia.getDiasAsistidos();

            // TAC: A / NA / NP
            String tac;
            if (asistencia.getPorcentaje() < 80.0) {
                tac = "NP"; // No acreditado por asistencia
            } else if (cf < 6.0) {
                tac = "NA"; // No acreditado por calificación
            } else {
                tac = "A";  // Acreditado
            }

            // Fila
            tCalif.addCell(new Phrase(b.getCurso(), fontNormal));
            tCalif.addCell(new Phrase(nota(p1), fontNormal));
            tCalif.addCell(new Phrase(nota(p2), fontNormal));
            tCalif.addCell(new Phrase(nota(p3), fontNormal));
            tCalif.addCell(new Phrase(nota(cf), fontNormalB));
            tCalif.addCell(new Phrase(String.valueOf(tas), fontNormalB));
            tCalif.addCell(new Phrase(tac, fontNormalB));
        }

        doc.add(tCalif);
        doc.add(Chunk.NEWLINE);

        // ===== 5. Nota de asistencia global =====
        Paragraph pNota = new Paragraph(
                String.format(
                        "NOTA: Asistencia global del alumno en el periodo actual: %d de %d días (%.2f%%). " +
                        "Se requiere al menos 80%% de asistencias para acreditar.",
                        asistencia.getDiasAsistidos(),
                        asistencia.getDiasEscolares(),
                        asistencia.getPorcentaje()
                ),
                fontNormal
        );
        doc.add(pNota);

        // ===== 6. Leyenda / Glosario =====
        doc.add(Chunk.NEWLINE);

        Paragraph leyendaTitulo = new Paragraph("LEYENDA DE CALIFICACIONES", fontSub);
        leyendaTitulo.setAlignment(Element.ALIGN_LEFT);
        doc.add(leyendaTitulo);
        doc.add(Chunk.NEWLINE);

        PdfPTable tLeyenda = new PdfPTable(2);
        tLeyenda.setWidthPercentage(80);
        tLeyenda.setWidths(new float[]{15, 85});

        java.util.function.BiConsumer<String, String> addLey = (key, desc) -> {
            PdfPCell k = new PdfPCell(new Phrase(key, fontNormalB));
            k.setBorder(Rectangle.NO_BORDER);
            tLeyenda.addCell(k);

            PdfPCell v = new PdfPCell(new Phrase(desc, fontNormal));
            v.setBorder(Rectangle.NO_BORDER);
            tLeyenda.addCell(v);
        };

        addLey.accept("1o, 2o, 3o:",
                "Calificaciones parciales del periodo (unidades).");
        addLey.accept("CF:",
                "Calificación Final: promedio de las unidades 1, 2 y 3 (sin considerar examen final).");
        addLey.accept("TAS:",
                "Total de asistencias del alumno en el periodo escolar (entrada al plantel).");
        addLey.accept("TAC:",
                "Tipo de Acreditación:\n" +
                "   - A  : Acreditado.\n" +
                "   - NA : No Acreditado (CF menor a 6.0).\n" +
                "   - NP : No Acreditado por asistencia menor al 80%.");

        doc.add(tLeyenda);

        // ===== Cerrar documento =====
        doc.close();
    }

    // ---------- Helpers ----------

    private static String nota(double v) {
        return v > 0 ? String.format("%.0f", v) : "";
    }

    private static void addPar(PdfPTable t,
                               String label, Font fLabel,
                               String valor, Font fValor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setBorder(Rectangle.NO_BORDER);
        t.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "", fValor));
        c2.setBorder(Rectangle.NO_BORDER);
        t.addCell(c2);
    }

    /**
     * Carga datos básicos del alumno + grupo + carrera.
     */
    private AlumnoInfo cargarInfoAlumno(String matricula) throws SQLException {
        String sql = """
            SELECT a.Matricula,
                   a.Nombre, a.Paterno, a.Materno,
                   a.Semestre,
                   COALESCE(a.Carrera, e.Nombre) AS Carrera,
                   COALESCE(g.NombreGrupo, '')   AS Grupo
            FROM alumno a
            LEFT JOIN grupo g        ON g.GrupoID = a.GrupoID
            LEFT JOIN especialidad e ON e.Clave = g.Especialidad_Clave
            WHERE a.Matricula = ?
        """;

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AlumnoInfo info = new AlumnoInfo();
                    info.matricula = rs.getString("Matricula");
                    info.nombreCompleto = String.format("%s %s %s",
                            rs.getString("Paterno"),
                            rs.getString("Materno"),
                            rs.getString("Nombre")).trim();
                    info.semestre = rs.getInt("Semestre");
                    info.carrera = rs.getString("Carrera");
                    info.grupo = rs.getString("Grupo");
                    return info;
                }
            }
        }
        return null;
    }

    private String obtenerNombrePeriodoActual() throws SQLException {
        String sql = "SELECT Nombre FROM v_periodo_actual";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("Nombre");
            }
        }
        return null;
    }

    // DTO interno
    private static class AlumnoInfo {
        String matricula;
        String nombreCompleto;
        int semestre;
        String carrera;
        String grupo;
    }
}
