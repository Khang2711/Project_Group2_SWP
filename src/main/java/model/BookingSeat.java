package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Model cho bảng Booking_Seat. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class BookingSeat {
    private int bookingId; // booking_id
    private int seatId; // seat_id
    private BigDecimal price; // price
    private String status; // status
    private Timestamp reservedAt; // reserved_at

    public BookingSeat() {
    }

    public BookingSeat(int bookingId, int seatId, BigDecimal price, String status, Timestamp reservedAt) {
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.price = price;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Timestamp reservedAt) {
        this.reservedAt = reservedAt;
    }
}
