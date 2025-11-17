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

    // ===== Vista previa (anverso) =====
    @FXML private StackPane paneAnverso;
    @FXML private ImageView imgFotoPlantilla, imgBarcodePlantilla;
    @FXML private Label lblNombreLinea1, lblNombreLinea2, lblCurp, lblNss, lblNoControl;

    private byte[] fotoActual;

    // Fondo (classpath)
    private static final String[] FONDO_CANDIDATES = {
            "/cbtis239/front/credencial_enfrente.jpg",
            "/cbtis239/front/assets/credencial_frente.png",
            "/cbtis239/front/assets/credencial_enfrente.jpg"
    };

    private static final String[] FONDO_REVERSO_CANDIDATES = {
            "/cbtis239/front/credencial_reverso.png"
    };
    // ======= Canvas PDF =======
    private static final int W = 1000, H = 560;

    // ======= Foto y código de barras (no mueven el fondo) =======
    private static final int FOTO_X = 110, FOTO_Y = 240, FOTO_W = 160, FOTO_H = 190;
    private static final int BAR_X  = 80, BAR_Y  = 460, BAR_W  = 230, BAR_H  = 30;

    // ======= Posiciones del TEXTO NEGRO (independientes) =======
    private static final int TXT_X        = 340;  // columna de valores
    private static final int Y_NOMBRE1    = 265;  // ✔ ya correcta
    private static final int Y_NOMBRE2    = 299;  // apellidos
    private static final int Y_CURP       = 365;  // valor de CURP
    private static final int Y_NSS        = 420;  // valor de NSS
    private static final int Y_NOCONTROL  = 470;  // valor de No. CONTROL

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

            // Código de barras (vista)
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

    // ===== Plantilla (labels para vista) =====
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

    // ===== ANVERSO: sólo texto negro y barcode; fondo intacto =====
    private BufferedImage buildAnversoImage() throws IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo desde vista o classpath (NO mover)
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, W, H);
        boolean fondoPintado = false;
        try {
            if (paneAnverso != null && !paneAnverso.getChildren().isEmpty()
                    && paneAnverso.getChildren().get(0) instanceof ImageView iv
                    && iv.getImage() != null) {
                var fondoFX = iv.getImage();
                var fondoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(fondoFX, null);
                if (fondoBI != null) { g.drawImage(fondoBI, 0, 0, W, H, null); fondoPintado = true; }
            }
        } catch (Exception ignore) {}
        if (!fondoPintado) {
            try (InputStream is = openFondoStream()) {
                if (is != null) { BufferedImage f = ImageIO.read(is); g.drawImage(f, 0, 0, W, H, null); fondoPintado = true; }
            }
        }
        if (!fondoPintado) { g.setColor(java.awt.Color.LIGHT_GRAY); g.drawString("FONDO NO ENCONTRADO", 20, 30); }

        // Foto (3 fuentes)
        BufferedImage fotoBI = null;
        try {
            if (imgFotoPlantilla != null && imgFotoPlantilla.getImage() != null)
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(imgFotoPlantilla.getImage(), null);
            if (fotoBI == null && imgAlumno != null && imgAlumno.getImage() != null)
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(imgAlumno.getImage(), null);
            if (fotoBI == null && fotoActual != null && fotoActual.length > 0)
                fotoBI = javafx.embed.swing.SwingFXUtils.fromFXImage(new Image(new ByteArrayInputStream(fotoActual)), null);
        } catch (Exception ignore) {}
        if (fotoBI != null) g.drawImage(fotoBI, FOTO_X, FOTO_Y, FOTO_W, FOTO_H, null);

        // Código de barras (alto/angosto, debajo de la foto)
        try {
            String noCtrl = nvl(lblNoControl.getText());
            if (!noCtrl.isBlank()) {
                var hints = new java.util.EnumMap<EncodeHintType,Object>(EncodeHintType.class);
                hints.put(EncodeHintType.MARGIN, 8);
                BitMatrix m = new Code128Writer()
                        .encode(noCtrl, BarcodeFormat.CODE_128, BAR_W * 3, BAR_H * 2, hints);
                BufferedImage bc = new BufferedImage(m.getWidth(), m.getHeight(), BufferedImage.TYPE_INT_RGB);
                for (int yy = 0; yy < m.getHeight(); yy++)
                    for (int xx = 0; xx < m.getWidth(); xx++)
                        bc.setRGB(xx, yy, m.get(xx, yy) ? 0xFF000000 : 0xFFFFFFFF);
                java.awt.Image scaled = bc.getScaledInstance(BAR_W, BAR_H, java.awt.Image.SCALE_SMOOTH);
                g.drawImage(scaled, BAR_X, BAR_Y, BAR_W, BAR_H, null);
            }
        } catch (Exception ignore) {}

        // Texto negro en posiciones independientes
        java.awt.Font f = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f);

        drawTextClipped(g, safeUpper(lblNombreLinea1),     TXT_X, Y_NOMBRE1,   440);
        drawTextClipped(g, safeUpper(lblNombreLinea2),     TXT_X, Y_NOMBRE2,   440);
        drawTextClipped(g, safeUpper(lblCurp),             TXT_X, Y_CURP,      460);
        drawTextClipped(g, nvl(lblNss.getText()),          TXT_X, Y_NSS,       460);
        drawTextClipped(g, nvl(lblNoControl.getText()),    TXT_X, Y_NOCONTROL, 460);

        g.dispose();
        return img;
    }

    // ===== REVERSO simple =====
