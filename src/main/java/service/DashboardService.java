package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import repository.BookingRepository;

public class DashboardService {

    private final BookingRepository bookingRepository
            = new BookingRepository();

    public Cinema getManagerCinema(int managerId) throws SQLException {

        if (managerId <= 0) {
            return null;
        }

        return bookingRepository.findDashboardCinema(managerId);
    }

    public LocalDate parseDate(String value, String fieldName) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn " + fieldName.toLowerCase() + ".");
        }

        try {
            LocalDate date = LocalDate.parse(value.trim());

            if (date.getYear() < 1 || date.getYear() > 9999) {
                throw new IllegalArgumentException(
                        fieldName + " không hợp lệ.");
            }

            return date;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " không hợp lệ.");
        }
    }

    public Map<String, Object> getStatistics(
            int cinemaId, LocalDate fromDate, LocalDate toDate)
            throws SQLException {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Rạp không hợp lệ.");
        }

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn đủ ngày bắt đầu và ngày kết thúc.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc.");
        }

        List<Map<String, Object>> rows
                = bookingRepository.getDashboardDailyData(
                        cinemaId, fromDate, toDate);

        int totalBookings = 0;
        int totalTickets = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal maxRevenue = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {

            int bookingCount = (Integer) row.get("bookingCount");
            int ticketCount = (Integer) row.get("ticketCount");
            BigDecimal revenue = (BigDecimal) row.get("revenue");

            totalBookings += bookingCount;
            totalTickets += ticketCount;
            totalRevenue = totalRevenue.add(revenue);

            if (revenue.compareTo(maxRevenue) > 0) {
                maxRevenue = revenue;
            }
        }

        // Ngày có doanh thu cao nhất dài 100%, các ngày khác tính theo tỷ lệ.
        for (Map<String, Object> row : rows) {

            BigDecimal revenue = (BigDecimal) row.get("revenue");
            BigDecimal percent = BigDecimal.ZERO;

            if (maxRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percent = revenue.multiply(new BigDecimal("100"))
                        .divide(maxRevenue, 2, RoundingMode.HALF_UP);
            }

            row.put("revenuePercent", percent);
        }

        Map<String, Object> statistics = new LinkedHashMap<>();

        statistics.put("totalBookings", totalBookings);
        statistics.put("totalTickets", totalTickets);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("dailyRows", rows);

        return statistics;
    }
}
