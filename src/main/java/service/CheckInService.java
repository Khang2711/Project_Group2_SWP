package service;

import model.Ticket;

public class CheckInService {

    public String validateTicketCode(String ticketCode) {

        if (ticketCode == null || ticketCode.trim().isEmpty()) {
            return "Vui lòng nhập mã vé.";
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

        if ("USED".equalsIgnoreCase(status)) {
            return "Vé đã được check-in trước đó.";
        }

        if (!"VALID".equalsIgnoreCase(status)) {
            return "Trạng thái vé không hợp lệ.";
        }

        return null;
    }
}
