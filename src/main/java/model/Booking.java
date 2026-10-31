package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model cho bảng Booking. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ.
 */
public class Booking {

    private int bookingId;
    private Integer customerId; // NULL nếu khách mua trực tiếp tại quầy
    private Integer staffId;
    private int showtimeId;
    private Integer promotionId;
    private Timestamp bookingTime;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;

    public Booking() {
    }

    public Booking(int bookingId, Integer customerId, int showtimeId,
            Integer promotionId, Timestamp bookingTime,
            BigDecimal subtotal, BigDecimal discountAmount,
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

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
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
