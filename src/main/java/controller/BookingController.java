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
import model.Combo;
import model.FoodOrderItem;
import java.util.Set;
import model.Booking;
import model.Cinema;
import model.Movie;
import model.Promotion;
import model.Seat;
import model.Showtime;
import model.Ticket;
import model.User;
import service.BookingService;
import service.CinemaService;

@WebServlet(urlPatterns = {
    "/user/select-seats",
    "/user/checkout",
    "/user/bookings",
    "/user/booking-detail"
})
public class BookingController extends HttpServlet {

    private BookingService bookingService;
    private final CinemaService cinemaService = new CinemaService();

    @Override
    public void init() {
        bookingService = new BookingService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
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
    protected void doPost(
            HttpServletRequest request,
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

        List<Seat> selectedSeats
                = bookingService.prepareSelectedSeats(
                        showtimeId,
                        request.getParameterValues("seatId"),
                        selectWholeRoom);

        if (fullRoom && !selectWholeRoom) {
            bookingService.requireFullRoomSelection(showtimeId, selectedSeats);
        }

        Map<Integer, BigDecimal> seatPrices
                = bookingService.getSeatPrices(
                        showtimeId,
                        selectedSeats);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Seat seat : selectedSeats) {
            BigDecimal price = seatPrices.get(seat.getSeatId());

            if (price == null) {
                throw new IllegalArgumentException(
                        "Có ghế chưa có giá vé hợp lệ. "
                        + "Vui lòng chọn lại.");
            }

            subtotal = subtotal.add(price);
        }

        List<Combo> checkoutCombos = bookingService.getCombosForCheckout(showtimeId);
        List<model.FoodItem> checkoutFoods = bookingService.getFoodItemsForCheckout(showtimeId);
        Map<Integer, Integer> selectedComboQuantities = readComboQuantities(request);
        Map<Integer, Integer> selectedFoodQuantities = readFoodQuantities(request);
        BigDecimal comboTotal = BigDecimal.ZERO;
        for (Combo combo : checkoutCombos) {
            Integer quantity = selectedComboQuantities.get(combo.getComboId());
            if (quantity != null) comboTotal = comboTotal.add(combo.getComboPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        BigDecimal foodTotal = BigDecimal.ZERO;
        for (model.FoodItem food : checkoutFoods) {
            Integer quantity = selectedFoodQuantities.get(food.getFoodItemId());
            if (quantity != null) {
                foodTotal = foodTotal.add(food.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }
        String action = request.getParameter("action");
        String promoCode = request.getParameter("promoCode");

        if (promoCode == null
                || "REMOVE_PROMO".equals(action)) {
            promoCode = "";
        }

        promoCode = promoCode.trim();

        // Both discounts use the complete amount: seats + combos + drinks.
        BigDecimal orderTotal = subtotal.add(comboTotal).add(foodTotal);
        BigDecimal fullRoomDiscount = bookingService.calculateFullRoomDiscount(
                orderTotal, fullRoom);
        BigDecimal promoBase = orderTotal.subtract(fullRoomDiscount);

        Promotion promotion = null;
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        String promoError = null;

        try {
            if ("APPLY_PROMO".equals(action)
                    && promoCode.isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập mã khuyến mãi.");
            }

            promotion = bookingService.getValidPromotion(
                    promoCode,
                    promoBase,
                    showtimeId);

            voucherDiscount = bookingService.calculatePromotionDiscount(
                    promotion,
                    promoBase);

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

        Showtime checkoutShowtime
                = bookingService.getBookableShowtime(showtimeId);
        Movie checkoutMovie
                = bookingService.getMovieForShowtime(checkoutShowtime);

        request.setAttribute("showtime", checkoutShowtime);
        request.setAttribute("cinema", cinemaService.getCinemaByShowtime(checkoutShowtime));
        request.setAttribute("movie", checkoutMovie);
        request.setAttribute(
                "availablePromotions",
                bookingService.getAvailablePromotions(promoBase, showtimeId));

        request.setAttribute("selectedSeats", selectedSeats);
        request.setAttribute("checkoutCombos", checkoutCombos);
        request.setAttribute("checkoutFoods", checkoutFoods);
        request.setAttribute("selectedFoodQuantities", selectedFoodQuantities);
        request.setAttribute("foodTotal", foodTotal);
        request.setAttribute("selectedComboQuantities", selectedComboQuantities);
        request.setAttribute("comboTotal", comboTotal);
        request.setAttribute("seatPrices", seatPrices);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("fullRoom", fullRoom);
        request.setAttribute("promoCode", promoCode);
        request.setAttribute("appliedPromotion", promotion);
        request.setAttribute("fullRoomDiscount", fullRoomDiscount);
        request.setAttribute("voucherDiscount", voucherDiscount);
        request.setAttribute("discountAmount",
                fullRoomDiscount.add(voucherDiscount));

        request.setAttribute(
                "finalAmount",
                orderTotal.subtract(fullRoomDiscount).subtract(voucherDiscount));

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

        int bookingId;

        try {
            // Service đọc lại giá và kiểm tra mã trước khi lưu.
            bookingId = bookingService.createPendingBooking(
                    customerId,
                    showtimeId,
                    request.getParameterValues("seatId"),
                    request.getParameter("promoCode"),
                    readComboItems(request),
                    "FULL_ROOM".equals(request.getParameter("bookingMode")));

        } catch (IllegalArgumentException e) {
            request.setAttribute(
                    "checkoutError",
                    e.getMessage());

            prepareCheckout(request, response);
            return;

        } catch (SQLException e) {
            log("Cannot create pending booking with online food order.", e);

            String message = "Không thể tạo đơn đặt vé hoặc lưu sản phẩm đã chọn. "
                    + "Vui lòng kiểm tra cấu hình database rồi thử lại.";
            String sqlMessage = e.getMessage();

            if (sqlMessage != null
                    && sqlMessage.toLowerCase().contains("staff_id")) {
                message = "Database hiện tại chưa cho phép Food_Order.staff_id nhận NULL. "
                        + "Hãy chạy database/allow_online_food_orders.sql một lần rồi thử lại.";
            }

            request.setAttribute("checkoutError", message);
            prepareCheckout(request, response);
            return;
        }

        // Giữ luồng chuyển sang trang thanh toán VNPay của nhóm.
        response.sendRedirect(
                request.getContextPath()
                + "/user/payment?bookingId=" + bookingId);
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
        request.setAttribute("cinema", cinemaService.getCinemaByShowtime(showtime));
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
        Map<Integer, Cinema> cinemaMap = new LinkedHashMap<>();

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

                Cinema cinema = cinemaService.getCinemaByShowtime(showtime);
                if (cinema != null) {
                    cinemaMap.put(booking.getBookingId(), cinema);
                }
            }
        }

        request.setAttribute("bookings", bookings);
        request.setAttribute("showtimeMap", showtimeMap);
        request.setAttribute("movieMap", movieMap);
        request.setAttribute("cinemaMap", cinemaMap);

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
        
        List<Map<String, Object>> onlineFoodItems
        = bookingService.getOnlineFoodItemsForBooking(
                bookingId,
                customerId
        );

request.setAttribute("onlineFoodItems", onlineFoodItems);

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
        request.setAttribute("cinema", cinemaService.getCinemaByShowtime(showtime));
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

    private List<FoodOrderItem> readComboItems(HttpServletRequest request) {
        List<FoodOrderItem> items = new ArrayList<>();
        String[] ids = request.getParameterValues("comboId");
        String[] quantities = request.getParameterValues("comboQuantity");
        if (ids != null || quantities != null) {
            if (ids == null || quantities == null || ids.length != quantities.length)
                throw new IllegalArgumentException("Thông tin combo không hợp lệ. Vui lòng chọn lại.");
        }
        if (ids != null) for (int i=0;i<ids.length;i++) {
            int id = parsePositiveInt(ids[i], "Combo được chọn không hợp lệ.");
            int quantity;
            try { quantity = Integer.parseInt(quantities[i]); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng combo không hợp lệ."); }
            if (quantity < 0 || quantity > 20) throw new IllegalArgumentException("Mỗi combo chỉ được chọn từ 0 đến 20 phần.");
            if (quantity > 0) { FoodOrderItem item = new FoodOrderItem(); item.setComboId(id); item.setQuantity(quantity); items.add(item); }
        }
        String[] foodIds = request.getParameterValues("foodItemId");
        String[] foodQuantities = request.getParameterValues("foodQuantity");
        if (foodIds != null || foodQuantities != null) {
            if (foodIds == null || foodQuantities == null || foodIds.length != foodQuantities.length)
                throw new IllegalArgumentException("Thông tin đồ ăn/nước uống không hợp lệ. Vui lòng chọn lại.");
            for (int i = 0; i < foodIds.length; i++) {
                int id = parsePositiveInt(foodIds[i], "Sản phẩm được chọn không hợp lệ.");
                int quantity;
                try { quantity = Integer.parseInt(foodQuantities[i]); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ."); }
                if (quantity < 0 || quantity > 20) throw new IllegalArgumentException("Mỗi sản phẩm chỉ được chọn từ 0 đến 20 phần.");
                if (quantity > 0) {
                    FoodOrderItem item = new FoodOrderItem();
                    item.setFoodItemId(id);
                    item.setComboId(null);
                    item.setQuantity(quantity);
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Map<Integer, Integer> readFoodQuantities(HttpServletRequest request) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        String[] ids = request.getParameterValues("foodItemId");
        String[] values = request.getParameterValues("foodQuantity");
        if (ids == null || values == null || ids.length != values.length) return map;
        for (int i = 0; i < ids.length; i++) {
            try {
                int id = Integer.parseInt(ids[i]);
                int quantity = Integer.parseInt(values[i]);
                if (id > 0 && quantity > 0 && quantity <= 20) map.put(id, quantity);
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private Map<Integer, Integer> readComboQuantities(HttpServletRequest request) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        String[] ids=request.getParameterValues("comboId"), values=request.getParameterValues("comboQuantity");
        if(ids==null || values==null || ids.length!=values.length) return map;
        for(int i=0;i<ids.length;i++) try { int id=Integer.parseInt(ids[i]), q=Integer.parseInt(values[i]); if(id>0 && q>0 && q<=20) map.put(id,q); } catch(NumberFormatException ignored) { }
        return map;
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
