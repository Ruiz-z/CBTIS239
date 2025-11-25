package cbtis239.front.ui.users;

import cbtis239.bo.CredencialBO;
import cbtis239.bo.DirectorBO;
import cbtis239.model.Alumno;
import cbtis239.model.Director;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
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
import java.time.format.DateTimeFormatter;
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

    // ===== Datos binarios =====
    private byte[] fotoActual;
    private byte[] firmaActual;           // firma del alumno
    private byte[] firmaDirectorActual;   // firma del director

    // ===== Recursos de fondo =====
    private static final String[] FONDO_CANDIDATES = {
            "/cbtis239/front/credencial_enfrente.jpg",
            "/cbtis239/front/assets/credencial_frente.png",
            "/cbtis239/front/assets/credencial_enfrente.jpg"
    };

    private static final String[] FONDO_REVERSO_CANDIDATES = {
            "/cbtis239/front/credencial_reverso.png"
    };

    // Firma estática de respaldo del director
    private static final String FIRMA_DIRECTOR_PATH =
            "/cbtis239/front/firmadirector.png";

    // ===== Tamaño del canvas PDF =====
    private static final int W = 1000, H = 560;

    // ===== Foto y código de barras (anverso) =====
    private static final int FOTO_X = 110, FOTO_Y = 240, FOTO_W = 160, FOTO_H = 190;
    private static final int BAR_X  = 80,  BAR_Y  = 460, BAR_W  = 230, BAR_H  = 30;

    // ===== Posiciones de texto negro (anverso) =====
    private static final int TXT_X        = 340;
    private static final int Y_NOMBRE1    = 265;
    private static final int Y_NOMBRE2    = 299;
    private static final int Y_CURP       = 365;
    private static final int Y_NSS        = 420;
    private static final int Y_NOCONTROL  = 470;

    // ===== Posiciones reverso =====
    private static final int REV_FECHA_X = 700, REV_FECHA_Y = 120;
    private static final int REV_VIG_X   = 100, REV_VIG_Y   = 240;

    // Firmas en reverso
    private static final int FIRMA_ALUMNO_X = 160,  FIRMA_ALUMNO_Y = 310,
            FIRMA_ALUMNO_W = 200, FIRMA_ALUMNO_H = 80;
    private static final int FIRMA_DIR_X    = 500, FIRMA_DIR_Y    = 310,
            FIRMA_DIR_W    = 360, FIRMA_DIR_H    = 80;

    // Texto del nombre del director en el reverso
    // Ajusta estos valores según tu diseño
    private static final int DIR_NOMBRE_X = 500;
    private static final int DIR_NOMBRE_Y = 410;
    private static final int DIR_NOMBRE_MAX_W = 360;

    // ===== BOs =====
    private final CredencialBO bo = new CredencialBO();
    private final DirectorBO directorBO = new DirectorBO();

    // ===== Director actual =====
    private Director directorActual;

    // ================== INIT ==================
    @FXML
    private void initialize() {
        imgAlumno.setPreserveRatio(true);
        imgAlumno.setFitWidth(220);
        imgAlumno.setFitHeight(280);

        imgBarcode.setPreserveRatio(true);
        imgBarcode.setFitWidth(280);
        imgBarcode.setFitHeight(90);

        txtNombre.setEditable(false);
        txtPaterno.setEditable(false);
        txtMaterno.setEditable(false);
        txtCurp.setEditable(false);
        txtNss.setEditable(false);
        txtVigencia.setEditable(false);

        dpFechaEmision.setEditable(false);
        dpFechaEmision.setDisable(true);

        Platform.runLater(() -> {
            if (openFondoStream() == null) {
                warn("No se encontró la imagen de fondo:\n" +
                        String.join("\n", FONDO_CANDIDATES) + "\nColócala en resources.");
            }
            cargarDirector();
        });
    }

    private Stage getStage() {
        return (Stage) txtMatricula.getScene().getWindow();
    }

    // ================== DIRECTOR ==================
    private void cargarDirector() {
        try {
            directorActual = directorBO.obtenerDirector();
            if (directorActual != null) {
                firmaDirectorActual = directorActual.getFirma();
            } else {
                firmaDirectorActual = null;
            }
        } catch (SQLException e) {
            directorActual = null;
            firmaDirectorActual = null;
            error("No se pudo cargar la información del director:\n" + nvl(e.getMessage()));
        }
    }

    // ================== NAVEGACIÓN ==================
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
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            showErr("No se pudo volver al Menú", e);
        }
    }

    // ================== ACCIONES ==================
    @FXML
    private void onBuscar() {
        try {
            String m = trim(txtMatricula.getText());
            if (m.isEmpty()) {
                warn("Ingresa la matrícula.");
                return;
            }

            Alumno a = bo.cargarAlumnoParaCredencial(m);
            if (a == null) {
                warn("No se encontró información para: " + m);
                limpiar();
                return;
            }

            txtNombre.setText(nvl(a.getNombre()));
            txtPaterno.setText(nvl(a.getPaterno()));
            txtMaterno.setText(nvl(a.getMaterno()));
            txtCurp.setText(nvl(a.getCurp()));
            txtNss.setText(nvl(a.getNss()));

            // Foto y firma desde BD
            setImageFromString(a.getFoto());
            setFirmaFromString(a.getFirma());

            if (dpFechaEmision.getValue() == null)
                dpFechaEmision.setValue(LocalDate.now());
            txtVigencia.setText(bo.calcularVigencia(m));

            // Código de barras en la vista
            imgBarcode.setImage(generarCodigoBarrasFX(m, 600, 180));
            if (imgBarcodePlantilla != null)
                imgBarcodePlantilla.setImage(generarCodigoBarrasFX(m, 210, 70));

            actualizarPlantilla(a);

        } catch (SQLException e) {
            String msg = nvl(e.getMessage());
            if (msg.toLowerCase().contains("no existe")) {
                warn(msg);
                limpiar();
            } else {
                showErr("Error al buscar alumno (SQL)", e);
            }
        } catch (Exception e) {
            showErr("Error inesperado en la búsqueda", e);
        }
    }

    @FXML
    private void onGuardar() {
        info("Esta pantalla solo genera la credencial en PDF. No se guarda información desde aquí.");
    }

    @FXML
    private void onCargarImagen() {
        info("La fotografía se toma automáticamente del registro del alumno.\nNo se selecciona manualmente en esta pantalla.");
    }

    @FXML
    private void onCancelar() {
        limpiar();
    }

    @FXML
    private void onGenerarPDF() {
        try {
            if (trim(txtMatricula.getText()).isEmpty()) {
                warn("Primero busca un alumno.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar credencial en PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fc.setInitialFileName("credencial_" + txtMatricula.getText().trim() + ".pdf");
            File out = fc.showSaveDialog(getStage());
            if (out == null) return;

            BufferedImage frente = buildAnversoImage();
            BufferedImage reverso = buildReversoImage();

            try (PDDocument doc = new PDDocument()) {
                PDRectangle size = new PDRectangle(frente.getWidth(), frente.getHeight());

                // Página 1 - anverso
                PDPage p1 = new PDPage(size);
                doc.addPage(p1);
                var imgFront = LosslessFactory.createFromImage(doc, frente);
                try (PDPageContentStream cs = new PDPageContentStream(doc, p1)) {
                    cs.drawImage(imgFront, 0, 0, size.getWidth(), size.getHeight());
                }

                // Página 2 - reverso
                PDPage p2 = new PDPage(size);
                doc.addPage(p2);
                var imgBack = LosslessFactory.createFromImage(doc, reverso);
                try (PDPageContentStream cs = new PDPageContentStream(doc, p2)) {
                    cs.drawImage(imgBack, 0, 0, size.getWidth(), size.getHeight());
                }

                doc.save(out);
            }

            info("PDF generado correctamente:\n" + out.getAbsolutePath());

        } catch (Exception e) {
            showErr("No se pudo generar el PDF", e);
        }
    }

    // ================== PLANTILLA (labels de vista) ==================
    private void actualizarPlantilla(Alumno a) {
        String n = nvl(a.getNombre()).toUpperCase();
        String p = nvl(a.getPaterno()).toUpperCase();
        String m = nvl(a.getMaterno()).toUpperCase();

        String l1 = n;
        String l2 = (p + " " + m).trim();

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

    // ================== ANVERSO ==================
    private BufferedImage buildAnversoImage() throws IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, W, H);
        boolean fondoPintado = false;

        try {
            if (paneAnverso != null && !paneAnverso.getChildren().isEmpty()
                    && paneAnverso.getChildren().get(0) instanceof ImageView iv
                    && iv.getImage() != null) {
                var fondoFX = iv.getImage();
                var fondoBI = SwingFXUtils.fromFXImage(fondoFX, null);
                if (fondoBI != null) {
                    g.drawImage(fondoBI, 0, 0, W, H, null);
                    fondoPintado = true;
                }
            }
        } catch (Exception ignore) { }

        if (!fondoPintado) {
            try (InputStream is = openFondoStream()) {
                if (is != null) {
                    BufferedImage f = ImageIO.read(is);
                    g.drawImage(f, 0, 0, W, H, null);
                    fondoPintado = true;
                }
            }
        }
        if (!fondoPintado) {
            g.setColor(java.awt.Color.LIGHT_GRAY);
            g.drawString("FONDO NO ENCONTRADO", 20, 30);
        }

        // Foto
        BufferedImage fotoBI = null;
        try {
            if (imgFotoPlantilla != null && imgFotoPlantilla.getImage() != null)
                fotoBI = SwingFXUtils.fromFXImage(imgFotoPlantilla.getImage(), null);
            if (fotoBI == null && imgAlumno != null && imgAlumno.getImage() != null)
                fotoBI = SwingFXUtils.fromFXImage(imgAlumno.getImage(), null);
            if (fotoBI == null && fotoActual != null && fotoActual.length > 0)
                fotoBI = SwingFXUtils.fromFXImage(
                        new Image(new ByteArrayInputStream(fotoActual)), null);
        } catch (Exception ignore) { }
        if (fotoBI != null)
            g.drawImage(fotoBI, FOTO_X, FOTO_Y, FOTO_W, FOTO_H, null);

        // Código de barras
        try {
            String noCtrl = nvl(lblNoControl.getText());
            if (!noCtrl.isBlank()) {
                var hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
                hints.put(EncodeHintType.MARGIN, 8);
                BitMatrix m = new Code128Writer()
                        .encode(noCtrl, BarcodeFormat.CODE_128, BAR_W * 3, BAR_H * 2, hints);
                BufferedImage bc = new BufferedImage(
                        m.getWidth(), m.getHeight(), BufferedImage.TYPE_INT_RGB);
                for (int yy = 0; yy < m.getHeight(); yy++)
                    for (int xx = 0; xx < m.getWidth(); xx++)
                        bc.setRGB(xx, yy, m.get(xx, yy) ? 0xFF000000 : 0xFFFFFFFF);
                java.awt.Image scaled = bc.getScaledInstance(
                        BAR_W, BAR_H, java.awt.Image.SCALE_SMOOTH);
                g.drawImage(scaled, BAR_X, BAR_Y, BAR_W, BAR_H, null);
            }
        } catch (Exception ignore) { }

        // Texto negro
        java.awt.Font f = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f);

        drawTextClipped(g, safeUpper(lblNombreLinea1), TXT_X, Y_NOMBRE1, 440);
        drawTextClipped(g, safeUpper(lblNombreLinea2), TXT_X, Y_NOMBRE2, 440);
        drawTextClipped(g, safeUpper(lblCurp), TXT_X, Y_CURP, 460);
        drawTextClipped(g, nvl(lblNss.getText()), TXT_X, Y_NSS, 460);
        drawTextClipped(g, nvl(lblNoControl.getText()), TXT_X, Y_NOCONTROL, 460);

        g.dispose();
        return img;
    }

    // ================== REVERSO ==================
    private BufferedImage buildReversoImage() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();

        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, W, H);
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

        // Datos dinámicos
        String vigencia = nvl(txtVigencia.getText());
        String fechaStr = "";
        if (dpFechaEmision.getValue() != null) {
            fechaStr = dpFechaEmision.getValue()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        g.setColor(java.awt.Color.BLACK);

        // Fecha de emisión
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        drawTextClipped(g, fechaStr, REV_FECHA_X, REV_FECHA_Y, 230);

        // Vigencia (texto más pequeño)
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        drawTextClipped(g, vigencia, REV_VIG_X, REV_VIG_Y, 600);

        // Firma del alumno
        if (firmaActual != null && firmaActual.length > 0) {
            try {
                Image fx = new Image(new ByteArrayInputStream(firmaActual));
                BufferedImage firmaBI = SwingFXUtils.fromFXImage(fx, null);
                if (firmaBI != null) {
                    g.drawImage(firmaBI, FIRMA_ALUMNO_X, FIRMA_ALUMNO_Y,
                            FIRMA_ALUMNO_W, FIRMA_ALUMNO_H, null);
                }
            } catch (Exception ignore) { }
        }

        // Firma del director (dinámica, desde BD; si no hay, usa la imagen estática)
        try {
            BufferedImage firmaDirBI = null;

            if (firmaDirectorActual != null && firmaDirectorActual.length > 0) {
                Image fxDir = new Image(new ByteArrayInputStream(firmaDirectorActual));
                firmaDirBI = SwingFXUtils.fromFXImage(fxDir, null);
            } else {
                try (InputStream is = getClass().getResourceAsStream(FIRMA_DIRECTOR_PATH)) {
                    if (is != null) {
                        firmaDirBI = ImageIO.read(is);
                    }
                }
            }

            if (firmaDirBI != null) {
                g.drawImage(firmaDirBI, FIRMA_DIR_X, FIRMA_DIR_Y,
                        FIRMA_DIR_W, FIRMA_DIR_H, null);
            }
        } catch (Exception ignore) { }

        // Nombre del director en texto
        if (directorActual != null) {
            String nombreDir = directorActual.getNombreCompleto().toUpperCase();
            g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
            g.setColor(java.awt.Color.RED); // si lo quieres rojo como en el ejemplo
            drawTextClipped(g, nombreDir, DIR_NOMBRE_X, DIR_NOMBRE_Y, DIR_NOMBRE_MAX_W);
        }

        g.dispose();
        return img;
    }

    // ================== CÓDIGO DE BARRAS ==================
    private Image generarCodigoBarrasFX(String data, int w, int h) {
        try {
            if (data == null || data.isBlank()) return null;
            var hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 6);
            BitMatrix m = new Code128Writer().encode(data, BarcodeFormat.CODE_128, w, h, hints);
            WritableImage wi = new WritableImage(w, h);
            var pw = wi.getPixelWriter();
            final int B = 0xFF000000, W = 0xFFFFFFFF;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    pw.setArgb(x, y, m.get(x, y) ? B : W);
            return wi;
        } catch (Exception e) {
            error("No se pudo generar el código de barras: " + e.getMessage());
            return null;
        }
    }

    // ================== FONDOS ==================
    private InputStream openFondoStream() {
        for (String path : FONDO_CANDIDATES) {
            URL u = getClass().getResource(path);
            if (u != null) {
                try { return u.openStream(); } catch (IOException ignore) { }
            }
        }
        return null;
    }

    private InputStream openFondoReversoStream() {
        for (String path : FONDO_REVERSO_CANDIDATES) {
            URL u = getClass().getResource(path);
            if (u != null) {
                try { return u.openStream(); } catch (IOException ignore) { }
            }
        }
        return null;
    }

    // ================== HELPERS ==================
    private int drawTextClipped(java.awt.Graphics2D g, String text, int x, int y, int maxWidth) {
        if (text == null) text = "";
        var fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) {
            g.drawString(text, x, y);
            return y;
        }
        String ell = "…";
        int wEll = fm.stringWidth(ell);
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(sb.toString()) + fm.charWidth(c) + wEll > maxWidth) break;
            sb.append(c);
        }
        g.drawString(sb.toString() + ell, x, y);
        return y;
    }

    private String safeUpper(Label l) {
        return (l == null || l.getText() == null) ? "" : l.getText().toUpperCase();
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static String nvl(String s)   { return Objects.toString(s, ""); }

    // ---------- Foto desde String ----------
    private void setImageFromString(String fotoStr) {
        try {
            if (fotoStr == null || fotoStr.isBlank()) {
                imgAlumno.setImage(null);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
                fotoActual = null;
                return;
            }

            if (fotoStr.startsWith("data:image")) {
                String base64 = fotoStr.substring(fotoStr.indexOf(",") + 1);
                byte[] bytes = Base64.getDecoder().decode(base64);
                fotoActual = bytes;
                Image img = new Image(new ByteArrayInputStream(bytes));
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                return;
            }

            String compact = fotoStr.replaceAll("\\s+", "");
            if (compact.matches("^[A-Za-z0-9+/=]+$") && compact.length() % 4 == 0) {
                byte[] bytes = Base64.getDecoder().decode(compact);
                Image img = new Image(new ByteArrayInputStream(bytes));
                if (!img.isError()) {
                    fotoActual = bytes;
                    imgAlumno.setImage(img);
                    if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                    return;
                }
            }

            Path p = Path.of(fotoStr);
            if (Files.exists(p)) {
                byte[] bytes = Files.readAllBytes(p);
                fotoActual = bytes;
                Image img = new Image(new ByteArrayInputStream(bytes));
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
                return;
            }

            Image img = new Image(fotoStr.startsWith("http") ? fotoStr : "file:" + fotoStr);
            if (!img.isError()) {
                fotoActual = null;
                imgAlumno.setImage(img);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(img);
            } else {
                imgAlumno.setImage(null);
                if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
            }
        } catch (Exception e) {
            imgAlumno.setImage(null);
            if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
            fotoActual = null;
        }
    }

    // ---------- Firma desde String (alumno) ----------
    private void setFirmaFromString(String firmaStr) {
        try {
            if (firmaStr == null || firmaStr.isBlank()) {
                firmaActual = null;
                return;
            }

            if (firmaStr.startsWith("data:image")) {
                String base64 = firmaStr.substring(firmaStr.indexOf(",") + 1);
                firmaActual = Base64.getDecoder().decode(base64);
                return;
            }

            String compact = firmaStr.replaceAll("\\s+", "");
            if (compact.matches("^[A-Za-z0-9+/=]+$") && compact.length() % 4 == 0) {
                byte[] bytes = Base64.getDecoder().decode(compact);
                Image test = new Image(new ByteArrayInputStream(bytes));
                if (!test.isError()) {
                    firmaActual = bytes;
                    return;
                }
            }

            Path p = Path.of(firmaStr);
            if (Files.exists(p)) {
                firmaActual = Files.readAllBytes(p);
                return;
            }

            if (firmaStr.startsWith("http")) {
                try (InputStream is = new URL(firmaStr).openStream();
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    is.transferTo(bos);
                    firmaActual = bos.toByteArray();
                    return;
                }
            }

            firmaActual = null;
        } catch (Exception e) {
            firmaActual = null;
        }
    }

    // ---------- Limpieza y mensajes ----------
    private void limpiar() {
        txtMatricula.clear();
        txtNombre.clear();
        txtPaterno.clear();
        txtMaterno.clear();
        txtCurp.clear();
        txtNss.clear();
        txtVigencia.clear();
        dpFechaEmision.setValue(null);

        imgAlumno.setImage(null);
        if (imgFotoPlantilla != null) imgFotoPlantilla.setImage(null);
        imgBarcode.setImage(null);
        if (imgBarcodePlantilla != null) imgBarcodePlantilla.setImage(null);

        fotoActual = null;
        firmaActual = null;
    }

    private void info(String m)  { showAlert(Alert.AlertType.INFORMATION, "Mensaje", m); }
    private void warn(String m)  { showAlert(Alert.AlertType.WARNING, "Aviso", m); }
    private void error(String m) { showAlert(Alert.AlertType.ERROR, "Error", m); }

    private void showErr(String titulo, Exception e) {
        e.printStackTrace();
        Throwable c = (e.getCause() != null ? e.getCause() : e);
        String msg = titulo + ":\n" + nvl(c.getMessage());
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }

    private void showAlert(Alert.AlertType type, String header, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(message);
        if (txtMatricula != null && txtMatricula.getScene() != null) {
            Stage owner = (Stage) txtMatricula.getScene().getWindow();
            a.initOwner(owner);
            a.initModality(Modality.WINDOW_MODAL);
        }
        a.showAndWait();
    }
}
