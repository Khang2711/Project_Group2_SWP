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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import model.Booking;
import model.FoodOrder;
import model.FoodOrderItem;
import model.Movie;
import model.Room;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;
import service.FoodOrderService;
import service.MovieService;
import service.SeatService;
import service.ShowtimeService;
import service.TicketService;

@WebServlet(urlPatterns = {
    "/staff/counter", "/staff/ticket-sale", "/staff/food-sale"
})
public class CounterSaleController extends HttpServlet {

    private FoodOrderService foodOrderService;
    private MovieService movieService;
    private ShowtimeService showtimeService;
    private SeatService seatService;
    private BookingService bookingService;
    private TicketService ticketService;

    @Override
    public void init() {
        foodOrderService = new FoodOrderService();
        movieService = new MovieService();
        showtimeService = new ShowtimeService();
        seatService = new SeatService();
        bookingService = new BookingService();
        ticketService = new TicketService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireStaff(request, response)) {
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/staff/counter":
                request.getRequestDispatcher(
                        "/WEB-INF/views/staff/counter.jsp"
                ).forward(request, response);
                break;

            case "/staff/ticket-sale":
                showTicketSale(request, response);
                break;

            case "/staff/food-sale":
                showFoodSale(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if ("/staff/food-sale".equals(request.getServletPath())) {
            createFoodOrder(request, response);

        } else if ("/staff/ticket-sale".equals(request.getServletPath())) {
            createTicketSale(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private boolean requireStaff(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return false;
        }

        if (!"STAFF".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private void showTicketSale(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        LocalDate date = LocalDate.now();

        request.setAttribute("today", date.toString());
        request.setAttribute("selectedDate", date.toString());

        try {
            if (user.getCinemaId() == null || user.getCinemaId() <= 0) {
                throw new IllegalArgumentException(
                        "Nhân viên chưa được phân công rạp.");
            }

            int cinemaId = user.getCinemaId();

            List<Movie> movies
                    = movieService.getMoviesByCinema(cinemaId);

            request.setAttribute("movies", movies);

            String dateParam = request.getParameter("date");

            if (dateParam != null && !dateParam.trim().isEmpty()) {
                date = LocalDate.parse(dateParam.trim());
            }

            request.setAttribute("selectedDate", date.toString());

            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn ngày hôm nay hoặc ngày sắp tới.");
            }

            String movieParam = request.getParameter("movieId");

            if (movieParam == null || movieParam.trim().isEmpty()) {
                forwardTicketSale(request, response);
                return;
            }

            int movieId = parsePositiveInt(
                    movieParam, "Phim không hợp lệ.");

            Movie selectedMovie = null;

            // Chỉ nhận phim nằm trong danh sách của rạp Staff.
            for (Movie movie : movies) {
                if (movie.getMovieId() == movieId) {
                    selectedMovie = movie;
                    break;
                }
            }

            if (selectedMovie == null) {
                throw new IllegalArgumentException(
                        "Phim không thuộc rạp của nhân viên.");
            }

            request.setAttribute("selectedMovieId", movieId);
            request.setAttribute("selectedMovie", selectedMovie);

            Map<Integer, Room> rooms
                    = showtimeService.getActiveRoomById();

            List<Showtime> movieShowtimes
                    = showtimeService.getByMovieAndDate(movieId, date);

            List<Showtime> showtimes = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // Giữ suất chưa bắt đầu và có phòng thuộc đúng rạp.
            for (Showtime showtime : movieShowtimes) {
                Room room = rooms.get(showtime.getRoomId());

                if (room != null
                        && room.getCinemaId() == cinemaId
                        && showtime.getStartTime() != null
                        && showtime.getStartTime()
                                .toLocalDateTime().isAfter(now)) {

                    showtimes.add(showtime);
                }
            }

            request.setAttribute("rooms", rooms);
            request.setAttribute("showtimes", showtimes);

            String showtimeParam = request.getParameter("showtimeId");

            if (showtimeParam == null || showtimeParam.trim().isEmpty()) {
                forwardTicketSale(request, response);
                return;
            }

            int showtimeId = parsePositiveInt(
                    showtimeParam, "Suất chiếu không hợp lệ.");

            Showtime selectedShowtime = null;

            for (Showtime showtime : showtimes) {
                if (showtime.getShowtimeId() == showtimeId) {
                    selectedShowtime = showtime;
                    break;
                }
            }

            if (selectedShowtime == null
                    || !showtimeService.isBookable(showtimeId)) {

                throw new IllegalArgumentException(
                        "Suất chiếu không hợp lệ hoặc hiện không mở bán.");
            }

            Map<Integer, List<Seat>> seatRows
                    = seatService.getSeatRowsByRoom(
                            selectedShowtime.getRoomId(), cinemaId);

            List<Seat> seats = new ArrayList<>();

            for (List<Seat> rowSeats : seatRows.values()) {
                seats.addAll(rowSeats);
            }

            Set<Integer> occupiedSeatIds
                    = bookingService.getOccupiedSeatIds(showtimeId);

            Map<Integer, BigDecimal> seatPrices
                    = bookingService.getSeatPrices(showtimeId, seats);

            request.setAttribute("selectedShowtimeId", showtimeId);
            request.setAttribute("selectedShowtime", selectedShowtime);

            request.setAttribute(
                    "selectedRoom",
                    rooms.get(selectedShowtime.getRoomId()));

            request.setAttribute("seatRows", seatRows);
            request.setAttribute("occupiedSeatIds", occupiedSeatIds);
            request.setAttribute("seatPrices", seatPrices);

        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày chiếu không hợp lệ.");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();

            request.setAttribute("error",
                    "Không thể tải ghế hoặc giá vé. Vui lòng thử lại.");
        }

        forwardTicketSale(request, response);
    }

    private void forwardTicketSale(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if (session.getAttribute("counterSaleToken") == null) {
            session.setAttribute(
                    "counterSaleToken", UUID.randomUUID().toString());
        }

        Object completed
                = session.getAttribute("counterCompletedBooking");

        if (completed != null) {
            request.setAttribute("completedBooking", completed);

            // Giữ đơn bán gần nhất để tải lại trang vẫn in được.
            session.setAttribute("counterLastBooking", completed);
            session.removeAttribute("counterCompletedBooking");
        }

        loadTicketsForPrinting(request, session);

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/ticket-sale.jsp"
        ).forward(request, response);
    }

    private void createTicketSale(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        if (!"STAFF".equals(session.getAttribute("roleName"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            // Mỗi mã dùng một lần để tránh tạo đơn trùng.
            synchronized (session) {
                String expected
                        = (String) session.getAttribute("counterSaleToken");

                String submitted = request.getParameter("saleToken");

                if (expected == null || !expected.equals(submitted)) {
                    throw new IllegalArgumentException(
                            "Yêu cầu đã xử lý hoặc trang đã cũ. Vui lòng chọn lại.");
                }

                session.removeAttribute("counterSaleToken");
            }

            if (!"yes".equals(request.getParameter("cashReceived"))) {
                throw new IllegalArgumentException(
                        "Vui lòng xác nhận đã thu tiền mặt.");
            }

            User user = (User) session.getAttribute("user");

            if (user.getCinemaId() == null || user.getCinemaId() <= 0) {
                throw new IllegalArgumentException(
                        "Nhân viên chưa được phân công rạp.");
            }

            int showtimeId = parsePositiveInt(
                    request.getParameter("showtimeId"),
                    "Suất chiếu không hợp lệ.");

            Booking booking = ticketService.sellCounterCashTickets(
                    user.getUserId(),
                    user.getCinemaId(),
                    showtimeId,
                    request.getParameter("customerEmail"),
                    request.getParameterValues("seatIds"));

            session.setAttribute("counterCompletedBooking", booking);
            session.setAttribute("counterLastBooking", booking);

            // Chuyển sang GET để F5 không gửi lại yêu cầu bán vé.
            response.sendRedirect(
                    request.getContextPath() + "/staff/ticket-sale");

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showTicketSale(request, response);

        } catch (SQLException e) {
            e.printStackTrace();

            request.setAttribute("error",
                    "Không thể lưu đơn vé. Vui lòng tải lại ghế và thử lại.");

            showTicketSale(request, response);
        }
    }

    private void loadTicketsForPrinting(
            HttpServletRequest request,
            HttpSession session) {

        Booking saved
                = (Booking) session.getAttribute("counterLastBooking");

        if (saved == null) {
            return;
        }

        try {
            User user = (User) session.getAttribute("user");

            // Dùng ID của đơn đã lưu trong session, không lấy từ URL.
            Booking booking = bookingService.getBookingDetail(
                    saved.getBookingId(),
                    saved.getCustomerId());

            Showtime showtime
                    = bookingService.getShowtimeForBooking(booking);

            if (!"CONFIRMED".equals(booking.getStatus())
                    || showtime == null) {

                throw new IllegalArgumentException(
                        "Đơn vé không còn được xác nhận.");
            }

            Room room = showtimeService.getActiveRoomById()
                    .get(showtime.getRoomId());

            if (room == null
                    || user.getCinemaId() == null
                    || room.getCinemaId() != user.getCinemaId()) {

                throw new IllegalArgumentException(
                        "Đơn vé không thuộc rạp của nhân viên.");
            }

            Movie movie
                    = bookingService.getMovieForShowtime(showtime);

            List<Ticket> tickets = bookingService.getTicketsByBooking(
                    booking.getBookingId());

            if (movie == null || tickets.isEmpty()) {
                throw new IllegalArgumentException(
                        "Chưa tải được thông tin vé.");
            }

            Map<Integer, Seat> seats = new LinkedHashMap<>();

            for (Ticket ticket : tickets) {
                Seat seat = bookingService.getSeatForTicket(
                        ticket, showtime);

                if (seat == null
                        || ticket.getTicketCode() == null
                        || ticket.getTicketCode().trim().isEmpty()
                        || (!"VALID".equals(ticket.getStatus())
                        && !"USED".equals(ticket.getStatus()))) {

                    throw new IllegalArgumentException(
                            "Có vé chưa đủ thông tin để in.");
                }

                seats.put(ticket.getSeatId(), seat);
            }

            request.setAttribute("printedBooking", booking);
            request.setAttribute("printedShowtime", showtime);
            request.setAttribute("printedRoom", room);
            request.setAttribute("printedMovie", movie);
            request.setAttribute("printedTickets", tickets);
            request.setAttribute("printedSeats", seats);

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();

            // Lỗi tải bản in không làm thay đổi đơn đã thanh toán.
            request.setAttribute(
                    "printError",
                    "Chưa tải được bản in đơn #" + saved.getBookingId()
                    + ". Vui lòng tải lại trang; "
                    + "không tạo lại đơn đã thanh toán.");
        }
    }

    private void showFoodSale(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireStaff(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        request.setAttribute("canSellFood", false);

        try {
            int cinemaId
                    = foodOrderService.getStaffCinema(user.getUserId());

            request.setAttribute(
                    "foodItems",
                    foodOrderService.getFoodsForCounter(cinemaId));

            request.setAttribute(
                    "combos",
                    foodOrderService.getCombosForCounter(cinemaId));

            request.setAttribute("canSellFood", true);

            String orderIdParam = request.getParameter("orderId");

            if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
                int orderId = parsePositiveInt(
                        orderIdParam, "Mã đơn không hợp lệ.");

                FoodOrder order
                        = foodOrderService.getOrderById(orderId);

                if (order == null || order.getCinemaId() != cinemaId) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng tại rạp của bạn.");
                }

                request.setAttribute(
                        "receiptNames",
                        foodOrderService.getReceiptNames(orderId, cinemaId));

                request.setAttribute(
                        "completedItems",
                        foodOrderService.getOrderItems(orderId));

                request.setAttribute("completedOrder", order);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
        }

        synchronized (session) {
            if (session.getAttribute("foodSaleToken") == null) {
                session.setAttribute(
                        "foodSaleToken", UUID.randomUUID().toString());
            }
        }

        Object success = session.getAttribute("foodSaleSuccess");

        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("foodSaleSuccess");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/food-sale.jsp"
        ).forward(request, response);
    }

    private void createFoodOrder(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireStaff(request, response)) {
            return;
        }

        HttpSession session = request.getSession();

        try {
            synchronized (session) {
                String expected
                        = (String) session.getAttribute("foodSaleToken");

                if (expected == null
                        || !expected.equals(request.getParameter("saleToken"))) {

                    throw new IllegalArgumentException(
                            "Yêu cầu đã xử lý hoặc trang đã cũ. Vui lòng chọn lại.");
                }

                session.removeAttribute("foodSaleToken");
            }

            if (!"yes".equals(request.getParameter("cashReceived"))) {
                throw new IllegalArgumentException(
                        "Vui lòng xác nhận đã thu tiền mặt.");
            }

            User user = (User) session.getAttribute("user");

            FoodOrder order = new FoodOrder();
            order.setStaffId(user.getUserId());

            order.setCinemaId(
                    foodOrderService.getStaffCinema(user.getUserId()));

            order.setPaymentMethod(
                    request.getParameter("paymentMethod"));

            int orderId = foodOrderService.createPaidOrder(
                    order, buildOrderItems(request));

            session.setAttribute(
                    "foodSaleSuccess", "Thanh toán đơn hàng thành công.");

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/food-sale?orderId=" + orderId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showFoodSale(request, response);
        }
    }

    private List<FoodOrderItem> buildOrderItems(
            HttpServletRequest request) {

        String[] types = request.getParameterValues("itemType");
        String[] ids = request.getParameterValues("itemId");
        String[] quantities = request.getParameterValues("quantity");

        if (types == null || ids == null || quantities == null) {
            throw new IllegalArgumentException(
                    "Đơn hàng chưa có sản phẩm.");
        }

        if (types.length != ids.length
                || types.length != quantities.length) {

            throw new IllegalArgumentException(
                    "Dữ liệu đơn hàng không hợp lệ.");
        }

        List<FoodOrderItem> items = new ArrayList<>();

        for (int i = 0; i < types.length; i++) {
            FoodOrderItem item = new FoodOrderItem();

            int id = parsePositiveInt(
                    ids[i], "Sản phẩm không hợp lệ.");

            if ("FOOD".equals(types[i])) {
                item.setFoodItemId(id);

            } else if ("COMBO".equals(types[i])) {
                item.setComboId(id);

            } else {
                throw new IllegalArgumentException(
                        "Loại sản phẩm không hợp lệ.");
            }

            item.setQuantity(parsePositiveInt(
                    quantities[i],
                    "Số lượng sản phẩm phải là số nguyên lớn hơn 0."));

            items.add(item);
        }

        return items;
    }

    private int parsePositiveInt(String value, String errorMessage) {

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
