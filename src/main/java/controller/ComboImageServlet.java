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
import util.ComboImageStorage;

/** Phục vụ ảnh combo được lưu ngoài thư mục triển khai WAR. */
@WebServlet("/combo-images/*")
public class ComboImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = pathInfo.substring(1);
        Path imagePath = ComboImageStorage.findUploadedImage(fileName);
        if (imagePath == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String lowerName = fileName.toLowerCase(java.util.Locale.ROOT);
        if (lowerName.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (lowerName.endsWith(".gif")) {
            response.setContentType("image/gif");
        } else {
            response.setContentType("image/jpeg");
        }

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.setContentLengthLong(Files.size(imagePath));

        try (InputStream input = Files.newInputStream(imagePath);
             OutputStream output = response.getOutputStream()) {
            input.transferTo(output);
        }
    }
}
