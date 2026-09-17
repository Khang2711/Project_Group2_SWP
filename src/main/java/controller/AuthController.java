package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chỉ mở trang khung. TODO: xử lý nghiệp vụ và kiểm tra quyền trước khi triển khai. */
@WebServlet(urlPatterns = {"/auth/login", "/auth/logout", "/auth/register", "/auth/forgot-password", "/auth/reset-password"})
public class AuthController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view;
        switch (request.getServletPath()) {
            case "/auth/login": view = "/WEB-INF/views/auth/login.jsp"; break;
            case "/auth/logout": view = "/WEB-INF/views/auth/logout.jsp"; break;
            case "/auth/register": view = "/WEB-INF/views/auth/register.jsp"; break;
            case "/auth/forgot-password": view = "/WEB-INF/views/auth/forgot-password.jsp"; break;
            case "/auth/reset-password": view = "/WEB-INF/views/auth/reset-password.jsp"; break;
            default: response.sendError(404); return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }
    // TODO: doPost nhận dữ liệu form, validate rồi gọi service. Chưa xử lý thêm/sửa/xóa.
}
