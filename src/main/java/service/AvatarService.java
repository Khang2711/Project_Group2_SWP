package service;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class AvatarService {

    public Path getDirectory() {
        String base = System.getProperty(
                "catalina.base",
                System.getProperty("user.home"));

        return Paths.get(base, "fcinema-uploads", "avatars");
    }

    public boolean isAvatarPath(String value) {
        return value != null && value.matches(
                "/avatars/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}"
                + "-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|gif)");
    }

    public String save(Part part) throws IOException {
        if (part.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Ảnh đại diện tối đa 5 MB.");
        }

        String format;

        try (InputStream input = part.getInputStream();
                ImageInputStream imageInput =
                        ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new IllegalArgumentException(
                        "File ảnh không hợp lệ.");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Chỉ chấp nhận ảnh JPG, PNG hoặc GIF.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput);

                format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                if ("jpeg".equals(format)) {
                    format = "jpg";
                }

                if (!"jpg".equals(format)
                        && !"png".equals(format)
                        && !"gif".equals(format)) {

                    throw new IllegalArgumentException(
                            "Chỉ chấp nhận ảnh JPG, PNG hoặc GIF.");
                }

                if (reader.getWidth(0) > 6000
                        || reader.getHeight(0) > 6000) {

                    throw new IllegalArgumentException(
                            "Kích thước ảnh tối đa 6000 × 6000 pixel.");
                }

                reader.read(0);

            } finally {
                reader.dispose();
            }
        }

        Files.createDirectories(getDirectory());

        String fileName = UUID.randomUUID() + "." + format;
        Path target = getDirectory().resolve(fileName);

        try (InputStream input = part.getInputStream()) {
            try {
                Files.copy(input, target);

            } catch (IOException e) {
                Files.deleteIfExists(target);
                throw e;
            }
        }

        return "/avatars/" + fileName;
    }

    public void delete(String avatarPath) throws IOException {
        if (isAvatarPath(avatarPath)) {
            Files.deleteIfExists(
                    getDirectory().resolve(
                            avatarPath.substring("/avatars/".length())));
        }
    }
}