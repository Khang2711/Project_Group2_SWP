package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Booking;

/**
 * Repository methods used by UC-12.
 */
public class BookingRepository {

    public int insert(
            Connection connection,
            Booking booking
    ) throws SQLException {

        String sql = "INSERT INTO Booking "
                + "(customer_id, showtime_id, promotion_id, "
                + "subtotal, discount_amount, total_amount, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps
                = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            ps.setInt(
                    1,
                    booking.getCustomerId()
            );

            ps.setInt(
                    2,
                    booking.getShowtimeId()
            );

            if (booking.getPromotionId() == null) {

                ps.setNull(
                        3,
                        java.sql.Types.INTEGER
                );

            } else {

                ps.setInt(
                        3,
                        booking.getPromotionId()
                );
            }

            ps.setBigDecimal(
                    4,
                    booking.getSubtotal()
            );

            ps.setBigDecimal(
                    5,
                    booking.getDiscountAmount()
            );

            ps.setBigDecimal(
                    6,
                    booking.getTotalAmount()
            );

            ps.setString(
                    7,
                    booking.getStatus()
            );

            ps.executeUpdate();

            try (ResultSet rs
                    = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Cannot get booking_id after insert."
        );
    }


    public BigDecimal findSeatPrice(
            int showtimeId,
            int seatId
    ) throws SQLException {

        String sql = "SELECT TOP 1 p.price "
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
                + "AND p.start_date "
                + "<= CAST(SYSDATETIME() AS DATE) "
                + "AND (p.end_date IS NULL "
                + "OR p.end_date "
                + ">= CAST(SYSDATETIME() AS DATE)) "
                + "ORDER BY p.start_date DESC, "
                + "p.pricing_id DESC";

        try (
                Connection connection = getConnection();
                PreparedStatement ps
                = connection.prepareStatement(sql)
        ) {

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