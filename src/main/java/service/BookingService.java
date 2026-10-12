package service;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Booking;
import model.Seat;
import model.Showtime;
import model.Ticket;
import repository.BookingRepository;
import repository.SeatRepository;
import repository.ShowtimeRepository;
import repository.TicketRepository;
import model.Movie;
import repository.MovieRepository;
/**
 * Business logic for UC-12.
 */
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    public BookingService() {

        bookingRepository
                = new BookingRepository();

        ticketRepository
                = new TicketRepository();

        showtimeRepository
                = new ShowtimeRepository();

        seatRepository
                = new SeatRepository();
        movieRepository
        = new MovieRepository();
    }


    public Showtime getBookableShowtime(
            int showtimeId
    ) {

        if (showtimeId <= 0) {

            throw new IllegalArgumentException(
                    "Suất chiếu không hợp lệ."
            );
        }

        Showtime showtime
                = showtimeRepository
                        .getById(showtimeId);

        if (showtime == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy suất chiếu."
            );
        }

        if (!"ACTIVE".equals(
                showtime.getStatus()
        )) {

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

        return showtime;
    }


    public List<Seat> getSeatsForShowtime(
            int showtimeId
    ) {

        Showtime showtime
                = getBookableShowtime(
                        showtimeId
                );

        return seatRepository
                .findByRoomId(
                        showtime.getRoomId()
                );
    }


    public Set<Integer> getOccupiedSeatIds(
            int showtimeId
    ) throws SQLException {

        getBookableShowtime(
                showtimeId
        );

        return ticketRepository
                .findOccupiedSeatIds(
                        showtimeId
                );
    }


    public Map<Integer, BigDecimal> getSeatPrices(
            int showtimeId,
            List<Seat> seats
    ) throws SQLException {

        Map<Integer, BigDecimal> prices
                = new LinkedHashMap<>();

        for (Seat seat : seats) {

            if (!"ACTIVE".equals(
                    seat.getStatus()
            )) {

                continue;
            }

            BigDecimal price
                    = bookingRepository
                            .findSeatPrice(
                                    showtimeId,
                                    seat.getSeatId()
                            );

            if (price != null) {

                prices.put(
                        seat.getSeatId(),
                        price
                );
            }
        }

        return prices;
    }


    public List<Seat> prepareSelectedSeats(
            int showtimeId,
            String[] seatValues,
            boolean fullRoom
    ) throws SQLException {

        List<Seat> roomSeats
                = getSeatsForShowtime(
                        showtimeId
                );

        Set<Integer> occupiedSeatIds
                = ticketRepository
                        .findOccupiedSeatIds(
                                showtimeId
                        );

        List<Seat> activeSeats
                = new ArrayList<>();


        for (Seat seat : roomSeats) {

            if ("ACTIVE".equals(
                    seat.getStatus()
            )) {

                activeSeats.add(
                        seat
                );
            }
        }


        if (activeSeats.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phòng chiếu chưa có ghế đang hoạt động."
            );
        }


        /*
         * Đặt toàn phòng:
         * Chỉ cho phép nếu toàn bộ ghế ACTIVE
         * chưa có Ticket VALID hoặc USED.
         */
        if (fullRoom) {

            for (Seat seat : activeSeats) {

                if (occupiedSeatIds.contains(
                        seat.getSeatId()
                )) {

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

                int seatId
                        = Integer.parseInt(
                                value
                        );

                if (seatId <= 0) {

                    throw new NumberFormatException();
                }

                selectedIds.add(
                        seatId
                );

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

                if (seat.getSeatId()
                        == selectedId) {

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


            if (occupiedSeatIds.contains(
                    selectedId
            )) {

                throw new IllegalArgumentException(
                        "Có ghế đã được người khác xác nhận. "
                        + "Vui lòng chọn lại."
                );
            }


            selectedSeats.add(
                    matchedSeat
            );
        }


        return selectedSeats;
    }


    public BigDecimal calculateSubtotal(
            int showtimeId,
            List<Seat> selectedSeats
    ) throws SQLException {

        BigDecimal subtotal
                = BigDecimal.ZERO;


        for (Seat seat : selectedSeats) {

            BigDecimal price
                    = bookingRepository
                            .findSeatPrice(
                                    showtimeId,
                                    seat.getSeatId()
                            );


            if (price == null) {

                throw new IllegalArgumentException(
                        "Chưa có giá vé hợp lệ cho ghế đã chọn."
                );
            }


            subtotal
                    = subtotal.add(
                            price
                    );
        }


        return subtotal;
    }


    public int createPendingBooking(
            int customerId,
            int showtimeId,
            String[] seatValues
    ) throws SQLException {

        if (customerId <= 0) {

            throw new IllegalArgumentException(
                    "Tài khoản khách hàng không hợp lệ."
            );
        }


        /*
         * Kiểm tra ghế lại ngay trước lúc ghi DB.
         */
        List<Seat> selectedSeats
                = prepareSelectedSeats(
                        showtimeId,
                        seatValues,
                        false
                );


        /*
         * Lấy giá mới nhất.
         */
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


        BigDecimal subtotal
                = BigDecimal.ZERO;


        for (Seat seat : selectedSeats) {

            subtotal
                    = subtotal.add(
                            selectedPrices.get(
                                    seat.getSeatId()
                            )
                    );
        }


        Booking booking
                = new Booking();


        booking.setCustomerId(
                customerId
        );

        booking.setShowtimeId(
                showtimeId
        );

        /*
         * UC-22 không nằm trong UC-12.
         */
        booking.setPromotionId(
                null
        );

        booking.setSubtotal(
                subtotal
        );

        booking.setDiscountAmount(
                BigDecimal.ZERO
        );

        booking.setTotalAmount(
                subtotal
        );

        booking.setStatus(
                "PENDING"
        );


        /*
         * Booking và Ticket phải tạo cùng transaction.
         * Nếu Ticket lỗi thì rollback luôn Booking.
         */
        try (Connection connection
                = getConnection()) {

            connection.setAutoCommit(
                    false
            );


            try {

                int bookingId
                        = bookingRepository
                                .insert(
                                        connection,
                                        booking
                                );


                for (Seat seat : selectedSeats) {

                    Ticket ticket
                            = new Ticket();


                    ticket.setBookingId(
                            bookingId
                    );

                    ticket.setSeatId(
                            seat.getSeatId()
                    );

                    /*
                     * PENDING Ticket chưa có mã vé.
                     */
                    ticket.setTicketCode(
                            null
                    );

                    ticket.setTicketPrice(
                            selectedPrices.get(
                                    seat.getSeatId()
                            )
                    );

                    ticket.setStatus(
                            "PENDING"
                    );

                    ticket.setCheckedInAt(
                            null
                    );


                    ticketRepository.insert(
                            connection,
                            ticket,
                            showtimeId
                    );
                }


                connection.commit();


                return bookingId;


            } catch (SQLException
                    | RuntimeException e) {

                connection.rollback();

                throw e;


            } finally {

                connection.setAutoCommit(
                        true
                );
            }
        }
    }

    public List<Booking> getBookingsByCustomer(
        int customerId
) throws SQLException {

    if (customerId <= 0) {

        throw new IllegalArgumentException(
                "Khách hàng không hợp lệ."
        );
    }

    return bookingRepository
            .findByCustomerId(
                    customerId
            );
}


public Booking getBookingDetail(
        int bookingId,
        int customerId
) throws SQLException {

    if (bookingId <= 0
            || customerId <= 0) {

        throw new IllegalArgumentException(
                "Đơn đặt vé không hợp lệ."
        );
    }

    Booking booking
            = bookingRepository
                    .findByIdAndCustomerId(
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
        int bookingId
) throws SQLException {

    if (bookingId <= 0) {

        throw new IllegalArgumentException(
                "Đơn đặt vé không hợp lệ."
        );
    }

    return ticketRepository
            .findByBookingId(
                    bookingId
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

    private Connection getConnection()
            throws SQLException {

        Connection connection
                = new DBContext()
                        .getConnection();


        if (connection == null) {

            throw new SQLException(
                    "Cannot connect to FCinemaDB."
            );
        }


        return connection;
    }
}