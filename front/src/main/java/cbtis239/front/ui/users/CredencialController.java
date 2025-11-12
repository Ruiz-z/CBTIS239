package cbtis239.front.ui.users;

import cbtis239.bo.CredencialBO;
import cbtis239.model.Alumno;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.EncodeHintType;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class CredencialController {

    // ===== Formulario =====
    @FXML private TextField txtMatricula, txtNombre, txtPaterno, txtMaterno, txtCurp, txtNss, txtVigencia;
    @FXML private DatePicker dpFechaEmision;
    @FXML private ImageView imgAlumno, imgBarcode;

    // ===== Plantilla (solo vista) =====
    @FXML private StackPane paneAnverso;
    @FXML private ImageView imgFotoPlantilla, imgBarcodePlantilla;
    @FXML private Label lblNombreLinea1, lblNombreLinea2, lblCurp, lblNss, lblNoControl;

    private byte[] fotoActual;

    // Fondos posibles (.png/.jpg con/sin /assets)
    private static final String[] FONDO_CANDIDATES = {
            "/cbtis239/front/credencial_enfrente.jpg"

    };

    // ======= Ajustes de layout (anverso) =======
    private static final int W = 1000, H = 560; // tamaño canvas PDF
    private static final int FOTO_X = 88,  FOTO_Y = 190, FOTO_W = 210, FOTO_H = 260;
    private static final int BAR_X  = 88,  BAR_Y  = 460, BAR_W  = 210, BAR_H  = 70;
    private static final int TXT_X  = 345;       // columna valores
    private static final int LINE_H = 30;        // interlineado

    @FXML
    private void initialize() {
        imgAlumno.setPreserveRatio(true);
        imgAlumno.setFitWidth(220);
        imgAlumno.setFitHeight(280);

        imgBarcode.setPreserveRatio(true);
        imgBarcode.setFitWidth(280);
        imgBarcode.setFitHeight(90);

        dpFechaEmision.setEditable(false);
        txtVigencia.setEditable(false);

        Platform.runLater(() -> {
            if (openFondoStream() == null) {
                warn("No se encontró la imagen de fondo:\n" +
                        String.join("\n", FONDO_CANDIDATES) + "\nColócala en resources.");
            }
        });
    }

    // ===== Navegación =====
    @FXML
    private void onVolverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/cbtis239/front/views/Menu2.fxml"));
            Parent root = fx.load();
            Stage s = new Stage();
            s.setTitle("Menú Principal");
            s.setScene(new Scene(root));
            s.initStyle(StageStyle.UNDECORATED);
            s.setFullScreen(true);
            s.setFullScreenExitHint("");
            s.show();
            ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) { showErr("No se pudo volver al Menú", e); }
    }

    private Stage getStage() { return (Stage) txtMatricula.getScene().getWindow(); }

    // ===== Acciones =====
    @FXML
    private void onBuscar() {
        try {
            String m = trim(txtMatricula.getText());
            if (m.isEmpty()) { warn("Ingresa la matrícula."); return; }

            CredencialBO bo = new CredencialBO();
            Alumno a = bo.cargarAlumnoParaCredencial(m);
            if (a == null) { warn("No se encontró información para: " + m); limpiar(); return; }

            txtNombre.setText(nvl(a.getNombre()));
            txtPaterno.setText(nvl(a.getPaterno()));
            txtMaterno.setText(nvl(a.getMaterno()));
            txtCurp.setText(nvl(a.getCurp()));
            txtNss.setText(nvl(a.getNss()));
            setImageFromString(a.getFoto());

            if (dpFechaEmision.getValue() == null) dpFechaEmision.setValue(LocalDate.now());
            txtVigencia.setText(bo.calcularVigencia(m));

            // Código de barras (vista + plantilla debajo de la foto)
            imgBarcode.setImage(generarCodigoBarrasFX(m, 600, 180));
            if (imgBarcodePlantilla != null)
                imgBarcodePlantilla.setImage(generarCodigoBarrasFX(m, 210, 70));

            actualizarPlantilla(a);
        } catch (SQLException e) { showErr("Error al buscar alumno (SQL)", e);
        } catch (Exception e) { showErr("Error inesperado en la búsqueda", e); }
    }

    @FXML private void onGuardar() { info("Prototipo: esta vista no persiste en BD."); }

    @FXML
    private void onCargarImagen() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg;*.jpeg;*.png"));
            File f = fc.showOpenDialog(getStage());
            if (f == null) return;
            try (FileInputStream fis = new FileInputStream(f)) {
                fotoActual = fis.readAllBytes();
                Image img = new Image(new ByteArrayInputStream(fotoActual));
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
            }
        } catch (Exception e) { showErr("No se pudo cargar la imagen", e); }
    }

    @FXML private void onCancelar() { limpiar(); }

    @FXML
    private void onGenerarPDF() {
        try {
            if (trim(txtMatricula.getText()).isEmpty()) { warn("Primero busca un alumno."); return; }

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar credencial en PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fc.setInitialFileName("credencial_" + txtMatricula.getText().trim() + ".pdf");
            File out = fc.showSaveDialog(getStage());
            if (out == null) return;

            BufferedImage frente  = buildAnversoImage();
            BufferedImage reverso = buildReversoImage();

            try (PDDocument doc = new PDDocument()) {
                PDRectangle size = new PDRectangle(frente.getWidth(), frente.getHeight());

                PDPage p1 = new PDPage(size);
                doc.addPage(p1);
                var imgFront = LosslessFactory.createFromImage(doc, frente);
                try (PDPageContentStream cs = new PDPageContentStream(doc, p1)) {
                    cs.drawImage(imgFront, 0, 0, size.getWidth(), size.getHeight());
                }

                PDPage p2 = new PDPage(size);
                doc.addPage(p2);
                var imgBack = LosslessFactory.createFromImage(doc, reverso);
                try (PDPageContentStream cs = new PDPageContentStream(doc, p2)) {
                    cs.drawImage(imgBack, 0, 0, size.getWidth(), size.getHeight());
                }

                doc.save(out);
            }

            info("PDF generado correctamente:\n" + out.getAbsolutePath());
        } catch (Exception e) { showErr("No se pudo generar el PDF", e); }
    }

    // ===== Plantilla (labels) =====
    private void actualizarPlantilla(Alumno a) {
        String n = nvl(a.getNombre()).toUpperCase();
        String p = nvl(a.getPaterno()).toUpperCase();
        String m = nvl(a.getMaterno()).toUpperCase();

        String l1 = n, l2 = (p + " " + m).trim();
        if ((l1 + " " + l2).length() > 26 && n.contains(" ")) {
            int cut = n.indexOf(' ');
            l1 = n.substring(0, cut).trim();
            l2 = (n.substring(cut + 1) + " " + p + " " + m).trim();
        }

        lblNombreLinea1.setText(l1);
        lblNombreLinea2.setText(l2);
        lblCurp.setText(nvl(a.getCurp()).toUpperCase());
        lblNss.setText(nvl(a.getNss()));
        lblNoControl.setText(nvl(a.getMatricula()));
    }

    // ===== Dibujo ANVERSO (afinado) =====
    private BufferedImage buildAnversoImage() throws IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, W, H);

        // 1) fondo desde la vista  2) classpath
        boolean fondoPintado = false;
        try {
            if (paneAnverso != null && !paneAnverso.getChildren().isEmpty()
                    && paneAnverso.getChildren().get(0) instanceof ImageView iv
                    && iv.getImage() != null) {
                var fondoFX = iv.getImage();
                var fondoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(fondoFX, null);
                if (fondoBI != null) {
                    g.drawImage(fondoBI, 0, 0, W, H, null);
                    fondoPintado = true;
                }
            }
        } catch (Exception ignore) {}

        if (!fondoPintado) {
            try (InputStream is = openFondoStream()) {
                if (is != null) {
                    BufferedImage fondo = ImageIO.read(is);
                    g.drawImage(fondo, 0, 0, W, H, null);
                    fondoPintado = true;
                }
            }
        }
        if (!fondoPintado) {
            g.setColor(java.awt.Color.LIGHT_GRAY);
            g.drawString("FONDO NO ENCONTRADO", 20, 30);
        }

        // Foto (3 fuentes posibles)
        BufferedImage fotoBI = null;
        try {
            if (imgFotoPlantilla != null && imgFotoPlantilla.getImage() != null)
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(imgFotoPlantilla.getImage(), null);
            if (fotoBI == null && imgAlumno != null && imgAlumno.getImage() != null)
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(imgAlumno.getImage(), null);
            if (fotoBI == null && fotoActual != null && fotoActual.length > 0) {
                var fx = new Image(new ByteArrayInputStream(fotoActual));
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(fx, null);
            }
        } catch (Exception ignore) {}
        if (fotoBI != null) g.drawImage(fotoBI, FOTO_X, FOTO_Y, FOTO_W, FOTO_H, null);

        // Código de barras debajo de la foto (con quiet zone)
        try {
            String noCtrl = nvl(lblNoControl.getText());
            if (!noCtrl.isBlank()) {
                BufferedImage bc = generarCodigoBarrasBuffered(noCtrl, BAR_W * 2, BAR_H * 2, 6);
                java.awt.Image scaled = bc.getScaledInstance(BAR_W, BAR_H, java.awt.Image.SCALE_SMOOTH);
                g.drawImage(scaled, BAR_X, BAR_Y, BAR_W, BAR_H, null);
            }
        } catch (Exception ignore) {}

        // Valores (sin rótulos, el fondo ya los tiene)
        java.awt.Font f = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f);

        int y = 205 + LINE_H; // primera línea (ALUMNO)
        y = drawTextClipped(g, safeUpper(lblNombreLinea1), TXT_X, y, 600) + LINE_H;
        y = drawTextClipped(g, safeUpper(lblNombreLinea2), TXT_X, y, 600) + LINE_H;
        y += 4;
        y = drawTextClipped(g, safeUpper(lblCurp), TXT_X, y, 620) + LINE_H;
        y = drawTextClipped(g, nvl(lblNss.getText()), TXT_X, y, 620) + LINE_H;
        drawTextClipped(g, nvl(lblNoControl.getText()), TXT_X, y, 620);

        g.dispose();
        return img;
    }

    // ===== Dibujo REVERSO =====
    private BufferedImage buildReversoImage() {
        int M = 40;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        g.setColor(java.awt.Color.WHITE); g.fillRect(0, 0, W, H);

        java.awt.Color rojo = new java.awt.Color(155, 0, 0);
        java.awt.Font fT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28);
        java.awt.Font f  = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24);
        java.awt.Font fs = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 20);

        g.setColor(rojo); g.setFont(fT); g.drawString("CREDENCIAL — REVERSO", M, M + 20);

        int y = M + 70;
        g.setColor(java.awt.Color.DARK_GRAY); g.setFont(f);
        g.drawString("Alumno:", M, y); y += 36;
        g.setFont(fs); g.drawString(nvl(txtNombre.getText()).toUpperCase(), M, y); y += 30;
        g.drawString((nvl(txtPaterno.getText()) + " " + nvl(txtMaterno.getText())).toUpperCase(), M, y); y += 40;

        g.setFont(f); g.setColor(java.awt.Color.DARK_GRAY);
        g.drawString("CURP: " + nvl(txtCurp.getText()).toUpperCase(), M, y); y += 36;
        g.drawString("Vigencia: " + nvl(txtVigencia.getText()), M, y);       y += 36;
        g.drawString("Fecha de emisión: " + (dpFechaEmision.getValue() == null ? "" : dpFechaEmision.getValue().toString()), M, y); y += 50;

        try {
            BufferedImage barcode = generarCodigoBarrasBuffered(nvl(txtMatricula.getText()), 800, 200, 8);
            int bx = M, by = H - M - barcode.getHeight();
            g.drawImage(barcode, bx, by, null);
            g.setFont(fs); g.setColor(java.awt.Color.GRAY);
            g.drawString("No. control: " + nvl(txtMatricula.getText()), bx, by - 10);
        } catch (Exception ignore) {}

        g.dispose();
        return img;
    }

    // ===== Código de barras =====
    private Image generarCodigoBarrasFX(String data, int w, int h) {
        try {
            if (data == null || data.isBlank()) return null;
            // quiet zone
            var hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 6);
            BitMatrix m = new Code128Writer().encode(data, BarcodeFormat.CODE_128, w, h, hints);
            WritableImage wi = new WritableImage(w, h);
            var pw = wi.getPixelWriter();
            final int B = 0xFF000000, W = 0xFFFFFFFF;
            for (int y=0; y<h; y++) for (int x=0; x<w; x++) pw.setArgb(x, y, m.get(x,y) ? B : W);
            return wi;
        } catch (Exception e) { error("No se pudo generar el código de barras: " + e.getMessage()); return null; }
    }

    private BufferedImage generarCodigoBarrasBuffered(String data, int w, int h, int marginPx) throws Exception {
        var hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, marginPx); // quiet zone
        BitMatrix m = new Code128Writer().encode(data, BarcodeFormat.CODE_128, w, h, hints);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y=0; y<h; y++) for (int x=0; x<w; x++) bi.setRGB(x, y, m.get(x,y) ? 0xFF000000 : 0xFFFFFFFF);
        return bi;
    }

    private InputStream openFondoStream() {
        for (String path : FONDO_CANDIDATES) {
            URL u = getClass().getResource(path);
            if (u != null) {
                try { return u.openStream(); } catch (IOException ignore) {}
            }
        }
        return null;
    }

    // ===== Helpers dibujo/texto =====
    private int drawTextClipped(java.awt.Graphics2D g, String text, int x, int y, int maxWidth) {
        if (text == null) text = "";
        var fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) { g.drawString(text, x, y); return y; }
        String ell = "…"; int wEll = fm.stringWidth(ell);
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(sb.toString()) + fm.charWidth(c) + wEll > maxWidth) break;
            sb.append(c);
        }
        g.drawString(sb.toString() + ell, x, y);
        return y;
    }

    private String safeUpper(Label l) { return (l == null || l.getText() == null) ? "" : l.getText().toUpperCase(); }

    // ===== Foto desde String (dataURL / base64 / archivo / URL) =====
    private void setImageFromString(String fotoStr) {
        try {
            if (fotoStr == null || fotoStr.isBlank()) {
                imgAlumno.setImage(null);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
                return;
            }
            if (fotoStr.startsWith("data:image")) {
                String base64 = fotoStr.substring(fotoStr.indexOf(",") + 1);
                byte[] bytes = Base64.getDecoder().decode(base64);
                Image img = new Image(new ByteArrayInputStream(bytes));
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                fotoActual = bytes; return;
            }
            String compact = fotoStr.replaceAll("\\s+","");
            if (compact.matches("^[A-Za-z0-9+/=]+$") && compact.length() % 4 == 0) {
                byte[] bytes = Base64.getDecoder().decode(compact);
                Image img = new Image(new ByteArrayInputStream(bytes));
                if (!img.isError()) {
                    imgAlumno.setImage(img);
                    if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                    fotoActual = bytes; return;
                }
            }
            Path p = Path.of(fotoStr);
            if (Files.exists(p)) {
                byte[] bytes = Files.readAllBytes(p);
                Image img = new Image(new ByteArrayInputStream(bytes));
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                fotoActual = bytes; return;
            }
            Image img = new Image(fotoStr.startsWith("http") ? fotoStr : "file:" + fotoStr);
            if (!img.isError()) {
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
            } else {
                imgAlumno.setImage(null);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
            }
        } catch (Exception e) {
            imgAlumno.setImage(null);
            if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
        }
    }

    // ===== Utilidades varias =====
    private void limpiar() {
        txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        txtCurp.clear(); txtNss.clear(); txtVigencia.clear();
        dpFechaEmision.setValue(null);
        imgAlumno.setImage(null);
        if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
        imgBarcode.setImage(null);
        if (imgBarcodePlantilla != null) imgBarcodePlantilla.setImage(null);
    }

    private static String trim(String s){ return s==null? "": s.trim(); }
    private static String nvl(String s){ return Objects.toString(s,""); }

    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void warn(String m){ new Alert(Alert.AlertType.WARNING, m).showAndWait(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
    private void showErr(String titulo, Exception e){
        e.printStackTrace();
        Throwable c = (e.getCause()!=null? e.getCause(): e);
        Alert a = new Alert(Alert.AlertType.ERROR, titulo + ":\n" + c.getClass().getSimpleName() + ": " + nvl(c.getMessage()));
        a.setHeaderText("Error");
        a.showAndWait();
    }
}

