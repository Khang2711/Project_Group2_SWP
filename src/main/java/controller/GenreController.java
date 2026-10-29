package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Genre;
import service.GenreService;

@WebServlet(urlPatterns = {"/admin/genres", "/admin/genre-form"})
public class GenreController extends HttpServlet {

    private GenreService genreService = new GenreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String view;

        switch (request.getServletPath()) {
            case "/admin/genres":
                request.setAttribute("genres", genreService.getAllGenres());

                view = "/WEB-INF/views/admin/genres.jsp";
                break;

            case "/admin/genre-form":
                String id = request.getParameter("id");
                Genre genre = new Genre();
                genre.setStatus("ACTIVE");

                boolean isEdit = false;

                if (id != null && !id.trim().isEmpty()) {
                    try {
                        int genreId = Integer.parseInt(id);

                        genre = genreService.getGenreById(genreId);

                        if (genre == null) {
                            response.sendError(404, "Không tìm thấy thể loại.");
                            return;
                        }

                        isEdit = true;
                    } catch (Exception e) {
                        response.sendError(400, "Mã thể loại không hợp lệ");
                        return;
                    }
                }

                request.setAttribute("genre", genre);
                request.setAttribute("isEdit", isEdit);

                view = "/WEB-INF/views/admin/genre-form.jsp";
                break;

            default:
                response.sendError(404);
                return;
        }

        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String view;

        switch (request.getServletPath()) {

            case "/admin/genre-form":

                String id = request.getParameter("id");
                String genreName = request.getParameter("genreName");
                String description = request.getParameter("description");

                Genre genre = new Genre();
                genre.setGenreName(genreName);
                genre.setDescription(description);
                genre.setStatus(request.getParameter("status"));

                boolean isEdit = id != null && !id.trim().isEmpty();

                String error;

                if (isEdit) {

                    try {
                        genre.setGenreId(Integer.parseInt(id));

                    } catch (NumberFormatException e) {
                        response.sendError(400, "Mã thể loại không hợp lệ.");
                        return;
                    }

                    error = genreService.updateGenre(genre);

                } else {
                    error = genreService.createGenre(genre);
                }

                if (error != null) {

                    request.setAttribute("error", error);
                    request.setAttribute("genre", genre);
                    request.setAttribute("isEdit", isEdit);

                    view = "/WEB-INF/views/admin/genre-form.jsp";
                    break;
                }

                response.sendRedirect(request.getContextPath() + "/admin/genres?saved=1"
                );
                return;

            case "/admin/genres":

                int genreId;

                try {
                    genreId = Integer.parseInt(
                            request.getParameter("id")
                    );

                } catch (NumberFormatException e) {
                    response.sendError(400, "Mã thể loại không hợp lệ.");
                    return;
                }

                if ("delete".equals(request.getParameter("action"))) {

                    String deleteError = genreService.deleteGenre(genreId);

                    if (deleteError != null) {
                        request.setAttribute("error", deleteError);
                        request.setAttribute("genres", genreService.getAllGenres());

                        view = "/WEB-INF/views/admin/genres.jsp";
                        break;
                    }

                    response.sendRedirect(
                            request.getContextPath() + "/admin/genres?deleted=1"
                    );
                    return;
                }

                String newStatus = request.getParameter("status");

                String statusError = genreService.updateStatus(genreId, newStatus);

                if (statusError != null) {

                    request.setAttribute("error", statusError);
                    request.setAttribute("genres", genreService.getAllGenres());

                    view = "/WEB-INF/views/admin/genres.jsp";
                    break;
                }

                response.sendRedirect(request.getContextPath() + "/admin/genres?statusUpdated=1");
                return;

            default:
                response.sendError(404);
                return;
        }

        request.getRequestDispatcher(view).forward(request, response);
    }
}
