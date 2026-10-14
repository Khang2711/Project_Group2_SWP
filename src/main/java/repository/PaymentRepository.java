package repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import model.Payment;

/**
 * Repository for VNPay payment records. All methods use the caller's
 * connection so payment, booking and ticket changes can share one transaction.
 */
public class PaymentRepository {

    public Payment findPendingByBookingId(
            Connection connection,
            int bookingId
    ) throws SQLException {
        String sql = "SELECT TOP 1 payment_id, booking_id, payment_method, "
                + "merchant_reference, transaction_code, amount, payment_time, "
                + "status, refund_status "
                + "FROM dbo.Payment WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE booking_id = ? AND status = 'PENDING' "
                + "ORDER BY payment_id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPayment(rs) : null;
            }
        }
    }

    public Payment findByMerchantReference(
            Connection connection,
            String merchantReference,
            boolean forUpdate
    ) throws SQLException {
        String sql = "SELECT payment_id, booking_id, payment_method, "
                + "merchant_reference, transaction_code, amount, payment_time, "
                + "status, refund_status FROM dbo.Payment "
                + (forUpdate ? "WITH (UPDLOCK, HOLDLOCK) " : "")
                + "WHERE merchant_reference = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, merchantReference);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPayment(rs) : null;
            }
        }
    }

    public Payment insertPending(
            Connection connection,
            int bookingId,
            String merchantReference,
            BigDecimal amount
    ) throws SQLException {
        String sql = "INSERT INTO dbo.Payment "
                + "(booking_id, payment_method, merchant_reference, "
                + "transaction_code, amount, payment_time, status) "
                + "VALUES (?, 'VNPAY', ?, NULL, ?, NULL, 'PENDING')";

        try (PreparedStatement ps = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setInt(1, bookingId);
            ps.setString(2, merchantReference);
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt(1));
                    payment.setBookingId(bookingId);
                    payment.setPaymentMethod("VNPAY");
                    payment.setMerchantReference(merchantReference);
                    payment.setAmount(amount);
                    payment.setStatus("PENDING");
                    return payment;
                }
            }
        }

        throw new SQLException("Không thể tạo bản ghi thanh toán VNPay.");
    }

    public boolean hasSuccessfulPayment(
            Connection connection,
            int bookingId,
            int excludedPaymentId
    ) throws SQLException {
        String sql = "SELECT TOP 1 payment_id "
                + "FROM dbo.Payment WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE booking_id = ? AND status = 'SUCCESS' "
                + "AND payment_id <> ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, excludedPaymentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int updateStatus(
            Connection connection,
            int paymentId,
            String status,
            String transactionCode,
            Timestamp paymentTime,
            String refundStatus
    ) throws SQLException {
        String sql = "UPDATE dbo.Payment "
                + "SET status = ?, "
                + "transaction_code = COALESCE(NULLIF(?, ''), transaction_code), "
                + "payment_time = ?, refund_status = ? "
                + "WHERE payment_id = ? AND status IN ('PENDING', 'FAILED')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, transactionCode);
            ps.setTimestamp(3, paymentTime);
            ps.setString(4, refundStatus);
            ps.setInt(5, paymentId);
            return ps.executeUpdate();
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setMerchantReference(rs.getString("merchant_reference"));
        payment.setTransactionCode(rs.getString("transaction_code"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentTime(rs.getTimestamp("payment_time"));
        payment.setStatus(rs.getString("status"));
        payment.setRefundStatus(rs.getString("refund_status"));
        return payment;
    }
}
