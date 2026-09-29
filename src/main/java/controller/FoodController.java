package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/food-items", "/admin/food-item-form"})
public class FoodController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = "/admin/food-item-form".equals(request.getServletPath())
                ? "/WEB-INF/views/admin/food-item-form.jsp"
                : "/WEB-INF/views/admin/food-items.jsp";
        request.getRequestDispatcher(view).forward(request, response);
    }
}
