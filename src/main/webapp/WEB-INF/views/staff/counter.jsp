<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<c:set var="activeCounterTab" value="home" />

<c:if test="${param.tab eq 'statistics' or param.tab eq 'tickets' or param.tab eq 'food'}">
    <c:set var="activeCounterTab" value="${param.tab}" />
</c:if>

<c:url var="counterResetUrl" value="/staff/counter">
    <c:param name="tab" value="${activeCounterTab}" />
</c:url>

<style>
    .counter-home {
        padding-bottom: 30px;
        color: #eeeeee;
    }

    .counter-home-heading {
        margin-bottom: 18px;
        padding-bottom: 16px;
        border-bottom: 1px solid #303038;
    }

    .counter-home-heading h1 {
        margin: 0;
        padding-left: 14px;
        border-left: 4px solid #e50914;
        color: #ffffff;
        font-size: 30px;
    }

    .counter-home-heading p {
        margin: 10px 0 0;
        color: #b8b8c2;
        font-size: 14px;
        line-height: 1.6;
    }

    .counter-home-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 14px;
        margin-bottom: 20px;
    }

    .counter-home-card {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        min-width: 0;
        min-height: 76px;
        padding: 14px 16px;
        background: #18181c;
        border: 1px solid #33333d;
        border-radius: 6px;
    }

    .counter-home-card:hover {
        border-color: #55555f;
    }

    .counter-home-card h2 {
        min-width: 0;
        margin: 0;
        color: #ffffff;
        font-size: 16px;
        line-height: 1.4;
        overflow-wrap: anywhere;
    }

    .counter-home-button,
    .counter-filter-button {
        background: #e50914;
        border: 1px solid #e50914;
        border-radius: 4px;
        color: #ffffff;
        font-weight: bold;
        text-align: center;
        text-decoration: none;
        cursor: pointer;
    }

    .counter-home-button {
        display: inline-block;
        flex-shrink: 0;
        padding: 9px 12px;
        font-size: 14px;
        white-space: nowrap;
    }

    .counter-filter-button {
        padding: 11px 16px;
        font-size: 15px;
    }

    .counter-home-button:hover,
    .counter-filter-button:hover {
        background: #ff1f2d;
        border-color: #ff1f2d;
        color: #ffffff;
    }

    .counter-report-panel {
        min-width: 0;
        margin-bottom: 20px;
        padding: 22px;
        background: #18181c;
        border: 1px solid #33333d;
        border-radius: 8px;
    }

    .counter-report-panel h2 {
        margin: 0 0 12px;
        color: #ffffff;
        font-size: 22px;
    }

    .counter-report-note {
        margin: 0 0 18px;
        color: #b8b8c2;
        font-size: 14px;
        line-height: 1.7;
    }

    .counter-report-filter {
        display: flex;
        flex-wrap: wrap;
        align-items: end;
        gap: 14px;
    }

    .counter-report-field {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .counter-report-field label {
        font-size: 14px;
        font-weight: bold;
    }

    .counter-report-field input {
        padding: 10px 12px;
        background: #101014;
        color: #ffffff;
        border: 1px solid #44444d;
        border-radius: 4px;
        font-size: 16px;
        color-scheme: dark;
    }

    .counter-report-error {
        margin-top: 18px;
        padding: 14px;
        border: 1px solid #91303a;
        border-radius: 4px;
        background: #35191e;
        color: #ffb8c0;
    }

    .counter-stat-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 20px;
    }

    .counter-stat-card {
        min-width: 0;
        padding: 22px;
        background: #18181c;
        border: 1px solid #33333d;
        border-radius: 8px;
    }

    .counter-stat-card p {
        margin: 0 0 12px;
        color: #b8b8c2;
        font-size: 14px;
    }

    .counter-stat-card strong {
        display: block;
        color: #ffffff;
        font-size: 24px;
        overflow-wrap: anywhere;
    }

    .counter-stat-card small {
        display: block;
        margin-top: 10px;
        color: #b8b8c2;
        line-height: 1.5;
    }

    .counter-total-revenue {
        margin-bottom: 20px;
        padding: 18px 22px;
        border-left: 4px solid #e50914;
        background: #22171b;
        font-size: 18px;
        line-height: 1.7;
    }

    .counter-total-revenue strong {
        color: #ff6472;
    }

    .counter-table-wrap {
        overflow-x: auto;
    }

    .counter-sales-table {
        width: 100%;
        min-width: 760px;
        border-collapse: collapse;
        font-size: 14px;
    }

    .counter-sales-table th,
    .counter-sales-table td {
        padding: 14px 12px;
        border-bottom: 1px solid #303038;
        text-align: left;
        vertical-align: top;
    }

    .counter-sales-table th {
        color: #b8b8c2;
        font-weight: bold;
    }

    .counter-sales-table td small {
        display: block;
        margin-top: 6px;
        color: #b8b8c2;
    }

    .counter-sales-table .counter-money {
        white-space: nowrap;
    }

    .counter-sales-table a {
        color: #ff6472;
        font-weight: bold;
        white-space: nowrap;
    }

    .counter-sales-table .counter-empty {
        padding: 28px 12px;
        color: #b8b8c2;
        text-align: center;
    }

    @media (max-width: 1000px) {
        .counter-stat-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
    }

    @media (max-width: 800px) {
        .counter-home-grid {
            grid-template-columns: 1fr;
            gap: 10px;
        }

        .counter-home-card {
            min-height: 64px;
            padding: 12px 16px;
        }
    }

    @media (max-width: 500px) {
        .counter-home-heading h1 {
            font-size: 25px;
        }

        .counter-stat-grid {
            grid-template-columns: 1fr;
        }

        .counter-report-panel {
            padding: 18px;
        }

        .counter-report-field {
            width: 100%;
        }
    }
