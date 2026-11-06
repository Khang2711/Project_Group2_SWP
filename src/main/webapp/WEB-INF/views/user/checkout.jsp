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

    .uc22-checkout .discount-value { color: #6cdb92; }
    .uc22-checkout .menu-section { margin-top:24px; }
    .uc22-checkout .menu-section-heading { display:flex; align-items:center; justify-content:space-between; gap:10px; margin-bottom:8px; }
    .uc22-checkout .menu-section-heading h2 { margin:0; }
    .uc22-checkout .menu-hint { color:#aaa; font-size:13px; margin:5px 0 12px; }
    .uc22-checkout .combo-list { display:grid; grid-template-columns:repeat(auto-fit,minmax(250px,1fr)); gap:14px; margin-top:12px; }
    .uc22-checkout .combo-item { display:flex; align-items:center; gap:12px; padding:13px; border:1px solid #393943; border-radius:12px; background:linear-gradient(145deg,#1d1d23,#141418); min-width:0; transition:border-color .2s,transform .2s; }
    .uc22-checkout .combo-item:hover { border-color:#e50914; transform:translateY(-2px); }
    .uc22-checkout .combo-item img { width:88px; height:82px; flex-shrink:0; object-fit:cover; border-radius:9px; background:#292930; }
    .uc22-checkout .combo-info { flex:1; min-width:0; }
    .uc22-checkout .combo-info small { display:block; color:#aaa; margin:5px 0 8px; line-height:1.4; }
    .uc22-checkout .combo-price { display:block; color:#f5c451; white-space:nowrap; }
    .uc22-checkout .quantity-control { display:flex; align-items:center; gap:5px; flex-shrink:0; padding:4px; border:1px solid #41414b; border-radius:9px; background:#101014; }
    .uc22-checkout .quantity-btn { width:32px; height:32px; display:grid; place-items:center; border:0; border-radius:7px; background:#303039; color:#fff; font-size:20px; font-weight:700; line-height:1; cursor:pointer; transition:background .15s,transform .15s; }
    .uc22-checkout .quantity-btn:hover { background:#e50914; transform:scale(1.05); }
    .uc22-checkout .quantity-btn:active { transform:scale(.94); }
    .uc22-checkout .quantity-value { width:30px; padding:0; border:0; outline:0; background:transparent; color:#fff; text-align:center; font-weight:700; font-size:15px; -moz-appearance:textfield; }
    .uc22-checkout .quantity-value::-webkit-inner-spin-button,.uc22-checkout .quantity-value::-webkit-outer-spin-button { -webkit-appearance:none; margin:0; }
    .uc22-checkout .menu-empty { padding:16px; color:#aaa; border:1px dashed #484852; border-radius:10px; text-align:center; }

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

            <div class="menu-section">
                <div class="menu-section-heading">
                    <h2>🍿 Combo bắp nước</h2>
                    <span class="seat-count">Không bắt buộc</span>
                </div>
                <p class="menu-hint">Chọn combo tiện lợi để tiết kiệm hơn khi xem phim.</p>
                <div class="combo-list">
                    <c:choose><c:when test="${not empty checkoutCombos}">
                        <c:forEach var="combo" items="${checkoutCombos}">
                            <div class="combo-item">
                                <c:set var="comboImagePath" value="${fn:trim(combo.imageUrl)}" />
                                <c:if test="${not empty comboImagePath}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(comboImagePath, 'http://') or fn:startsWith(comboImagePath, 'https://') or fn:startsWith(comboImagePath, '/') }"><c:url var="comboImageSrc" value="${comboImagePath}" /></c:when>
                                        <c:otherwise><c:url var="comboImageSrc" value="/${comboImagePath}" /></c:otherwise>
                                    </c:choose>
                                    <img src="${comboImageSrc}" alt="Combo bắp nước">
                                </c:if>
                                <div class="combo-info"><strong><c:out value="${combo.comboName}" /></strong>
                                    <small><c:out value="${combo.description}" /></small>
                                    <strong class="combo-price"><fmt:formatNumber value="${combo.comboPrice}" pattern="#,##0.##" /> VNĐ</strong>
                                </div>
                                <input type="hidden" name="comboId" value="${combo.comboId}" form="checkoutForm">
                                <div class="quantity-control">
                                    <button class="quantity-btn" type="button" data-quantity-action="minus" aria-label="Giảm số lượng">−</button>
                                    <input class="quantity-value" type="number" name="comboQuantity" form="checkoutForm" min="0" max="20" value="${selectedComboQuantities[combo.comboId] != null ? selectedComboQuantities[combo.comboId] : 0}" aria-label="Số lượng combo">
                                    <button class="quantity-btn" type="button" data-quantity-action="plus" aria-label="Tăng số lượng">+</button>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when><c:otherwise><p class="menu-empty">Hiện chưa có combo khả dụng tại rạp này.</p></c:otherwise></c:choose>
                </div>
            </div>

            <div class="menu-section">
                <div class="menu-section-heading"><h2>🍟 Đồ ăn riêng</h2><span class="seat-count">Food</span></div>
                <p class="menu-hint">Chọn món ăn lẻ theo sở thích, không cần mua theo combo.</p>
                <div class="combo-list">
                    <c:set var="hasFoodItems" value="false" />
                    <c:forEach var="food" items="${checkoutFoods}">
                        <c:if test="${food.itemType eq 'FOOD'}">
                            <c:set var="hasFoodItems" value="true" />
                            <div class="combo-item">
                                <c:if test="${not empty food.imageUrl}"><c:url var="foodImageSrc" value="/${food.imageUrl}" /><img src="${foodImageSrc}" alt="Đồ ăn"></c:if>
                                <div class="combo-info"><strong><c:out value="${food.itemName}" /></strong><small><c:out value="${food.description}" /></small><strong class="combo-price"><fmt:formatNumber value="${food.price}" pattern="#,##0.##" /> VNĐ</strong></div>
                                <input type="hidden" name="foodItemId" value="${food.foodItemId}" form="checkoutForm">
                                <div class="quantity-control"><button class="quantity-btn" type="button" data-quantity-action="minus" aria-label="Giảm số lượng">−</button><input class="quantity-value" type="number" name="foodQuantity" form="checkoutForm" min="0" max="20" value="${selectedFoodQuantities[food.foodItemId] != null ? selectedFoodQuantities[food.foodItemId] : 0}" aria-label="Số lượng đồ ăn"><button class="quantity-btn" type="button" data-quantity-action="plus" aria-label="Tăng số lượng">+</button></div>
                            </div>
                        </c:if>
                    </c:forEach>
                    <c:if test="${not hasFoodItems}"><p class="menu-empty">Hiện chưa có món ăn riêng tại rạp này.</p></c:if>
                </div>
            </div>

            <div class="menu-section">
                <div class="menu-section-heading"><h2>🥤 Nước uống riêng</h2><span class="seat-count">Drink</span></div>
                <p class="menu-hint">Chọn nước uống riêng hoặc kết hợp cùng đồ ăn.</p>
                <div class="combo-list">
                    <c:set var="hasDrinkItems" value="false" />
                    <c:forEach var="food" items="${checkoutFoods}">
                        <c:if test="${food.itemType eq 'DRINK'}">
                            <c:set var="hasDrinkItems" value="true" />
                            <div class="combo-item">
                                <c:if test="${not empty food.imageUrl}"><c:url var="foodImageSrc" value="/${food.imageUrl}" /><img src="${foodImageSrc}" alt="Nước uống"></c:if>
                                <div class="combo-info"><strong><c:out value="${food.itemName}" /></strong><small><c:out value="${food.description}" /></small><strong class="combo-price"><fmt:formatNumber value="${food.price}" pattern="#,##0.##" /> VNĐ</strong></div>
                                <input type="hidden" name="foodItemId" value="${food.foodItemId}" form="checkoutForm">
                                <div class="quantity-control"><button class="quantity-btn" type="button" data-quantity-action="minus" aria-label="Giảm số lượng">−</button><input class="quantity-value" type="number" name="foodQuantity" form="checkoutForm" min="0" max="20" value="${selectedFoodQuantities[food.foodItemId] != null ? selectedFoodQuantities[food.foodItemId] : 0}" aria-label="Số lượng nước uống"><button class="quantity-btn" type="button" data-quantity-action="plus" aria-label="Tăng số lượng">+</button></div>
                            </div>
                        </c:if>
                    </c:forEach>
                    <c:if test="${not hasDrinkItems}"><p class="menu-empty">Hiện chưa có nước uống riêng tại rạp này.</p></c:if>
                </div>
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
                  id="checkoutForm" class="checkout-confirm-form">

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

                <div class="summary-row"><span>Combo bắp nước</span><strong><fmt:formatNumber value="${comboTotal}" pattern="#,##0.##" /> VNĐ</strong></div>
                <div class="summary-row"><span>Đồ ăn &amp; nước uống</span><strong><fmt:formatNumber value="${foodTotal}" pattern="#,##0.##" /> VNĐ</strong></div>
                <div class="summary-row summary-total">
                    <span>Tổng thanh toán</span>

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

<script>
document.addEventListener("click", function (event) {
    var button = event.target.closest("[data-quantity-action]");
    if (!button) return;
    var control = button.closest(".quantity-control");
    var input = control ? control.querySelector(".quantity-value") : null;
    if (!input) return;
    var current = parseInt(input.value, 10) || 0;
    input.value = button.dataset.quantityAction === "plus"
        ? Math.min(20, current + 1)
        : Math.max(0, current - 1);
});
document.addEventListener("change", function (event) {
    if (!event.target.classList.contains("quantity-value")) return;
    var value = parseInt(event.target.value, 10);
    if (isNaN(value)) value = 0;
    event.target.value = Math.max(0, Math.min(20, value));
});
</script>
<%@ include file="/WEB-INF/include/footer.jsp" %>