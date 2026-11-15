<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<c:set var="receiptMode"
       value="${not isCounterDraftSale
                and (param.orderId ne null or not empty completedOrder)}" />



<div class="food-sale-page${receiptMode ? ' receipt-page' : ''}" data-css-page="staff-food-sale">

    <c:choose>
        <c:when test="${receiptMode}">
            <a class="back-link"
               href="${ctx}/staff/counter?tab=food">
                ← Quay lại lịch sử bắp nước/combo
            </a>
            <h1>HÓA ĐƠN BẮP NƯỚC / COMBO</h1>
        </c:when>

        <c:when test="${isCounterDraftSale}">
            <a class="back-link"
               href="${ctx}/staff/ticket-sale?step=extras">
                ← Quay lại đơn vé
            </a>
            <h1>CHỌN BẮP NƯỚC KÈM VÉ</h1>
        </c:when>

        <c:otherwise>
            <a class="back-link" href="${ctx}/staff/counter">
                ← Quay lại quầy
            </a>
            <h1>BÁN BẮP NƯỚC / COMBO</h1>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
        <div class="message message-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message message-success">
            <c:out value="${success}" />
        </div>
    </c:if>

    <c:if test="${isCounterDraftSale and canSellFood}">
        <section class="sale-section">
            <h2>Đơn vé đang chuẩn bị</h2>

            <p>
                Phim:
                <strong><c:out value="${draftMovie.title}" /></strong>
            </p>

            <p>
                Suất:
                <fmt:formatDate value="${draftShowtime.startTime}"
                                pattern="dd/MM/yyyy HH:mm" />

                · Phòng:
                <c:out value="${draftRoom.roomName}" />
            </p>

            <p>
                Khách hàng:
                <c:out value="${draftEmail}" />
            </p>

            <p>
                Tiền vé:
                <fmt:formatNumber value="${draftTicketTotal}"
                                  pattern="#,##0" /> đ
            </p>

            <p>Chọn món rồi tiếp tục để xem tổng tiền vé và bắp nước.</p>

            <form method="post" action="${ctx}/staff/ticket-sale">
                <input type="hidden" name="action" value="skipFood" />
                <input type="hidden"
                       name="saleToken"
                       value="${sessionScope.counterSaleToken}" />

                <button type="submit" class="sale-button">
                    Không mua bắp nước, tiếp tục
                </button>
            </form>
        </section>
    </c:if>

    <c:if test="${not empty linkedBooking}">
        <section class="sale-section">
            <h2>
                Đơn vé liên quan
                #<c:out value="${linkedBooking.bookingId}" />
            </h2>

            <p>
                <c:out value="${linkedMovie.title}" />
                ·
                <fmt:formatDate value="${linkedShowtime.startTime}"
                                pattern="dd/MM/yyyy HH:mm" />
            </p>

            <a href="${ctx}/staff/ticket-sale?bookingId=${linkedBooking.bookingId}#purchaseSummary">
                Xem vé và tổng thanh toán
            </a>
        </section>
    </c:if>

    <c:if test="${not receiptMode}">
        <section class="sale-section">
            <h2>1. Chọn sản phẩm</h2>
            <h3>Món ăn / Nước uống</h3>

            <c:choose>
                <c:when test="${not empty foodItems}">
                    <div class="product-list">
                        <c:forEach var="item" items="${foodItems}">
                            <div class="product-card">
                                <div class="product-image">
                                    <span>Chưa có ảnh</span>

                                    <c:set var="imagePath"
                                           value="${fn:trim(item.imageUrl)}" />

                                    <c:if test="${not empty imagePath}">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(imagePath, 'http://')
                                                            or fn:startsWith(imagePath, 'https://')
                                                            or fn:startsWith(imagePath, '/')}">
                                                <c:url var="imageSrc"
                                                       value="${imagePath}" />
                                            </c:when>

                                            <c:otherwise>
                                                <c:url var="imageSrc"
                                                       value="/${imagePath}" />
                                            </c:otherwise>
                                        </c:choose>

                                        <img src="<c:out value='${imageSrc}' />"
                                             alt="<c:out value='${item.itemName}' />"
                                             loading="lazy"
                                             onerror="this.style.display='none';" />
                                    </c:if>
                                </div>

                                <strong class="product-name">
                                    <c:out value="${item.itemName}" />
                                </strong>

                                <p class="product-description">
                                    <c:out value="${item.description}" />
                                </p>

                                <p class="product-price">
                                    <fmt:formatNumber value="${item.price}"
                                                      pattern="#,##0" /> đ
                                </p>

                                <button type="button"
                                        class="sale-button"
                                        data-type="FOOD"
                                        data-id="${item.foodItemId}"
                                        data-name="<c:out value='${item.itemName}' />"
                                        data-price="${item.price}"
                                        onclick="addItem(this)"
                                        ${canSellFood ? '' : 'disabled'}>
                                    + Thêm vào đơn
                                </button>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <c:otherwise>
                    <p class="empty-text">
                        Chưa có món ăn, nước uống đang bán tại rạp.
                    </p>
                </c:otherwise>
            </c:choose>

            <h3>Combo</h3>

            <c:choose>
                <c:when test="${not empty combos}">
                    <div class="product-list">
                        <c:forEach var="combo" items="${combos}">
                            <div class="product-card">
                                <div class="product-image">
                                    <span>Chưa có ảnh</span>

                                    <c:set var="imagePath"
                                           value="${fn:trim(combo.imageUrl)}" />

                                    <c:if test="${not empty imagePath}">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(imagePath, 'http://')
                                                            or fn:startsWith(imagePath, 'https://')
                                                            or fn:startsWith(imagePath, '/')}">
                                                <c:url var="imageSrc"
                                                       value="${imagePath}" />
                                            </c:when>

                                            <c:otherwise>
                                                <c:url var="imageSrc"
                                                       value="/${imagePath}" />
                                            </c:otherwise>
                                        </c:choose>

                                        <img src="<c:out value='${imageSrc}' />"
                                             alt="<c:out value='${combo.comboName}' />"
                                             loading="lazy"
                                             onerror="this.style.display='none';" />
                                    </c:if>
                                </div>

                                <strong class="product-name">
                                    <c:out value="${combo.comboName}" />
                                </strong>

                                <p class="product-description">
                                    <c:out value="${combo.description}" />
                                </p>

                                <p class="product-price">
                                    <fmt:formatNumber value="${combo.comboPrice}"
                                                      pattern="#,##0" /> đ
                                </p>

                                <button type="button"
                                        class="sale-button"
                                        data-type="COMBO"
                                        data-id="${combo.comboId}"
                                        data-name="<c:out value='${combo.comboName}' />"
                                        data-price="${combo.comboPrice}"
                                        onclick="addItem(this)"
                                        ${canSellFood ? '' : 'disabled'}>
                                    + Thêm vào đơn
                                </button>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <c:otherwise>
                    <p class="empty-text">
                        Chưa có combo đang bán tại rạp.
                    </p>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="sale-section">
            <h2>2. Đơn hàng tại quầy</h2>

            <form id="foodOrderForm"
                  data-draft="${isCounterDraftSale eq true}"
                  data-can-sell="${canSellFood eq true}"
                  action="${ctx}/staff/food-sale"
                  method="post"
                  onsubmit="return checkOrder();">

                <c:choose>
                    <c:when test="${isCounterDraftSale}">
                        <input type="hidden"
                               name="action"
                               value="counterDraft" />

                        <input type="hidden"
                               name="saleToken"
                               value="${sessionScope.counterSaleToken}" />
                    </c:when>

                    <c:otherwise>
                        <input type="hidden"
                               name="saleToken"
                               value="${sessionScope.foodSaleToken}" />

                        <input type="hidden"
                               name="paymentMethod"
                               value="CASH" />

                        <c:if test="${not empty linkedBooking}">
                            <input type="hidden"
                                   name="bookingId"
                                   value="${linkedBooking.bookingId}" />
                        </c:if>
                    </c:otherwise>
                </c:choose>

                <div id="hiddenItems"></div>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Số lượng</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>

                        <tbody id="cartBody">
                            <tr>
                                <td colspan="5" class="empty-text">
                                    Chưa có sản phẩm trong đơn hàng.
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <p class="total-row">
                    Tạm tính bắp nước:
                    <strong id="totalText">0 đ</strong>
                </p>

                <%-- Mua kèm vé thì xác nhận thu tiền ở trang tổng đơn. --%>
                <c:if test="${not isCounterDraftSale}">
                    <p>
                        Phương thức thanh toán:
                        <strong>Tiền mặt</strong>
                    </p>

                    <label class="cash-confirm">
                        <input type="checkbox"
                               id="cashReceived"
                               name="cashReceived"
                               value="yes"
                               required
                               autocomplete="off" />

                        Tôi xác nhận đã thu đủ tiền mặt của khách.
                    </label>
                </c:if>

                <button type="submit"
                        id="payButton"
                        class="pay-button"
                        disabled>
                    ${isCounterDraftSale ? 'Tiếp tục thanh toán' : 'Xác nhận thanh toán'}
                </button>
            </form>
        </section>
    </c:if>

    <c:if test="${not empty completedOrder}">
        <section class="sale-section" id="foodReceipt">
            <div class="receipt-heading">
                <p class="receipt-brand">F-CINEMA</p>
                <h2>HÓA ĐƠN BẮP NƯỚC / COMBO</h2>

                <p>
                    Mã đơn:
                    <strong>
                        #<c:out value="${completedOrder.foodOrderId}" />
                    </strong>
                </p>

                <p>
                    Thời gian:
                    <fmt:formatDate value="${completedOrder.createdAt}"
                                    pattern="dd/MM/yyyy HH:mm" />
                </p>

                <p>
                    Mã rạp:
                    <c:out value="${completedOrder.cinemaId}" />

                    · Mã nhân viên:
                    <c:out value="${completedOrder.staffId}" />
                </p>

                <c:if test="${not empty completedOrder.bookingId}">
                    <p>
                        Đơn vé liên quan:
                        #<c:out value="${completedOrder.bookingId}" />
                    </p>
                </c:if>

                <p>
                    Thanh toán:
                    <c:out value="${completedOrder.paymentMethod}" />

                    · Trạng thái:
                    <c:out value="${completedOrder.paymentStatus}" />
                </p>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Thành tiền</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:forEach var="item" items="${completedItems}">
                            <tr>
                                <td>
                                    <c:out value="${receiptNames[item.foodOrderItemId]}"
                                           default="Sản phẩm" />
                                </td>

                                <td>
                                    <c:out value="${item.quantity}" />
                                </td>

                                <td>
                                    <fmt:formatNumber value="${item.unitPrice}"
                                                      pattern="#,##0" /> đ
                                </td>

                                <td>
                                    <fmt:formatNumber value="${item.lineTotal}"
                                                      pattern="#,##0" /> đ
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <p class="total-row">
                Tổng thanh toán bắp nước:
                <strong>
                    <fmt:formatNumber value="${completedOrder.totalAmount}"
                                      pattern="#,##0" /> đ
                </strong>
            </p>

            <div class="receipt-actions">
                <button type="button"
                        class="pay-button"
                        onclick="window.print();">
                    In hóa đơn
                </button>

                <a href="${ctx}/staff/counter?tab=food">
                    Quay lại lịch sử
                </a>

                <a href="${ctx}/staff/food-sale">
                    Bán đơn mới
                </a>
            </div>
        </section>
    </c:if>

    <c:if test="${receiptMode and empty completedOrder and empty error}">
        <div class="message message-error">
            Không thể hiển thị hóa đơn.
            Vui lòng quay lại lịch sử và chọn đơn.
        </div>
    </c:if>

