<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />



<section class="booking-page uc22-checkout" data-css-page="user-checkout">

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
                    Đặt toàn phòng được giảm 10% trên tổng tiền vé, combo và đồ ăn.
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
                                    <input class="quantity-value" type="number" name="comboQuantity" form="checkoutForm" data-unit-price="${combo.comboPrice}" data-total-group="combo" min="0" max="20" value="${selectedComboQuantities[combo.comboId] != null ? selectedComboQuantities[combo.comboId] : 0}" aria-label="Số lượng combo">
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
                                <c:if test="${not empty food.imageUrl}"><c:set var="foodImagePath" value="${fn:trim(food.imageUrl)}" /><c:choose><c:when test="${fn:startsWith(foodImagePath, 'http://') or fn:startsWith(foodImagePath, 'https://') or fn:startsWith(foodImagePath, '/') }"><c:url var="foodImageSrc" value="${foodImagePath}" /></c:when><c:otherwise><c:url var="foodImageSrc" value="/${foodImagePath}" /></c:otherwise></c:choose><img src="${foodImageSrc}" alt="Sản phẩm"></c:if>
                                <div class="combo-info"><strong><c:out value="${food.itemName}" /></strong><small><c:out value="${food.description}" /></small><strong class="combo-price"><fmt:formatNumber value="${food.price}" pattern="#,##0.##" /> VNĐ</strong></div>
                                <input type="hidden" name="foodItemId" value="${food.foodItemId}" form="checkoutForm">
                                <div class="quantity-control"><button class="quantity-btn" type="button" data-quantity-action="minus" aria-label="Giảm số lượng">−</button><input class="quantity-value" type="number" name="foodQuantity" form="checkoutForm" data-unit-price="${food.price}" data-total-group="food" min="0" max="20" value="${selectedFoodQuantities[food.foodItemId] != null ? selectedFoodQuantities[food.foodItemId] : 0}" aria-label="Số lượng đồ ăn"><button class="quantity-btn" type="button" data-quantity-action="plus" aria-label="Tăng số lượng">+</button></div>
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
                                <c:if test="${not empty food.imageUrl}"><c:set var="foodImagePath" value="${fn:trim(food.imageUrl)}" /><c:choose><c:when test="${fn:startsWith(foodImagePath, 'http://') or fn:startsWith(foodImagePath, 'https://') or fn:startsWith(foodImagePath, '/') }"><c:url var="foodImageSrc" value="${foodImagePath}" /></c:when><c:otherwise><c:url var="foodImageSrc" value="/${foodImagePath}" /></c:otherwise></c:choose><img src="${foodImageSrc}" alt="Sản phẩm"></c:if>
                                <div class="combo-info"><strong><c:out value="${food.itemName}" /></strong><small><c:out value="${food.description}" /></small><strong class="combo-price"><fmt:formatNumber value="${food.price}" pattern="#,##0.##" /> VNĐ</strong></div>
                                <input type="hidden" name="foodItemId" value="${food.foodItemId}" form="checkoutForm">
                                <div class="quantity-control"><button class="quantity-btn" type="button" data-quantity-action="minus" aria-label="Giảm số lượng">−</button><input class="quantity-value" type="number" name="foodQuantity" form="checkoutForm" data-unit-price="${food.price}" data-total-group="food" min="0" max="20" value="${selectedFoodQuantities[food.foodItemId] != null ? selectedFoodQuantities[food.foodItemId] : 0}" aria-label="Số lượng nước uống"><button class="quantity-btn" type="button" data-quantity-action="plus" aria-label="Tăng số lượng">+</button></div>
                            </div>
                        </c:if>
                    </c:forEach>
                    <c:if test="${not hasDrinkItems}"><p class="menu-empty">Hiện chưa có nước uống riêng tại rạp này.</p></c:if>
                </div>
            </div>

        </div>

        <div class="checkout-summary">

            <h2>Thông tin đặt vé</h2>

            <c:if test="${not empty cinema}">
                <div class="summary-row">
                    <span>Rạp chiếu</span>
                    <strong><c:out value="${cinema.cinemaName}" /></strong>
                </div>
                <c:if test="${not empty cinema.address}">
                    <div class="summary-row">
                        <span>Địa chỉ rạp</span>
                        <strong><c:out value="${cinema.address}" /></strong>
                    </div>
                </c:if>
            </c:if>

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

                    <div class="available-promo-list">
                        <strong>Voucher khả dụng cho đơn hàng này</strong>
                        <c:choose>
                            <c:when test="${not empty availablePromotions}">
                                <c:forEach var="availablePromo" items="${availablePromotions}">
                                    <div class="available-promo-item">
                                        <div class="available-promo-top">
                                            <span class="available-promo-code">
                                                <c:out value="${availablePromo.promoCode}" />
                                            </span>
                                            <c:choose>
                                                <c:when test="${availablePromo.discountType eq 'PERCENT'}">
                                                    <strong>Giảm <fmt:formatNumber value="${availablePromo.discountValue}" pattern="#,##0.##" />%</strong>
                                                </c:when>
                                                <c:otherwise>
                                                    <strong>Giảm <fmt:formatNumber value="${availablePromo.discountValue}" pattern="#,##0.##" /> VNĐ</strong>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <c:if test="${not empty availablePromo.description}">
                                            <span class="available-promo-detail"><c:out value="${availablePromo.description}" /></span>
                                        </c:if>
                                        <span class="available-promo-detail">
                                            Đơn tối thiểu:
                                            <fmt:formatNumber value="${availablePromo.minOrderAmount}" pattern="#,##0.##" /> VNĐ
                                            <c:if test="${not empty availablePromo.maxDiscountAmount}">
                                                · Giảm tối đa:
                                                <fmt:formatNumber value="${availablePromo.maxDiscountAmount}" pattern="#,##0.##" /> VNĐ
                                            </c:if>
                                        </span>
                                        <span class="available-promo-detail">
                                            Hạn dùng đến:
                                            <fmt:formatDate value="${availablePromo.endDate}" pattern="dd/MM/yyyy HH:mm" />
                                            
                                        </span>
                                        <button type="submit"
                                                name="action"
                                                value="APPLY_PROMO"
                                                class="promo-button"
                                                data-select-promo="${availablePromo.promoCode}">
                                            Chọn voucher
                                        </button>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="available-promo-empty">
                                    Hiện không có voucher nào đủ điều kiện áp dụng cho đơn hàng này.
                                </p>
                            </c:otherwise>
                        </c:choose>
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
                    <p id="promoMinimumHint" class="promo-message promo-error"
                       hidden>Mã khuyến mãi không còn đạt giá trị đơn tối thiểu.
                       Hãy chỉnh lại sản phẩm hoặc bỏ mã.</p>

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

                <c:if test="${fullRoom}">
                    <div class="summary-row">
                        <span>Giảm đặt toàn phòng (10%)</span>
                        <strong class="discount-value" id="fullRoomDiscountDisplay">
                            - <fmt:formatNumber value="${fullRoomDiscount}" pattern="#,##0.##" /> VNĐ
                        </strong>
                    </div>
                </c:if>

                <div class="summary-row">
                    <span>Giảm từ mã khuyến mãi</span>
                    <strong class="discount-value" id="discountDisplay">
                        - <fmt:formatNumber value="${voucherDiscount}" pattern="#,##0.##" /> VNĐ
                    </strong>
                </div>

                <div class="summary-row"><span>Combo bắp nước</span><strong id="comboTotalDisplay"><fmt:formatNumber value="${comboTotal}" pattern="#,##0.##" /> VNĐ</strong></div>
                <div class="summary-row"><span>Đồ ăn &amp; nước uống</span><strong id="foodTotalDisplay"><fmt:formatNumber value="${foodTotal}" pattern="#,##0.##" /> VNĐ</strong></div>
                <div class="summary-row summary-total">
                    <span>Tổng thanh toán</span>

                    <strong id="finalAmountDisplay">
                        <fmt:formatNumber value="${finalAmount}" pattern="#,##0.##" /> VNĐ
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


