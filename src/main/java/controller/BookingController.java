package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chỉ mở trang khung. TODO: xử lý nghiệp vụ và kiểm tra quyền trước khi triển khai. */
@WebServlet(urlPatterns = {"/user/select-seats", "/user/checkout", "/user/bookings", "/user/booking-detail"})
public class BookingController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view;
        switch (request.getServletPath()) {
            case "/user/select-seats": view = "/WEB-INF/views/user/select-seats.jsp"; break;
            case "/user/checkout": view = "/WEB-INF/views/user/checkout.jsp"; break;
            case "/user/bookings": view = "/WEB-INF/views/user/bookings.jsp"; break;
            case "/user/booking-detail": view = "/WEB-INF/views/user/booking-detail.jsp"; break;
            default: response.sendError(404); return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }
    // TODO: doPost nhận dữ liệu form, validate rồi gọi service. Chưa xử lý thêm/sửa/xóa.
}
