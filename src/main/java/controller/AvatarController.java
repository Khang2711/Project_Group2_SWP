package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import service.AvatarService;

@WebServlet("/avatars/*")
public class AvatarController extends HttpServlet {

    private final AvatarService avatarService = new AvatarService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String avatarPath = "/avatars" + request.getPathInfo();

        if (!avatarService.isAvatarPath(avatarPath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path file = avatarService.getDirectory().resolve(
                avatarPath.substring("/avatars/".length()));

        if (!Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String type = avatarPath.endsWith(".png")
                ? "image/png"
                : avatarPath.endsWith(".gif")
                        ? "image/gif"
                        : "image/jpeg";

        response.setContentType(type);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.setContentLengthLong(Files.size(file));

        Files.copy(file, response.getOutputStream());
    }
}