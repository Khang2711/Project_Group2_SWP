package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Model cho bảng Ticket. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Ticket {
    private int ticketId; // ticket_id
    private int bookingId; // booking_id
    private int showtimeId; // showtime_id, dùng khi tra cứu vé check-in
    private int seatId; // seat_id
    private String ticketCode; // ticket_code
    private BigDecimal ticketPrice; // ticket_price
    private String status; // status
    private Timestamp checkedInAt; // checked_in_at

    public Ticket() {
    }

    public Ticket(int ticketId, int bookingId, int seatId, String ticketCode, BigDecimal ticketPrice, String status, Timestamp checkedInAt) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.ticketCode = ticketCode;
        this.ticketPrice = ticketPrice;
        this.status = status;
        this.checkedInAt = checkedInAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Timestamp checkedInAt) {
        this.checkedInAt = checkedInAt;
    }
}
