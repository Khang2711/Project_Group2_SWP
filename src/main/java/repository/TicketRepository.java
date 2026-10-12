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