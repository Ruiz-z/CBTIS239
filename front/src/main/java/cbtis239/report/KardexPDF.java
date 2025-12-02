package cbtis239.report;

import cbtis239.bo.KardexBO;
import cbtis239.model.KardexAlumnoInfo;
import cbtis239.model.KardexFila;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.OutputStream;
import java.util.List;

public class KardexPDF {

    private final KardexBO bo = new KardexBO();

    public void generar(String matricula, OutputStream out) throws Exception {

        KardexAlumnoInfo info = bo.infoAlumno(matricula);
        if (info == null) {
            throw new IllegalArgumentException("No se encontró alumno con matrícula " + matricula);
        }

        List<KardexFila> filas = bo.kardex(matricula);
        if (filas == null || filas.isEmpty()) {
            throw new IllegalArgumentException("El alumno no tiene materias registradas en el kardex.");
        }

        int totalPlan   = bo.totalCreditosPlan(info);
        int credAcred   = bo.creditosAcreditados(filas);
        int credCursado = bo.creditosCursados(filas);
        double avance   = (totalPlan > 0) ? (credAcred * 100.0 / totalPlan) : 0.0;
        double promedio = bo.promedioGeneral(filas);

        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font fTitulo   = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fSub      = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fNormal   = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fNormalB  = new Font(Font.HELVETICA, 9, Font.BOLD);

        // ===== Título =====
        Paragraph pTitulo = new Paragraph("HISTORIAL ACADÉMICO", fTitulo);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTitulo);
        doc.add(Chunk.NEWLINE);

        // ===== Encabezado estilo ficha =====
        PdfPTable tHead = new PdfPTable(4);
        tHead.setWidthPercentage(100);
        tHead.setWidths(new float[]{25, 25, 25, 25});

        addPar(tHead, "Nombre del subsistema:", fSub,
                "DIRECCIÓN GENERAL DE EDUCACIÓN TECNOLÓGICA INDUSTRIAL Y DE SERVICIOS", fNormal);
        addPar(tHead, "Nombre del plantel:", fSub,
                "CENTRO DE BACHILLERATO TECNOLÓGICO INDUSTRIAL Y DE SERVICIOS NO. 239", fNormal);

        addPar(tHead, "CURP:", fSub, info.getCurp(), fNormal);
        addPar(tHead, "Modalidad educativa:", fSub, "Escolarizada", fNormal);

        addPar(tHead, "Nombre del alumno:", fSub, info.getNombreCompleto(), fNormal);
        addPar(tHead, "Opción educativa:", fSub, "Presencial", fNormal);

        addPar(tHead, "Número de control:", fSub, info.getMatricula(), fNormal);
        addPar(tHead, "Plan de estudios:", fSub, "Acuerdo 653", fNormal);

        addPar(tHead, "Carrera Técnica en:", fSub, info.getCarrera(), fNormal);
        addPar(tHead, "Periodo Ingreso:", fSub, "", fNormal);  // no lo tenemos

        doc.add(tHead);
        doc.add(Chunk.NEWLINE);

        // ===== Avance de créditos / UAC =====
        PdfPTable tRes = new PdfPTable(4);
        tRes.setWidthPercentage(100);
        tRes.setWidths(new float[]{25, 25, 25, 25});

        addPar(tRes, "Avance créditos (cursados):", fSub,
                String.format("%d de %d (%.1f%%)", credCursado, totalPlan, avance), fNormal);
        addPar(tRes, "Avance de UAC acreditadas:", fSub,
                String.format("%d acreditadas", credAcred), fNormal);

        addPar(tRes, "Promedio general:", fSub,
                String.format("%.1f", promedio), fNormalB);
        addPar(tRes, "", fSub, "", fNormal);

        doc.add(tRes);
        doc.add(Chunk.NEWLINE);

        // ===== Tabla de detalle =====
        PdfPTable tDet = new PdfPTable(8);
        tDet.setWidthPercentage(100);
        tDet.setWidths(new float[]{15, 8, 12, 6, 25, 6, 8, 15});

        String[] headers = {
                "Nombre Plantel", "Tipo UAC", "Clave UAC",
                "Semestre", "Nombre", "Calif", "Créditos",
                "Periodo Escolar"
        };

        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, fSub));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(new Color(200, 200, 220));
            tDet.addCell(c);
        }

        for (KardexFila k : filas) {
            tDet.addCell(new Phrase(k.getPlantel(), fNormal));
            tDet.addCell(new Phrase(k.getTipoUac(), fNormal));
            tDet.addCell(new Phrase(k.getClaveUac(), fNormal));
            tDet.addCell(new Phrase(k.getSemestre() > 0 ? String.valueOf(k.getSemestre()) : "", fNormal));
            tDet.addCell(new Phrase(k.getNombreUac(), fNormal));
            tDet.addCell(new Phrase(k.getCalificacion() > 0 ? String.format("%.1f", k.getCalificacion()) : "", fNormal));
            tDet.addCell(new Phrase(k.getCreditos() > 0 ? String.valueOf(k.getCreditos()) : "", fNormal));
            tDet.addCell(new Phrase(k.getPeriodoEscolar(), fNormal));
        }

        doc.add(tDet);

        doc.close();
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
}
