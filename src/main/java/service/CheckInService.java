package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import model.Showtime;
import model.Ticket;
import repository.TicketRepository;

public class CheckInService {

    private final TicketRepository ticketRepository
            = new TicketRepository();

    public String validateTicketCode(String ticketCode) {
        if (ticketCode == null || ticketCode.trim().isEmpty()) {
            return "Vui lòng nhập mã vé.";
        }

        if (ticketCode.trim().length() > 100) {
            return "Mã vé không hợp lệ.";
        }

        return null;
    }

    public String validateCheckIn(Ticket ticket) {
        if (ticket == null) {
            return "Không tìm thấy vé.";
        }

        String status = ticket.getStatus();

        if (status == null) {
            return "Trạng thái vé không hợp lệ.";
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            return "Vé chưa được xác nhận.";
        }

        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "Vé đã bị hủy.";
        }

        if ("USED".equalsIgnoreCase(status)
                || ticket.getCheckedInAt() != null) {

            return "Vé đã được check-in trước đó.";
        }

        if (!"VALID".equalsIgnoreCase(status)) {
            return "Trạng thái vé không hợp lệ.";
        }

        return null;
    }

    public List<Showtime> getCheckInShowtimes(int staffId)
            throws SQLException {

        if (staffId <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên không hợp lệ.");
        }

        return ticketRepository.findCheckInShowtimes(staffId);
    }

    public Ticket lookupTicket(String ticketCode,
            int showtimeId, int staffId) throws SQLException {

        String error = validateTicketCode(ticketCode);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        if (staffId <= 0 || showtimeId <= 0) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn suất chiếu cần check-in.");
        }

        String normalizedCode
                = ticketCode.trim().toUpperCase(Locale.ROOT);

        Ticket ticket = ticketRepository.findTicketForCheckIn(
                normalizedCode, showtimeId, staffId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy vé hợp lệ cho rạp và suất đã chọn. "
                    + "Vé có thể đã hết hiệu lực.");
        }

        return ticket;
    }

    public void checkInTicket(String ticketCode,
            int showtimeId, int staffId) throws SQLException {

        // Tra cứu lại khi xác nhận, không tin kết quả cũ trên màn hình.
        Ticket ticket = lookupTicket(
                ticketCode, showtimeId, staffId);

        String error = validateCheckIn(ticket);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        boolean updated = ticketRepository.markTicketUsed(
                ticket.getTicketCode(), showtimeId, staffId);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Vé không còn hợp lệ hoặc vừa được check-in. "
                    + "Vui lòng tra cứu lại.");
        }
    }
}
