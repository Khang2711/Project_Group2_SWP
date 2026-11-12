package service;

import db.DBContext;
import integration.VnPayService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import model.Booking;
import model.Payment;
import repository.BookingRepository;
import repository.PaymentRepository;
import repository.TicketRepository;

/**
 * UC-13: creates VNPay payment URLs and processes signed callbacks.
 * A booking and its tickets are confirmed only inside the same database
 * transaction that records a verified successful payment.
 */
public class PaymentService {

    private static final DateTimeFormatter VNP_PAY_DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayService vnPayService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    public PaymentService() {
        vnPayService = new VnPayService();
        paymentRepository = new PaymentRepository();
        bookingRepository = new BookingRepository();
        ticketRepository = new TicketRepository();
    }

    public String createPaymentUrl(
            int bookingId,
            int customerId,
            String clientIp
    ) throws SQLException {
        if (!vnPayService.isConfigured()) {
            throw new IllegalStateException(
                    "VNPay chưa được cấu hình. Hãy thiết lập VNPAY_TMN_CODE, "
                    + "VNPAY_HASH_SECRET và VNPAY_RETURN_URL rồi khởi động lại Tomcat."
            );
        }

        Payment payment;

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                Booking booking = bookingRepository.findByIdForPayment(
                        connection,
                        bookingId,
                        customerId
                );

                if (booking == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn đặt vé của tài khoản này."
                    );
                }

                if (!"PENDING".equals(booking.getStatus())) {
                    throw new IllegalArgumentException(
                            "Đơn đặt vé không còn ở trạng thái chờ thanh toán."
                    );
                }

                if (!isBeforeShowtime(connection, bookingId)) {
                    throw new IllegalArgumentException(
                            "Suất chiếu đã bắt đầu. Không thể tiếp tục thanh toán.");
                }
                if (!promotionHasCapacity(connection, booking)) {
                    throw new IllegalArgumentException(
                            "Mã giảm giá đã hết lượt sử dụng. Vui lòng đặt lại vé.");
                }

                BigDecimal amount = booking.getTotalAmount();
                if (amount == null || amount.signum() <= 0) {
                    throw new IllegalArgumentException(
                            "Tổng tiền đơn đặt vé không hợp lệ."
                    );
                }

                if (ticketRepository.countPendingTickets(
                        connection,
                        bookingId
                ) <= 0) {
                    throw new IllegalArgumentException(
                            "Đơn đặt vé không có vé đang chờ thanh toán."
                    );
                }

                if (paymentRepository.hasSuccessfulPayment(
                        connection,
                        bookingId,
                        0
                )) {
                    throw new IllegalArgumentException(
                            "Đơn đặt vé đã có giao dịch thanh toán thành công."
                    );
                }

                // Refreshing the GET payment page must not invalidate a still
                // valid payment attempt. Reuse it for up to 14 minutes.
                Payment existing = paymentRepository.findPendingByBookingId(
                        connection, bookingId);
                if (existing != null && existing.getAmount() != null
                        && existing.getAmount().compareTo(amount) == 0
                        && isFreshPaymentReference(existing.getMerchantReference())) {
                    payment = existing;
                } else {
                    paymentRepository.failPendingPaymentsForBooking(
                            connection, bookingId);
                    payment = paymentRepository.insertPending(
                            connection, bookingId, createMerchantReference(), amount);
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        return vnPayService.createPaymentUrl(
                payment.getMerchantReference(),
                payment.getAmount(),
                clientIp
        );
    }

