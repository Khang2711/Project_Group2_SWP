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



<c:if test="${activeCounterTab eq 'home'}">

    <section class="counter-home" data-css-page="staff-counter">

        <div class="counter-home-heading">
            <h1>Bán hàng tại quầy</h1>

            <c:if test="${not empty staffCinema}">
                <p class="counter-home-branch">
                    <c:out value="${staffCinema.cinemaName}" />
                </p>
            </c:if>
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

        <h2>Báo cáo bán hàng</h2>

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
                Tổng doanh thu trong khoảng ngày đã chọn:

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

                <h2>Lịch sử vé</h2>

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

                <h2>Lịch sử bắp nước/combo</h2>

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