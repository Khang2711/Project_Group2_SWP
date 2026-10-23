<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .food-sale-page {
        max-width: 1250px;
        margin: auto;
        padding: 40px 25px 60px;
    }

    .food-sale-page * {
        box-sizing: border-box;
    }

    .food-sale-page h1 {
        border-left: 5px solid #ff243e;
        padding-left: 15px;
        font-size: 28px;
    }

    .food-sale-page h2 {
        font-size: 22px;
        margin-bottom: 20px;
    }

    .food-sale-page h3 {
        font-size: 18px;
        margin: 22px 0 15px;
    }

    .food-sale-page .sale-section {
        margin-top: 25px;
        padding: 24px;
        background: #18181c;
        border: 1px solid #33333a;
        border-radius: 10px;
    }

    .food-sale-page .product-list {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 18px;
    }

    .food-sale-page .product-card {
        padding: 14px;
        background: #222228;
        border: 1px solid #393940;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        gap: 12px;
        min-width: 0;
    }

    .food-sale-page .product-image {
        position: relative;
        height: 155px;
        background: #111114;
        border-radius: 6px;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
    }

    .food-sale-page .product-image img {
        position: absolute;
        inset: 0;
        width: 100%;
        height: 100%;
        object-fit: contain;
    }

    .food-sale-page .product-name {
        font-size: 18px;
        overflow-wrap: anywhere;
    }

    .food-sale-page .product-description {
        color: #b5b5bd;
        font-size: 14px;
        flex: 1;
        margin: 0;
        overflow-wrap: anywhere;
    }

    .food-sale-page .product-price {
        font-weight: bold;
        color: #ff5267;
        margin: 0;
    }

    .food-sale-page button {
        font: inherit;
        cursor: pointer;
        border-radius: 5px;
        padding: 10px 14px;
    }

    .food-sale-page .sale-button {
        background: #303038;
        color: white;
        border: 1px solid #55555f;
    }

    .food-sale-page .sale-button:hover {
        background: #42424b;
    }

    .food-sale-page .pay-button {
        background: #ff243e;
        color: white;
        border: 0;
        font-weight: bold;
    }

    .food-sale-page button:disabled {
        background: #55555f;
        cursor: not-allowed;
    }

    .food-sale-page .table-wrap {
        overflow-x: auto;
    }

    .food-sale-page table {
        width: 100%;
        border-collapse: collapse;
        min-width: 620px;
    }

    .food-sale-page th,
    .food-sale-page td {
        padding: 12px;
        border: 1px solid #3a3a42;
        text-align: left;
    }

    .food-sale-page th {
        background: #25252c;
    }

    .food-sale-page .quantity-input {
        width: 80px;
        padding: 8px;
        background: #111114;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
        font: inherit;
    }

    .food-sale-page .total-row {
        font-size: 21px;
        margin: 22px 0;
    }

    .food-sale-page .cash-confirm {
        display: block;
        margin: 18px 0;
        line-height: 1.6;
    }

    .food-sale-page .cash-confirm input {
        width: 17px;
        height: 17px;
        vertical-align: middle;
    }

    .food-sale-page .empty-text {
        color: #b5b5bd;
        line-height: 1.6;
    }

    .food-sale-page .message {
        margin-top: 20px;
        padding: 15px;
        border-radius: 6px;
        line-height: 1.6;
    }

    .food-sale-page .message-error {
        background: #401b23;
        border: 1px solid #ff5267;
    }

    .food-sale-page .message-success {
        background: #183a2b;
        border: 1px solid #4caf80;
    }

    .food-sale-page .back-link {
        color: #ccc;
        display: inline-block;
        margin-bottom: 20px;
    }

    .food-sale-page .receipt-heading {
        margin-bottom: 20px;
    }

    .food-sale-page .receipt-heading h2 {
        margin: 5px 0 15px;
    }

    .food-sale-page .receipt-heading p {
        margin: 7px 0;
    }

    .food-sale-page .receipt-brand {
        font-size: 22px;
        font-weight: bold;
        color: #ff3045;
    }

    .food-sale-page .receipt-actions {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 18px;
        margin-top: 20px;
    }

    .food-sale-page .receipt-actions a {
        color: #ddd;
    }

    @media (max-width: 1000px) {
        .food-sale-page .product-list {
            grid-template-columns: repeat(3, minmax(0, 1fr));
        }
    }

    @media (max-width: 700px) {
        .food-sale-page {
            padding: 28px 15px 40px;
        }

        .food-sale-page .sale-section {
            padding: 16px;
        }

        .food-sale-page .product-list {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }

        .food-sale-page h1 {
            font-size: 24px;
        }
    }

    @media (max-width: 400px) {
        .food-sale-page .product-list {
            grid-template-columns: 1fr;
        }
    }

    /* Chỉ áp dụng các quy tắc bên dưới khi in trang food-sale. */
    @media print {
        @page {
            size: A4;
            margin: 15mm;
        }

        html,
        body {
            background: white !important;
            color: black !important;
            margin: 0 !important;
            padding: 0 !important;
            min-height: 0 !important;
            height: auto !important;
        }

        body {
            display: block !important;
        }

        .site-header,
        .site-footer {
            display: none !important;
        }

        main.page-container {
            display: block !important;
            width: 100% !important;
            max-width: none !important;
            margin: 0 !important;
            padding: 0 !important;
            min-height: 0 !important;
        }

        .food-sale-page {
            width: 100% !important;
            max-width: none !important;
            margin: 0 !important;
            padding: 0 !important;
        }

        /* Chỉ giữ hóa đơn đã lưu, ẩn danh sách món và giỏ hàng. */
        .food-sale-page > :not(#foodReceipt) {
            display: none !important;
        }

        #foodReceipt {
            display: block !important;
            margin: 0 !important;
            padding: 0 !important;
            border: 0 !important;
            border-radius: 0 !important;
        }

        #foodReceipt,
        #foodReceipt * {
            background: white !important;
            color: black !important;
            box-shadow: none !important;
        }

        #foodReceipt .receipt-heading {
            break-inside: avoid;
        }

        #foodReceipt .table-wrap {
            overflow: visible !important;
        }

        #foodReceipt table {
            width: 100%;
            min-width: 0;
            table-layout: fixed;
            font-size: 12px;
        }

        #foodReceipt th,
        #foodReceipt td {
            border: 1px solid #777;
            padding: 8px;
            overflow-wrap: anywhere;
        }

        #foodReceipt thead {
            display: table-header-group;
        }

        #foodReceipt tr {
            break-inside: avoid;
        }

        #foodReceipt .total-row {
            font-size: 18px;
            break-inside: avoid;
        }

        #foodReceipt .receipt-actions {
            display: none !important;
        }
    }
