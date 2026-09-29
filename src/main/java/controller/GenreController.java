package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/genres", "/admin/genre-form"})
public class GenreController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/admin/genre-form".equals(request.getServletPath())
                ? "/WEB-INF/views/admin/genre-form.jsp"
                : "/WEB-INF/views/admin/genres.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
