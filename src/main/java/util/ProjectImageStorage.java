package util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Stores uploaded images inside the Maven web source tree so image files can
 * be committed to Git and shared with other developers.
 */
public final class ProjectImageStorage {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Pattern STORED_IMAGE_PATH = Pattern.compile(
            "assets/images/(food|combo|movies)/"
            + "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|jpeg|png|gif)",
            Pattern.CASE_INSENSITIVE);

    private ProjectImageStorage() {
    }

    public static String saveImage(Part part, String category,
            ServletContext context) throws IOException {
        if (part == null || part.getSize() <= 0) {
            return null;
        }
        if (!isAllowedCategory(category)) {
            throw new IllegalArgumentException("Loại ảnh không hợp lệ.");
        }
        if (part.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Ảnh không được vượt quá 5 MB.");
        }

        String submittedName = part.getSubmittedFileName();
        if (submittedName == null || submittedName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tệp ảnh hợp lệ.");
        }
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

        Path webappDirectory = resolveWebappDirectory(context);
        Path imageRoot = webappDirectory.resolve("assets").resolve("images")
                .toAbsolutePath().normalize();
        Path directory = imageRoot.resolve(category).normalize();
        if (!directory.startsWith(imageRoot)) {
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ.");
        }
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

        return "assets/images/" + category + "/" + fileName;
    }

    public static Path findImageFile(String imagePath, ServletContext context) {
        if (imagePath == null) {
            return null;
        }
        String normalized = imagePath.trim().replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!STORED_IMAGE_PATH.matcher(normalized).matches()) {
            return null;
        }

        String relativePath = normalized.substring("assets/images/".length());
        try {
            Path webappDirectory = resolveWebappDirectory(context);
            Path imageRoot = webappDirectory.resolve("assets").resolve("images")
                    .toAbsolutePath().normalize();
            Path imageFile = imageRoot.resolve(relativePath).normalize();
            if (!imageFile.startsWith(imageRoot) || !Files.isRegularFile(imageFile)) {
                return null;
            }
            return imageFile;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static void deleteUploadedImage(String imagePath, ServletContext context) {
        Path imageFile = findImageFile(imagePath, context);
        if (imageFile != null) {
            try {
                Files.deleteIfExists(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isAllowedCategory(String category) {
        return "food".equals(category) || "combo".equals(category)
                || "movies".equals(category);
    }

    private static Path resolveWebappDirectory(ServletContext context) throws IOException {
        String configuredRoot = System.getProperty("fcinema.project.dir");
        if (configuredRoot == null || configuredRoot.trim().isEmpty()) {
            configuredRoot = System.getenv("FCINEMA_PROJECT_DIR");
        }
        if (configuredRoot != null && !configuredRoot.trim().isEmpty()) {
            Path configuredPath = Paths.get(configuredRoot.trim()).toAbsolutePath().normalize();
            Path webapp = findWebappDirectory(configuredPath);
            if (webapp != null) {
                return webapp;
            }
            throw new IOException("Không tìm thấy src/main/webapp trong thư mục project đã cấu hình.");
        }

        List<Path> startingPoints = new ArrayList<>();
        String workingDirectory = System.getProperty("user.dir");
        if (workingDirectory != null && !workingDirectory.trim().isEmpty()) {
            startingPoints.add(Paths.get(workingDirectory));
        }
        if (context != null) {
            String realPath = context.getRealPath("/");
            if (realPath != null && !realPath.trim().isEmpty()) {
                startingPoints.add(Paths.get(realPath));
            }
            String classesPath = context.getRealPath("/WEB-INF/classes");
            if (classesPath != null && !classesPath.trim().isEmpty()) {
                startingPoints.add(Paths.get(classesPath));
            }
        }

        for (Path startingPoint : startingPoints) {
            Path webapp = findWebappDirectory(startingPoint);
            if (webapp != null) {
                return webapp;
            }
        }

        throw new IOException("Không tìm thấy thư mục source của F-Cinema. "
                + "Hãy mở project từ thư mục chứa pom.xml hoặc cấu hình "
                + "-Dfcinema.project.dir=<đường-dẫn-project>.");
    }

    private static Path findWebappDirectory(Path startingPoint) {
        if (startingPoint == null) {
            return null;
        }
        Path current = startingPoint.toAbsolutePath().normalize();
        if (!Files.isDirectory(current)) {
            current = current.getParent();
        }
        for (int depth = 0; current != null && depth < 12; depth++) {
            if (current.getFileName() != null
                    && "webapp".equalsIgnoreCase(current.getFileName().toString())
                    && Files.isDirectory(current)) {
                return current;
            }
            Path candidate = current.resolve("src").resolve("main").resolve("webapp");
            if (Files.isDirectory(candidate) && Files.isRegularFile(current.resolve("pom.xml"))) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }
}