</style>

<div class="food-sale-page">

    <a class="back-link" href="${ctx}/staff/counter">
        ← Quay lại quầy
    </a>

    <h1>BÁN FOOD / COMBO</h1>

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
                                         onerror="this.style.display='none';">
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
                                                  pattern="#,###" />
                                VNĐ
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
                                         onerror="this.style.display='none';">
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
                                                  pattern="#,###" />
                                VNĐ
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

        <form action="${ctx}/staff/food-sale"
              method="post"
              onsubmit="return checkOrder();">

            <input type="hidden"
                   name="saleToken"
                   value="<c:out value='${sessionScope.foodSaleToken}' />">

            <input type="hidden"
                   name="paymentMethod"
                   value="CASH">

            <div id="hiddenItems"></div>

            <div class="table-wrap">

                <table>
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Thành tiền</th>
                            <th></th>
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
                Tạm tính:
                <strong id="totalText">0 VNĐ</strong>
            </p>

            <p>
                Phương thức thanh toán:
                <strong>Tiền mặt</strong>
            </p>

            <label class="cash-confirm">
                <input type="checkbox"
                       id="cashReceived"
                       name="cashReceived"
                       value="yes"
                       required>

                Tôi xác nhận đã thu đủ tiền mặt của khách.
            </label>

            <button type="submit"
                    id="payButton"
                    class="pay-button"
                    disabled>
                Thanh toán
            </button>

        </form>

    </section>

    <c:if test="${not empty completedOrder}">

        <section class="sale-section" id="foodReceipt">

            <div class="receipt-heading">

                <p class="receipt-brand">F-CINEMA</p>

                <h2>HÓA ĐƠN FOOD / COMBO</h2>

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
                                                      pattern="#,###" />
                                    VNĐ
                                </td>

                                <td>
                                    <fmt:formatNumber value="${item.lineTotal}"
                                                      pattern="#,###" />
                                    VNĐ
                                </td>
                            </tr>
                        </c:forEach>

                    </tbody>
                </table>

            </div>

            <p class="total-row">
                Tổng thanh toán:
                <strong>
                    <fmt:formatNumber value="${completedOrder.totalAmount}"
                                      pattern="#,###" />
                    VNĐ
                </strong>
            </p>

            <div class="receipt-actions">

                <button type="button"
                        class="pay-button"
                        onclick="window.print();">
                    In hóa đơn
                </button>

                <a href="${ctx}/staff/food-sale">
                    Bán đơn mới
                </a>

            </div>

        </section>

    </c:if>

</div>

<script>
    var cart = [];
    var submitting = false;

    function addItem(button) {
        var type = button.getAttribute("data-type");
        var id = button.getAttribute("data-id");
        var price = Number(button.getAttribute("data-price"));

        if (!Number.isFinite(price) || price < 0) {
            alert("Giá sản phẩm không hợp lệ.");
            return;
        }

        for (var i = 0; i < cart.length; i++) {
            if (cart[i].type === type && cart[i].id === id) {
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

        // Thay đổi giỏ hàng thì cần xác nhận lại số tiền đã thu.
        document.getElementById("cashReceived").checked = false;

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

            // Hiển thị tên như văn bản, không hiểu thành HTML.
            row.cells[0].textContent = item.name;
            row.cells[2].textContent = formatMoney(item.price) + " VNĐ";
            row.cells[3].textContent = formatMoney(lineTotal) + " VNĐ";

            body.appendChild(row);

            addHiddenInput(hidden, "itemType", item.type);
            addHiddenInput(hidden, "itemId", item.id);
            addHiddenInput(hidden, "quantity", item.quantity);
        }

        document.getElementById("totalText").textContent
                = formatMoney(total) + " VNĐ";

        document.getElementById("payButton").disabled
                = cart.length === 0 || submitting;
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
        if (submitting) {
            return false;
        }

        if (cart.length === 0) {
            alert("Vui lòng chọn ít nhất một sản phẩm.");
            return false;
        }

        if (!document.getElementById("cashReceived").checked) {
            alert("Vui lòng xác nhận đã thu đủ tiền mặt.");
            return false;
        }

        if (!confirm("Xác nhận thanh toán đơn food/combo này?")) {
            return false;
        }

        submitting = true;
        document.getElementById("payButton").disabled = true;

        return true;
    }
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>