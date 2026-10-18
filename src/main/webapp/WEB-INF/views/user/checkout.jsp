<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<style>
    .uc22-checkout .promo-area {
        margin: 20px 0;
        padding: 16px;
        border: 1px solid #3a3a42;
        border-radius: 6px;
    }

    .uc22-checkout .promo-area label {
        display: block;
        margin-bottom: 10px;
        font-weight: bold;
    }

    .uc22-checkout .promo-input {
        width: 100%;
        box-sizing: border-box;
        padding: 11px;
        background: #101012;
        color: white;
        border: 1px solid #55555d;
        border-radius: 5px;
    }

    .uc22-checkout .promo-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-top: 12px;
    }

    .uc22-checkout .promo-button {
        padding: 9px 14px;
        border: 1px solid #666;
        border-radius: 5px;
        background: #29292f;
        color: white;
        cursor: pointer;
        font: inherit;
    }

    .uc22-checkout .promo-message {
        padding: 12px;
        margin: 14px 0;
        border-radius: 5px;
        line-height: 1.5;
    }

    .uc22-checkout .promo-error {
        background: #40191d;
        border: 1px solid #ff3045;
    }

    .uc22-checkout .promo-success {
        background: #16351a;
        border: 1px solid #4caf50;
    }

    .uc22-checkout .discount-value {
        color: #6cdb92;
    }

    .uc22-checkout .booking-primary-button:disabled {
        background: #55555f;
        border-color: #55555f;
        cursor: not-allowed;
    }
</style>

<section class="booking-page uc22-checkout">

    <div class="booking-heading">
        <h1>Xác nhận đặt vé</h1>
        <div class="title-line"></div>
        <p>Kiểm tra lại thông tin trước khi tạo đơn đặt vé.</p>
    </div>

    <c:if test="${not empty checkoutError}">
        <div class="promo-message promo-error">
            <c:out value="${checkoutError}" />
        </div>
    </c:if>

    <div class="checkout-layout">

        <div class="checkout-card">

            <div class="checkout-card-title">
                <h2>Ghế đã chọn</h2>

                <span class="seat-count">
                    ${fn:length(selectedSeats)} ghế
                </span>
            </div>

            <c:if test="${fullRoom}">
                <div class="booking-notice">
                    Bạn đang chọn hình thức đặt toàn phòng.
                </div>
            </c:if>

            <div class="checkout-seat-list">

                <c:forEach var="seat" items="${selectedSeats}">

                    <div class="checkout-seat-item">

                        <div class="checkout-seat-name">

                            <span class="seat-icon">
                                ${seat.seatRow}-${seat.seatNumber}
                            </span>

                            <div>
                                <strong>
                                    Hàng ${seat.seatRow}
                                    - Ghế ${seat.seatNumber}
                                </strong>

                                <small>
                                    <c:out value="${seat.seatType}" />
                                </small>
                            </div>

                        </div>

                        <strong class="checkout-seat-price">
                            <fmt:formatNumber
                                value="${seatPrices[seat.seatId]}"
                                pattern="#,##0.##" />
                            VNĐ
                        </strong>

                    </div>

                </c:forEach>

            </div>

        </div>

        <div class="checkout-summary">

            <h2>Thông tin đặt vé</h2>

            <div class="summary-row">
                <span>Suất chiếu</span>

                <strong>
                    #<c:out value="${showtime.showtimeId}" />
                </strong>
            </div>

            <div class="summary-row">
                <span>Thời gian</span>

                <strong>
                    <fmt:formatDate
                        value="${showtime.startTime}"
                        pattern="dd/MM/yyyy HH:mm" />
                </strong>
            </div>

            <div class="summary-row">
                <span>Số lượng ghế</span>

                <strong>
                    ${fn:length(selectedSeats)}
                </strong>
            </div>

            <form action="${ctx}/user/checkout"
                  method="post"
                  class="checkout-confirm-form">

                <input type="hidden"
                       name="showtimeId"
                       value="${showtime.showtimeId}">

                <input type="hidden"
                       name="bookingMode"
                       value="${fullRoom ? 'FULL_ROOM' : 'NORMAL'}">

                <c:forEach var="seat" items="${selectedSeats}">
                    <input type="hidden"
                           name="seatId"
                           value="${seat.seatId}">
                </c:forEach>

                <div class="promo-area">

                    <label for="promoCode">
                        Mã khuyến mãi
                    </label>

                    <input type="text"
                           id="promoCode"
                           name="promoCode"
                           class="promo-input"
                           maxlength="50"
                           value="<c:out value='${promoCode}' />"
                           placeholder="Nhập mã nếu có">

                    <div class="promo-buttons">

                        <button type="submit"
                                name="action"
                                value="APPLY_PROMO"
                                class="promo-button">
                            Áp dụng mã
                        </button>

                        <button type="submit"
                                name="action"
                                value="REMOVE_PROMO"
                                class="promo-button">
                            Bỏ mã
                        </button>

                    </div>

                    <c:if test="${not empty promoError
                                  and empty checkoutError}">
                          <div class="promo-message promo-error">
                              <c:out value="${promoError}" />
                          </div>
                    </c:if>

                    <c:if test="${not empty promoSuccess}">
                        <div class="promo-message promo-success">
                            <c:out value="${promoSuccess}" />
                        </div>
                    </c:if>

                </div>

                <div class="summary-divider"></div>

                <div class="summary-row">
                    <span>Tiền vé</span>

                    <strong>
                        <fmt:formatNumber
                            value="${subtotal}"
                            pattern="#,##0.##" />
                        VNĐ
                    </strong>
                </div>

                <div class="summary-row">
                    <span>Giảm giá</span>

                    <strong class="discount-value">
                        - <fmt:formatNumber
                            value="${discountAmount}"
                            pattern="#,##0.##" />
                        VNĐ
                    </strong>
                </div>

                <div class="summary-row summary-total">
                    <span>Tổng sau giảm</span>

                    <strong>
                        <fmt:formatNumber
                            value="${finalAmount}"
                            pattern="#,##0.##" />
                        VNĐ
                    </strong>
                </div>

                <button type="submit"
                        name="action"
                        value="CONFIRM"
                        class="booking-primary-button"
                        ${not empty promoError ? 'disabled' : ''}>
                    Xác nhận đặt vé
                </button>

            </form>

            <a class="booking-secondary-button"
               href="${ctx}/user/select-seats?showtimeId=${showtime.showtimeId}">
                Quay lại chọn ghế
            </a>

            <p class="checkout-note">
                Sau khi xác nhận, hệ thống tạo đơn đặt vé chờ thanh toán.
            </p>

        </div>

    </div>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>