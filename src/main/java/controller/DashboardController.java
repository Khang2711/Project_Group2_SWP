package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import model.Cinema;
import model.User;
import service.DashboardService;

@WebServlet(urlPatterns = {"/admin/dashboard"})
public class DashboardController extends HttpServlet {

    private final DashboardService dashboardService
            = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        if (!"CINEMA_MANAGER".equals(
                session.getAttribute("roleName"))) {

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        User user = (User) session.getAttribute("user");

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh"));

        String fromValue = request.getParameter("fromDate");
        String toValue = request.getParameter("toDate");

        if (fromValue == null) {
            fromValue = today.withDayOfMonth(1).toString();
        }

        if (toValue == null) {
            toValue = today.toString();
        }

        request.setAttribute("fromDate", fromValue);
        request.setAttribute("toDate", toValue);

        try {
            Cinema cinema = dashboardService.getManagerCinema(
                    user.getUserId());

            if (cinema == null) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản không còn quyền Manager "
                        + "hoặc chưa được phân công rạp.");
                return;
            }

            request.setAttribute("cinema", cinema);

            LocalDate fromDate = dashboardService.parseDate(
                    fromValue, "Ngày bắt đầu");

            LocalDate toDate = dashboardService.parseDate(
                    toValue, "Ngày kết thúc");

            Map<String, Object> statistics
                    = dashboardService.getStatistics(
                            cinema.getCinemaId(), fromDate, toDate);

            request.setAttribute(
                    "totalBookings", statistics.get("totalBookings"));

            request.setAttribute(
                    "totalTickets", statistics.get("totalTickets"));

            request.setAttribute(
                    "totalRevenue", statistics.get("totalRevenue"));

            request.setAttribute(
                    "dailyRows", statistics.get("dailyRows"));

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Không thể tải Dashboard.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải thống kê. Vui lòng thử lại.");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/dashboard.jsp"
        ).forward(request, response);
    }
}
