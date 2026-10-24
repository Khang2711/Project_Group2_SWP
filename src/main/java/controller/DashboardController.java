package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.DashboardService;

@WebServlet(urlPatterns = {"/admin/dashboard"})
public class DashboardController extends HttpServlet {

    private final DashboardService dashboardService
            = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof User)) {

            response.sendRedirect(
                    request.getContextPath() + "/auth/login");
            return;
        }

        if (!"CINEMA_MANAGER".equals(
                session.getAttribute("roleName"))) {

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setHeader("Cache-Control", "no-store");

        User user = (User) session.getAttribute("user");

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh"));

        String fromValue = request.getParameter("fromDate");
        String toValue = request.getParameter("toDate");

        if (fromValue == null) {
            fromValue = today.withDayOfMonth(1).toString();
        }

        if (toValue == null) {
            toValue = today.toString();
        }

        request.setAttribute("fromDate", fromValue);
        request.setAttribute("toDate", toValue);

        try {
            Cinema cinema = dashboardService.getManagerCinema(
                    user.getUserId());

            if (cinema == null) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản không còn quyền Manager "
                        + "hoặc chưa được phân công rạp.");
                return;
            }

            request.setAttribute("cinema", cinema);

            LocalDate fromDate = dashboardService.parseDate(
                    fromValue, "Ngày bắt đầu");

            LocalDate toDate = dashboardService.parseDate(
                    toValue, "Ngày kết thúc");

            Map<String, Object> statistics
                    = dashboardService.getStatistics(
                            cinema.getCinemaId(),
                            fromDate,
                            toDate);

            // Chỉ xuất sau khi đã kiểm tra quyền, rạp và khoảng ngày.
            if ("excel".equals(request.getParameter("export"))) {
                exportExcel(
                        response,
                        cinema,
                        fromDate,
                        toDate,
                        statistics);
                return;
            }

            request.setAttribute(
                    "totalBookings",
                    statistics.get("totalBookings"));

            request.setAttribute(
                    "totalTickets",
                    statistics.get("totalTickets"));

            request.setAttribute(
                    "totalRevenue",
                    statistics.get("totalRevenue"));

            request.setAttribute(
                    "totalFoodOrders",
                    statistics.get("totalFoodOrders"));

            request.setAttribute(
                    "foodRevenue",
                    statistics.get("foodRevenue"));

            request.setAttribute(
                    "combinedRevenue",
                    statistics.get("combinedRevenue"));

            request.setAttribute(
                    "dailyRows",
                    statistics.get("dailyRows"));

            request.setAttribute(
                    "movieRows",
                    statistics.get("movieRows"));

            request.setAttribute(
                    "showHourRows",
                    statistics.get("showHourRows"));

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());

        } catch (SQLException e) {
            log("Không thể tải Dashboard.", e);

            request.setAttribute(
                    "error",
                    "Không thể tải thống kê. Vui lòng thử lại.");
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/dashboard.jsp"
        ).forward(request, response);
    }

    private void exportExcel(
            HttpServletResponse response,
            Cinema cinema,
            LocalDate fromDate,
            LocalDate toDate,
            Map<String, Object> statistics) throws IOException {

        List<Map<String, Object>> dailyRows
                = (List<Map<String, Object>>) statistics.get("dailyRows");

        List<Map<String, Object>> movieRows
                = (List<Map<String, Object>>) statistics.get("movieRows");

        List<Map<String, Object>> showHourRows
                = (List<Map<String, Object>>) statistics.get("showHourRows");

        DateTimeFormatter dateFormat
                = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook()) {

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setWrapText(true);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("#,##0"));

            Sheet summary = workbook.createSheet("Tong quan");

            summary.createRow(0).createCell(0)
                    .setCellValue("THỐNG KÊ HOẠT ĐỘNG F-CINEMA");

            summary.getRow(0).getCell(0).setCellStyle(headerStyle);

            Row cinemaRow = summary.createRow(1);
            cinemaRow.createCell(0).setCellValue("Rạp");
            cinemaRow.createCell(1).setCellValue(cinema.getCinemaName());

            Row fromRow = summary.createRow(2);
            fromRow.createCell(0).setCellValue("Từ ngày");
            fromRow.createCell(1).setCellValue(fromDate.format(dateFormat));

            Row toRow = summary.createRow(3);
            toRow.createCell(0).setCellValue("Đến ngày");
            toRow.createCell(1).setCellValue(toDate.format(dateFormat));

            createHeader(
                    summary,
                    5,
                    new String[]{"Chỉ tiêu", "Giá trị"},
                    headerStyle);

            String[] labels = {
                "Đơn đặt vé đã xác nhận",
                "Vé đã bán",
                "Đơn bắp nước/combo hoàn tất",
                "Doanh thu vé sau giảm giá (VND)",
                "Doanh thu bắp nước/combo (VND)",
                "Tổng doanh thu (VND)"
            };

            String[] keys = {
                "totalBookings",
                "totalTickets",
                "totalFoodOrders",
                "totalRevenue",
                "foodRevenue",
                "combinedRevenue"
            };

            for (int i = 0; i < labels.length; i++) {
                Row row = summary.createRow(i + 6);
                row.createCell(0).setCellValue(labels[i]);

                Number value = (Number) statistics.get(keys[i]);

                Cell cell = row.createCell(1);
                cell.setCellValue(value.doubleValue());

                if (i >= 3) {
                    cell.setCellStyle(moneyStyle);
                }
            }

            summary.createRow(13).createCell(0)
                    .setCellValue("Lọc theo ngày tạo đơn.");

            summary.createRow(14).createCell(0)
                    .setCellValue(
                            "Vé: đơn CONFIRMED; bắp nước/combo: PAID và COMPLETED.");

            summary.setColumnWidth(0, 48 * 256);
            summary.setColumnWidth(1, 32 * 256);

            Sheet daily = workbook.createSheet("Theo ngay");

            createHeader(
                    daily,
                    0,
                    new String[]{
                        "Ngày tạo đơn",
                        "Đơn vé",
                        "Vé đã bán",
                        "Đơn bắp nước/combo",
                        "Doanh thu vé (VND)",
                        "Doanh thu bắp nước/combo (VND)",
                        "Tổng doanh thu (VND)"
                    },
                    headerStyle);

            int rowIndex = 1;

            for (Map<String, Object> data : dailyRows) {
                Row row = daily.createRow(rowIndex);
                rowIndex++;

                java.sql.Date date
                        = (java.sql.Date) data.get("bookingDate");

                row.createCell(0).setCellValue(
                        date.toLocalDate().format(dateFormat));

                row.createCell(1).setCellValue(
                        (Integer) data.get("bookingCount"));

                row.createCell(2).setCellValue(
                        (Integer) data.get("ticketCount"));

                row.createCell(3).setCellValue(
                        (Integer) data.get("foodOrderCount"));

                writeMoney(
                        row, 4,
                        (BigDecimal) data.get("revenue"),
                        moneyStyle);

                writeMoney(
                        row, 5,
                        (BigDecimal) data.get("foodRevenue"),
                        moneyStyle);

                writeMoney(
                        row, 6,
                        (BigDecimal) data.get("dailyTotalRevenue"),
                        moneyStyle);
            }

            Row totals = daily.createRow(rowIndex);

            totals.createCell(0).setCellValue("Tổng cộng");
            totals.getCell(0).setCellStyle(headerStyle);

            totals.createCell(1).setCellValue(
                    (Integer) statistics.get("totalBookings"));

            totals.createCell(2).setCellValue(
                    (Integer) statistics.get("totalTickets"));

            totals.createCell(3).setCellValue(
                    (Integer) statistics.get("totalFoodOrders"));

            writeMoney(
                    totals, 4,
                    (BigDecimal) statistics.get("totalRevenue"),
                    moneyStyle);

            writeMoney(
                    totals, 5,
                    (BigDecimal) statistics.get("foodRevenue"),
                    moneyStyle);

            writeMoney(
                    totals, 6,
                    (BigDecimal) statistics.get("combinedRevenue"),
                    moneyStyle);

            for (int i = 0; i < 7; i++) {
                daily.setColumnWidth(i, 24 * 256);
            }

            daily.setColumnWidth(5, 30 * 256);
            daily.createFreezePane(0, 1);

            Sheet movies = workbook.createSheet("Theo phim");

            createHeader(
                    movies,
                    0,
                    new String[]{"Tên phim", "Vé đã bán"},
                    headerStyle);

            rowIndex = 1;

            for (Map<String, Object> data : movieRows) {
                Row row = movies.createRow(rowIndex);
                rowIndex++;

                row.createCell(0).setCellValue(
                        (String) data.get("movieTitle"));

                row.createCell(1).setCellValue(
                        (Integer) data.get("ticketCount"));
            }

            if (movieRows.isEmpty()) {
                movies.createRow(1).createCell(0)
                        .setCellValue(
                                "Chưa có vé bán trong khoảng ngày này.");
            }

            movies.setColumnWidth(0, 48 * 256);
            movies.setColumnWidth(1, 18 * 256);
            movies.createFreezePane(0, 1);

            Sheet hours = workbook.createSheet("Theo gio chieu");

            createHeader(
                    hours,
                    0,
                    new String[]{
                        "Khung giờ bắt đầu suất chiếu",
                        "Vé đã bán"
                    },
                    headerStyle);

            rowIndex = 1;

            for (Map<String, Object> data : showHourRows) {
                Row row = hours.createRow(rowIndex);
                rowIndex++;

                row.createCell(0).setCellValue(
                        (String) data.get("hourLabel"));

                row.createCell(1).setCellValue(
                        (Integer) data.get("ticketCount"));
            }

            if (showHourRows.isEmpty()) {
                hours.createRow(1).createCell(0)
                        .setCellValue(
                                "Chưa có vé bán trong khoảng ngày này.");
            }

            hours.setColumnWidth(0, 42 * 256);
            hours.setColumnWidth(1, 18 * 256);
            hours.createFreezePane(0, 1);

            String fileName = "FCinema-rap-" + cinema.getCinemaId()
                    + "-" + fromDate + "-" + toDate + ".xlsx";

            // Báo cho trình duyệt tải file Excel về máy.
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");

            workbook.write(response.getOutputStream());
        }
    }

    private void createHeader(
            Sheet sheet,
            int rowIndex,
            String[] titles,
            CellStyle headerStyle) {

        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(32);

        for (int i = 0; i < titles.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeMoney(
            Row row,
            int column,
            BigDecimal amount,
            CellStyle moneyStyle) {

        Cell cell = row.createCell(column);
        cell.setCellValue(amount.doubleValue());
        cell.setCellStyle(moneyStyle);
    }
}