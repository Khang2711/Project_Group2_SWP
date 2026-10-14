package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Ticket;

/**
 * Repository methods used by UC-12.
 */
public class TicketRepository {

    public Set<Integer> findOccupiedSeatIds(
            int showtimeId
    ) throws SQLException {

        Set<Integer> seatIds
                = new HashSet<>();

        String sql = "SELECT seat_id "
                + "FROM Ticket "
                + "WHERE showtime_id = ? "
                + "AND status IN ('VALID', 'USED')";

        try (
                Connection connection = getConnection();
                PreparedStatement ps
                = connection.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    showtimeId
            );

            try (ResultSet rs
                    = ps.executeQuery()) {

                while (rs.next()) {

                    seatIds.add(
                            rs.getInt(
                                    "seat_id"
                            )
                    );
                }
            }
        }

        return seatIds;
    }


    public void insert(
            Connection connection,
            Ticket ticket,
            int showtimeId
    ) throws SQLException {

        String sql = "INSERT INTO Ticket "
                + "(booking_id, showtime_id, seat_id, "
                + "ticket_code, ticket_price, status, checked_in_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps
                = connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    ticket.getBookingId()
            );

            ps.setInt(
                    2,
                    showtimeId
            );

            ps.setInt(
                    3,
                    ticket.getSeatId()
            );

            ps.setString(
                    4,
                    ticket.getTicketCode()
            );

            ps.setBigDecimal(
                    5,
                    ticket.getTicketPrice()
            );

            ps.setString(
                    6,
                    ticket.getStatus()
            );

            ps.setTimestamp(
                    7,
                    ticket.getCheckedInAt()
            );

            ps.executeUpdate();
        }
    }

    
    public List<Ticket> findByBookingId(
        int bookingId
) throws SQLException {

    List<Ticket> tickets
            = new ArrayList<>();

    String sql = "SELECT ticket_id, booking_id, seat_id, "
            + "ticket_code, ticket_price, status, checked_in_at "
            + "FROM Ticket "
            + "WHERE booking_id = ? "
            + "ORDER BY ticket_id";

    try (
            Connection connection = getConnection();
            PreparedStatement ps
            = connection.prepareStatement(sql)
    ) {

        ps.setInt(
                1,
                bookingId
        );

        try (ResultSet rs
                = ps.executeQuery()) {

            while (rs.next()) {

                Ticket ticket
                        = new Ticket();

                ticket.setTicketId(
                        rs.getInt("ticket_id")
                );

                ticket.setBookingId(
                        rs.getInt("booking_id")
                );

                ticket.setSeatId(
                        rs.getInt("seat_id")
                );

                ticket.setTicketCode(
                        rs.getString("ticket_code")
                );

                ticket.setTicketPrice(
                        rs.getBigDecimal(
                                "ticket_price"
                        )
                );

                ticket.setStatus(
                        rs.getString("status")
                );

                ticket.setCheckedInAt(
                        rs.getTimestamp(
                                "checked_in_at"
                        )
                );

                tickets.add(ticket);
            }
        }
    }

    return tickets;
}


    public int countPendingTickets(
            Connection connection,
            int bookingId
    ) throws SQLException {
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
            int bookingId
    ) throws SQLException {
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
            int bookingId
    ) throws SQLException {
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
            int bookingId
    ) throws SQLException {
        String sql = "UPDATE dbo.Ticket "
                + "SET status = 'CANCELLED', ticket_code = NULL "
                + "WHERE booking_id = ? AND status = 'PENDING'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
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