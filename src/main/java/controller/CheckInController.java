package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import model.Movie;
import model.Room;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;
import service.CheckInService;
import service.ShowtimeService;

@WebServlet(urlPatterns = {"/staff/check-in"})
public class CheckInController extends HttpServlet {

    private CheckInService checkInService;
    private ShowtimeService showtimeService;
    private BookingService bookingService;

    @Override
    public void init() {
        checkInService = new CheckInService();
        showtimeService = new ShowtimeService();
        bookingService = new BookingService();
    }

    private User requireStaff(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return null;
        }

        if (!"STAFF".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return (User) session.getAttribute("user");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = requireStaff(request, response);

        if (user == null) {
            return;
        }

        HttpSession session = request.getSession();

        Object success = session.getAttribute("checkInSuccess");
        Object error = session.getAttribute("checkInError");

        session.removeAttribute("checkInSuccess");
        session.removeAttribute("checkInError");

        request.setAttribute("success", success);
        request.setAttribute("error", error);

        String code = request.getParameter("ticketCode");
        request.setAttribute("ticketCode", code);

        try {
            if (code != null && !code.trim().isEmpty()) {
                Ticket ticket = checkInService.lookupTicket(
                        code, user.getUserId());

                // Lấy suất chiếu trực tiếp từ thông tin của vé.
                Showtime showtime = showtimeService.getShowtimeById(
                        ticket.getShowtimeId());

                if (showtime == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy thông tin suất chiếu của vé.");
                }

                Movie movie = bookingService.getMovieForShowtime(showtime);

                Map<Integer, Room> rooms
                        = showtimeService.getActiveRoomById();

                Room room = rooms.get(showtime.getRoomId());

                Seat seat = bookingService.getSeatForTicket(
                        ticket, showtime);

                if (movie == null || room == null || seat == null) {
                    throw new IllegalArgumentException(
                            "Không thể tải đầy đủ thông tin của vé. "
                            + "Vui lòng liên hệ quản lý.");
                }

                request.setAttribute("ticket", ticket);
                request.setAttribute("showtime", showtime);
                request.setAttribute("movie", movie);
                request.setAttribute("room", room);
                request.setAttribute("seat", seat);

                String validation
                        = checkInService.validateCheckIn(ticket);

                request.setAttribute(
                        "canCheckIn", validation == null);

                if (validation != null
                        && success == null && error == null) {
                    request.setAttribute("error", validation);
                }
            }

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Không thể tra cứu vé. Vui lòng thử lại.");
        }

        if (session.getAttribute("checkInToken") == null) {
            session.setAttribute(
                    "checkInToken", UUID.randomUUID().toString());
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/check-in.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = requireStaff(request, response);

        if (user == null) {
            return;
        }

        HttpSession session = request.getSession();

        String code = request.getParameter("ticketCode");

        try {
            // Mỗi mã xác nhận chỉ được dùng một lần để tránh gửi lặp.
            synchronized (session) {
                String expected
                        = (String) session.getAttribute("checkInToken");

                String submitted = request.getParameter("token");

                if (expected == null || !expected.equals(submitted)) {
                    throw new IllegalArgumentException(
                            "Yêu cầu đã xử lý hoặc trang đã cũ. "
                            + "Vui lòng tra cứu lại.");
                }

                session.removeAttribute("checkInToken");
            }

            checkInService.checkInTicket(code, user.getUserId());

            session.setAttribute(
                    "checkInSuccess", "Check-in vé thành công.");

        } catch (IllegalArgumentException e) {
            session.setAttribute("checkInError", e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();

            session.setAttribute(
                    "checkInError",
                    "Không thể check-in. Vui lòng thử lại.");
        }

        // Chuyển sang GET để F5 không thực hiện lại thao tác cập nhật.
        String url = request.getContextPath() + "/staff/check-in";

        if (code != null && !code.trim().isEmpty()) {
            url += "?ticketCode="
                    + URLEncoder.encode(code.trim(), "UTF-8");
        }

        response.sendRedirect(url);
    }
}