<c:set var="checkoutAgeRating" value="${fn:toUpperCase(fn:trim(movie.ageRating))}" />



<div class="checkout-age-overlay" id="checkoutAgeModal" aria-hidden="true">
    <div class="checkout-age-dialog" role="dialog" aria-modal="true"
         aria-labelledby="checkoutAgeTitle" aria-describedby="checkoutAgeMessage">
        <h2 id="checkoutAgeTitle">Xác nhận thông tin trước khi thanh toán</h2>
        <div id="checkoutAgeMessage">
            <strong>1. Quy định độ tuổi của phim</strong>
            <p>
                <c:choose>
                    <c:when test="${checkoutAgeRating eq 'P'}">
                        Phim được phân loại P – phù hợp với mọi độ tuổi. Khán giả ở mọi lứa tuổi đều có thể xem phim.
                    </c:when>
                    <c:when test="${checkoutAgeRating eq 'T13'}">
                        Phim được phân loại T13 – chỉ dành cho khán giả từ đủ 13 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 13 tuổi sẽ không được vào xem.
                    </c:when>
                    <c:when test="${checkoutAgeRating eq 'T16'}">
                        Phim được phân loại T16 – chỉ dành cho khán giả từ đủ 16 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 16 tuổi sẽ không được vào xem.
                    </c:when>
                    <c:when test="${checkoutAgeRating eq 'T18'}">
                        Phim được phân loại T18 – chỉ dành cho khán giả từ đủ 18 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 18 tuổi sẽ không được vào xem.
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${not empty movie.ageRating}">
                                Phim có phân loại ${movie.ageRating}. Vui lòng kiểm tra điều kiện độ tuổi và liên hệ nhân viên F-Cinema nếu cần được giải đáp trước khi thanh toán.
                            </c:when>
                            <c:otherwise>
                                Phim hiện chưa có thông tin phân loại độ tuổi. Vui lòng liên hệ nhân viên F-Cinema để xác nhận trước khi thanh toán.
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                </c:choose>
            </p>
            <strong>2. Chính sách hủy và hoàn tiền</strong>
            <p>
                Sau khi thanh toán thành công, đơn đặt vé không được hủy và không được hoàn tiền.
                Vui lòng kiểm tra kỹ phim, ngày giờ chiếu, rạp, ghế, combo, đồ ăn và tổng tiền trước khi xác nhận.
                F-Cinema sẽ không chịu trách nhiệm trong trường hợp người xem không đáp ứng điều kiện độ tuổi của phim.
            </p>
        </div>
        <div class="checkout-age-actions">
            <button type="button" class="checkout-age-cancel" id="cancelCheckoutAge">Hủy</button>
            <button type="button" class="checkout-age-agree" id="agreeCheckoutAge">Đồng ý</button>
        </div>
    </div>
