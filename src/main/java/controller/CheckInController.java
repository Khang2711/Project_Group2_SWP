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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import model.Movie;
import model.Room;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;
import service.CheckInService;
import service.MovieService;
import service.ShowtimeService;

@WebServlet(urlPatterns = {"/staff/check-in"})
public class CheckInController extends HttpServlet {

    private CheckInService checkInService;
    private MovieService movieService;
    private ShowtimeService showtimeService;
    private BookingService bookingService;

    @Override
    public void init() {
        checkInService = new CheckInService();
        movieService = new MovieService();
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
            List<Showtime> showtimes
                    = checkInService.getCheckInShowtimes(user.getUserId());

            request.setAttribute("showtimes", showtimes);

            Map<Integer, Room> rooms
                    = showtimeService.getActiveRoomById();

            request.setAttribute("rooms", rooms);

            // Dùng ID phim để lấy tên khi hiển thị danh sách suất chiếu.
            Map<Integer, String> movieTitles = new HashMap<>();

            for (Movie movie : movieService.getAllMovies()) {
                movieTitles.put(movie.getMovieId(), movie.getTitle());
            }

            request.setAttribute("movieTitles", movieTitles);

            String idParam = request.getParameter("showtimeId");

            if (idParam != null && !idParam.trim().isEmpty()) {
                int showtimeId = parseShowtimeId(idParam);
                Showtime selected = null;

                for (Showtime showtime : showtimes) {
                    if (showtime.getShowtimeId() == showtimeId) {
                        selected = showtime;
                        break;
                    }
                }

                if (selected == null) {
                    throw new IllegalArgumentException(
                            "Suất chiếu không thuộc rạp hoặc đã hết hiệu lực.");
                }

                request.setAttribute("selectedShowtimeId", showtimeId);
                request.setAttribute("selectedShowtime", selected);

                if (code != null) {
                    Ticket ticket = checkInService.lookupTicket(
                            code, showtimeId, user.getUserId());

                    request.setAttribute("ticket", ticket);

                    request.setAttribute(
                            "seat",
                            bookingService.getSeatForTicket(ticket, selected));

                    String validation
                            = checkInService.validateCheckIn(ticket);

                    request.setAttribute(
                            "canCheckIn", validation == null);

                    if (validation != null
                            && success == null && error == null) {

                        request.setAttribute("error", validation);
                    }
                }

            } else if (code != null) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn suất chiếu.");
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
        String idParam = request.getParameter("showtimeId");

        try {
            // Mỗi mã xác nhận dùng một lần để tránh gửi lặp yêu cầu.
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

            checkInService.checkInTicket(
                    code,
                    parseShowtimeId(idParam),
                    user.getUserId());

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

        if (idParam != null && code != null) {
            url += "?showtimeId="
                    + URLEncoder.encode(idParam, "UTF-8")
                    + "&ticketCode="
                    + URLEncoder.encode(code, "UTF-8");
        }

        response.sendRedirect(url);
    }

    private int parseShowtimeId(String value) {
        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Suất chiếu không hợp lệ.");
        }
    }
}
