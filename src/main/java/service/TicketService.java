package service;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Booking;
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
    
    // UC-15: danh sách vé đã được phát hành của customer.
public List<Ticket> getTicketsByCustomer(int customerId)
        throws SQLException {

    if (customerId <= 0) {

        throw new IllegalArgumentException(
                "Khách hàng không hợp lệ."
        );
    }

    return ticketRepository
            .findIssuedTicketsByCustomerId(
                    customerId
            );
}


public Ticket getTicketDetail(
        int ticketId,
        int customerId
) throws SQLException {

    if (ticketId <= 0
            || customerId <= 0) {

        throw new IllegalArgumentException(
                "Vé không hợp lệ."
        );
    }

    Ticket ticket
            = ticketRepository
                    .findIssuedTicketByIdAndCustomerId(
                            ticketId,
                            customerId
                    );

    if (ticket == null) {

        throw new IllegalArgumentException(
                "Không tìm thấy vé của bạn."
        );
    }

    return ticket;
}


public Booking getBookingForTicket(
        Ticket ticket,
        int customerId
) throws SQLException {

    if (ticket == null
            || customerId <= 0) {

        return null;
    }

    return bookingRepository
            .findByIdAndCustomerId(
                    ticket.getBookingId(),
                    customerId
            );
}


public Showtime getShowtimeForBooking(
        Booking booking
) {

    if (booking == null) {
        return null;
    }

    return showtimeRepository
            .getById(
                    booking.getShowtimeId()
            );
}


public Movie getMovieForShowtime(
        Showtime showtime
) {

    if (showtime == null) {
        return null;
    }

    return movieRepository
            .getByIdAnyStatus(
                    showtime.getMovieId()
            );
}


public Seat getSeatForTicket(
        Ticket ticket,
        Showtime showtime
) {

    if (ticket == null
            || showtime == null) {

        return null;
    }

    return seatRepository
            .findByIdAndRoom(
                    ticket.getSeatId(),
                    showtime.getRoomId()
            );
}


    // UC-25: bán vé tiền mặt tại quầy.
    public Booking sellCounterCashTickets(
            int staffId,
            int cinemaId,
            int showtimeId,
            String email,
            String[] seatValues) throws SQLException {

        if (staffId <= 0 || cinemaId <= 0 || showtimeId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin bán vé không hợp lệ.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập email khách hàng.");
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

        // Kiểm tra ghế theo cùng thứ tự để giảm nguy cơ khóa chéo.
        Collections.sort(seatIds);

        Connection connection = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Không thể kết nối database.");
        }

        try (Connection conn = connection) {

            // Booking, Ticket và Payment phải được lưu cùng nhau.
            conn.setAutoCommit(false);

            try {
                int staffCinemaId
                        = ticketRepository.findCounterStaffCinema(
                                conn, staffId);

                if (staffCinemaId != cinemaId) {
                    throw new IllegalArgumentException(
                            "Nhân viên không thuộc rạp này.");
                }

                int customerId
                        = ticketRepository.findCounterCustomerId(
                                conn, email.trim());

                if (customerId <= 0) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy tài khoản khách hàng đang hoạt động.");
                }

                Showtime showtime
                        = ticketRepository.findCounterShowtime(
                                conn, showtimeId, cinemaId);

                if (showtime == null) {
                    throw new IllegalArgumentException(
                            "Suất chiếu không thuộc rạp hoặc không còn mở bán.");
                }

                List<Ticket> tickets = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (int seatId : seatIds) {

                    if (ticketRepository.isCounterSeatSold(
                            conn, showtimeId, seatId)) {

                        throw new IllegalArgumentException(
                                "Có ghế vừa được bán. Vui lòng chọn lại.");
                    }

                    // Lấy giá từ database khi xác nhận bán vé.
                    BigDecimal price
                            = ticketRepository.findCounterSeatPrice(
                                    conn,
                                    seatId,
                                    showtime.getRoomId(),
                                    cinemaId);

                    if (price == null || price.signum() < 0) {
                        throw new IllegalArgumentException(
                                "Ghế không thuộc phòng, ngừng sử dụng hoặc chưa có giá.");
                    }

                    Ticket ticket = new Ticket();

                    ticket.setSeatId(seatId);
                    ticket.setTicketPrice(price);
                    ticket.setStatus("VALID");

                    tickets.add(ticket);
                    total = total.add(price);
                }

                Booking booking = new Booking();

                booking.setCustomerId(customerId);

                // ID nhân viên được controller lấy từ session.
                booking.setStaffId(staffId);

                booking.setShowtimeId(showtimeId);
                booking.setPromotionId(null);
                booking.setSubtotal(total);
                booking.setDiscountAmount(BigDecimal.ZERO);
                booking.setTotalAmount(total);
                booking.setStatus("CONFIRMED");

                int bookingId
                        = bookingRepository.insert(conn, booking);

                booking.setBookingId(bookingId);

                for (Ticket ticket : tickets) {

                    ticket.setBookingId(bookingId);

                    ticket.setTicketCode(
                            "COUNTER-" + bookingId
                            + "-" + ticket.getSeatId());

                    ticketRepository.insert(
                            conn, ticket, showtimeId);
                }

                paymentRepository.insertCounterCashPayment(
                        conn, bookingId, total);

                conn.commit();

                return booking;

            } catch (SQLException | RuntimeException e) {

                // Có lỗi thì hủy toàn bộ thay đổi của đơn đang tạo.
                conn.rollback();
                throw e;
            }
        }
    }
}
