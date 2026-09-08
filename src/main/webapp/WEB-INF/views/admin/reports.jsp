<%@page import="model.MovieRevenue"%>
<%@page import="java.util.List"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.YearMonth"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Báo cáo doanh thu");
    request.setAttribute("fullWidthPage", Boolean.TRUE);

    DecimalFormat df = new DecimalFormat("#,###");
    String selectedMonth = (String) request.getAttribute("selectedMonth");
    String currentMonth = (String) request.getAttribute("currentMonth");
    String reportMonthLabel = (String) request.getAttribute("reportMonthLabel");

    if (selectedMonth == null || selectedMonth.isEmpty()) {
        selectedMonth = YearMonth.now().toString();
    }
    if (currentMonth == null || currentMonth.isEmpty()) {
        currentMonth = YearMonth.now().toString();
    }
    if (reportMonthLabel == null || reportMonthLabel.isEmpty()) {
        YearMonth ym = YearMonth.parse(selectedMonth);
        reportMonthLabel = String.format("Tháng %02d/%04d", ym.getMonthValue(), ym.getYear());
    }

    Integer movieCount = (Integer) request.getAttribute("movieCount");
    Integer orderCount = (Integer) request.getAttribute("orderCount");
    Integer ticketCount = (Integer) request.getAttribute("ticketCount");
    Float ticketRevenue = (Float) request.getAttribute("ticketRevenue");
    Float comboRevenue = (Float) request.getAttribute("comboRevenue");
    Float revenue = (Float) request.getAttribute("revenue");
    List<MovieRevenue> movieRevenueList
            = (List<MovieRevenue>) request.getAttribute("movieRevenueList");

    int movieTicketTotal = 0;
    float movieRevenueTotal = 0;
    if (movieRevenueList != null) {
        for (MovieRevenue item : movieRevenueList) {
            movieTicketTotal += item.getTicketCount();
            movieRevenueTotal += item.getRevenue();
        }
    }
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel report-page">
    <div class="report-title-row">
        <div>
            <h1 class="page-title">Báo cáo doanh thu theo tháng</h1>
            <p class="muted">Dữ liệu được lọc theo ngày tạo đơn hàng thành công.</p>
        </div>
        <span class="report-month-badge"><%= reportMonthLabel%></span>
    </div>

    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-error"><%= request.getAttribute("error")%></div>
    <% } %>

    <form class="report-filter" method="get"
          action="<%= request.getContextPath()%>/admin/reports">
        <div class="field report-month-field">
            <label for="month">Chọn tháng báo cáo</label>
            <input id="month" type="month" name="month"
                   value="<%= selectedMonth%>" max="<%= currentMonth%>" required>
        </div>
        <button class="btn btn-primary" type="submit">Xem báo cáo</button>
        <a class="btn btn-green"
           href="<%= request.getContextPath()%>/admin/reports/export?month=<%= selectedMonth%>">
            Xuất Excel
        </a>
    </form>

    <div class="report-kpi-grid">
        <div class="report-kpi">
            <span>Phim có doanh thu</span>
            <strong><%= movieCount != null ? movieCount : 0%></strong>
        </div>
        <div class="report-kpi">
            <span>Đơn hàng thành công</span>
            <strong><%= orderCount != null ? orderCount : 0%></strong>
        </div>
        <div class="report-kpi">
            <span>Vé đã bán</span>
            <strong><%= ticketCount != null ? ticketCount : 0%></strong>
        </div>
        <div class="report-kpi">
            <span>Doanh thu vé</span>
            <strong><%= df.format(ticketRevenue != null ? ticketRevenue : 0)%> VNĐ</strong>
        </div>
        <div class="report-kpi">
            <span>Doanh thu combo</span>
            <strong><%= df.format(comboRevenue != null ? comboRevenue : 0)%> VNĐ</strong>
        </div>
        <div class="report-kpi report-kpi-total">
            <span>Tổng doanh thu</span>
            <strong><%= df.format(revenue != null ? revenue : 0)%> VNĐ</strong>
        </div>
    </div>

    <div class="report-section-heading">
        <div>
            <h2>Doanh thu vé theo phim</h2>
            <p class="muted">Mỗi dòng là số vé được mua trong <%= reportMonthLabel.toLowerCase()%>.</p>
        </div>
    </div>

    <div class="table-wrap">
        <table class="report-table">
            <thead>
                <tr>
                    <th>STT</th>
                    <th>Mã phim</th>
                    <th>Tên phim</th>
                    <th>Số đơn có phim</th>
                    <th>Số vé đã bán</th>
                    <th>Doanh thu vé</th>
                </tr>
            </thead>
            <tbody>
                <% if (movieRevenueList != null && !movieRevenueList.isEmpty()) {
                    int index = 1;
                    for (MovieRevenue item : movieRevenueList) { %>
                <tr>
                    <td><%= index++%></td>
                    <td><strong><%= item.getMovieID()%></strong></td>
                    <td><%= item.getMovieTitle()%></td>
                    <td><%= item.getOrderCount()%></td>
                    <td><%= item.getTicketCount()%></td>
                    <td><strong><%= df.format(item.getRevenue())%> VNĐ</strong></td>
                </tr>
                <%  }
                } else { %>
                <tr>
                    <td colspan="6" class="report-empty">
                        Chưa có đơn hàng thành công trong tháng đã chọn.
                    </td>
                </tr>
                <% } %>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="4"><strong>Tổng doanh thu vé</strong></td>
                    <td><strong><%= movieTicketTotal%></strong></td>
                    <td><strong><%= df.format(movieRevenueTotal)%> VNĐ</strong></td>
                </tr>
            </tfoot>
        </table>
    </div>

    <p class="report-note">
        Lưu ý: doanh thu theo phim chỉ tính tiền vé. Tiền combo được hiển thị riêng
        và đã được cộng vào “Tổng doanh thu”.
    </p>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
