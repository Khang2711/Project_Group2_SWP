package service;

import java.sql.SQLException;
import java.util.Locale;
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

    public Ticket lookupTicket(String ticketCode, int staffId)
            throws SQLException {

        String error = validateTicketCode(ticketCode);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        if (staffId <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên không hợp lệ.");
        }

        String normalizedCode
                = ticketCode.trim().toUpperCase(Locale.ROOT);

        Ticket ticket = ticketRepository.findTicketForCheckIn(
                normalizedCode, staffId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy vé hợp lệ tại rạp của bạn. "
                    + "Vé có thể không tồn tại hoặc đã hết hiệu lực.");
        }

        return ticket;
    }

    public void checkInTicket(String ticketCode, int staffId)
            throws SQLException {

        // Tra cứu lại lúc xác nhận để không dùng dữ liệu cũ trên màn hình.
        Ticket ticket = lookupTicket(ticketCode, staffId);

        String error = validateCheckIn(ticket);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        // Repository tự lấy showtime_id từ chính vé trong database.
        boolean updated = ticketRepository.markTicketUsed(
                ticket.getTicketCode(), staffId);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Vé không còn hợp lệ hoặc vừa được check-in. "
                    + "Vui lòng tra cứu lại.");
        }
    }
}
