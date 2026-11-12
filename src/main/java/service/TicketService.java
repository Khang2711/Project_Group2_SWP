package service;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Booking;
import model.FoodOrder;
import model.FoodOrderItem;
import model.Movie;
import model.Seat;
import model.Showtime;
import model.Ticket;
import repository.BookingRepository;
import repository.MovieRepository;
import repository.PaymentRepository;
import repository.SeatRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;

public class TicketService {

    private final BookingRepository bookingRepository
            = new BookingRepository();

    private final TicketRepository ticketRepository
            = new TicketRepository();

    private final PaymentRepository paymentRepository
            = new PaymentRepository();

    private final ShowtimeRepository showtimeRepository
            = new ShowtimeRepository();

    private final MovieRepository movieRepository
            = new MovieRepository();

    private final SeatRepository seatRepository
            = new SeatRepository();

    public List<Ticket> getTicketsByCustomer(int customerId)
            throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Khách hàng không hợp lệ.");
        }

        return ticketRepository.findIssuedTicketsByCustomerId(
                customerId);
    }

    public Ticket getTicketDetail(
            int ticketId,
            int customerId) throws SQLException {

        if (ticketId <= 0 || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Vé không hợp lệ.");
        }

        Ticket ticket
                = ticketRepository.findIssuedTicketByIdAndCustomerId(
                        ticketId, customerId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy vé của bạn.");
        }

        return ticket;
    }

    public Booking getBookingForTicket(
            Ticket ticket,
            int customerId) throws SQLException {

        if (ticket == null || customerId <= 0) {
            return null;
        }

        return bookingRepository.findByIdAndCustomerId(
                ticket.getBookingId(), customerId);
    }

    public Showtime getShowtimeForBooking(Booking booking) {

        if (booking == null) {
            return null;
        }

        return showtimeRepository.getById(
                booking.getShowtimeId());
    }

    public Movie getMovieForShowtime(Showtime showtime) {

        if (showtime == null) {
            return null;
        }

        return movieRepository.getByIdAnyStatus(
                showtime.getMovieId());
    }

    public Seat getSeatForTicket(
            Ticket ticket,
            Showtime showtime) {

        if (ticket == null || showtime == null) {
            return null;
        }

        return seatRepository.findByIdAndRoom(
                ticket.getSeatId(),
                showtime.getRoomId());
    }

    // Chỉ xem trước, chưa ghi Booking, Ticket hoặc Payment.
    public Booking previewCounterTickets(
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            String[] seatValues) throws SQLException {

        List<Integer> seatIds = validateCounterInput(
                staffId,
                cinemaId,
                showtimeId,
                email,
                seatValues);

        try (Connection connection = openConnection()) {

            List<Ticket> tickets = new ArrayList<>();

            return prepareCounterBooking(
                    connection,
                    staffId,
                    cinemaId,
                    showtimeId,
                    email,
                    seatIds,
                    tickets);
        }
    }

    // Giữ hàm cũ để các nơi đang gọi không bị lỗi.
    public Booking sellCounterCashTickets(
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            String[] seatValues) throws SQLException {

        return sellCounterCashOrder(
                staffId,
                cinemaId,
                showtimeId,
                email,
                seatValues,
                Collections.emptyList(),
                null,
                null);
    }

    public Booking sellCounterCashOrder(
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            String[] seatValues,
            List<FoodOrderItem> foodItems,
            BigDecimal expectedTicketTotal,
            BigDecimal expectedFoodTotal) throws SQLException {

        List<Integer> seatIds = validateCounterInput(
                staffId,
                cinemaId,
                showtimeId,
                email,
                seatValues);

        if (foodItems == null) {
            foodItems = Collections.emptyList();
        }

        try (Connection connection = openConnection()) {

            connection.setAutoCommit(false);

            try {
                List<Ticket> tickets = new ArrayList<>();

                // Kiểm tra lại ghế, khách hàng và giá khi xác nhận.
                Booking booking = prepareCounterBooking(
                        connection,
                        staffId,
                        cinemaId,
                        showtimeId,
                        email,
                        seatIds,
                        tickets);

                if (expectedTicketTotal != null
                        && booking.getTotalAmount()
                                .compareTo(expectedTicketTotal) != 0) {

                    throw new IllegalArgumentException(
                            "Giá vé đã thay đổi. Vui lòng kiểm tra lại tổng tiền.");
                }

                if (foodItems.isEmpty()
                        && expectedFoodTotal != null
                        && expectedFoodTotal.signum() != 0) {

                    throw new IllegalArgumentException(
                            "Danh sách bắp nước không khớp với đơn đang xác nhận.");
                }

                booking.setStatus("CONFIRMED");

                int bookingId = bookingRepository.insert(
                        connection, booking);

                booking.setBookingId(bookingId);

                for (Ticket ticket : tickets) {

                    ticket.setBookingId(bookingId);
                    ticket.setStatus("VALID");

                    ticket.setTicketCode(
                            "COUNTER-" + bookingId
                            + "-" + ticket.getSeatId());

                    ticketRepository.insert(
                            connection,
                            ticket,
                            showtimeId);
                }

                paymentRepository.insertCounterCashPayment(
                        connection,
                        bookingId,
                        booking.getTotalAmount());

                if (!foodItems.isEmpty()) {

                    FoodOrder order = new FoodOrder();

                    order.setStaffId(staffId);
                    order.setCinemaId(cinemaId);
                    order.setBookingId(bookingId);
                    order.setPaymentMethod("CASH");

                    FoodOrderService foodService
                            = new FoodOrderService();

                    foodService.createPaidOrder(
                            connection,
                            order,
                            foodItems,
                            expectedFoodTotal);
                }

                // Cả vé và bắp nước đều lưu được thì mới hoàn tất.
                connection.commit();

                return booking;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private List<Integer> validateCounterInput(
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            String[] seatValues) {

        if (staffId <= 0 || cinemaId <= 0 || showtimeId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin bán vé không hợp lệ.");
        }

        if (seatValues == null || seatValues.length == 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn ít nhất một ghế.");
        }

        List<Integer> seatIds = new ArrayList<>();

        for (String value : seatValues) {

            int seatId;

            try {
                seatId = Integer.parseInt(value);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Mã ghế không hợp lệ.");
            }

            if (seatId <= 0 || seatIds.contains(seatId)) {
                throw new IllegalArgumentException(
                        "Ghế không hợp lệ hoặc bị trùng.");
            }

            seatIds.add(seatId);
        }

        Collections.sort(seatIds);

        return seatIds;
    }

    private Booking prepareCounterBooking(
            Connection connection,
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            List<Integer> seatIds,
            List<Ticket> tickets) throws SQLException {

        int staffCinemaId
                = ticketRepository.findCounterStaffCinema(
                        connection, staffId);

        if (staffCinemaId != cinemaId) {
            throw new IllegalArgumentException(
                    "Nhân viên không thuộc rạp này.");
        }

        Showtime showtime
                = ticketRepository.findCounterShowtime(
                        connection,
                        showtimeId,
                        cinemaId);

        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Suất chiếu không thuộc rạp hoặc không còn mở bán.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (int seatId : seatIds) {

            if (ticketRepository.isCounterSeatSold(
                    connection,
                    showtimeId,
                    seatId)) {

                throw new IllegalArgumentException(
                        "Có ghế vừa được bán. Vui lòng chọn lại.");
            }

            // Dùng chung quy tắc giá với phần chọn ghế của nhóm.
            BigDecimal price = bookingRepository.findSeatPrice(
                    showtimeId, seatId);

            if (price == null || price.signum() < 0) {
                throw new IllegalArgumentException(
                        "Ghế không hợp lệ hoặc chưa có giá cho suất này.");
            }

            Ticket ticket = new Ticket();

            ticket.setSeatId(seatId);
            ticket.setTicketPrice(price);
            ticket.setStatus("PENDING");

            tickets.add(ticket);

            total = total.add(price);
        }

        Booking booking = new Booking();

        booking.setCustomerId(null);
        booking.setStaffId(staffId);
        booking.setShowtimeId(showtimeId);
        booking.setPromotionId(null);
        booking.setSubtotal(total);
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setTotalAmount(total);
        booking.setStatus("PENDING");

        return booking;
    }

    private Connection openConnection() throws SQLException {

        Connection connection = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Không thể kết nối database.");
        }

        return connection;
    }
}
