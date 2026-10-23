package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Showtime;
import model.Ticket;

public class TicketRepository {

    public Set<Integer> findOccupiedSeatIds(int showtimeId)
            throws SQLException {

        Set<Integer> seatIds = new HashSet<>();

        String sql = "SELECT seat_id FROM Ticket "
                + "WHERE showtime_id = ? "
                + "AND status IN ('VALID', 'USED')";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seatIds.add(rs.getInt("seat_id"));
                }
            }
        }

        return seatIds;
    }

    public void insert(
            Connection connection,
            Ticket ticket,
            int showtimeId) throws SQLException {

        String sql = "INSERT INTO Ticket "
                + "(booking_id, showtime_id, seat_id, "
                + "ticket_code, ticket_price, status, checked_in_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ticket.getBookingId());
            ps.setInt(2, showtimeId);
            ps.setInt(3, ticket.getSeatId());
            ps.setString(4, ticket.getTicketCode());
            ps.setBigDecimal(5, ticket.getTicketPrice());
            ps.setString(6, ticket.getStatus());
            ps.setTimestamp(7, ticket.getCheckedInAt());

            ps.executeUpdate();
        }
    }

    public List<Ticket> findByBookingId(int bookingId)
            throws SQLException {

        List<Ticket> tickets = new ArrayList<>();

        String sql = "SELECT ticket_id, booking_id, seat_id, "
                + "ticket_code, ticket_price, status, checked_in_at "
                + "FROM Ticket WHERE booking_id = ? "
                + "ORDER BY ticket_id";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket ticket = new Ticket();

                    ticket.setTicketId(rs.getInt("ticket_id"));
                    ticket.setBookingId(rs.getInt("booking_id"));
                    ticket.setSeatId(rs.getInt("seat_id"));
                    ticket.setTicketCode(rs.getString("ticket_code"));
                    ticket.setTicketPrice(rs.getBigDecimal("ticket_price"));
                    ticket.setStatus(rs.getString("status"));
                    ticket.setCheckedInAt(rs.getTimestamp("checked_in_at"));

                    tickets.add(ticket);
                }
            }
        }

        return tickets;
    }

    // UC-25: bán vé tại quầy.
    public int findCounterStaffCinema(
            Connection connection,
            int staffId) throws SQLException {

        String sql = "SELECT st.cinema_id FROM Staff st "
                + "JOIN [User] u ON u.user_id = st.staff_id "
                + "WHERE st.staff_id = ? AND u.status = 'ACTIVE'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, staffId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cinema_id");
                }
            }
        }

        return 0;
    }

    public int findCounterCustomerId(
            Connection connection,
            String email) throws SQLException {

        String sql = "SELECT c.customer_id FROM Customer c "
                + "JOIN [User] u ON u.user_id = c.customer_id "
                + "WHERE u.email = ? AND u.status = 'ACTIVE'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        }

        return 0;
    }

    public Showtime findCounterShowtime(
            Connection connection,
            int showtimeId,
            int cinemaId) throws SQLException {

        // Giữ khóa đến khi lưu hoặc hủy đơn tại quầy.
        String sql = "SELECT st.showtime_id, st.room_id FROM Showtime st "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN Room r ON r.room_id = st.room_id "
                + "JOIN Cinema c ON c.cinema_id = r.cinema_id "
                + "JOIN Movie m ON m.movie_id = st.movie_id "
                + "WHERE st.showtime_id = ? AND r.cinema_id = ? "
                + "AND st.status = 'ACTIVE' "
                + "AND st.start_time > SYSDATETIME() "
                + "AND r.status = 'ACTIVE' AND c.status = 'ACTIVE' "
                + "AND m.status = 'ACTIVE' "
                + "AND m.release_date <= CAST(SYSDATETIME() AS DATE) "
                + "AND (m.end_date IS NULL "
                + "OR m.end_date >= CAST(SYSDATETIME() AS DATE))";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setInt(2, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Showtime showtime = new Showtime();

                    showtime.setShowtimeId(rs.getInt("showtime_id"));
                    showtime.setRoomId(rs.getInt("room_id"));

                    return showtime;
                }
            }
        }

        return null;
    }

    public BigDecimal findCounterSeatPrice(
            Connection connection,
            int seatId,
            int roomId,
            int cinemaId) throws SQLException {

        String sql = "SELECT TOP 1 p.price FROM Seat s "
                + "JOIN Pricing p ON p.seat_type = s.seat_type "
                + "WHERE s.seat_id = ? AND s.room_id = ? "
                + "AND s.status = 'ACTIVE' AND p.cinema_id = ? "
                + "AND p.status = 'ACTIVE' "
                + "AND p.start_date <= CAST(SYSDATETIME() AS DATE) "
                + "AND (p.end_date IS NULL "
                + "OR p.end_date >= CAST(SYSDATETIME() AS DATE)) "
                + "ORDER BY p.start_date DESC, p.pricing_id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, roomId);
            ps.setInt(3, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }
        }

        return null;
    }

    public boolean isCounterSeatSold(
            Connection connection,
            int showtimeId,
            int seatId) throws SQLException {

        String sql = "SELECT TOP 1 ticket_id FROM Ticket "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE showtime_id = ? AND seat_id = ? "
                + "AND status IN ('VALID', 'USED')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setInt(2, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // UC-16: tra cứu và check-in vé.
    public List<Showtime> findCheckInShowtimes(int staffId)
            throws SQLException {

        List<Showtime> showtimes = new ArrayList<>();

        String sql = "SELECT st.showtime_id, st.movie_id, st.room_id, "
                + "st.start_time, st.end_time, st.status FROM Showtime st "
                + "JOIN Room r ON r.room_id = st.room_id "
                + "JOIN Cinema c ON c.cinema_id = r.cinema_id "
                + "JOIN Staff sf ON sf.cinema_id = r.cinema_id "
                + "JOIN [User] u ON u.user_id = sf.staff_id "
                + "WHERE sf.staff_id = ? AND u.status = 'ACTIVE' "
                + "AND st.status = 'ACTIVE' AND r.status = 'ACTIVE' "
                + "AND c.status = 'ACTIVE' "
                + "AND st.end_time > SYSDATETIME() "
                + "ORDER BY st.start_time, st.showtime_id";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, staffId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime showtime = new Showtime();

                    showtime.setShowtimeId(rs.getInt("showtime_id"));
                    showtime.setMovieId(rs.getInt("movie_id"));
                    showtime.setRoomId(rs.getInt("room_id"));
                    showtime.setStartTime(rs.getTimestamp("start_time"));
                    showtime.setEndTime(rs.getTimestamp("end_time"));
                    showtime.setStatus(rs.getString("status"));

                    showtimes.add(showtime);
                }
            }
        }

        return showtimes;
    }

    public Ticket findTicketForCheckIn(
            String ticketCode,
            int showtimeId,
            int staffId) throws SQLException {

        String sql = "SELECT t.ticket_id, t.booking_id, t.seat_id, "
                + "t.ticket_code, t.ticket_price, t.status, t.checked_in_at "
                + "FROM Ticket t "
                + "JOIN Booking b ON b.booking_id = t.booking_id "
                + "AND b.showtime_id = t.showtime_id "
                + "JOIN Showtime st ON st.showtime_id = t.showtime_id "
                + "JOIN Room r ON r.room_id = st.room_id "
                + "JOIN Seat s ON s.seat_id = t.seat_id "
                + "AND s.room_id = r.room_id "
                + "JOIN Cinema c ON c.cinema_id = r.cinema_id "
                + "JOIN Staff sf ON sf.cinema_id = r.cinema_id "
                + "JOIN [User] u ON u.user_id = sf.staff_id "
                + "WHERE t.ticket_code = ? AND st.showtime_id = ? "
                + "AND sf.staff_id = ? AND u.status = 'ACTIVE' "
                + "AND b.status = 'CONFIRMED' AND st.status = 'ACTIVE' "
                + "AND r.status = 'ACTIVE' AND s.status = 'ACTIVE' "
                + "AND c.status = 'ACTIVE' "
                + "AND st.end_time > SYSDATETIME()";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticketCode);
            ps.setInt(2, showtimeId);
            ps.setInt(3, staffId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ticket ticket = new Ticket();

                    ticket.setTicketId(rs.getInt("ticket_id"));
                    ticket.setBookingId(rs.getInt("booking_id"));
                    ticket.setSeatId(rs.getInt("seat_id"));
                    ticket.setTicketCode(rs.getString("ticket_code"));
                    ticket.setTicketPrice(rs.getBigDecimal("ticket_price"));
                    ticket.setStatus(rs.getString("status"));
                    ticket.setCheckedInAt(rs.getTimestamp("checked_in_at"));

                    return ticket;
                }
            }
        }

        return null;
    }

    public boolean markTicketUsed(
            String ticketCode,
            int showtimeId,
            int staffId) throws SQLException {

        // Kiểm tra ngay trong UPDATE để hai lần quét cùng lúc
        // chỉ có một lần đổi vé từ VALID sang USED.
        String sql = "UPDATE t SET status = 'USED', "
                + "checked_in_at = SYSDATETIME() FROM Ticket t "
                + "JOIN Booking b ON b.booking_id = t.booking_id "
                + "AND b.showtime_id = t.showtime_id "
                + "JOIN Showtime st ON st.showtime_id = t.showtime_id "
                + "JOIN Room r ON r.room_id = st.room_id "
                + "JOIN Seat s ON s.seat_id = t.seat_id "
                + "AND s.room_id = r.room_id "
                + "JOIN Cinema c ON c.cinema_id = r.cinema_id "
                + "JOIN Staff sf ON sf.cinema_id = r.cinema_id "
                + "JOIN [User] u ON u.user_id = sf.staff_id "
                + "WHERE t.ticket_code = ? AND st.showtime_id = ? "
                + "AND sf.staff_id = ? AND u.status = 'ACTIVE' "
                + "AND t.status = 'VALID' AND t.checked_in_at IS NULL "
                + "AND b.status = 'CONFIRMED' AND st.status = 'ACTIVE' "
                + "AND r.status = 'ACTIVE' AND s.status = 'ACTIVE' "
                + "AND c.status = 'ACTIVE' "
                + "AND st.end_time > SYSDATETIME()";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticketCode);
            ps.setInt(2, showtimeId);
            ps.setInt(3, staffId);

            return ps.executeUpdate() == 1;
        }
    }

    private Connection getConnection() throws SQLException {

        Connection connection = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException("Cannot connect to FCinemaDB.");
        }

        return connection;
    }

    // Các hàm từ main dùng khi xử lý thanh toán VNPay.
    public int countPendingTickets(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = "SELECT COUNT(*) AS ticket_count "
                + "FROM dbo.Ticket WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ticket_count") : 0;
            }
        }
    }

    public boolean hasSeatConflict(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = "SELECT TOP 1 candidate.ticket_id "
                + "FROM dbo.Ticket candidate WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN dbo.Ticket occupied WITH (UPDLOCK, HOLDLOCK) "
                + "ON occupied.showtime_id = candidate.showtime_id "
                + "AND occupied.seat_id = candidate.seat_id "
                + "AND occupied.ticket_id <> candidate.ticket_id "
                + "WHERE candidate.booking_id = ? "
                + "AND candidate.status = 'PENDING' "
                + "AND occupied.status IN ('VALID', 'USED')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int confirmPendingTickets(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = "UPDATE dbo.Ticket "
                + "SET status = 'VALID', "
                + "ticket_code = 'FC-' + REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', '') "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
    }

    public int cancelPendingTickets(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = "UPDATE dbo.Ticket "
                + "SET status = 'CANCELLED', ticket_code = NULL "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
    }
}
