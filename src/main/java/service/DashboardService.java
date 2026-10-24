package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Cinema;
import repository.BookingRepository;
import repository.FoodOrderRepository;

public class DashboardService {

    private final BookingRepository bookingRepository
            = new BookingRepository();

    private final FoodOrderRepository foodOrderRepository
            = new FoodOrderRepository();

    public Cinema getManagerCinema(int managerId)
            throws SQLException {

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
            int cinemaId,
            LocalDate fromDate,
            LocalDate toDate) throws SQLException {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ.");
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
        int totalFoodOrders = 0;

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal foodRevenue = BigDecimal.ZERO;
        BigDecimal maxRevenue = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {

            totalBookings += (Integer) row.get("bookingCount");
            totalTickets += (Integer) row.get("ticketCount");
            totalFoodOrders += (Integer) row.get("foodOrderCount");

            BigDecimal ticketAmount = (BigDecimal) row.get("revenue");
            BigDecimal foodAmount = (BigDecimal) row.get("foodRevenue");
            BigDecimal dailyTotal = ticketAmount.add(foodAmount);

            totalRevenue = totalRevenue.add(ticketAmount);
            foodRevenue = foodRevenue.add(foodAmount);

            row.put("dailyTotalRevenue", dailyTotal);

            if (dailyTotal.compareTo(maxRevenue) > 0) {
                maxRevenue = dailyTotal;
            }
        }

        for (Map<String, Object> row : rows) {

            BigDecimal dailyTotal
                    = (BigDecimal) row.get("dailyTotalRevenue");

            BigDecimal percent = BigDecimal.ZERO;

            if (maxRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percent = dailyTotal.multiply(new BigDecimal("100"))
                        .divide(maxRevenue, 2, RoundingMode.HALF_UP);
            }

            row.put("revenuePercent", percent);
        }

        List<Map<String, Object>> movieRows
                = bookingRepository.getDashboardMovieData(
                        cinemaId, fromDate, toDate);

        List<Map<String, Object>> showHourRows
                = bookingRepository.getDashboardShowHourData(
                        cinemaId, fromDate, toDate);

        calculateTicketPercent(movieRows);
        calculateTicketPercent(showHourRows);

        for (Map<String, Object> row : showHourRows) {

            int hour = (Integer) row.get("showHour");
            String hourText;

            if (hour < 10) {
                hourText = "0" + hour;
            } else {
                hourText = String.valueOf(hour);
            }

            row.put(
                    "hourLabel",
                    hourText + ":00 - " + hourText + ":59");
        }

        Map<String, Object> statistics = new LinkedHashMap<>();

        statistics.put("totalBookings", totalBookings);
        statistics.put("totalTickets", totalTickets);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalFoodOrders", totalFoodOrders);
        statistics.put("foodRevenue", foodRevenue);
        statistics.put("combinedRevenue", totalRevenue.add(foodRevenue));
        statistics.put("dailyRows", rows);
        statistics.put("movieRows", movieRows);
        statistics.put("showHourRows", showHourRows);

        return statistics;
    }

    public Map<String, Object> getStaffSalesReport(
            int staffId,
            LocalDate fromDate,
            LocalDate toDate) throws SQLException {

        if (staffId <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên không hợp lệ.");
        }

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn đủ ngày bắt đầu và ngày kết thúc.");
        }

        if (fromDate.getYear() < 1 || fromDate.getYear() > 9999
                || toDate.getYear() < 1 || toDate.getYear() > 9999) {

            throw new IllegalArgumentException(
                    "Khoảng ngày không hợp lệ.");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc.");
        }

        int cinemaId = foodOrderRepository.findStaffCinema(staffId);

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên chưa được phân công rạp đang hoạt động.");
        }

        List<Map<String, Object>> ticketSales
                = bookingRepository.findStaffTicketSales(
                        staffId, cinemaId, fromDate, toDate);

        List<Map<String, Object>> foodSales
                = bookingRepository.findStaffFoodSales(
                        staffId, cinemaId, fromDate, toDate);

        int totalTickets = 0;
        int totalFoodQuantity = 0;

        BigDecimal ticketRevenue = BigDecimal.ZERO;
        BigDecimal foodRevenue = BigDecimal.ZERO;

        for (Map<String, Object> row : ticketSales) {

            totalTickets += (Integer) row.get("ticketCount");

            ticketRevenue = ticketRevenue.add(
                    (BigDecimal) row.get("totalAmount"));
        }

        for (Map<String, Object> row : foodSales) {

            totalFoodQuantity += (Integer) row.get("quantity");

            foodRevenue = foodRevenue.add(
                    (BigDecimal) row.get("totalAmount"));
        }

        Map<String, Object> report = new LinkedHashMap<>();

        report.put("ticketSales", ticketSales);
        report.put("foodSales", foodSales);

        report.put("totalTicketOrders", ticketSales.size());
        report.put("totalTickets", totalTickets);
        report.put("ticketRevenue", ticketRevenue);

        report.put("totalFoodOrders", foodSales.size());
        report.put("totalFoodQuantity", totalFoodQuantity);
        report.put("foodRevenue", foodRevenue);

        report.put(
                "totalRevenue",
                ticketRevenue.add(foodRevenue));

        return report;
    }

    public Booking getStaffBooking(int staffId, int bookingId)
            throws SQLException {

        if (staffId <= 0 || bookingId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin đơn vé không hợp lệ.");
        }

        int cinemaId = foodOrderRepository.findStaffCinema(staffId);

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Nhân viên chưa được phân công rạp đang hoạt động.");
        }

        Booking booking = bookingRepository.findByIdAndStaffId(
                bookingId, staffId, cinemaId);

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đơn vé do bạn bán tại rạp này.");
        }

        return booking;
    }

    private void calculateTicketPercent(
            List<Map<String, Object>> rows) {

        int maxTickets = 0;

        for (Map<String, Object> row : rows) {

            int ticketCount = (Integer) row.get("ticketCount");

            if (ticketCount > maxTickets) {
                maxTickets = ticketCount;
            }
        }

        // Mục bán nhiều vé nhất có thanh dài 100%.
        for (Map<String, Object> row : rows) {

            int ticketCount = (Integer) row.get("ticketCount");
            BigDecimal percent = BigDecimal.ZERO;

            if (maxTickets > 0) {
                percent = BigDecimal.valueOf(ticketCount)
                        .multiply(new BigDecimal("100"))
                        .divide(
                                BigDecimal.valueOf(maxTickets),
                                2,
                                RoundingMode.HALF_UP);
            }

            row.put("ticketPercent", percent);
        }
    }
}
