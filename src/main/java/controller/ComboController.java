package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/combos", "/admin/combo-form"})
public class ComboController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/admin/combo-form".equals(request.getServletPath())
                ? "/WEB-INF/views/admin/combo-form.jsp"
                : "/WEB-INF/views/admin/combos.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
