package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Model cho bảng Payment. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Payment {
    private int paymentId; // payment_id
    private int bookingId; // booking_id
    private String paymentMethod; // payment_method
    private String transactionCode; // transaction_code
    private BigDecimal amount; // amount
    private Timestamp paymentTime; // payment_time
    private String status; // status

    public Payment() {
    }

    public Payment(int paymentId, int bookingId, String paymentMethod, String transactionCode, BigDecimal amount, Timestamp paymentTime, String status) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.transactionCode = transactionCode;
        this.amount = amount;
        this.paymentTime = paymentTime;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Timestamp paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
