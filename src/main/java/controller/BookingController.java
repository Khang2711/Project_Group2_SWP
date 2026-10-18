package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Booking;
import model.Movie;
import model.Promotion;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;

@WebServlet(urlPatterns = {
    "/user/select-seats",
    "/user/checkout",
    "/user/bookings",
    "/user/booking-detail"
})
public class BookingController extends HttpServlet {

    private BookingService bookingService;

    @Override
    public void init() {
        bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            switch (path) {

                case "/user/select-seats":
                    showSeatSelection(request, response);
                    break;

                case "/user/checkout":
                    response.sendRedirect(
                            request.getContextPath() + "/user/movies");
                    break;

                case "/user/bookings":
                    showBookings(request, response);
                    break;

                case "/user/booking-detail":
                    showBookingDetail(request, response);
                    break;

                default:
                    response.sendError(
                            HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (IllegalArgumentException e) {

            request.setAttribute("error", e.getMessage());

            request.getRequestDispatcher(
                    "/WEB-INF/views/error.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        try {
            if ("/user/select-seats".equals(path)) {
                prepareCheckout(request, response);
                return;
            }

            if ("/user/checkout".equals(path)) {

                String action = request.getParameter("action");

                // Áp hoặc bỏ mã chỉ tính lại tiền, chưa tạo booking.
                if ("APPLY_PROMO".equals(action)
                        || "REMOVE_PROMO".equals(action)) {

                    prepareCheckout(request, response);

                } else if (action == null
                        || "CONFIRM".equals(action)) {

                    createBooking(request, response);

                } else {
                    throw new IllegalArgumentException(
                            "Thao tác không hợp lệ.");
                }

                return;
            }

            response.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        } catch (IllegalArgumentException e) {

            if ("/user/select-seats".equals(path)
                    || "/user/checkout".equals(path)) {

                try {
                    showSeatSelectionWithError(
                            request,
                            response,
                            e.getMessage());

                } catch (SQLException sqlException) {
                    throw new ServletException(sqlException);
                }

                return;
            }

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showSeatSelection(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int showtimeId = parsePositiveInt(
                request.getParameter("showtimeId"),
                "Suất chiếu không hợp lệ.");

        loadSeatSelection(request, showtimeId);

        request.getRequestDispatcher(
                "/WEB-INF/views/user/select-seats.jsp"
        ).forward(request, response);
    }

    private void prepareCheckout(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int showtimeId = parsePositiveInt(
                request.getParameter("showtimeId"),
                "Suất chiếu không hợp lệ.");

        boolean fullRoom = "FULL_ROOM".equals(
                request.getParameter("bookingMode"));

        // Khi áp mã, dùng đúng danh sách ghế đã gửi từ checkout.
        boolean selectWholeRoom = fullRoom
                && "/user/select-seats".equals(
                        request.getServletPath());

        List<Seat> selectedSeats = bookingService.prepareSelectedSeats(
                showtimeId,
                request.getParameterValues("seatId"),
                selectWholeRoom);

        Map<Integer, BigDecimal> seatPrices
                = bookingService.getSeatPrices(
                        showtimeId,
                        selectedSeats);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Seat seat : selectedSeats) {

            BigDecimal price = seatPrices.get(
                    seat.getSeatId());

            if (price == null) {
                throw new IllegalArgumentException(
                        "Có ghế chưa có giá vé hợp lệ. "
                        + "Vui lòng chọn lại.");
            }

            subtotal = subtotal.add(price);
        }

        String action = request.getParameter("action");
        String promoCode = request.getParameter("promoCode");

        if (promoCode == null
                || "REMOVE_PROMO".equals(action)) {

            promoCode = "";
        }

        promoCode = promoCode.trim();

        Promotion promotion = null;
        BigDecimal discount = BigDecimal.ZERO;
        String promoError = null;

        try {
            if ("APPLY_PROMO".equals(action)
                    && promoCode.isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập mã khuyến mãi.");
            }

            promotion = bookingService.getValidPromotion(
                    promoCode,
                    subtotal);

            discount = bookingService.calculatePromotionDiscount(
                    promotion,
                    subtotal);

            if (promotion != null) {

                promoCode = promotion.getPromoCode();

                request.setAttribute(
                        "promoSuccess",
                        "Đã áp dụng mã " + promoCode + ".");

            } else if ("REMOVE_PROMO".equals(action)) {

                request.setAttribute(
                        "promoSuccess",
                        "Đã bỏ mã khuyến mãi.");
            }

        } catch (IllegalArgumentException e) {
            promoError = e.getMessage();
        }

        request.setAttribute(
                "showtime",
                bookingService.getBookableShowtime(showtimeId));

        request.setAttribute("selectedSeats", selectedSeats);
        request.setAttribute("seatPrices", seatPrices);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("fullRoom", fullRoom);
        request.setAttribute("promoCode", promoCode);
        request.setAttribute("appliedPromotion", promotion);
        request.setAttribute("discountAmount", discount);
        request.setAttribute(
                "finalAmount",
                subtotal.subtract(discount));
        request.setAttribute("promoError", promoError);

        request.getRequestDispatcher(
                "/WEB-INF/views/user/checkout.jsp"
        ).forward(request, response);
    }

    private void createBooking(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int customerId = getCurrentCustomerId(request);

        int showtimeId = parsePositiveInt(
                request.getParameter("showtimeId"),
                "Suất chiếu không hợp lệ.");

        try {
            // Service tự đọc giá và kiểm tra mã lại trước khi lưu.
            bookingService.createPendingBooking(
                    customerId,
                    showtimeId,
                    request.getParameterValues("seatId"),
                    request.getParameter("promoCode"));

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "checkoutError",
                    e.getMessage());

            prepareCheckout(request, response);
            return;
        }

        // Giữ chuyển hướng hiện tại sau khi tạo đơn PENDING.
        response.sendRedirect(
                request.getContextPath() + "/user/bookings");
    }

    private void loadSeatSelection(
            HttpServletRequest request,
            int showtimeId) throws SQLException {

        Showtime showtime
                = bookingService.getBookableShowtime(showtimeId);

        List<Seat> seats
                = bookingService.getSeatsForShowtime(showtimeId);

        Set<Integer> occupiedSeatIds
                = bookingService.getOccupiedSeatIds(showtimeId);

        Map<Integer, BigDecimal> seatPrices
                = bookingService.getSeatPrices(showtimeId, seats);

        Map<Integer, List<Seat>> seatRows = new LinkedHashMap<>();

        for (Seat seat : seats) {

            if (!seatRows.containsKey(seat.getSeatRow())) {
                seatRows.put(
                        seat.getSeatRow(),
                        new ArrayList<>());
            }

            seatRows.get(seat.getSeatRow()).add(seat);
        }

        request.setAttribute("showtime", showtime);
        request.setAttribute("seats", seats);
        request.setAttribute("seatRows", seatRows);
        request.setAttribute("occupiedSeatIds", occupiedSeatIds);
        request.setAttribute("seatPrices", seatPrices);
    }

    private void showBookings(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int customerId = getCurrentCustomerId(request);

        List<Booking> bookings
                = bookingService.getBookingsByCustomer(customerId);

        Map<Integer, Showtime> showtimeMap = new LinkedHashMap<>();
        Map<Integer, Movie> movieMap = new LinkedHashMap<>();

        for (Booking booking : bookings) {

            Showtime showtime
                    = bookingService.getShowtimeForBooking(booking);

            if (showtime != null) {

                showtimeMap.put(
                        booking.getBookingId(),
                        showtime);

                Movie movie
                        = bookingService.getMovieForShowtime(showtime);

                if (movie != null) {
                    movieMap.put(
                            booking.getBookingId(),
                            movie);
                }
            }
        }

        request.setAttribute("bookings", bookings);
        request.setAttribute("showtimeMap", showtimeMap);
        request.setAttribute("movieMap", movieMap);

        request.getRequestDispatcher(
                "/WEB-INF/views/user/bookings.jsp"
        ).forward(request, response);
    }

    private void showBookingDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int customerId = getCurrentCustomerId(request);

        int bookingId = parsePositiveInt(
                request.getParameter("id"),
                "Đơn đặt vé không hợp lệ.");

        Booking booking = bookingService.getBookingDetail(
                bookingId,
                customerId);

        Showtime showtime
                = bookingService.getShowtimeForBooking(booking);

        Movie movie
                = bookingService.getMovieForShowtime(showtime);

        List<Ticket> tickets
                = bookingService.getTicketsByBooking(bookingId);

        Map<Integer, Seat> seatMap = new LinkedHashMap<>();

        for (Ticket ticket : tickets) {

            Seat seat = bookingService.getSeatForTicket(
                    ticket,
                    showtime);

            if (seat != null) {
                seatMap.put(ticket.getTicketId(), seat);
            }
        }

        request.setAttribute("booking", booking);
        request.setAttribute("showtime", showtime);
        request.setAttribute("movie", movie);
        request.setAttribute("tickets", tickets);
        request.setAttribute("seatMap", seatMap);

        request.getRequestDispatcher(
                "/WEB-INF/views/user/booking-detail.jsp"
        ).forward(request, response);
    }

    private void showSeatSelectionWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage)
            throws ServletException, IOException, SQLException {

        int showtimeId;

        try {
            showtimeId = parsePositiveInt(
                    request.getParameter("showtimeId"),
                    errorMessage);

        } catch (IllegalArgumentException e) {

            request.setAttribute("error", errorMessage);

            request.getRequestDispatcher(
                    "/WEB-INF/views/error.jsp"
            ).forward(request, response);

            return;
        }

        loadSeatSelection(request, showtimeId);

        request.setAttribute("error", errorMessage);

        request.getRequestDispatcher(
                "/WEB-INF/views/user/select-seats.jsp"
        ).forward(request, response);
    }

    private int getCurrentCustomerId(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)) {

            throw new IllegalArgumentException(
                    "Vui lòng đăng nhập lại.");
        }

        User user = (User) session.getAttribute("user");

        return user.getUserId();
    }

    private int parsePositiveInt(
            String value,
            String errorMessage) {

        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
