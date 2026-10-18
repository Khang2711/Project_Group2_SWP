<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>
<fmt:setLocale value="vi_VN" />

<style>
    .uc17-dashboard {
        color: #fff;
    }

    .uc17-dashboard h1 {
        margin-bottom: 10px;
        font-size: 32px;
    }

    .uc17-dashboard .dashboard-subtitle {
        margin-bottom: 26px;
        color: #aaa;
        line-height: 1.6;
    }

    .uc17-dashboard .dashboard-filter {
        display: flex;
        flex-wrap: wrap;
        align-items: end;
        gap: 16px;
        padding: 22px;
        margin-bottom: 24px;
        border: 1px solid #303039;
        border-radius: 10px;
        background: #18181d;
    }

    .uc17-dashboard .filter-field {
        flex: 1;
        min-width: 180px;
    }

    .uc17-dashboard label {
        display: block;
        margin-bottom: 9px;
    }

    .uc17-dashboard input {
        width: 100%;
        padding: 11px 12px;
        border: 1px solid #50505b;
        border-radius: 6px;
        background: #101014;
        color: #fff;
        color-scheme: dark;
        font-size: 15px;
    }

    .uc17-dashboard .dashboard-button {
        display: inline-block;
        padding: 12px 20px;
        border: 0;
        border-radius: 6px;
        background: #ff1f2d;
        color: #fff;
        cursor: pointer;
        font-size: 15px;
        font-weight: bold;
    }

    .uc17-dashboard .dashboard-reset {
        padding: 12px 8px;
        color: #ccc;
    }

    .uc17-dashboard .dashboard-cards {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 20px;
        margin-bottom: 24px;
    }

    .uc17-dashboard .dashboard-card {
        padding: 24px;
        border: 1px solid #303039;
        border-radius: 10px;
        background: #18181d;
    }

    .uc17-dashboard .card-label {
        margin-bottom: 14px;
        color: #bbb;
    }

    .uc17-dashboard .card-value {
        font-size: 28px;
        font-weight: bold;
        overflow-wrap: anywhere;
    }

    .uc17-dashboard .revenue-value {
        color: #ff3542;
    }

    .uc17-dashboard .dashboard-panel {
        margin-bottom: 24px;
        padding: 24px;
        border: 1px solid #303039;
        border-radius: 10px;
        background: #18181d;
    }

    .uc17-dashboard h2 {
        margin-bottom: 12px;
        font-size: 21px;
    }

    .uc17-dashboard .panel-note {
        margin-bottom: 22px;
        color: #aaa;
        line-height: 1.6;
    }

    .uc17-dashboard .chart-row {
        display: grid;
        grid-template-columns: 95px minmax(0, 1fr) 145px;
        align-items: center;
        gap: 14px;
        margin: 20px 0;
        font-size: 14px;
    }

    .uc17-dashboard .chart-track {
        height: 18px;
        overflow: hidden;
        border-radius: 5px;
        background: #2b2b34;
    }

    .uc17-dashboard .chart-bar {
        height: 100%;
        border-radius: 5px;
        background: #ff1f2d;
    }

    .uc17-dashboard .chart-amount {
        text-align: right;
    }

    .uc17-dashboard .table-wrap {
        overflow-x: auto;
    }

    .uc17-dashboard table {
        width: 100%;
        border-collapse: collapse;
    }

    .uc17-dashboard th,
    .uc17-dashboard td {
        padding: 15px 12px;
        border-bottom: 1px solid #303039;
        text-align: right;
        white-space: nowrap;
    }

    .uc17-dashboard th {
        color: #aaa;
        font-size: 14px;
    }

    .uc17-dashboard th:first-child,
    .uc17-dashboard td:first-child {
        text-align: left;
    }

    .uc17-dashboard tfoot {
        font-weight: bold;
    }

    .uc17-dashboard .dashboard-empty {
        padding: 35px 15px;
        color: #bbb;
        text-align: center;
    }

    .uc17-dashboard .dashboard-error {
        padding: 18px;
        border: 1px solid #b53d49;
        border-radius: 8px;
        background: #36191f;
        color: #ffb9c1;
    }

    @media (max-width: 700px) {
        .uc17-dashboard .dashboard-cards {
            grid-template-columns: 1fr;
        }

        .uc17-dashboard .dashboard-panel {
            padding: 18px;
        }

        .uc17-dashboard .chart-row {
            grid-template-columns: 80px minmax(0, 1fr);
        }

        .uc17-dashboard .chart-amount {
            grid-column: 2;
        }
    }
</style>

<section class="uc17-dashboard">

    <h1>Thống kê hoạt động</h1>

    <p class="dashboard-subtitle">
        <c:if test="${not empty cinema}">
            <strong>
                <c:out value="${cinema.cinemaName}" />
            </strong>
            <br>
        </c:if>

        Thống kê theo ngày đặt vé, chỉ tính các đơn đã xác nhận.
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
                    <fmt:formatNumber
                        value="${totalBookings}"
                        pattern="#,##0" />
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">Vé đã bán</p>

                    <p class="card-value">
                    <fmt:formatNumber
                        value="${totalTickets}"
                        pattern="#,##0" />
                    </p>
                </article>

                <article class="dashboard-card">
                    <p class="card-label">
                        Doanh thu vé sau giảm giá
                    </p>

                    <p class="card-value revenue-value">
                    <fmt:formatNumber
                        value="${totalRevenue}"
                        pattern="#,##0" />
                    đ
                    </p>
                </article>

            </div>

            <c:choose>
                <c:when test="${empty dailyRows}">
                    <div class="dashboard-panel dashboard-empty">
                        Chưa có đơn đặt vé đã xác nhận
                        trong khoảng ngày đã chọn.
                    </div>
                </c:when>

                <c:otherwise>

                    <section class="dashboard-panel">
                        <h2>Doanh thu theo ngày</h2>

                        <p class="panel-note">
                            Hiển thị các ngày có đơn đã xác nhận
                            trong khoảng đã chọn.
                        </p>

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
                                        value="${row.revenue}"
                                        pattern="#,##0" />
                                    đ
                                </strong>

                            </div>
                        </c:forEach>
                    </section>

                    <section class="dashboard-panel">
                        <h2>Chi tiết theo ngày</h2>

                        <div class="table-wrap">
                            <table>

                                <thead>
                                    <tr>
                                        <th>Ngày đặt vé</th>
                                        <th>Đơn xác nhận</th>
                                        <th>Vé đã bán</th>
                                        <th>Doanh thu vé</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:forEach items="${dailyRows}"
                                               var="row">

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
                                        value="${row.revenue}"
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
                                    value="${totalRevenue}"
                                    pattern="#,##0" />
                                đ
                                </td>
                                </tr>
                                </tfoot>

                            </table>
                        </div>
                    </section>

                </c:otherwise>
            </c:choose>

        </c:otherwise>
    </c:choose>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>