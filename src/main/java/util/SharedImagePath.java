package util;

public final class SharedImagePath {
    private SharedImagePath() { }

    public static String validate(String value, String folder, String previous) {
        String path = value == null ? "" : value.trim();
        if (path.isEmpty()) return null;
        if (path.equals(previous)) return path; // Keep already saved legacy images.
        String prefix = "assets/images/" + folder + "/";
        if (!path.startsWith(prefix) || path.contains("..") || path.contains("\\")
                || path.contains("?") || path.contains("#")
                || !path.matches("[A-Za-z0-9_./-]+\\.(?i:jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("Ảnh phải có đường dẫn trong " + prefix
                    + " và định dạng JPG, PNG, GIF hoặc WEBP.");
        }
        return path;
    }
}
