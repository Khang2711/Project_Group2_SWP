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
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import model.Booking;
import model.Combo;
import model.FoodItem;
import model.FoodOrder;
import model.FoodOrderItem;
import model.Movie;
import model.Room;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;
import service.DashboardService;
import service.FoodOrderService;
import service.MovieService;
import service.RoomService;
import service.SeatService;
import service.ShowtimeService;
import service.TicketService;

@WebServlet(urlPatterns = {
    "/staff/counter",
    "/staff/ticket-sale",
    "/staff/food-sale"
})
public class CounterSaleController extends HttpServlet {

    private FoodOrderService foodOrderService;
    private MovieService movieService;
    private ShowtimeService showtimeService;
    private SeatService seatService;
    private BookingService bookingService;
    private TicketService ticketService;
    private DashboardService dashboardService;
    private RoomService roomService;

    @Override
    public void init() {
        foodOrderService = new FoodOrderService();
        movieService = new MovieService();
        showtimeService = new ShowtimeService();
        seatService = new SeatService();
        bookingService = new BookingService();
        ticketService = new TicketService();
        dashboardService = new DashboardService();
        roomService = new RoomService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireStaff(request, response)) {
            return;
        }

        response.setHeader("Cache-Control", "no-store");

        switch (request.getServletPath()) {
            case "/staff/counter":
                showCounter(request, response);
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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        if (!requireStaff(request, response)) {
            return;
        }

        /*
         * Các yêu cầu trong cùng phiên Staff được xử lý lần lượt.
         * Tránh hai lần bấm thanh toán cùng chạy một lúc.
         * Phiên của nhân viên khác vẫn hoạt động bình thường.
         */
        synchronized (request.getSession()) {
            String path = request.getServletPath();
            String action = request.getParameter("action");

            if ("/staff/ticket-sale".equals(path)) {
                if ("pay".equals(action)) {
                    confirmCounterPayment(request, response);

                } else if ("skipFood".equals(action)) {
                    skipCounterFood(request, response);

                } else if (action == null || "prepare".equals(action)) {
                    prepareCounterDraft(request, response);

                } else {
                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST);
                }

            } else if ("/staff/food-sale".equals(path)) {
                if ("counterDraft".equals(action)) {
                    saveCounterDraftFoods(request, response);
                } else {
                    createFoodOrder(request, response);
                }

            } else {
                response.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }
    }