</div>

<script>
(function () {
    var form = document.getElementById("checkoutForm");
    if (!form) return;

    // Chỉ yêu cầu xác nhận khi người dùng thực sự xác nhận đặt vé.
    var confirmButton = form.querySelector('button[name="action"][value="CONFIRM"]');
    var ageModal = document.getElementById("checkoutAgeModal");
    var cancelAgeButton = document.getElementById("cancelCheckoutAge");
    var agreeAgeButton = document.getElementById("agreeCheckoutAge");
    var ageConfirmed = false;

    function closeAgeModal() {
        ageModal.classList.remove("is-visible");
        ageModal.setAttribute("aria-hidden", "true");
        if (confirmButton) confirmButton.focus();
    }

    form.addEventListener("click", function (event) {
        var voucherButton = event.target.closest("[data-select-promo]");
        if (!voucherButton) return;
        var promoInput = document.getElementById("promoCode");
        if (promoInput) {
            promoInput.value = voucherButton.dataset.selectPromo || "";
        }
    });

    form.addEventListener("submit", function (event) {
        var submitter = event.submitter;
        var isConfirmAction = !submitter
            || (submitter.name === "action" && submitter.value === "CONFIRM");

        // Áp dụng/bỏ mã khuyến mãi không cần hiện hộp thoại.
        if (!isConfirmAction || ageConfirmed) return;

        event.preventDefault();
        ageModal.classList.add("is-visible");
        ageModal.setAttribute("aria-hidden", "false");
        agreeAgeButton.focus();
    });

    cancelAgeButton.addEventListener("click", closeAgeModal);

    agreeAgeButton.addEventListener("click", function () {
        ageConfirmed = true;
        closeAgeModal();

        if (form.requestSubmit && confirmButton) {
            form.requestSubmit(confirmButton);
        } else {
            var actionInput = document.createElement("input");
            actionInput.type = "hidden";
            actionInput.name = "action";
            actionInput.value = "CONFIRM";
            form.appendChild(actionInput);
            form.submit();
        }
    });

    ageModal.addEventListener("click", function (event) {
        if (event.target === ageModal) closeAgeModal();
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && ageModal.classList.contains("is-visible")) {
            closeAgeModal();
        }
    });

    var moneyFormat = new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 2 });

    function roundMoney(value) {
        return Math.round((value + Number.EPSILON) * 100) / 100;
    }

    function updateCheckoutTotal() {
        var comboTotal = 0;
        var foodTotal = 0;

        document.querySelectorAll(".quantity-value[data-unit-price]").forEach(function (input) {
            var quantity = Math.max(0, Math.min(20, parseInt(input.value, 10) || 0));
            var lineTotal = (Number(input.dataset.unitPrice) || 0) * quantity;
            if (input.dataset.totalGroup === "combo") comboTotal += lineTotal;
            if (input.dataset.totalGroup === "food") foodTotal += lineTotal;
        });

        var comboDisplay = document.getElementById("comboTotalDisplay");
        var foodDisplay = document.getElementById("foodTotalDisplay");
        var finalDisplay = document.getElementById("finalAmountDisplay");
        var discountDisplay = document.getElementById("discountDisplay");
        var subtotal = Number("${subtotal}") || 0;
        var totalBeforeDiscount = subtotal + comboTotal + foodTotal;
        var fullRoomDiscount = ${fullRoom ? 'true' : 'false'}
                ? roundMoney(totalBeforeDiscount * 0.10) : 0;
        var amountAfterFullRoom = Math.max(0, totalBeforeDiscount - fullRoomDiscount);

        // Preview is recalculated whenever food quantities change.
        // The server validates the same price and voucher again on submit.
        var hasPromo = ${not empty appliedPromotion ? 'true' : 'false'};
        var promoType = "${appliedPromotion.discountType}";
        var promoValue = Number("${appliedPromotion.discountValue}") || 0;
        var promoMaxRaw = "${appliedPromotion.maxDiscountAmount}";
        var promoMinimum = Number("${appliedPromotion.minOrderAmount}") || 0;
        var discount = 0;
        var promoBelowMinimum = hasPromo && amountAfterFullRoom < promoMinimum;

        if (hasPromo && !promoBelowMinimum) {
            discount = promoType === "PERCENT"
                ? roundMoney(amountAfterFullRoom * promoValue / 100)
                : promoValue;
            if (promoMaxRaw !== "") {
                discount = Math.min(discount, Number(promoMaxRaw));
            }
            discount = Math.min(amountAfterFullRoom, Math.max(0, discount));
        }

        var fullRoomDisplay = document.getElementById("fullRoomDiscountDisplay");
        if (comboDisplay) comboDisplay.textContent = moneyFormat.format(comboTotal) + " VNĐ";
        if (foodDisplay) foodDisplay.textContent = moneyFormat.format(foodTotal) + " VNĐ";
        if (fullRoomDisplay) {
            fullRoomDisplay.textContent = "- " + moneyFormat.format(fullRoomDiscount) + " VNĐ";
        }
        if (discountDisplay) {
            discountDisplay.textContent = "- " + moneyFormat.format(discount) + " VNĐ";
        }
        if (finalDisplay) {
            finalDisplay.textContent = moneyFormat.format(
                Math.max(0, amountAfterFullRoom - discount)
            ) + " VNĐ";
        }
        var promoHint = document.getElementById("promoMinimumHint");
        if (promoHint) promoHint.hidden = !promoBelowMinimum;
        if (confirmButton) {
            confirmButton.disabled = ${not empty promoError ? 'true' : 'false'}
                    || promoBelowMinimum;
        }
    }

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
        updateCheckoutTotal();
    });

    document.addEventListener("input", function (event) {
        if (!event.target.classList.contains("quantity-value")) return;
        var value = parseInt(event.target.value, 10);
        if (isNaN(value)) value = 0;
        event.target.value = Math.max(0, Math.min(20, value));
        updateCheckoutTotal();
    });

    document.addEventListener("change", function (event) {
        if (!event.target.classList.contains("quantity-value")) return;
        var value = parseInt(event.target.value, 10);
        if (isNaN(value)) value = 0;
        event.target.value = Math.max(0, Math.min(20, value));
        updateCheckoutTotal();
    });

    updateCheckoutTotal();
})();
</script>
<%@ include file="/WEB-INF/include/footer.jsp" %>