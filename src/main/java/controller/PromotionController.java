package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Chỉ mở trang khung. TODO: xử lý nghiệp vụ và kiểm tra quyền trước khi triển khai. */
@WebServlet(urlPatterns = {"/user/apply-promo", "/admin/promotions", "/admin/promotion-form"})
public class PromotionController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view;
        switch (request.getServletPath()) {
            case "/user/apply-promo": view = "/WEB-INF/views/user/apply-promo.jsp"; break;
            case "/admin/promotions": view = "/WEB-INF/views/admin/promotions.jsp"; break;
            case "/admin/promotion-form": view = "/WEB-INF/views/admin/promotion-form.jsp"; break;
            default: response.sendError(404); return;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }
    // TODO: doPost nhận dữ liệu form, validate rồi gọi service. Chưa xử lý thêm/sửa/xóa.
}