    private boolean requireStaff(
            HttpServletRequest request,
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

    private void showCounter(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String tab = request.getParameter("tab");

        // Trang Quầy bán hàng chỉ hiển thị các tác vụ.
        // Chỉ tải báo cáo khi nhân viên mở một mục báo cáo cụ thể.
        boolean showReport = "statistics".equals(tab)
                || "tickets".equals(tab)
                || "food".equals(tab);

        if (!showReport) {
            request.getRequestDispatcher(
                    "/WEB-INF/views/staff/counter.jsp"
            ).forward(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDate fromDate = today.withDayOfMonth(1);
        LocalDate toDate = today;

        request.setAttribute("fromDate", fromDate.toString());
        request.setAttribute("toDate", toDate.toString());

        try {
            String fromValue = request.getParameter("fromDate");
            String toValue = request.getParameter("toDate");

            if (fromValue != null) {
                fromDate = dashboardService.parseDate(
                        fromValue, "Ngày bắt đầu");

                request.setAttribute(
                        "fromDate", fromDate.toString());
            }

            if (toValue != null) {
                toDate = dashboardService.parseDate(
                        toValue, "Ngày kết thúc");

                request.setAttribute(
                        "toDate", toDate.toString());
            }

            // ID người bán luôn lấy từ tài khoản đang đăng nhập.
            Map<String, Object> report
                    = dashboardService.getStaffSalesReport(
                            user.getUserId(), fromDate, toDate);

            request.setAttribute("staffReport", report);

        } catch (IllegalArgumentException e) {
            request.setAttribute("reportError", e.getMessage());

        } catch (SQLException e) {
            log("Cannot load staff sales report.", e);

            request.setAttribute(
                    "reportError",
                    "Không thể tải lịch sử và thống kê. Vui lòng thử lại.");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/counter.jsp"
        ).forward(request, response);
    }

    private void showTicketSale(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate date = LocalDate.now(zone);

        request.setAttribute("counterStep", "sale");

        if ("1".equals(request.getParameter("newSale"))) {
            synchronized (session) {
                clearCounterDraft(session);
                session.removeAttribute("counterCompletedBooking");
                session.removeAttribute("counterDraftError");

                session.setAttribute(
                        "counterSaleToken",
                        UUID.randomUUID().toString());
            }
        }

        String step = request.getParameter("step");

        if ("extras".equals(step) || "review".equals(step)) {
            showCounterDraft(request, response, step);
            return;
        }

        if (request.getParameter("bookingId") != null) {
            request.setAttribute("counterStep", "receipt");
            forwardTicketSale(request, response);
            return;
        }

        request.setAttribute("today", date.toString());
        request.setAttribute("selectedDate", date.toString());

        try {
            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            List<Movie> movies = movieService.getMoviesByCinema(
                    cinemaId);

            request.setAttribute("movies", movies);

            String dateParam = request.getParameter("date");

            if (dateParam != null && !dateParam.trim().isEmpty()) {
                date = LocalDate.parse(dateParam.trim());
            }

            request.setAttribute("selectedDate", date.toString());

            if (date.isBefore(LocalDate.now(zone))) {
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

            // Chỉ cho chọn phim thuộc rạp của Staff.
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
            LocalDateTime now = LocalDateTime.now(zone);

            // Giữ các suất chưa bắt đầu và thuộc đúng rạp.
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

            if (showtimeParam == null
                    || showtimeParam.trim().isEmpty()) {

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
            request.setAttribute(
                    "error", "Ngày chiếu không hợp lệ.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Cannot load counter seats or prices.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải ghế hoặc giá vé. Vui lòng thử lại.");
        }

        forwardTicketSale(request, response);
    }

    private void forwardTicketSale(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Booking completed;

        synchronized (session) {
            if (session.getAttribute("counterSaleToken") == null) {
                session.setAttribute(
                        "counterSaleToken",
                        UUID.randomUUID().toString());
            }

            completed = (Booking) session.getAttribute(
                    "counterCompletedBooking");

            session.removeAttribute("counterCompletedBooking");
        }

        if (request.getParameter("bookingId") != null) {
            loadTicketsForPrinting(request, session);

            Booking printed = (Booking) request.getAttribute(
                    "printedBooking");

            if (completed != null
                    && printed != null
                    && completed.getBookingId()
                    == printed.getBookingId()) {

                request.setAttribute("completedBooking", printed);
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/ticket-sale.jsp"
        ).forward(request, response);
    }

    private void consumeCounterToken(HttpServletRequest request) {

        HttpSession session = request.getSession();

        String expected = (String) session.getAttribute(
                "counterSaleToken");

        /*
         * Token là mã dùng một lần cho form.
         * Form đã gửi hoặc form ở tab cũ sẽ không được xử lý lại.
         */
        if (expected == null
                || !expected.equals(request.getParameter("saleToken"))) {

            throw new IllegalArgumentException(
                    "Trang đã cũ hoặc yêu cầu đã xử lý. Vui lòng tải lại trang.");
        }

        session.removeAttribute("counterSaleToken");
    }

    private void clearCounterDraft(HttpSession session) {
        session.removeAttribute("counterDraftBooking");
        session.removeAttribute("counterDraftCinema");
        session.removeAttribute("counterDraftEmail");
        session.removeAttribute("counterDraftSeats");
        session.removeAttribute("counterDraftFoods");
        session.removeAttribute("counterDraftFoodTotal");
        session.removeAttribute("counterDraftReady");
    }

    private Booking getCounterDraft(
            HttpSession session,
            User user,
            int cinemaId) {

        Booking booking = (Booking) session.getAttribute(
                "counterDraftBooking");

        Integer draftCinema = (Integer) session.getAttribute(
                "counterDraftCinema");

        if (booking == null || draftCinema == null) {
            throw new IllegalArgumentException(
                    "Đơn đang chuẩn bị đã hết phiên. Vui lòng chọn vé lại.");
        }

        if (booking.getStaffId() == null
                || booking.getStaffId() != user.getUserId()
                || draftCinema != cinemaId) {

            clearCounterDraft(session);

            throw new IllegalArgumentException(
                    "Đơn đang chuẩn bị không thuộc nhân viên hoặc rạp hiện tại.");
        }

        return booking;
    }

    private List<FoodOrderItem> getCounterFoods(HttpSession session) {

        FoodOrderItem[] saved = (FoodOrderItem[]) session.getAttribute(
                "counterDraftFoods");

        List<FoodOrderItem> items = new ArrayList<>();

        if (saved != null) {
            for (FoodOrderItem item : saved) {
                items.add(item);
            }
        }

        return items;
    }

    private void prepareCounterDraft(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            consumeCounterToken(request);
            clearCounterDraft(session);

            session.removeAttribute("counterCompletedBooking");
            session.removeAttribute("counterDraftError");

            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            int showtimeId = parsePositiveInt(
                    request.getParameter("showtimeId"),
                    "Suất chiếu không hợp lệ.");

            String[] seats = request.getParameterValues("seatIds");

            // Chỉ kiểm tra và tính tiền, chưa lưu vé hoặc thanh toán.
            Booking preview = ticketService.previewCounterTickets(
                    user.getUserId(),
                    cinemaId,
                    showtimeId,
                    null,
                    seats);

            // Dùng lại model Booking để giữ đơn chưa thanh toán.
            session.setAttribute("counterDraftBooking", preview);
            session.setAttribute("counterDraftCinema", cinemaId);

            session.setAttribute("counterDraftSeats", seats.clone());

            session.setAttribute(
                    "counterDraftFoods", new FoodOrderItem[0]);

            session.setAttribute(
                    "counterDraftFoodTotal", BigDecimal.ZERO);

            session.setAttribute("counterDraftReady", false);

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/ticket-sale?step=extras");

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showTicketSale(request, response);

        } catch (SQLException e) {
            log("Cannot prepare counter sale.", e);

            request.setAttribute(
                    "error",
                    "Không thể kiểm tra đơn vé. Vui lòng thử lại.");

            showTicketSale(request, response);
        }
    }

    private void refreshCounterDraft(
            HttpServletRequest request,
            HttpSession session,
            User user,
            Booking draft,
            String step) throws SQLException {

        // Chỉ cho thanh toán sau khi tải thành công bước xem tổng đơn.
        session.setAttribute("counterDraftReady", false);

        int cinemaId = (Integer) session.getAttribute(
                "counterDraftCinema");

        int showtimeId = draft.getShowtimeId();

        String[] seatValues = (String[]) session.getAttribute(
                "counterDraftSeats");

        List<FoodOrderItem> items = getCounterFoods(session);

        Booking preview = ticketService.previewCounterTickets(
                user.getUserId(),
                cinemaId,
                showtimeId,
                null,
                seatValues);

        Showtime showtime = bookingService.getShowtimeForBooking(
                preview);

        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy suất chiếu đang chọn.");
        }

        Room room = roomService.getRoom(
                showtime.getRoomId(), cinemaId);

        Movie movie = bookingService.getMovieForShowtime(showtime);

        if (room == null || movie == null) {
            throw new IllegalArgumentException(
                    "Không thể tải phim hoặc phòng chiếu.");
        }

        List<Seat> seats = new ArrayList<>();

        for (String seatValue : seatValues) {
            Ticket ticket = new Ticket();

            ticket.setSeatId(parsePositiveInt(
                    seatValue, "Mã ghế không hợp lệ."));

            Seat seat = bookingService.getSeatForTicket(
                    ticket, showtime);

            if (seat == null) {
                throw new IllegalArgumentException(
                        "Ghế không thuộc phòng chiếu.");
            }

            seats.add(seat);
        }

        // Service đã tính giá vé, không truy vấn và cộng lại lần nữa.
        BigDecimal ticketTotal = preview.getTotalAmount();
        BigDecimal foodTotal = BigDecimal.ZERO;

        Map<String, String> names = new LinkedHashMap<>();

        if (!items.isEmpty()) {
            foodTotal = foodOrderService.calculateSubtotal(
                    cinemaId, items);

            for (FoodItem food
                    : foodOrderService.getFoodsForCounter(cinemaId)) {

                names.put(
                        "FOOD-" + food.getFoodItemId(),
                        food.getItemName());
            }

            for (Combo combo
                    : foodOrderService.getCombosForCounter(cinemaId)) {

                names.put(
                        "COMBO-" + combo.getComboId(),
                        combo.getComboName());
            }
        }

        BigDecimal total = ticketTotal.add(foodTotal);

        session.setAttribute("counterDraftBooking", preview);
        session.setAttribute("counterDraftFoodTotal", foodTotal);

        session.setAttribute(
                "counterDraftReady", "review".equals(step));

        request.setAttribute("counterStep", step);
        request.setAttribute("draftBooking", preview);

        request.setAttribute("draftMovie", movie);
        request.setAttribute("draftShowtime", showtime);
        request.setAttribute("draftRoom", room);
        request.setAttribute("draftSeats", seats);
        request.setAttribute("draftFoodItems", items);
        request.setAttribute("draftFoodNames", names);
        request.setAttribute("draftTicketTotal", ticketTotal);
        request.setAttribute("draftFoodTotal", foodTotal);
        request.setAttribute("draftTotal", total);

        // Tạo mã mới để form ở tab cũ không dùng tổng tiền đã cũ.
        session.setAttribute(
                "counterSaleToken", UUID.randomUUID().toString());
    }

    private void showCounterDraft(
            HttpServletRequest request,
            HttpServletResponse response,
            String step)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        request.setAttribute("counterStep", "draft-error");

        try {
            synchronized (session) {
                int cinemaId = foodOrderService.getStaffCinema(
                        user.getUserId());

                Booking draft = getCounterDraft(
                        session, user, cinemaId);

                refreshCounterDraft(
                        request, session, user, draft, step);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Cannot load counter draft.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải đơn đang chuẩn bị. Vui lòng thử lại.");
        }

        Object message = session.getAttribute("counterDraftError");

        if (message != null) {
            request.setAttribute("draftMessage", message);
            session.removeAttribute("counterDraftError");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/ticket-sale.jsp"
        ).forward(request, response);
    }

    private void skipCounterFood(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            getCounterDraft(session, user, cinemaId);
            consumeCounterToken(request);

            session.setAttribute(
                    "counterDraftFoods", new FoodOrderItem[0]);

            session.setAttribute("counterDraftReady", false);

        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("counterDraftError", e.getMessage());
        }

        response.sendRedirect(
                request.getContextPath()
                + "/staff/ticket-sale?step=review");
    }

    private void showCounterDraftFoods(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        request.setAttribute("isCounterDraftSale", true);
        request.setAttribute("canSellFood", false);

        try {
            synchronized (session) {
                int cinemaId = foodOrderService.getStaffCinema(
                        user.getUserId());

                Booking draft = getCounterDraft(
                        session, user, cinemaId);

                refreshCounterDraft(
                        request, session, user, draft, "food");

                request.setAttribute(
                        "foodItems",
                        foodOrderService.getFoodsForCounter(cinemaId));

                request.setAttribute(
                        "combos",
                        foodOrderService.getCombosForCounter(cinemaId));

                request.setAttribute("canSellFood", true);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Cannot load food selection for draft.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải đơn vé đang chuẩn bị.");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/staff/food-sale.jsp"
        ).forward(request, response);
    }

    private void saveCounterDraftFoods(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            getCounterDraft(session, user, cinemaId);
            consumeCounterToken(request);

            List<FoodOrderItem> items = buildOrderItems(request);

            foodOrderService.calculateSubtotal(cinemaId, items);

            session.setAttribute(
                    "counterDraftFoods",
                    items.toArray(new FoodOrderItem[0]));

            session.setAttribute("counterDraftReady", false);

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/ticket-sale?step=review");

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showCounterDraftFoods(request, response);
        }
    }

    private void confirmCounterPayment(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            Booking draft = getCounterDraft(
                    session, user, cinemaId);

            consumeCounterToken(request);

            if (!Boolean.TRUE.equals(
                    session.getAttribute("counterDraftReady"))) {

                throw new IllegalArgumentException(
                        "Vui lòng mở bước xem tổng đơn trước khi thanh toán.");
            }

            session.setAttribute("counterDraftReady", false);

            if (!"yes".equals(request.getParameter("cashReceived"))) {
                throw new IllegalArgumentException(
                        "Vui lòng xác nhận đã thu đủ tổng tiền.");
            }

            /*
             * Lấy dữ liệu đã chuẩn bị trên server.
             * Không nhận giá, tổng tiền hoặc ghế mới từ form thanh toán.
             * Service lưu vé và bắp nước trong cùng một transaction.
             */
            Booking booking = ticketService.sellCounterCashOrder(
                    user.getUserId(),
                    cinemaId,
                    draft.getShowtimeId(),
                    null,
                    (String[]) session.getAttribute("counterDraftSeats"),
                    getCounterFoods(session),
                    draft.getTotalAmount(),
                    (BigDecimal) session.getAttribute(
                            "counterDraftFoodTotal"));

            clearCounterDraft(session);
            session.setAttribute("counterCompletedBooking", booking);

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/ticket-sale?bookingId="
                    + booking.getBookingId());

        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("counterDraftError", e.getMessage());

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/ticket-sale?step=review");

        } catch (SQLException e) {
            log("Cannot save combined counter sale.", e);

            session.setAttribute(
                    "counterDraftError",
                    "Không thể lưu đơn. Vui lòng kiểm tra lại ghế "
                    + "và tổng tiền trước khi xác nhận.");

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/ticket-sale?step=review");
        }
    }

    private void loadTicketsForPrinting(
            HttpServletRequest request,
            HttpSession session) {

        try {
            User user = (User) session.getAttribute("user");

            int bookingId = parsePositiveInt(
                    request.getParameter("bookingId"),
                    "Mã đơn vé không hợp lệ.");

            // Chỉ cho Staff xem và in đơn do chính mình bán.
            Booking booking = dashboardService.getStaffBooking(
                    user.getUserId(), bookingId);

            Showtime showtime = bookingService.getShowtimeForBooking(
                    booking);

            if (!"CONFIRMED".equals(booking.getStatus())
                    || showtime == null) {

                throw new IllegalArgumentException(
                        "Đơn vé không còn được xác nhận.");
            }

            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            Room room = roomService.getRoom(
                    showtime.getRoomId(), cinemaId);

            if (room == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy phòng chiếu của đơn vé.");
            }

            Movie movie = bookingService.getMovieForShowtime(showtime);

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

            loadLinkedFoodOrders(
                    request, booking, user.getUserId(), cinemaId);

        } catch (IllegalArgumentException e) {
            request.setAttribute("printError", e.getMessage());

        } catch (SQLException | IllegalStateException e) {
            log("Cannot load counter ticket receipt.", e);

            request.setAttribute(
                    "printError",
                    "Chưa tải được bản in. Vui lòng tải lại trang; "
                    + "không tạo lại đơn đã thanh toán.");
        }
    }

    private void loadLinkedFoodOrders(
            HttpServletRequest request,
            Booking booking,
            int staffId,
            int cinemaId) {

        try {
            List<FoodOrder> orders
                    = foodOrderService.getOrdersByBooking(
                            booking.getBookingId(), staffId);

            Map<Integer, List<FoodOrderItem>> items
                    = new LinkedHashMap<>();

            Map<Integer, String> names = new LinkedHashMap<>();
            BigDecimal total = BigDecimal.ZERO;

            for (FoodOrder order : orders) {
                int id = order.getFoodOrderId();

                items.put(id, foodOrderService.getOrderItems(id));

                names.putAll(
                        foodOrderService.getReceiptNames(id, cinemaId));

                total = total.add(order.getTotalAmount());
            }

            request.setAttribute("linkedFoodOrders", orders);
            request.setAttribute("linkedFoodItems", items);
            request.setAttribute("linkedFoodNames", names);
            request.setAttribute("linkedFoodTotal", total);

            request.setAttribute(
                    "purchaseTotal",
                    booking.getTotalAmount().add(total));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log("Cannot load linked food orders.", e);

            request.setAttribute(
                    "linkedFoodError",
                    "Chưa tải được đầy đủ bắp nước và tổng tiền.");
        }
    }

    private void showFoodSale(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if ("1".equals(request.getParameter("draft"))) {
            showCounterDraftFoods(request, response);
            return;
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        request.setAttribute("canSellFood", false);

        try {
            int cinemaId = foodOrderService.getStaffCinema(
                    user.getUserId());

            String orderValue = request.getParameter("orderId");

            if (orderValue != null) {
                int orderId = parsePositiveInt(
                        orderValue, "Mã đơn không hợp lệ.");

                FoodOrder order = foodOrderService.getOrderById(orderId);

                if (order == null
                        || order.getStaffId() != user.getUserId()
                        || order.getCinemaId() != cinemaId) {

                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn bắp nước do bạn bán.");
                }

                if (!"PAID".equals(order.getPaymentStatus())
                        || !"COMPLETED".equals(order.getOrderStatus())) {

                    throw new IllegalArgumentException(
                            "Đơn hàng chưa hoàn tất thanh toán.");
                }

                /*
                 * Đơn vé liên quan phải lấy từ dữ liệu của hóa đơn.
                 * Không dùng bookingId do người dùng tự sửa trên URL.
                 */
                if (order.getBookingId() != null) {
                    Booking booking = dashboardService.getStaffBooking(
                            user.getUserId(), order.getBookingId());

                    setLinkedBooking(request, booking);
                }

                request.setAttribute(
                        "receiptNames",
                        foodOrderService.getReceiptNames(
                                orderId, cinemaId));

                request.setAttribute(
                        "completedItems",
                        foodOrderService.getOrderItems(orderId));

                request.setAttribute("completedOrder", order);

            } else {
                String bookingValue = request.getParameter("bookingId");

                // Hỗ trợ mua thêm cho một đơn vé đã thanh toán trước đó.
                if (bookingValue != null) {
                    int bookingId = parsePositiveInt(
                            bookingValue,
                            "Mã đơn vé không hợp lệ.");

                    Booking booking = dashboardService.getStaffBooking(
                            user.getUserId(), bookingId);

                    if (!"CONFIRMED".equals(booking.getStatus())) {
                        throw new IllegalArgumentException(
                                "Đơn vé chưa được xác nhận.");
                    }

                    setLinkedBooking(request, booking);
                }

                request.setAttribute(
                        "foodItems",
                        foodOrderService.getFoodsForCounter(cinemaId));

                request.setAttribute(
                        "combos",
                        foodOrderService.getCombosForCounter(cinemaId));

                request.setAttribute("canSellFood", true);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Cannot load linked food sale.", e);

            request.setAttribute(
                    "error", "Không thể tải đơn vé đi kèm.");
        }

        synchronized (session) {
            if (session.getAttribute("foodSaleToken") == null) {
                session.setAttribute(
                        "foodSaleToken",
                        UUID.randomUUID().toString());
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

    private void setLinkedBooking(
            HttpServletRequest request,
            Booking booking) {

        request.setAttribute("linkedBooking", booking);

        Showtime showtime = bookingService.getShowtimeForBooking(
                booking);

        if (showtime != null) {
            request.setAttribute("linkedShowtime", showtime);

            request.setAttribute(
                    "linkedMovie",
                    bookingService.getMovieForShowtime(showtime));
        }
    }

    private void createFoodOrder(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        try {
            String expected = (String) session.getAttribute(
                    "foodSaleToken");

            if (expected == null
                    || !expected.equals(
                            request.getParameter("saleToken"))) {

                throw new IllegalArgumentException(
                        "Yêu cầu đã xử lý hoặc trang đã cũ. Vui lòng chọn lại.");
            }

            session.removeAttribute("foodSaleToken");

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

            String bookingValue = request.getParameter("bookingId");

            if (bookingValue != null) {
                order.setBookingId(parsePositiveInt(
                        bookingValue,
                        "Mã đơn vé đi kèm không hợp lệ."));
            }

            int orderId = foodOrderService.createPaidOrder(
                    order, buildOrderItems(request));

            session.setAttribute(
                    "foodSaleSuccess",
                    "Thanh toán đơn hàng thành công.");

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
