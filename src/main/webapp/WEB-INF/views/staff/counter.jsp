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
        max-width: 1120px;
        margin: 0 auto;
        padding: 18px 0 42px;
        color: #eeeeee;
    }

    .counter-home-heading {
        margin-bottom: 30px;
        padding: 4px 0 22px 18px;
        border-bottom: 1px solid #303038;
        border-left: 5px solid #ff243e;
    }

    .counter-home-heading h1 {
        margin: 0;
        color: #ffffff;
        font-size: 30px;
        line-height: 1.25;
    }

    .counter-home-heading p {
        max-width: 620px;
        margin: 10px 0 0;
        color: #d0d0d8;
        font-size: 15px;
        font-weight: 500;
        line-height: 1.7;
    }

    .counter-home-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 20px;
    }

    .counter-home-card {
        display: flex;
        flex-direction: column;
        min-width: 0;
        min-height: 250px;
        padding: 25px 26px 24px;
        overflow: hidden;
        background: linear-gradient(145deg, #25252c 0%, #1d1d23 100%);
        border: 1px solid #41414b;
        border-radius: 12px;
        box-shadow: 0 8px 22px rgba(0, 0, 0, 0.16);
        transition: transform 0.2s ease, border-color 0.2s ease,
                    box-shadow 0.2s ease;
    }

    .counter-home-card:hover {
        transform: translateY(-4px);
        border-color: #a92d3d;
        box-shadow: 0 14px 30px rgba(0, 0, 0, 0.28);
    }

    .counter-home-card-top {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        margin-bottom: 18px;
    }

    .counter-home-index {
        color: #92929d;
        font-size: 12px;
        font-weight: 600;
        letter-spacing: 1px;
        opacity: 0.4;
    }

    .counter-home-card h2 {
        margin: 0 0 10px;
        color: #ffffff;
        font-size: 20px;
        font-weight: 700;
        line-height: 1.35;
    }

    .counter-home-card p {
        max-width: 300px;
        margin: 0;
        color: #d0d0d8;
        font-size: 14px;
        font-weight: 500;
        line-height: 1.75;
    }

    .counter-home-button,
    .counter-filter-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        background: #e50914;
        border: 1px solid #e50914;
        border-radius: 6px;
        color: #ffffff;
        font-weight: 700;
        text-align: center;
        text-decoration: none;
        cursor: pointer;
        transition: background 0.18s ease, border-color 0.18s ease;
    }

    .counter-home-button {
        width: 86%;
        box-sizing: border-box;
        margin: 24px auto 0;
        padding: 12px 16px;
        font-size: 14px;
    }

    .counter-home-button span {
        font-size: 18px;
        line-height: 1;
        transition: transform 0.18s ease;
    }

    .counter-home-button:hover span {
        transform: translateX(4px);
    }

    .counter-filter-button {
        padding: 11px 16px;
        font-size: 15px;
    }

    .counter-home-button:hover,
    .counter-filter-button:hover {
        background: #ff243e;
        border-color: #ff243e;
        color: #ffffff;
    }

    .counter-home-button:focus-visible,
    .counter-filter-button:focus-visible {
        outline: 2px solid #ffffff;
        outline-offset: 3px;
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

    @media (max-width: 950px) {
        .counter-home-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }

        .counter-home-card:last-child {
            grid-column: 1 / -1;
        }
    }

    @media (max-width: 650px) {
        .counter-home {
            padding-top: 8px;
        }

        .counter-home-grid {
            grid-template-columns: 1fr;
            gap: 14px;
        }

        .counter-home-card:last-child {
            grid-column: auto;
        }

        .counter-home-card {
            min-height: 230px;
        }
    }

    @media (max-width: 500px) {
        .counter-home-heading h1 {
            font-size: 25px;
        }

        .counter-home-heading {
            margin-bottom: 22px;
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
            <p>Chọn một tác vụ bên dưới để bắt đầu phục vụ khách hàng.</p>
        </div>

        <div class="counter-home-grid">

            <article class="counter-home-card">
                <div class="counter-home-card-top">
                    <span class="counter-home-index">01</span>
                </div>

                <div class="counter-home-card-content">
                    <h2>Bán vé tại quầy</h2>
                    <p>Chọn phim, suất chiếu và ghế ngồi để tạo đơn vé cho khách.</p>
                </div>

                <a class="counter-home-button"
                   href="${ctx}/staff/ticket-sale">
                    Bắt đầu bán vé <span aria-hidden="true">→</span>
                </a>
            </article>

            <article class="counter-home-card">
                <div class="counter-home-card-top">
                    <span class="counter-home-index">02</span>
                </div>

                <div class="counter-home-card-content">
                    <h2>Bắp nước / combo</h2>
                    <p>Tiếp nhận món lẻ hoặc combo và xử lý thanh toán tại quầy.</p>
                </div>

                <a class="counter-home-button"
                   href="${ctx}/staff/food-sale">
                    Tạo đơn hàng <span aria-hidden="true">→</span>
                </a>
            </article>

            <article class="counter-home-card">
                <div class="counter-home-card-top">
                    <span class="counter-home-index">03</span>
                </div>

                <div class="counter-home-card-content">
                    <h2>Check-in vé</h2>
                    <p>Tra cứu mã vé hoặc quét QR để xác nhận vé của khách.</p>
                </div>

                <a class="counter-home-button"
                   href="${ctx}/staff/check-in">
                    Kiểm tra vé <span aria-hidden="true">→</span>
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