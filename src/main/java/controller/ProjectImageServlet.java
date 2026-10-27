package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import util.ProjectImageStorage;

/**
 * Serves images from the checked-in webapp assets folder. When running from a
 * packaged WAR, falls back to the image resource included in the WAR.
 */
@WebServlet("/assets/images/*")
public class ProjectImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String relativePath = pathInfo.substring(1);
        if (relativePath.contains("..") || relativePath.contains("\\\\")
                || !relativePath.matches("[A-Za-z0-9._/-]+")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String resourcePath = "/assets/images/" + relativePath;
        Path imageFile = ProjectImageStorage.findImageFile(
                resourcePath.substring(1), getServletContext());

        if (imageFile != null) {
            response.setContentType(resolveContentType(resourcePath));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "public, max-age=86400");
            response.setContentLengthLong(Files.size(imageFile));
            try (InputStream input = Files.newInputStream(imageFile);
                 OutputStream output = response.getOutputStream()) {
                input.transferTo(output);
            }
            return;
        }

        try (InputStream input = getServletContext().getResourceAsStream(resourcePath)) {
            if (input == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(resolveContentType(resourcePath));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "public, max-age=86400");
            try (OutputStream output = response.getOutputStream()) {
                input.transferTo(output);
            }
        }
    }

    private String resolveContentType(String resourcePath) {
        String contentType = getServletContext().getMimeType(resourcePath);
        if (contentType != null) {
            return contentType;
        }
        String lower = resourcePath.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
