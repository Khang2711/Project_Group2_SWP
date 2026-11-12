package service;

import java.sql.SQLException;
import java.util.Locale;
import model.Ticket;
import repository.TicketRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import model.Showtime;
import repository.ShowtimeRepository;

public class CheckInService {

    private final ShowtimeRepository showtimeRepository
            = new ShowtimeRepository();

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

    public String validateCheckIn(Ticket ticket, Showtime showtime) {
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

        if (showtime == null
                || showtime.getStartTime() == null
                || showtime.getEndTime() == null) {
            return "Không tìm thấy thời gian suất chiếu.";
        }

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime checkInStart = showtime.getStartTime()
                .toLocalDateTime()
                .minusHours(1);

        LocalDateTime showtimeEnd = showtime.getEndTime()
                .toLocalDateTime();

        if (now.isBefore(checkInStart)) {
            return "Chưa đến thời gian check-in. "
                    + "Chỉ được check-in trước giờ chiếu 1 tiếng.";
        }

        if (!now.isBefore(showtimeEnd)) {
            return "Suất chiếu đã kết thúc. Không thể check-in vé.";
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

        Ticket ticket = lookupTicket(ticketCode, staffId);

        Showtime showtime = showtimeRepository.getById(
                ticket.getShowtimeId());

        String error = validateCheckIn(ticket, showtime);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        boolean updated = ticketRepository.markTicketUsed(
                ticket.getTicketCode(), staffId);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Vé không còn hợp lệ hoặc vừa được check-in. "
                    + "Vui lòng tra cứu lại.");
        }
    }
}
