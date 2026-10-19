package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Cinema;

public class BookingRepository {

    public int insert(Connection connection, Booking booking)
            throws SQLException {

        String sql = "INSERT INTO Booking "
                + "(customer_id, showtime_id, promotion_id, "
                + "subtotal, discount_amount, total_amount, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getShowtimeId());

            if (booking.getPromotionId() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, booking.getPromotionId());
            }

            ps.setBigDecimal(4, booking.getSubtotal());
            ps.setBigDecimal(5, booking.getDiscountAmount());
            ps.setBigDecimal(6, booking.getTotalAmount());
            ps.setString(7, booking.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Cannot get booking_id after insert.");
    }

    public BigDecimal findSeatPrice(
            int showtimeId,
            int seatId)
            throws SQLException {

        String sql
                = "SELECT TOP 1 p.price "
                + "FROM Showtime st "
                + "JOIN Room r "
                + "ON st.room_id = r.room_id "
                + "JOIN Seat s "
                + "ON s.room_id = r.room_id "
                + "JOIN Pricing p "
                + "ON p.cinema_id = r.cinema_id "
                + "AND p.seat_type = s.seat_type "
                + "WHERE st.showtime_id = ? "
                + "AND s.seat_id = ? "
                + "AND s.status = 'ACTIVE' "
                + "AND p.status = 'ACTIVE' "
                // Pricing phải áp dụng vào ngày SHOWTIME,
                // không phải ngày khách đang booking.
                + "AND p.start_date "
                + "<= CAST(st.start_time AS DATE) "
                + "AND (p.end_date IS NULL "
                + "OR p.end_date "
                + ">= CAST(st.start_time AS DATE)) "
                // NULL movie_id = tất cả phim.
                + "AND (p.movie_id IS NULL "
                + "OR p.movie_id = st.movie_id) "
                // NULL room_type = tất cả phòng.
                + "AND (p.room_type IS NULL "
                + "OR p.room_type = r.room_type) "
                // Ngày đặc biệt hoặc weekday/weekend.
                + "AND ("
                + "p.special_date "
                + "= CAST(st.start_time AS DATE) "
                + "OR ("
                + "p.special_date IS NULL "
                + "AND ("
                + "p.day_type = 'ALL' "
                + "OR ("
                + "p.day_type = 'WEEKDAY' "
                + "AND ("
                + "DATEDIFF("
                + "DAY, "
                + "'19000101', "
                + "CAST(st.start_time AS DATE)"
                + ") % 7"
                + ") BETWEEN 0 AND 4"
                + ") "
                + "OR ("
                + "p.day_type = 'WEEKEND' "
                + "AND ("
                + "DATEDIFF("
                + "DAY, "
                + "'19000101', "
                + "CAST(st.start_time AS DATE)"
                + ") % 7"
                + ") IN (5, 6)"
                + ")"
                + ")"
                + ")"
                + ") "
                // NULL time = cả ngày.
                + "AND (p.start_time IS NULL "
                + "OR CAST(st.start_time AS TIME) "
                + ">= p.start_time) "
                + "AND (p.end_time IS NULL "
                + "OR CAST(st.start_time AS TIME) "
                + "< p.end_time) "
                // Rule càng cụ thể thì càng ưu tiên.
                + "ORDER BY "
                + "("
                + "CASE WHEN p.special_date IS NOT NULL "
                + "THEN 100 ELSE 0 END "
                + "+ CASE WHEN p.movie_id IS NOT NULL "
                + "THEN 20 ELSE 0 END "
                + "+ CASE WHEN p.room_type IS NOT NULL "
                + "THEN 10 ELSE 0 END "
                + "+ CASE WHEN p.day_type <> 'ALL' "
                + "THEN 5 ELSE 0 END "
                + "+ CASE WHEN p.start_time IS NOT NULL "
                + "THEN 2 ELSE 0 END"
                + ") DESC, "
                + "p.start_date DESC, "
                + "p.pricing_id DESC";

        try (Connection connection
                = getConnection(); PreparedStatement ps
                = connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    showtimeId
            );

            ps.setInt(
                    2,
                    seatId
            );

            try (ResultSet rs
                    = ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getBigDecimal(
                            "price"
                    );
                }
            }
        }

        return null;
    }

    public List<Booking> findByCustomerId(int customerId)
            throws SQLException {

        List<Booking> bookings = new ArrayList<>();

        String sql = "SELECT booking_id, customer_id, showtime_id, "
                + "promotion_id, booking_time, subtotal, "
                + "discount_amount, total_amount, status "
                + "FROM Booking WHERE customer_id = ? "
                + "ORDER BY booking_time DESC";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapBooking(rs));
                }
            }
        }

        return bookings;
    }

    public Booking findByIdAndCustomerId(int bookingId, int customerId)
            throws SQLException {

        String sql = "SELECT booking_id, customer_id, showtime_id, "
                + "promotion_id, booking_time, subtotal, "
                + "discount_amount, total_amount, status "
                + "FROM Booking "
                + "WHERE booking_id = ? AND customer_id = ?";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        }

        return null;
    }

    // UC-17: đọc rạp được phân công từ DB.
    // Không lấy cinemaId do người dùng tự truyền trên URL.
    public Cinema findDashboardCinema(int managerId)
            throws SQLException {

        String sql = "SELECT c.cinema_id, c.cinema_name, "
                + "c.address, c.phone, c.status "
                + "FROM dbo.[User] u "
                + "JOIN dbo.[Role] ro ON u.role_id = ro.role_id "
                + "JOIN dbo.Staff s ON u.user_id = s.staff_id "
                + "JOIN dbo.Cinema c ON s.cinema_id = c.cinema_id "
                + "WHERE u.user_id = ? "
                + "AND u.status = 'ACTIVE' "
                + "AND ro.status = 'ACTIVE' "
                + "AND ro.role_name = 'CINEMA_MANAGER'";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, managerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cinema(
                            rs.getInt("cinema_id"),
                            rs.getString("cinema_name"),
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("status"));
                }
            }
        }

        return null;
    }

    public List<Map<String, Object>> getDashboardDailyData(
            int cinemaId,
            LocalDate fromDate,
            LocalDate toDate) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        // Đếm vé theo từng booking trước để tránh cộng lặp tiền đơn.
        String sql = "SELECT CAST(b.booking_time AS DATE) AS booking_date, "
                + "COUNT(*) AS booking_count, "
                + "COALESCE(SUM(b.total_amount), 0) AS revenue, "
                + "COALESCE(SUM(t.ticket_count), 0) AS ticket_count "
                + "FROM dbo.Booking b "
                + "JOIN dbo.Showtime st ON b.showtime_id = st.showtime_id "
                + "JOIN dbo.Room r ON st.room_id = r.room_id "
                + "LEFT JOIN ("
                + "SELECT booking_id, COUNT(*) AS ticket_count "
                + "FROM dbo.Ticket "
                + "WHERE status IN ('VALID', 'USED') "
                + "GROUP BY booking_id"
                + ") t ON b.booking_id = t.booking_id "
                + "WHERE r.cinema_id = ? "
                + "AND b.status = 'CONFIRMED' "
                + "AND CAST(b.booking_time AS DATE) BETWEEN ? AND ? "
                + "GROUP BY CAST(b.booking_time AS DATE) "
                + "ORDER BY booking_date";

        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, cinemaId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("bookingDate", rs.getDate("booking_date"));
                    row.put("bookingCount", rs.getInt("booking_count"));
                    row.put("ticketCount", rs.getInt("ticket_count"));
                    row.put("revenue", rs.getBigDecimal("revenue"));

                    rows.add(row);
                }
            }
        }

        return rows;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {

        Booking booking = new Booking();

        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setShowtimeId(rs.getInt("showtime_id"));

        int promotionId = rs.getInt("promotion_id");

        if (rs.wasNull()) {
            booking.setPromotionId(null);
        } else {
            booking.setPromotionId(promotionId);
        }

        booking.setBookingTime(rs.getTimestamp("booking_time"));
        booking.setSubtotal(rs.getBigDecimal("subtotal"));
        booking.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setStatus(rs.getString("status"));

        return booking;
    }

    private Connection getConnection() throws SQLException {

        Connection connection = new DBContext().getConnection();

        if (connection == null) {
            throw new SQLException("Cannot connect to FCinemaDB.");
        }

        return connection;
    }

    // Giữ các hàm từ main để service thanh toán VNPay tiếp tục hoạt động.
    public Booking findByIdForPayment(
            Connection connection,
            int bookingId,
            int customerId) throws SQLException {

        String sql = "SELECT booking_id, customer_id, showtime_id, "
                + "promotion_id, booking_time, subtotal, discount_amount, "
                + "total_amount, status FROM dbo.Booking WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE booking_id = ? AND customer_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBooking(rs) : null;
            }
        }
    }

    public Booking findByIdForUpdate(
            Connection connection,
            int bookingId) throws SQLException {

        String sql = "SELECT booking_id, customer_id, showtime_id, "
                + "promotion_id, booking_time, subtotal, discount_amount, "
                + "total_amount, status FROM dbo.Booking WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE booking_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBooking(rs) : null;
            }
        }
    }

    public int updateStatus(
            Connection connection,
            int bookingId,
            String status) throws SQLException {

        String sql = "UPDATE dbo.Booking SET status = ? "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);

            return ps.executeUpdate();
        }
    }
}
