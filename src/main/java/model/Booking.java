package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Model cho bảng Booking. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Booking {
    private int bookingId; // booking_id
    private int customerId; // customer_id
    private int showtimeId; // showtime_id
    private Integer promotionId; // promotion_id
    private Timestamp bookingTime; // booking_time
    private BigDecimal subtotal; // subtotal
    private BigDecimal discountAmount; // discount_amount
    private BigDecimal totalAmount; // total_amount
    private String status; // status

    public Booking() {
    }

    public Booking(int bookingId, int customerId, int showtimeId, Integer promotionId,
            Timestamp bookingTime, BigDecimal subtotal, BigDecimal discountAmount,
            BigDecimal totalAmount, String status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.showtimeId = showtimeId;
        this.promotionId = promotionId;
        this.bookingTime = bookingTime;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public Timestamp getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
