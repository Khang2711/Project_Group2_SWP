package service;

import db.DBContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Booking;
import model.Cinema;
import model.Combo;
import model.FoodItem;
import model.FoodOrderItem;
import model.Movie;
import model.Promotion;
import model.Room;
import model.Seat;
import model.Showtime;
import model.Ticket;
import repository.BookingRepository;
import repository.CinemaRepository;
import repository.ComboRepository;
import repository.FoodItemRepository;
import repository.MovieRepository;
import repository.RoomRepository;
import repository.SeatRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;
import repository.FoodOrderRepository;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final PromotionService promotionService;
    private final ComboRepository comboRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodOrderService foodOrderService;
    private final FoodOrderRepository foodOrderRepository
        = new FoodOrderRepository();

    public BookingService() {
        bookingRepository = new BookingRepository();
        ticketRepository = new TicketRepository();
        showtimeRepository = new ShowtimeRepository();
        seatRepository = new SeatRepository();
        movieRepository = new MovieRepository();
        cinemaRepository = new CinemaRepository();
        roomRepository = new RoomRepository();
        promotionService = new PromotionService();
        comboRepository = new ComboRepository();
        foodItemRepository = new FoodItemRepository();
        foodOrderService = new FoodOrderService();
    }
    
    public List<Map<String, Object>> getOnlineFoodItemsForBooking(
        int bookingId,
        int customerId) throws SQLException {

    // Xác minh booking thuộc khách hàng đang đăng nhập.
    getBookingDetail(bookingId, customerId);

    return foodOrderRepository.findOnlineItemsByBooking(bookingId);
}

    // =====================================================
    // BUG-06 FIX:
    // KIEM TRA DAY DU DIEU KIEN MO BAN
    // =====================================================
    public Showtime getBookableShowtime(int showtimeId) {

        if (showtimeId <= 0) {
            throw new IllegalArgumentException(
                    "Suất chiếu không hợp lệ."
            );
        }

        Showtime showtime
                = showtimeRepository.getById(showtimeId);

        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy suất chiếu."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                showtime.getStatus())) {

            throw new IllegalArgumentException(
                    "Suất chiếu hiện không mở bán."
            );
        }

        if (showtime.getStartTime() == null
                || !showtime.getStartTime()
                        .toLocalDateTime()
                        .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Suất chiếu đã bắt đầu hoặc đã kết thúc."
            );
        }

        // BUG-06 FIX 1:
        // Phim phai ton tai va dang ACTIVE.
        Movie movie = movieRepository.getByIdAnyStatus(
                showtime.getMovieId()
        );

        if (movie == null) {
            throw new IllegalArgumentException(
                    "Phim của suất chiếu không tồn tại."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                movie.getStatus())) {

            throw new IllegalArgumentException(
                    "Phim đã ngừng hoạt động, không thể đặt vé."
            );
        }

        // Kiem tra ngay chieu thuoc thoi gian
        // phat hanh va kinh doanh cua phim.
        LocalDate showDate = showtime.getStartTime()
                .toLocalDateTime()
                .toLocalDate();

        if (movie.getReleaseDate() == null
                || movie.getReleaseDate()
                        .toLocalDate()
                        .isAfter(showDate)) {

            throw new IllegalArgumentException(
                    "Phim chưa đến ngày phát hành."
            );
        }

        if (movie.getEndDate() != null
                && movie.getEndDate()
                        .toLocalDate()
                        .isBefore(showDate)) {

            throw new IllegalArgumentException(
                    "Phim đã hết thời gian chiếu."
            );
        }

        // BUG-06 FIX 2:
        // Xac dinh rap that su cua suat chieu.
        int cinemaId
                = showtimeRepository.getCinemaIdByShowtimeId(
                        showtimeId
                );

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được rạp của suất chiếu."
            );
        }

        // BUG-06 FIX 3:
        // Phong phai ton tai va dang ACTIVE.
        Room room = roomRepository.findByIdAndCinema(
                showtime.getRoomId(),
                cinemaId
        );

        if (room == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phòng chiếu."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                room.getStatus())) {

            throw new IllegalArgumentException(
                    "Phòng chiếu đã ngừng hoạt động."
            );
        }

        // BUG-06 FIX 4:
        // Rap phai ton tai va dang ACTIVE.
        Cinema cinema = cinemaRepository.findById(
                cinemaId
        );

        if (cinema == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy rạp chiếu."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                cinema.getStatus())) {

            throw new IllegalArgumentException(
                    "Rạp đã ngừng hoạt động, "
                    + "không thể đặt vé."
            );
        }

        return showtime;
    }

    // =====================================================
    // LAY DANH SACH GHE
    // =====================================================
    public List<Seat> getSeatsForShowtime(int showtimeId) {

        Showtime showtime = getBookableShowtime(showtimeId);

        return seatRepository.findByRoomId(
                showtime.getRoomId()
        );
    }

    public Set<Integer> getOccupiedSeatIds(int showtimeId)
            throws SQLException {

        getBookableShowtime(showtimeId);

        return ticketRepository.findOccupiedSeatIds(
                showtimeId
        );
    }

    // =====================================================
    // LAY GIA GHE
    // =====================================================
    public Map<Integer, BigDecimal> getSeatPrices(
            int showtimeId,
            List<Seat> seats) throws SQLException {

        getBookableShowtime(showtimeId);

        Map<Integer, BigDecimal> prices
                = new LinkedHashMap<>();

        for (Seat seat : seats) {

            if (!"ACTIVE".equals(seat.getStatus())) {
                continue;
            }

            BigDecimal price
                    = bookingRepository.findSeatPrice(
                            showtimeId,
                            seat.getSeatId()
                    );

            if (price != null) {
                prices.put(seat.getSeatId(), price);
            }
        }

        return prices;
    }

    // =====================================================
    // KIEM TRA GHE DUOC CHON
    // =====================================================
    public List<Seat> prepareSelectedSeats(
            int showtimeId,
            String[] seatValues,
            boolean fullRoom) throws SQLException {

        List<Seat> roomSeats
                = getSeatsForShowtime(showtimeId);

        Set<Integer> occupiedSeatIds
                = ticketRepository.findOccupiedSeatIds(
                        showtimeId
                );

        List<Seat> activeSeats = new ArrayList<>();

        for (Seat seat : roomSeats) {

            if ("ACTIVE".equals(seat.getStatus())) {
                activeSeats.add(seat);
            }
        }

        if (activeSeats.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phòng chiếu chưa có ghế đang hoạt động."
            );
        }

        if (fullRoom) {

            for (Seat seat : activeSeats) {

                if (occupiedSeatIds.contains(
                        seat.getSeatId())) {

                    throw new IllegalArgumentException(
                            "Không thể đặt toàn phòng "
                            + "vì đã có ghế được xác nhận."
                    );
                }
            }

            return activeSeats;
        }

        if (seatValues == null
                || seatValues.length == 0) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế."
            );
        }

        Set<Integer> selectedIds
                = new LinkedHashSet<>();

        for (String value : seatValues) {

            try {

                int seatId = Integer.parseInt(value);

                if (seatId <= 0) {
                    throw new NumberFormatException();
                }

                selectedIds.add(seatId);

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Ghế được chọn không hợp lệ."
                );
            }
        }

        List<Seat> selectedSeats
                = new ArrayList<>();

        for (Integer selectedId : selectedIds) {

            Seat matchedSeat = null;

            for (Seat seat : activeSeats) {

                if (seat.getSeatId() == selectedId) {
                    matchedSeat = seat;
                    break;
                }
            }

            if (matchedSeat == null) {
                throw new IllegalArgumentException(
                        "Có ghế không thuộc phòng chiếu "
                        + "hoặc đã ngừng hoạt động."
                );
            }

            if (occupiedSeatIds.contains(selectedId)) {
                throw new IllegalArgumentException(
                        "Có ghế đã được người khác xác nhận. "
                        + "Vui lòng chọn lại."
                );
            }

            selectedSeats.add(matchedSeat);
        }

        return selectedSeats;
    }

    // =====================================================
    // TINH TONG TIEN VE
    // =====================================================
    public BigDecimal calculateSubtotal(
            int showtimeId,
            List<Seat> selectedSeats) throws SQLException {

        getBookableShowtime(showtimeId);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Seat seat : selectedSeats) {

            BigDecimal price
                    = bookingRepository.findSeatPrice(
                            showtimeId,
                            seat.getSeatId()
                    );

            if (price == null) {
                throw new IllegalArgumentException(
                        "Chưa có giá vé hợp lệ cho ghế đã chọn."
                );
            }

            subtotal = subtotal.add(price);
        }

        return subtotal;
    }

    // =====================================================
    // KIEM TRA KHUYEN MAI
    // =====================================================
    public Promotion getValidPromotion(
            String promoCode,
            BigDecimal subtotal,
            int showtimeId) {

        if (promoCode == null
                || promoCode.trim().isEmpty()) {
            return null;
        }

        if (promoCode.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Mã khuyến mãi không được vượt quá 50 ký tự."
            );
        }

        int cinemaId
                = promotionService.getCinemaIdByShowtimeId(
                        showtimeId
                );

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được rạp của suất chiếu."
            );
        }

        Promotion promotion
                = promotionService.getPromotionByCode(
                        promoCode,
                        cinemaId
                );

        String error
                = promotionService.validateApplyPromotion(
                        promotion,
                        subtotal
                );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        return promotion;
    }

    public List<Promotion> getAvailablePromotions(
            BigDecimal subtotal,
            int showtimeId) {

        List<Promotion> availablePromotions
                = new ArrayList<>();

        if (subtotal == null || showtimeId <= 0) {
            return availablePromotions;
        }

        int cinemaId
                = promotionService.getCinemaIdByShowtimeId(
                        showtimeId
                );

        if (cinemaId <= 0) {
            return availablePromotions;
        }

        List<Promotion> promotions
                = promotionService.getAllPromotions(
                        cinemaId
                );

        for (Promotion promotion : promotions) {

            String error
                    = promotionService.validateApplyPromotion(
                            promotion,
                            subtotal
                    );

            if (error == null) {
                availablePromotions.add(promotion);
            }
        }

        return availablePromotions;
    }

    public BigDecimal calculatePromotionDiscount(
            Promotion promotion,
            BigDecimal subtotal) {

        BigDecimal discount
                = promotionService.calculateDiscount(
                        promotion,
                        subtotal
                );

        return discount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    // =====================================================
    // GIU CAC OVERLOAD CU
    // =====================================================
    public int createPendingBooking(
            int customerId,
            int showtimeId,
            String[] seatValues) throws SQLException {

        return createPendingBooking(
                customerId,
                showtimeId,
                seatValues,
                null
        );
    }

    public List<Combo> getCombosForCheckout(
            int showtimeId) {

        getBookableShowtime(showtimeId);

        int cinemaId
                = promotionService.getCinemaIdByShowtimeId(
                        showtimeId
                );

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được rạp của suất chiếu."
            );
        }

        return comboRepository.findActiveForSale(
                cinemaId
        );
    }

    public List<FoodItem> getFoodItemsForCheckout(
            int showtimeId) {

        getBookableShowtime(showtimeId);

        int cinemaId
                = promotionService.getCinemaIdByShowtimeId(
                        showtimeId
                );

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Không xác định được rạp của suất chiếu."
            );
        }

        return foodItemRepository.findActiveByCinema(
                cinemaId
        );
    }

    public int createPendingBooking(
            int customerId,
            int showtimeId,
            String[] seatValues,
            String promoCode) throws SQLException {

        return createPendingBooking(
                customerId,
                showtimeId,
                seatValues,
                promoCode,
                new ArrayList<FoodOrderItem>()
        );
    }

    // =====================================================
    // BUG-06 FIX:
    // KIEM TRA LAI TRUOC KHI GHI BOOKING
    // =====================================================
    public int createPendingBooking(
            int customerId,
            int showtimeId,
            String[] seatValues,
            String promoCode,
            List<FoodOrderItem> comboItems) throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản khách hàng không hợp lệ."
            );
        }

        // Kiem tra trang thai phim, phong, rap,
        // suat chieu ngay truoc luong checkout.
        getBookableShowtime(showtimeId);

        List<Seat> selectedSeats
                = prepareSelectedSeats(
                        showtimeId,
                        seatValues,
                        false
                );

        Map<Integer, BigDecimal> selectedPrices
                = getSeatPrices(
                        showtimeId,
                        selectedSeats
                );

        if (selectedPrices.size()
                != selectedSeats.size()) {

            throw new IllegalArgumentException(
                    "Có ghế chưa có giá vé hợp lệ. "
                    + "Vui lòng chọn lại."
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Seat seat : selectedSeats) {
            subtotal = subtotal.add(
                    selectedPrices.get(
                            seat.getSeatId()
                    )
            );
        }

        Promotion promotion
                = getValidPromotion(
                        promoCode,
                        subtotal,
                        showtimeId
                );

        BigDecimal discount
                = calculatePromotionDiscount(
                        promotion,
                        subtotal
                );

        int cinemaId
                = promotionService.getCinemaIdByShowtimeId(
                        showtimeId
                );

        BigDecimal comboTotal
                = foodOrderService.calculateOnlineComboTotal(
                        cinemaId,
                        comboItems
                );

        Booking booking = new Booking();

        booking.setCustomerId(customerId);
        booking.setShowtimeId(showtimeId);

        booking.setPromotionId(
                promotion == null
                        ? null
                        : promotion.getPromotionId()
        );

        booking.setSubtotal(subtotal);
        booking.setDiscountAmount(discount);

        booking.setTotalAmount(
                subtotal.subtract(discount)
                        .add(comboTotal)
        );

        booking.setStatus("PENDING");

        try (Connection connection = getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Recheck truoc khi ghi DB.
                // Chua thay the duoc database locking.
                getBookableShowtime(showtimeId);

                int bookingId
                        = bookingRepository.insert(
                                connection,
                                booking
                        );

                for (Seat seat : selectedSeats) {

                    Ticket ticket = new Ticket();

                    ticket.setBookingId(bookingId);
                    ticket.setSeatId(seat.getSeatId());
                    ticket.setTicketCode(null);

                    ticket.setTicketPrice(
                            selectedPrices.get(
                                    seat.getSeatId()
                            )
                    );

                    ticket.setStatus("PENDING");
                    ticket.setCheckedInAt(null);

                    ticketRepository.insert(
                            connection,
                            ticket,
                            showtimeId
                    );
                }

                // Có sản phẩm được chọn thì phải lưu,
// kể cả khi tổng tiền bắp nước bằng 0.
                if (comboItems != null && !comboItems.isEmpty()) {

                    foodOrderService.createPendingOnlineOrder(
                            connection,
                            bookingId,
                            cinemaId,
                            comboItems,
                            comboTotal
                    );
                }

                connection.commit();

                return bookingId;

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }

    // =====================================================
    // LICH SU VA CHI TIET BOOKING
    // =====================================================
    public List<Booking> getBookingsByCustomer(
            int customerId) throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Khách hàng không hợp lệ."
            );
        }

        return bookingRepository.findByCustomerId(
                customerId
        );
    }

    public Booking getBookingDetail(
            int bookingId,
            int customerId) throws SQLException {

        if (bookingId <= 0 || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Đơn đặt vé không hợp lệ."
            );
        }

        Booking booking
                = bookingRepository.findByIdAndCustomerId(
                        bookingId,
                        customerId
                );

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đơn đặt vé của bạn."
            );
        }

        return booking;
    }

    public List<Ticket> getTicketsByBooking(
            int bookingId) throws SQLException {

        if (bookingId <= 0) {
            throw new IllegalArgumentException(
                    "Đơn đặt vé không hợp lệ."
            );
        }

        return ticketRepository.findByBookingId(
                bookingId
        );
    }

    public Showtime getShowtimeForBooking(
            Booking booking) {

        if (booking == null) {
            return null;
        }

        return showtimeRepository.getById(
                booking.getShowtimeId()
        );
    }

    public Movie getMovieForShowtime(
            Showtime showtime) {

        if (showtime == null) {
            return null;
        }

        return movieRepository.getByIdAnyStatus(
                showtime.getMovieId()
        );
    }

    public Seat getSeatForTicket(
            Ticket ticket,
            Showtime showtime) {

        if (ticket == null || showtime == null) {
            return null;
        }

        return seatRepository.findByIdAndRoom(
                ticket.getSeatId(),
                showtime.getRoomId()
        );
    }

    private Connection getConnection()
            throws SQLException {

        Connection connection
                = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Cannot connect to FCinemaDB."
            );
        }

        return connection;
    }
    
    
}
