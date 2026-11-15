<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<fmt:setLocale value="vi_VN" />



<section class="uc17-dashboard" data-css-page="admin-dashboard">

    <h1>Thống kê hoạt động</h1>

    <p class="dashboard-subtitle">
        <c:if test="${not empty cinema}">
            <strong>
                <c:out value="${cinema.cinemaName}" />
            </strong>
            <br>
        </c:if>

      
    </p>

    <form class="dashboard-filter"
          method="get"
          action="${pageContext.request.contextPath}/admin/dashboard">

        <div class="filter-field">
            <label for="fromDate">Từ ngày</label>

            <input type="date"
                   id="fromDate"
                   name="fromDate"
                   value="<c:out value='${fromDate}' />"
                   required>
        </div>

        <div class="filter-field">
            <label for="toDate">Đến ngày</label>

            <input type="date"
                   id="toDate"
                   name="toDate"
                   value="<c:out value='${toDate}' />"
                   required>
        </div>

        <button type="submit" class="dashboard-button">
            Xem thống kê
        </button>

        <button type="submit"
                name="export"
                value="excel"
                class="dashboard-button dashboard-export">
            Xuất Excel
        </button>

        <a class="dashboard-reset"
           href="${pageContext.request.contextPath}/admin/dashboard">
            Tháng hiện tại
        </a>

    </form>

    <c:choose>
        <c:when test="${not empty error}">
            <div class="dashboard-error" role="alert">
                <c:out value="${error}" />
            </div>
        </c:when>

        <c:otherwise>

            <div class="dashboard-cards">

                <article class="dashboard-card">
                    <p class="card-label">
                        Đơn đặt vé đã xác nhận
                    </p>

                    <p class="card-value">
                        <fmt:formatNumber value="${totalBookings}"
                                          pattern="#,##0" />
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">Vé đã bán</p>

                    <p class="card-value">
                        <fmt:formatNumber value="${totalTickets}"
                                          pattern="#,##0" />
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">
                        Đơn bắp nước/combo đã thanh toán
                    </p>

                    <p class="card-value">
                        <fmt:formatNumber value="${totalFoodOrders}"
                                          pattern="#,##0" />
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">
                        Doanh thu vé sau giảm giá
                    </p>

                    <p class="card-value revenue-value">
                        <fmt:formatNumber value="${totalRevenue}"
                                          pattern="#,##0" />
                        đ
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">
                        Doanh thu bắp nước/combo
                    </p>

                    <p class="card-value revenue-value">
                        <fmt:formatNumber value="${foodRevenue}"
                                          pattern="#,##0" />
                        đ
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">Tổng doanh thu</p>

                    <p class="card-value revenue-value">
                        <fmt:formatNumber value="${combinedRevenue}"
                                          pattern="#,##0" />
                        đ
                    </p>
                </article>

            </div>

            <div class="analysis-grid">

                <section class="dashboard-panel">

                    <h2>Vé bán theo phim</h2>

                    <p class="panel-note">
                        Số vé có thanh toán thành công trong khoảng ngày đã chọn,
                        sắp xếp giảm dần.
                    </p>

                    <c:choose>
                        <c:when test="${empty movieRows}">
                            <p class="dashboard-empty">
                                Chưa có vé bán trong khoảng ngày này.
                            </p>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="row" items="${movieRows}">

                                <div class="ticket-chart-row">

                                    <span class="ticket-chart-label">
                                        <c:out value="${row.movieTitle}" />
                                    </span>

                                    <div class="chart-track"
                                         aria-hidden="true">
                                        <div class="chart-bar"
                                             style="width: ${row.ticketPercent}%;">
                                        </div>
                                    </div>

                                    <strong class="ticket-chart-count">
                                        <fmt:formatNumber
                                            value="${row.ticketCount}"
                                            pattern="#,##0" />
                                        vé
                                    </strong>

                                </div>

                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                </section>

                <section class="dashboard-panel">

                    <h2>Vé bán theo khung giờ chiếu</h2>

                    <p class="panel-note">
                        Số vé có thanh toán thành công trong khoảng ngày đã chọn,
                        nhóm theo giờ bắt đầu suất chiếu.
                    </p>

                    <c:choose>
                        <c:when test="${empty showHourRows}">
                            <p class="dashboard-empty">
                                Chưa có vé bán trong khoảng ngày này.
                            </p>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="row" items="${showHourRows}">

                                <div class="ticket-chart-row">

                                    <span class="ticket-chart-label">
                                        <c:out value="${row.hourLabel}" />
                                    </span>

                                    <div class="chart-track"
                                         aria-hidden="true">
                                        <div class="chart-bar"
                                             style="width: ${row.ticketPercent}%;">
                                        </div>
                                    </div>

                                    <strong class="ticket-chart-count">
                                        <fmt:formatNumber
                                            value="${row.ticketCount}"
                                            pattern="#,##0" />
                                        vé
                                    </strong>

                                </div>

                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                </section>

            </div>

            <c:choose>
                <c:when test="${empty dailyRows}">
                    <div class="dashboard-panel dashboard-empty">
                        Chưa có Booking được xác nhận theo ngày tạo hoặc
                        giao dịch thanh toán thành công trong khoảng ngày đã chọn.
                    </div>
                </c:when>

                <c:otherwise>

                    <section class="dashboard-panel">

                        <h2>Tổng doanh thu theo ngày</h2>

                        <c:forEach items="${dailyRows}" var="row">

                            <div class="chart-row">

                                <span>
                                    <fmt:formatDate
                                        value="${row.bookingDate}"
                                        pattern="dd/MM/yyyy" />
                                </span>

                                <div class="chart-track"
                                     aria-hidden="true">
                                    <div class="chart-bar"
                                         style="width: ${row.revenuePercent}%;">
                                    </div>
                                </div>

                                <strong class="chart-amount">
                                    <fmt:formatNumber
                                        value="${row.dailyTotalRevenue}"
                                        pattern="#,##0" />
                                    đ
                                </strong>

                            </div>

                        </c:forEach>

                    </section>

                    <details class="dashboard-panel dashboard-details">

                        <summary>Chi tiết theo ngày</summary>

                        <div class="table-wrap">
                            <table>

                                <thead>
                                    <tr>
                                        <th>Ngày tổng hợp</th>
                                        <th>Booking tạo trong ngày</th>
                                        <th>Vé thanh toán trong ngày</th>
                                        <th>Đơn đồ ăn ghi nhận trong ngày</th>
                                        <th>Doanh thu vé</th>
                                        <th>Doanh thu bắp nước/combo</th>
                                        <th>Tổng doanh thu</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:forEach items="${dailyRows}" var="row">

                                        <tr>
                                            <td>
                                                <fmt:formatDate
                                                    value="${row.bookingDate}"
                                                    pattern="dd/MM/yyyy" />
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.bookingCount}"
                                                    pattern="#,##0" />
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.ticketCount}"
                                                    pattern="#,##0" />
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.foodOrderCount}"
                                                    pattern="#,##0" />
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.revenue}"
                                                    pattern="#,##0" />
                                                đ
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.foodRevenue}"
                                                    pattern="#,##0" />
                                                đ
                                            </td>

                                            <td>
                                                <fmt:formatNumber
                                                    value="${row.dailyTotalRevenue}"
                                                    pattern="#,##0" />
                                                đ
                                            </td>
                                        </tr>

                                    </c:forEach>
                                </tbody>

                                <tfoot>
                                    <tr>
                                        <td>Tổng cộng</td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${totalBookings}"
                                                pattern="#,##0" />
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${totalTickets}"
                                                pattern="#,##0" />
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${totalFoodOrders}"
                                                pattern="#,##0" />
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${totalRevenue}"
                                                pattern="#,##0" />
                                            đ
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${foodRevenue}"
                                                pattern="#,##0" />
                                            đ
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${combinedRevenue}"
                                                pattern="#,##0" />
                                            đ
                                        </td>
                                    </tr>
                                </tfoot>

                            </table>
                        </div>

                    </details>

                </c:otherwise>
            </c:choose>

        </c:otherwise>
    </c:choose>

</section>

<script>
    (function () {
        const filterForm = document.querySelector('.dashboard-filter');
        let hasUnsavedFilterChanges = false;

        if (filterForm) {
            filterForm.addEventListener('input', function () {
                hasUnsavedFilterChanges = true;
            });

            filterForm.addEventListener('change', function () {
                hasUnsavedFilterChanges = true;
            });

            filterForm.addEventListener('submit', function () {
                hasUnsavedFilterChanges = false;
            });
        }

        // Refresh thống kê mỗi 60 giây khi Manager đang xem Dashboard.
        // Không refresh khi đang chỉnh bộ lọc hoặc mở bảng chi tiết.
        window.setInterval(function () {
            const detailIsOpen = document.querySelector(
                '.dashboard-details[open]'
            );

            if (document.visibilityState === 'visible'
                    && !hasUnsavedFilterChanges
                    && !detailIsOpen) {
                window.location.reload();
            }
        }, 60000);
    })();
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>