</div>

<c:if test="${not receiptMode}">

    <%-- Giữ món đã chọn bằng dữ liệu HTML, không ghép tên vào JavaScript. --%>
    <div id="savedDraftItems" hidden>
        <c:forEach var="item" items="${draftFoodItems}">
            <span data-type="${not empty item.foodItemId ? 'FOOD' : 'COMBO'}"
                  data-id="${not empty item.foodItemId ? item.foodItemId : item.comboId}"
                  data-quantity="${item.quantity}"></span>
        </c:forEach>
    </div>

    <script>
        var cart = [];
        var submitting = false;

        var orderForm = document.getElementById("foodOrderForm");
        var draftMode = orderForm.getAttribute("data-draft") === "true";
        var canSell = orderForm.getAttribute("data-can-sell") === "true";

        function addItem(button) {
            if (!canSell || submitting) {
                return;
            }

            var type = button.getAttribute("data-type");
            var id = button.getAttribute("data-id");
            var price = Number(button.getAttribute("data-price"));

            if (!Number.isFinite(price) || price < 0) {
                alert("Giá sản phẩm không hợp lệ.");
                return;
            }

            for (var i = 0; i < cart.length; i++) {
                if (cart[i].type === type && cart[i].id === id) {
                    if (cart[i].quantity >= 2147483647) {
                        alert("Số lượng vượt giới hạn cho phép.");
                        return;
                    }

                    cart[i].quantity++;
                    renderCart();
                    return;
                }
            }

            cart.push({
                type: type,
                id: id,
                name: button.getAttribute("data-name"),
                price: price,
                quantity: 1
            });

            renderCart();
        }

        function changeQuantity(index, value) {
            var quantity = Number(value);

            if (!Number.isInteger(quantity)
                    || quantity < 1
                    || quantity > 2147483647) {

                alert("Số lượng phải là số nguyên lớn hơn 0 và trong giới hạn cho phép.");
                renderCart();
                return;
            }

            cart[index].quantity = quantity;
            renderCart();
        }

        function removeItem(index) {
            cart.splice(index, 1);
            renderCart();
        }

        function renderCart() {
            var body = document.getElementById("cartBody");
            var hidden = document.getElementById("hiddenItems");
            var total = 0;

            body.innerHTML = "";
            hidden.innerHTML = "";

            // Đổi giỏ hàng thì phải xác nhận lại số tiền đã thu.
            var cash = document.getElementById("cashReceived");

            if (cash !== null) {
                cash.checked = false;
            }

            if (cart.length === 0) {
                body.innerHTML =
                        '<tr><td colspan="5" class="empty-text">'
                        + 'Chưa có sản phẩm trong đơn hàng.'
                        + '</td></tr>';
            }

            for (var i = 0; i < cart.length; i++) {
                var item = cart[i];
                var lineTotal = item.price * item.quantity;

                total += lineTotal;

                var row = document.createElement("tr");

                row.innerHTML =
                        '<td></td>'
                        + '<td>'
                        + '<input type="number" class="quantity-input"'
                        + ' min="1" max="2147483647"'
                        + ' value="' + item.quantity + '"'
                        + ' onchange="changeQuantity(' + i + ', this.value)">'
                        + '</td>'
                        + '<td></td>'
                        + '<td></td>'
                        + '<td>'
                        + '<button type="button" class="sale-button"'
                        + ' onclick="removeItem(' + i + ')">'
                        + 'Xóa'
                        + '</button>'
                        + '</td>';

                // Dùng textContent để tên sản phẩm không bị hiểu thành HTML.
                row.cells[0].textContent = item.name;
                row.cells[2].textContent = formatMoney(item.price) + " đ";
                row.cells[3].textContent = formatMoney(lineTotal) + " đ";

                body.appendChild(row);

                // Chỉ gửi loại, ID và số lượng; server tự lấy giá.
                addHiddenInput(hidden, "itemType", item.type);
                addHiddenInput(hidden, "itemId", item.id);
                addHiddenInput(hidden, "quantity", item.quantity);
            }

            document.getElementById("totalText").textContent
                    = formatMoney(total) + " đ";

            document.getElementById("payButton").disabled
                    = cart.length === 0 || submitting || !canSell;
        }

        function addHiddenInput(container, name, value) {
            var input = document.createElement("input");

            input.type = "hidden";
            input.name = name;
            input.value = value;

            container.appendChild(input);
        }

        function formatMoney(value) {
            return Number(value).toLocaleString("vi-VN");
        }

        function checkOrder() {
            if (submitting || !canSell) {
                return false;
            }

            if (cart.length === 0) {
                alert("Vui lòng chọn ít nhất một sản phẩm.");
                return false;
            }

            // Mua kèm vé chưa thu tiền tại bước chọn món.
            if (!draftMode
                    && !document.getElementById("cashReceived").checked) {

                alert("Vui lòng xác nhận đã thu đủ tiền mặt.");
                return false;
            }

            if (!draftMode
                    && !confirm("Xác nhận đã thu tiền và thanh toán đơn bắp nước/combo này?")) {

                return false;
            }

            submitting = true;
            document.getElementById("payButton").disabled = true;

            return true;
        }

        function restoreDraftItems() {
            var saved = document.querySelectorAll("#savedDraftItems span");
            var products = document.querySelectorAll(
                    ".product-card button[data-type]");

            // Tìm lại sản phẩm đã chọn trong danh mục hiện tại.
            for (var i = 0; i < saved.length; i++) {
                for (var j = 0; j < products.length; j++) {
                    if (saved[i].getAttribute("data-type")
                            === products[j].getAttribute("data-type")
                            && saved[i].getAttribute("data-id")
                            === products[j].getAttribute("data-id")) {

                        cart.push({
                            type: products[j].getAttribute("data-type"),
                            id: products[j].getAttribute("data-id"),
                            name: products[j].getAttribute("data-name"),
                            price: Number(products[j].getAttribute("data-price")),
                            quantity: Number(saved[i].getAttribute("data-quantity"))
                        });

                        break;
                    }
                }
            }

            renderCart();
        }

        restoreDraftItems();
    </script>
</c:if>

<%@ include file="/WEB-INF/include/footer.jsp" %>