    /**
     * A completely discounted booking has no VNPay charge to perform.
     * Returns false for paid bookings. Every change is committed atomically.
     */
    public boolean confirmFreeBooking(int bookingId, int customerId)
            throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                Booking booking = bookingRepository.findByIdForPayment(
                        connection, bookingId, customerId);
                if (booking == null) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn đặt vé của tài khoản này.");
                }
                BigDecimal amount = booking.getTotalAmount();
                if (amount == null || amount.signum() < 0) {
                    throw new IllegalArgumentException("Tổng tiền đơn không hợp lệ.");
                }
                if (amount.signum() > 0) {
                    connection.rollback();
                    return false;
                }
                if (!"PENDING".equals(booking.getStatus())) {
                    throw new IllegalArgumentException(
                            "Đơn đặt vé không còn chờ xác nhận.");
                }
                if (!isBeforeShowtime(connection, bookingId)) {
                    throw new IllegalArgumentException(
                            "Suất chiếu đã bắt đầu. Không thể xác nhận vé.");
                }
                if (!promotionHasCapacity(connection, booking)) {
                    throw new IllegalArgumentException(
                            "Mã giảm giá đã hết lượt sử dụng. Vui lòng đặt lại vé.");
                }
                int pending = ticketRepository.countPendingTickets(connection, bookingId);
                if (pending <= 0 || ticketRepository.hasSeatConflict(connection, bookingId)
                        || paymentRepository.hasSuccessfulPayment(connection, bookingId, 0)) {
                    throw new IllegalArgumentException(
                            "Đơn vé không còn khả dụng để xác nhận.");
                }
                if (ticketRepository.confirmPendingTickets(connection, bookingId) != pending
                        || bookingRepository.updateStatus(connection, bookingId, "CONFIRMED") != 1) {
                    throw new SQLException("Could not confirm free booking.");
                }
                paymentRepository.insertFreePayment(connection, bookingId);
                markOnlineFoodPaid(connection, bookingId);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Called by both the browser return URL and VNPay's server-to-server IPN.
     * Repeated callbacks for the same transaction are handled idempotently.
     */
    public PaymentResult processCallback(
            Map<String, String> params
    ) throws SQLException {
        if (!vnPayService.isConfigured()) {
            return result(
                    "99", "Configuration error", false, false,
                    false, false, 0,
                    "Máy chủ chưa được cấu hình VNPay."
            );
        }

        if (!vnPayService.verifySignature(params)) {
            return result(
                    "97", "Invalid signature", false, false,
                    false, false, 0,
                    "Không thể xác minh chữ ký kết quả thanh toán."
            );
        }

        if (!vnPayService.getTmnCode().equals(params.get("vnp_TmnCode"))) {
            return result(
                    "97", "Invalid merchant", true, false,
                    false, false, 0,
                    "Mã merchant trong kết quả thanh toán không hợp lệ."
            );
        }

        String merchantReference = params.get("vnp_TxnRef");
        if (isBlank(merchantReference)) {
            return result(
                    "01", "Order not found", true, false,
                    false, false, 0,
                    "Không tìm thấy mã giao dịch."
            );
        }

        /*
         * VNPay's browser return/IPN response does not guarantee that
         * vnp_Command and vnp_CurrCode are echoed back. Those values are
         * set when creating the payment request; validate the signed response
         * fields here and compare vnp_Amount with the stored DB amount below.
         */
        if (!isNumericAmount(params.get("vnp_Amount"))) {
            return result(
                    "04", "Invalid amount", true, false,
                    false, false, 0,
                    "VNPay không trả về số tiền giao dịch hợp lệ."
            );
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                // First read only to get booking_id. Lock order is Booking then Payment.
                Payment preliminary = paymentRepository.findByMerchantReference(
                        connection,
                        merchantReference,
                        false
                );

                if (preliminary == null) {
                    connection.rollback();
                    return result(
                            "01", "Order not found", true, false,
                            false, false, 0,
                            "Không tìm thấy giao dịch trong hệ thống."
                    );
                }

                Booking booking = bookingRepository.findByIdForUpdate(
                        connection,
                        preliminary.getBookingId()
                );

                Payment payment = paymentRepository.findByMerchantReference(
                        connection,
                        merchantReference,
                        true
                );

                if (booking == null || payment == null) {
                    connection.rollback();
                    return result(
                            "01", "Order not found", true, false,
                            false, false, 0,
                            "Không tìm thấy đơn đặt vé."
                    );
                }

                int bookingId = booking.getBookingId();

                if (!amountMatches(params.get("vnp_Amount"), payment.getAmount())) {
                    connection.rollback();
                    return result(
                            "04", "Invalid amount", true, false,
                            false, false, bookingId,
                            "Số tiền VNPay trả về không khớp với đơn đặt vé."
                    );
                }

                String transactionCode = params.get("vnp_TransactionNo");
                boolean transactionSucceeded
                        = "00".equals(params.get("vnp_ResponseCode"))
                        && "00".equals(params.get("vnp_TransactionStatus"));

                if (transactionSucceeded && isBlank(transactionCode)) {
                    connection.rollback();
                    return result(
                            "04", "Missing transaction number", true, false,
                            false, false, bookingId,
                            "Kết quả thanh toán thiếu mã giao dịch VNPay."
                    );
                }

                if ("SUCCESS".equals(payment.getStatus())) {
                    if (sameValue(payment.getTransactionCode(), transactionCode)) {
                        paymentRepository.failPendingPaymentsForBooking(
                                connection,
                                bookingId
                        );
                        connection.commit();
                        boolean confirmed = "CONFIRMED".equals(booking.getStatus());
                        boolean refundRequired
                                = "REQUIRED".equals(payment.getRefundStatus())
                                || "CANCELLED".equals(booking.getStatus());

                        return result(
                                "00", "Confirm Success", true, true,
                                confirmed, refundRequired, bookingId,
                                refundRequired
                                        ? "Giao dịch đã được ghi nhận; cần xử lý hoàn tiền."
                                        : "Thanh toán đã được xác nhận trước đó."
                        );
                    }

                    connection.rollback();
                    return result(
                            "02", "Order already confirmed", true, false,
                            false, false, bookingId,
                            "Mã đơn này đã được xử lý với một giao dịch khác."
                    );
                }

                if (!transactionSucceeded) {
                    int updated = paymentRepository.updateStatus(
                            connection,
                            payment.getPaymentId(),
                            "FAILED",
                            transactionCode,
                            parsePayDate(params.get("vnp_PayDate")),
                            null
                    );

                    if (updated != 1) {
                        throw new SQLException(
                                "Không thể cập nhật trạng thái thanh toán thất bại."
                        );
                    }

                    connection.commit();

                    return result(
                            "00", "Confirm Success", true, false,
                            false, false, bookingId,
                            "Giao dịch chưa thành công. Booking và vé vẫn ở trạng thái PENDING."
                    );
                }

                boolean anotherSuccessfulPayment
                        = paymentRepository.hasSuccessfulPayment(
                                connection,
                                bookingId,
                                payment.getPaymentId()
                        );

                if (!"PENDING".equals(booking.getStatus())) {
                    int updated = paymentRepository.updateStatus(
                            connection,
                            payment.getPaymentId(),
                            "SUCCESS",
                            transactionCode,
                            paymentTime(params),
                            "REQUIRED"
                    );

                    if (updated != 1) {
                        throw new SQLException(
                                "Không thể ghi nhận giao dịch cần hoàn tiền."
                        );
                    }

                    paymentRepository.failPendingPaymentsForBooking(
                            connection,
                            bookingId
                    );

                    if ("CANCELLED".equals(booking.getStatus())) {
                        ticketRepository.cancelPendingTickets(
                                connection,
                                bookingId
                        );
                    }

                    connection.commit();

                    return result(
                            "00", "Confirm Success", true, true,
                            "CONFIRMED".equals(booking.getStatus()),
                            true, bookingId,
                            "Đã nhận thanh toán nhưng đơn không thể được xác nhận tự động. "
                            + "Cần kiểm tra và hoàn tiền nếu giao dịch bị thu trùng."
                    );
                }

                // The gateway may capture money after the showtime starts or
                // after a promotion becomes fully used. Record the charge and
                // flag it for a manual refund; never issue an unusable ticket.
                if (!isBeforeShowtime(connection, bookingId)
                        || !promotionHasCapacity(connection, booking)) {
                    if (paymentRepository.updateStatus(
                            connection, payment.getPaymentId(),
                            "SUCCESS", transactionCode, paymentTime(params),
                            "REQUIRED") != 1) {
                        throw new SQLException("Could not record refund-required payment.");
                    }
                    paymentRepository.failPendingPaymentsForBooking(
                            connection, bookingId);
                    bookingRepository.updateStatus(connection, bookingId, "CANCELLED");
                    ticketRepository.cancelPendingTickets(connection, bookingId);
                    if (anotherSuccessfulPayment) {
                        paymentRepository.markSuccessfulPaymentsRefundRequired(
                                connection, bookingId);
                    }
                    connection.commit();
                    return result(
                            "00", "Confirm Success", true, true,
                            false, true, bookingId,
                            "Đơn không còn đủ điều kiện xác nhận. "
                            + "Giao dịch đã thu tiền và cần xử lý hoàn tiền.");
                }

                if (anotherSuccessfulPayment) {
                    int pendingTicketCount = ticketRepository.countPendingTickets(
                            connection,
                            bookingId
                    );
                    boolean conflict = pendingTicketCount <= 0
                            || ticketRepository.hasSeatConflict(connection, bookingId);

                    int updated = paymentRepository.updateStatus(
                            connection,
                            payment.getPaymentId(),
                            "SUCCESS",
                            transactionCode,
                            paymentTime(params),
                            "REQUIRED"
                    );

                    if (updated != 1) {
                        throw new SQLException(
                                "Không thể ghi nhận giao dịch thanh toán trùng."
                        );
                    }

                    paymentRepository.failPendingPaymentsForBooking(
                            connection,
                            bookingId
                    );

                    if (conflict) {
                        bookingRepository.updateStatus(
                                connection,
                                bookingId,
                                "CANCELLED"
                        );
                        ticketRepository.cancelPendingTickets(
                                connection,
                                bookingId
                        );
                        paymentRepository.markSuccessfulPaymentsRefundRequired(
                                connection,
                                bookingId
                        );
                    } else {
                        int confirmedTickets = ticketRepository.confirmPendingTickets(
                                connection,
                                bookingId
                        );

                        if (confirmedTickets != pendingTicketCount
                                || bookingRepository.updateStatus(
                                        connection,
                                        bookingId,
                                        "CONFIRMED"
                                ) != 1) {
                            throw new SQLException(
                                    "Không thể khôi phục trạng thái booking đã thanh toán."
                            );
                        }
                    }

                    connection.commit();

                    return result(
                            "00", "Confirm Success", true, true,
                            !conflict, true, bookingId,
                            conflict
                                    ? "Có nhiều giao dịch thành công nhưng ghế không còn khả dụng. "
                                    + "Cần xử lý hoàn tiền cho các giao dịch đã thu."
                                    : "Đơn đã được xác nhận bằng giao dịch thành công trước đó. "
                                    + "Giao dịch này cần được hoàn tiền để tránh thu trùng."
                    );
                }

                int pendingTicketCount = ticketRepository.countPendingTickets(
                        connection,
                        bookingId
                );

                if (pendingTicketCount <= 0
                        || ticketRepository.hasSeatConflict(connection, bookingId)) {
                    int updatedPayment = paymentRepository.updateStatus(
                            connection,
                            payment.getPaymentId(),
                            "SUCCESS",
                            transactionCode,
                            paymentTime(params),
                            "REQUIRED"
                    );

                    if (updatedPayment != 1) {
                        throw new SQLException(
                                "Không thể ghi nhận giao dịch đã thu tiền."
                        );
                    }

                    paymentRepository.failPendingPaymentsForBooking(
                            connection,
                            bookingId
                    );
                    bookingRepository.updateStatus(
                            connection,
                            bookingId,
                            "CANCELLED"
                    );
                    ticketRepository.cancelPendingTickets(
                            connection,
                            bookingId
                    );

                    connection.commit();

                    return result(
                            "00", "Confirm Success", true, true,
                            false, true, bookingId,
                            "VNPay đã báo thanh toán thành công nhưng ghế không còn khả dụng. "
                            + "Đơn chưa được xác nhận và cần xử lý hoàn tiền."
                    );
                }

                int confirmedTickets = ticketRepository.confirmPendingTickets(
                        connection,
                        bookingId
                );

                if (confirmedTickets != pendingTicketCount) {
                    throw new SQLException(
                            "Số vé cập nhật không khớp số vé đang chờ thanh toán."
                    );
                }

                int confirmedBooking = bookingRepository.updateStatus(
                        connection,
                        bookingId,
                        "CONFIRMED"
                );

                if (confirmedBooking != 1) {
                    throw new SQLException(
                            "Không thể xác nhận booking sau khi thanh toán."
                    );
                }

                int confirmedPayment = paymentRepository.updateStatus(
                        connection,
                        payment.getPaymentId(),
                        "SUCCESS",
                        transactionCode,
                        paymentTime(params),
                        null
                );

                if (confirmedPayment != 1) {
                    throw new SQLException(
                            "Không thể lưu trạng thái thanh toán thành công."
                    );
                }

                paymentRepository.failPendingPaymentsForBooking(connection, bookingId);
                markOnlineFoodPaid(connection, bookingId);
                connection.commit();
                return result(
                        "00", "Confirm Success", true, true,
                        true, false, bookingId,
                        "Thanh toán thành công. Booking đã CONFIRMED và vé đã VALID."
                );

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** SQL datetime values in this project are Vietnam-local showtimes. */
    private boolean isBeforeShowtime(Connection connection, int bookingId)
            throws SQLException {
        String sql = "SELECT st.start_time FROM dbo.Booking b "
                + "JOIN dbo.Showtime st ON st.showtime_id = b.showtime_id "
                + "WHERE b.booking_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookingId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getTimestamp(1) != null
                        && rs.getTimestamp(1).toLocalDateTime().isAfter(
                                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            }
        }
    }

    /**
     * Serialize confirmations sharing the same promotion by locking its row.
     * Usage is counted only after that lock is acquired in this transaction.
     */
    private boolean promotionHasCapacity(Connection connection, Booking booking)
            throws SQLException {
        if (booking.getPromotionId() == null) {
            return true;
        }
        String sql = "SELECT usage_limit, status FROM dbo.Promotion "
                + "WITH (UPDLOCK, HOLDLOCK) WHERE promotion_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getPromotionId());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next() || !"ACTIVE".equalsIgnoreCase(rs.getString("status"))) {
                    return false;
                }
                int limit = rs.getInt("usage_limit");
                if (rs.wasNull()) {
                    return true;
                }
                try (PreparedStatement count = connection.prepareStatement(
                        "SELECT COUNT(*) FROM dbo.Booking "
                        + "WHERE promotion_id = ? AND status = 'CONFIRMED'")) {
                    count.setInt(1, booking.getPromotionId());
                    try (ResultSet usage = count.executeQuery()) {
                        return usage.next() && usage.getInt(1) < limit;
                    }
                }
            }
        }
    }

    private boolean isFreshPaymentReference(String reference) {
        if (reference == null || !reference.matches("FC[0-9]{13}[A-Z0-9]{8}")) {
            return false;
        }
        try {
            long age = System.currentTimeMillis()
                    - Long.parseLong(reference.substring(2, 15));
            return age >= 0 && age < 14 * 60 * 1000L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void markOnlineFoodPaid(Connection connection, int bookingId)
            throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE dbo.Food_Order SET payment_status = 'PAID' "
                + "WHERE booking_id = ? AND payment_status = 'PENDING' "
                + "AND staff_id IS NULL")) {
            update.setInt(1, bookingId);
            update.executeUpdate();
        }
    }

    private boolean amountMatches(String returnedAmount, BigDecimal expectedAmount) {
        if (!isNumericAmount(returnedAmount) || expectedAmount == null) {
            return false;
        }

        try {
            BigInteger returned = new BigInteger(returnedAmount);
            BigInteger expected = expectedAmount
                    .movePointRight(2)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .toBigIntegerExact();
            return expected.equals(returned);
        } catch (ArithmeticException | NumberFormatException e) {
            return false;
        }
    }

    private String createMerchantReference() {
        return "FC"
                + System.currentTimeMillis()
                + UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }

    private Timestamp paymentTime(Map<String, String> params) {
        Timestamp payDate = parsePayDate(params.get("vnp_PayDate"));
        return payDate == null
                ? new Timestamp(System.currentTimeMillis())
                : payDate;
    }

    private Timestamp parsePayDate(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Timestamp.valueOf(
                    LocalDateTime.parse(value, VNP_PAY_DATE_FORMAT)
            );
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isNumericAmount(String value) {
        return value != null && value.matches("[0-9]{1,12}");
    }

    private boolean sameValue(String first, String second) {
        return first != null && second != null && first.equals(second);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private PaymentResult result(
            String ipnCode,
            String ipnMessage,
            boolean signatureValid,
            boolean paymentSuccessful,
            boolean bookingConfirmed,
            boolean refundRequired,
            int bookingId,
            String message
    ) {
        return new PaymentResult(
                ipnCode,
                ipnMessage,
                signatureValid,
                paymentSuccessful,
                bookingConfirmed,
                refundRequired,
                bookingId,
                message
        );
    }

    private Connection getConnection() throws SQLException {
        Connection connection = new DBContext().getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối FCinemaDB.");
        }
        return connection;
    }

    public static class PaymentResult {

        private final String ipnResponseCode;
        private final String ipnMessage;
        private final boolean signatureValid;
        private final boolean paymentSuccessful;
        private final boolean bookingConfirmed;
        private final boolean refundRequired;
        private final int bookingId;
        private final String message;

        public PaymentResult(
                String ipnResponseCode,
                String ipnMessage,
                boolean signatureValid,
                boolean paymentSuccessful,
                boolean bookingConfirmed,
                boolean refundRequired,
                int bookingId,
                String message
        ) {
            this.ipnResponseCode = ipnResponseCode;
            this.ipnMessage = ipnMessage;
            this.signatureValid = signatureValid;
            this.paymentSuccessful = paymentSuccessful;
            this.bookingConfirmed = bookingConfirmed;
            this.refundRequired = refundRequired;
            this.bookingId = bookingId;
            this.message = message;
        }

        public String getIpnResponseCode() {
            return ipnResponseCode;
        }

        public String getIpnMessage() {
            return ipnMessage;
        }

        public boolean isSignatureValid() {
            return signatureValid;
        }

        public boolean isPaymentSuccessful() {
            return paymentSuccessful;
        }

        public boolean isBookingConfirmed() {
            return bookingConfirmed;
        }

        public boolean isRefundRequired() {
            return refundRequired;
        }

        public int getBookingId() {
            return bookingId;
        }

        public String getMessage() {
            return message;
        }
    }
}
