package controller;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import model.User;

/** Same-origin fallback for browsers without BarcodeDetector. No frames are stored. */
@WebServlet(urlPatterns = {"/staff/qr-decode"})
public class QRDecodeServlet extends HttpServlet {
    private static final int MAX_BYTES = 1024 * 1024;
    private static final long MAX_PIXELS = 2_000_000;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("LOGIN_USER");
        if (user == null || !"ST".equals(user.getRoleID())) {
            response.setStatus(user == null ? 401 : 403);
            return;
        }
        synchronized (session) {
            Long last = (Long) session.getAttribute("QR_DECODE_AT");
            long now = System.currentTimeMillis();
            if (last != null && now - last < 300) {
                response.setStatus(429);
                return;
            }
            session.setAttribute("QR_DECODE_AT", now);
        }
        String type = request.getContentType();
        if (type == null || !(type.equalsIgnoreCase("image/jpeg") || type.equalsIgnoreCase("image/png"))) {
            response.setStatus(415);
            return;
        }
        byte[] bytes;
        try (InputStream input = request.getInputStream()) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        }
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            response.setStatus(bytes.length == 0 ? 400 : 413);
            return;
        }
        // Read dimensions before allocating the decompressed image.
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) { response.setStatus(400); return; }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!("JPEG".equalsIgnoreCase(format) || "PNG".equalsIgnoreCase(format))) {
                    response.setStatus(415); return;
                }
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > MAX_PIXELS) {
                    response.setStatus(413); return;
                }
                BufferedImage image = reader.read(0);
                int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
                String code = new QRCodeReader().decode(bitmap,
                        Collections.singletonMap(DecodeHintType.TRY_HARDER, Boolean.TRUE)).getText().trim();
                if (!code.matches("[A-Za-z0-9_-]{1,40}")) { response.setStatus(204); return; }
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"ticketID\":\"" + code + "\"}");
            } finally { reader.dispose(); }
        } catch (NotFoundException e) {
            response.setStatus(204);
        } catch (com.google.zxing.ChecksumException | com.google.zxing.FormatException e) {
            response.setStatus(204);
        } catch (IOException | IllegalArgumentException e) {
            response.setStatus(400);
        }
    }
}
