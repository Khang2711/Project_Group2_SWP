package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chỉ mở trang khung. TODO: xử lý nghiệp vụ và kiểm tra quyền trước khi triển khai. */
@WebServlet(urlPatterns = {"/user/movies", "/user/movie-detail", "/admin/movies", "/admin/movie-form"})
public class MovieController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view;
        switch (request.getServletPath()) {
            case "/user/movies": view = "/WEB-INF/views/user/movies.jsp"; break;
            case "/user/movie-detail": view = "/WEB-INF/views/user/movie-detail.jsp"; break;
            case "/admin/movies": view = "/WEB-INF/views/admin/movies.jsp"; break;
            case "/admin/movie-form": view = "/WEB-INF/views/admin/movie-form.jsp"; break;
            default: response.sendError(404); return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }
    // TODO: doPost nhận dữ liệu form, validate rồi gọi service. Chưa xử lý thêm/sửa/xóa.
}