</style>

<c:if test="${activeCounterTab eq 'home'}">

    <section class="counter-home">

        <div class="counter-home-heading">
            <h1>Bán hàng tại quầy</h1>
            <p>Chọn tác vụ bạn muốn thực hiện.</p>
        </div>

        <div class="counter-home-grid">

            <article class="counter-home-card">
                <h2>Vé tại quầy</h2>

                <a class="counter-home-button"
                   href="${ctx}/staff/ticket-sale">
                    Bán vé
                </a>
            </article>

            <article class="counter-home-card">
                <h2>Bắp nước/combo</h2>

                <a class="counter-home-button"
                   href="${ctx}/staff/food-sale">
                    Bán hàng
                </a>
            </article>

            <article class="counter-home-card">
                <h2>Check-in vé</h2>

                <a class="counter-home-button"
                   href="${ctx}/staff/check-in">
                    Check-in
                </a>
            </article>

        </div>

    </section>

</c:if>

<c:if test="${activeCounterTab ne 'home'}">

    <section class="counter-report-panel">

        <h2>Báo cáo bán hàng của tôi</h2>

        <p class="counter-report-note">
            Chỉ tính đơn đã hoàn tất do bạn bán. Lọc theo ngày bán.
        </p>

        <form class="counter-report-filter"
              method="get"
              action="${ctx}/staff/counter">

            <input type="hidden"
                   name="tab"
                   value="${activeCounterTab}">

            <div class="counter-report-field">
                <label for="counter-from-date">Từ ngày</label>

                <input id="counter-from-date"
                       type="date"
                       name="fromDate"
                       value="${fromDate}"
                       required>
            </div>

            <div class="counter-report-field">
                <label for="counter-to-date">Đến ngày</label>

                <input id="counter-to-date"
                       type="date"
                       name="toDate"
                       value="${toDate}"
                       required>
            </div>

            <button class="counter-filter-button" type="submit">
                Lọc
            </button>

            <a class="counter-reset-link"
               href="${counterResetUrl}">
                Tháng này
            </a>

        </form>

        <c:if test="${not empty reportError}">
            <div class="counter-report-error" role="alert">
                <c:out value="${reportError}" />
            </div>
        </c:if>

    </section>

    <c:if test="${not empty staffReport}">

        <c:if test="${activeCounterTab eq 'statistics'}">

            <div class="counter-stat-grid">

                <article class="counter-stat-card">
                    <p>Vé đã bán</p>

                    <strong>
                        <fmt:formatNumber
                            value="${staffReport.totalTickets}" />
                    </strong>

                    <small>
                        <fmt:formatNumber
                            value="${staffReport.totalTicketOrders}" />
                        đơn vé
                    </small>
                </article>

                <article class="counter-stat-card">
                    <p>Doanh thu vé</p>

                    <strong>
                        <fmt:formatNumber
                            value="${staffReport.ticketRevenue}"
                            maxFractionDigits="0" />
                        đ
                    </strong>
                </article>

                <article class="counter-stat-card">
                    <p>Đơn bắp nước/combo</p>

                    <strong>
                        <fmt:formatNumber
                            value="${staffReport.totalFoodOrders}" />
                    </strong>

                    <small>
                        <fmt:formatNumber
                            value="${staffReport.totalFoodQuantity}" />
                        món/combo đã bán
                    </small>
                </article>

                <article class="counter-stat-card">
                    <p>Doanh thu bắp nước/combo</p>

                    <strong>
                        <fmt:formatNumber
                            value="${staffReport.foodRevenue}"
                            maxFractionDigits="0" />
                        đ
                    </strong>
                </article>

            </div>

            <div class="counter-total-revenue">
                Tổng doanh thu của tôi trong khoảng ngày đã chọn:

                <strong>
                    <fmt:formatNumber
                        value="${staffReport.totalRevenue}"
                        maxFractionDigits="0" />
                    đ
                </strong>
            </div>

        </c:if>

        <c:if test="${activeCounterTab eq 'tickets'}">

            <section class="counter-report-panel">

                <h2>Lịch sử vé do tôi bán</h2>

                <div class="counter-table-wrap">
                    <table class="counter-sales-table">

                        <thead>
                            <tr>
                                <th>Đơn vé</th>
                                <th>Ngày bán</th>
                                <th>Phim / Suất chiếu</th>
                                <th>Phòng</th>
                                <th>Số vé</th>
                                <th>Tổng tiền</th>
                                <th>Chi tiết</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="sale"
                                       items="${staffReport.ticketSales}">

                                <tr>
                                    <td>
                                        #<c:out value="${sale.bookingId}" />
                                    </td>

                                    <td>
                                        <fmt:formatDate
                                            value="${sale.soldAt}"
                                            pattern="dd/MM/yyyy HH:mm" />
                                    </td>

                                    <td>
                                        <c:out value="${sale.movieTitle}" />

                                        <small>
                                            <fmt:formatDate
                                                value="${sale.showtimeTime}"
                                                pattern="dd/MM/yyyy HH:mm" />
                                        </small>
                                    </td>

                                    <td>
                                        <c:out value="${sale.roomName}" />
                                    </td>

                                    <td>
                                        <c:out value="${sale.ticketCount}" />
                                    </td>

                                    <td class="counter-money">
                                        <fmt:formatNumber
                                            value="${sale.totalAmount}"
                                            maxFractionDigits="0" />
                                        đ
                                    </td>

                                    <td>
                                        <a href="${ctx}/staff/ticket-sale?bookingId=${sale.bookingId}">
                                            Xem vé
                                        </a>
                                    </td>
                                </tr>

                            </c:forEach>

                            <c:if test="${empty staffReport.ticketSales}">
                                <tr>
                                    <td class="counter-empty" colspan="7">
                                        Bạn chưa có đơn vé hoàn tất
                                        trong khoảng ngày này.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>

                    </table>
                </div>

            </section>

        </c:if>

        <c:if test="${activeCounterTab eq 'food'}">

            <section class="counter-report-panel">

                <h2>Lịch sử bắp nước/combo do tôi bán</h2>

                <div class="counter-table-wrap">
                    <table class="counter-sales-table">

                        <thead>
                            <tr>
                                <th>Đơn hàng</th>
                                <th>Ngày bán</th>
                                <th>SL món/combo</th>
                                <th>Thanh toán</th>
                                <th>Tổng tiền</th>
                                <th>Chi tiết</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="sale"
                                       items="${staffReport.foodSales}">

                                <tr>
                                    <td>
                                        #<c:out value="${sale.orderId}" />
                                    </td>

                                    <td>
                                        <fmt:formatDate
                                            value="${sale.soldAt}"
                                            pattern="dd/MM/yyyy HH:mm" />
                                    </td>

                                    <td>
                                        <c:out value="${sale.quantity}" />
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${sale.paymentMethod eq 'CASH'}">
                                                Tiền mặt
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${sale.paymentMethod}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td class="counter-money">
                                        <fmt:formatNumber
                                            value="${sale.totalAmount}"
                                            maxFractionDigits="0" />
                                        đ
                                    </td>

                                    <td>
                                        <a href="${ctx}/staff/food-sale?orderId=${sale.orderId}">
                                            Xem hóa đơn
                                        </a>
                                    </td>
                                </tr>

                            </c:forEach>

                            <c:if test="${empty staffReport.foodSales}">
                                <tr>
                                    <td class="counter-empty" colspan="6">
                                        Bạn chưa có đơn bắp nước/combo hoàn tất
                                        trong khoảng ngày này.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>

                    </table>
                </div>

            </section>

        </c:if>

    </c:if>

</c:if>

<%@ include file="/WEB-INF/include/footer.jsp" %>