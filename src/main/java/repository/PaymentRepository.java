package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentRepository {

    // UC-25: ghi nhận tiền mặt đã được Staff xác nhận thu tại quầy.
    public void insertCounterCashPayment(Connection connection,
            int bookingId, BigDecimal amount) throws SQLException {

        String sql = "INSERT INTO Payment "
                + "(booking_id, payment_method, merchant_reference, "
                + "amount, payment_time, status) "
                + "VALUES (?, 'CASH', ?, ?, SYSDATETIME(), 'SUCCESS')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setString(2, "COUNTER-" + bookingId);
            ps.setBigDecimal(3, amount);

            ps.executeUpdate();
        }
    }
}
