package util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import jakarta.servlet.http.Part;

/**
 * Lưu ảnh combo bên ngoài thư mục triển khai ứng dụng để ảnh không bị mất
 * khi build hoặc deploy lại WAR.
 */
public final class ComboImageStorage {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final String URL_PREFIX = "combo-images/";

    private ComboImageStorage() {
    }

    public static String saveComboImage(Part part) throws IOException {
        if (part == null || part.getSize() <= 0) {
            return null;
        }

        if (part.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Ảnh combo không được vượt quá 5 MB.");
        }

        String submittedName = part.getSubmittedFileName();
        if (submittedName == null || submittedName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tệp ảnh hợp lệ.");
        }

        // Chỉ lấy tên cuối cùng, không dùng đường dẫn do trình duyệt gửi lên.
        submittedName = submittedName.replace('\\', '/');
        submittedName = submittedName.substring(submittedName.lastIndexOf('/') + 1);
        int dotIndex = submittedName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == submittedName.length() - 1) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG, PNG hoặc GIF.");
        }

        String extension = submittedName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!"jpg".equals(extension) && !"jpeg".equals(extension)
                && !"png".equals(extension) && !"gif".equals(extension)) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG, PNG hoặc GIF.");
        }

        BufferedImage image;
        try (InputStream input = part.getInputStream()) {
            image = ImageIO.read(input);
        }
        if (image == null) {
            throw new IllegalArgumentException("Tệp đã chọn không phải ảnh hợp lệ.");
        }
        if (image.getWidth() <= 0 || image.getHeight() <= 0
                || image.getWidth() > 10000 || image.getHeight() > 10000
                || (long) image.getWidth() * image.getHeight() > 40000000L) {
            throw new IllegalArgumentException("Kích thước ảnh quá lớn.");
        }

        Path directory = getComboImageDirectory();
        Files.createDirectories(directory);

        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path destination = directory.resolve(fileName).normalize();
        if (!destination.startsWith(directory)) {
            throw new IllegalArgumentException("Tên tệp ảnh không hợp lệ.");
        }

        String imageFormat = ("jpg".equals(extension) || "jpeg".equals(extension))
                ? "jpg" : extension;
        try (OutputStream output = Files.newOutputStream(
                destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            if (!ImageIO.write(image, imageFormat, output)) {
                throw new IllegalArgumentException("Không thể xử lý định dạng ảnh này.");
            }
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // Giữ nguyên lỗi ban đầu.
            }
            throw e;
        }

        return URL_PREFIX + fileName;
    }

    public static Path findUploadedImage(String fileName) {
        if (fileName == null
                || !fileName.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\\\.(jpg|jpeg|png|gif)")) {
            return null;
        }

        Path directory = getComboImageDirectory();
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            return null;
        }
        return file;
    }

    public static void deleteUploadedImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(URL_PREFIX)) {
            return;
        }

        Path file = findUploadedImage(imageUrl.substring(URL_PREFIX.length()));
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Path getComboImageDirectory() {
        String configuredRoot = System.getProperty("fcinema.upload.dir");
        Path root;
        if (configuredRoot != null && !configuredRoot.trim().isEmpty()) {
            root = Paths.get(configuredRoot.trim()).toAbsolutePath().normalize();
        } else {
            root = Paths.get(System.getProperty("user.home"),
                    "FCinema", "uploads").toAbsolutePath().normalize();
        }
        return root.resolve("combos").normalize();
    }
}
