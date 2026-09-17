package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chỉ mở trang khung. TODO: xử lý nghiệp vụ và kiểm tra quyền trước khi triển khai. */
@WebServlet(urlPatterns = {"/admin/seats", "/admin/seat-form"})
public class SeatController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view;
        switch (request.getServletPath()) {
            case "/admin/seats": view = "/WEB-INF/views/admin/seats.jsp"; break;
            case "/admin/seat-form": view = "/WEB-INF/views/admin/seat-form.jsp"; break;
            default: response.sendError(404); return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }
    // TODO: doPost nhận dữ liệu form, validate rồi gọi service. Chưa xử lý thêm/sửa/xóa.
}