// ===== Dibujo REVERSO (usa plantilla oficial) =====
    private BufferedImage buildReversoImage() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        // Calidad
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo blanco por si falla la imagen
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, W, H);

        // 1) Pintar fondo del reverso
        boolean fondoPintado = false;
        try (InputStream is = openFondoReversoStream()) {
            if (is != null) {
                BufferedImage fondo = ImageIO.read(is);
                if (fondo != null) {
                    g.drawImage(fondo, 0, 0, W, H, null);
                    fondoPintado = true;
                }
            }
        } catch (Exception ignore) { }

        if (!fondoPintado) {
            g.setColor(java.awt.Color.LIGHT_GRAY);
            g.drawString("FONDO REVERSO NO ENCONTRADO", 20, 30);
        }

        // 2) Datos dinámicos: fecha de emisión y vigencia
        String vigencia = nvl(txtVigencia.getText());       // ej: "Ago-Dic 2025"
        String fechaStr  = "";
        if (dpFechaEmision.getValue() != null) {
            fechaStr = dpFechaEmision.getValue()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        g.setColor(java.awt.Color.BLACK);

        // Fecha de emisión (recuadro superior derecho)
        // Ajusta X/Y si quieres afinar la posición
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        drawTextClipped(g, fechaStr, 720, 120, 230);    // x=720, y=120, ancho máx 230

        // Vigencia (sobre los cuadros de VIGENCIA)
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        drawTextClipped(g, vigencia, 180, 190, 600);    // x=180, y=190

        g.dispose();
        return img;
    }


    // ===== Código de barras FX/Buffered =====
    private Image generarCodigoBarrasFX(String data, int w, int h) {
        try {
            if (data == null || data.isBlank()) return null;
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

    private BufferedImage generarCodigoBarrasBuffered(String data, int w, int h, int marginPx) {
        try {
            var hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, marginPx);
            BitMatrix m = new Code128Writer().encode(data, BarcodeFormat.CODE_128, w, h, hints);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int y=0; y<h; y++) for (int x=0; x<w; x++) bi.setRGB(x, y, m.get(x,y) ? 0xFF000000 : 0xFFFFFFFF);
            return bi;
        } catch (Exception e) { return null; }
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
    private InputStream openFondoReversoStream() {
        for (String path : FONDO_REVERSO_CANDIDATES) {
            URL u = getClass().getResource(path);
            if (u != null) {
                try {
                    return u.openStream();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }

    // ===== Helpers =====
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
    private static String trim(String s){ return s==null? "": s.trim(); }
    private static String nvl(String s){ return Objects.toString(s,""); }

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

    private void limpiar() {
        txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        txtCurp.clear(); txtNss.clear(); txtVigencia.clear();
        dpFechaEmision.setValue(null);
        imgAlumno.setImage(null);
        if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
        imgBarcode.setImage(null);
        if (imgBarcodePlantilla != null) imgBarcodePlantilla.setImage(null);
    }

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
