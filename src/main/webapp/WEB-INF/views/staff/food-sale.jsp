<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .food-sale-page {
        max-width: 1400px;
        margin: 0 auto;
        padding: 35px 50px 60px;
    }

    .sale-section {
        margin-top: 30px;
        padding: 25px;
        background: #18181c;
        border: 1px solid #33333a;
        border-radius: 8px;
    }

    .product-list {
        display: flex;
        gap: 15px;
        flex-wrap: wrap;
        margin-top: 15px;
    }

    .product-card {
        width: 230px;
        padding: 16px;
        border: 1px solid #3a3a42;
        border-radius: 8px;
        background: #202026;
    }

    .product-card p {
        margin: 8px 0;
    }

    .product-description {
        color: #aaaaaa;
        min-height: 22px;
    }

    .sale-button {
        padding: 9px 14px;
        border: 1px solid #55555f;
        border-radius: 5px;
        background: #29292f;
        color: white;
        cursor: pointer;
    }

    .sale-button:hover {
        background: #35353c;
    }

    .pay-button {
        padding: 11px 18px;
        border: none;
        border-radius: 5px;
        background: #ff3045;
        color: white;
        font-weight: bold;
        cursor: pointer;
    }

    .pay-button:disabled {
        background: #55555f;
        cursor: not-allowed;
    }

    .cart-table {
        width: 100%;
        margin-top: 15px;
        border-collapse: collapse;
    }

    .cart-table th,
    .cart-table td {
        padding: 11px;
        border: 1px solid #3a3a42;
        text-align: left;
    }

    .cart-table th {
        background: #222228;
    }

    .quantity-input {
        width: 70px;
        padding: 7px;
        background: #101012;
        color: white;
        border: 1px solid #55555f;
        border-radius: 4px;
    }

    .payment-select {
        padding: 9px 12px;
        background: #101012;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
    }

    .message-error {
        margin-top: 20px;
        padding: 12px;
        border: 1px solid #ff3045;
        background: #40191d;
        border-radius: 5px;
    }

    .message-success {
        margin-top: 20px;
        padding: 12px;
        border: 1px solid #4caf50;
        background: #16351a;
        border-radius: 5px;
    }

    .empty-text {
        color: #aaaaaa;
    }

    .total-row {
        margin-top: 20px;
        font-size: 18px;
    }

    .checkout-row {
        display: flex;
        gap: 15px;
        align-items: center;
        margin-top: 18px;
        flex-wrap: wrap;
    }
</style>

<div class="food-sale-page">

    <h1>Bán Food / Combo tại quầy</h1>
    <p>Staff Counter · Food & Combo Sale</p>

    <c:if test="${not empty error}">
        <div class="message-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message-success">
            <c:out value="${success}" />

            <c:if test="${not empty completedOrder}">
                <br>
                Mã đơn:
                <strong>
                    #<c:out value="${completedOrder.foodOrderId}" />
                </strong>

                · Tổng tiền:
                <strong>
                    <fmt:formatNumber
                        value="${completedOrder.totalAmount}"
                        pattern="#,###" />
                    VNĐ
                </strong>
            </c:if>
        </div>
    </c:if>

    <section class="sale-section">

        <h2>1. Chọn sản phẩm</h2>

        <h3>Món ăn / Nước uống</h3>

        <c:choose>

            <c:when test="${not empty foodItems}">

                <div class="product-list">

                    <c:forEach var="item"
                               items="${foodItems}">

                        <div class="product-card">

                            <strong>
                                <c:out value="${item.itemName}" />
                            </strong>

                            <p class="product-description">
                                <c:out value="${item.description}" />
                            </p>

                            <p>
                                <fmt:formatNumber
                                    value="${item.price}"
                                    pattern="#,###" />
                                VNĐ
                            </p>

                            <button type="button"
                                    class="sale-button"
                                    data-type="FOOD"
                                    data-id="${item.foodItemId}"
                                    data-name="<c:out value='${item.itemName}' />"
                                    data-price="${item.price}"
                                    onclick="addItem(this)">
                                Thêm
                            </button>

                        </div>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>
                <p class="empty-text">
                    Chưa có danh sách món ăn từ UC-26.
                </p>
            </c:otherwise>

        </c:choose>


        <h3 style="margin-top:30px;">
            Combo
        </h3>

        <c:choose>

            <c:when test="${not empty combos}">

                <div class="product-list">

                    <c:forEach var="combo"
                               items="${combos}">

                        <div class="product-card">

                            <strong>
                                <c:out value="${combo.comboName}" />
                            </strong>

                            <p class="product-description">
                                <c:out value="${combo.description}" />
                            </p>

                            <p>
                                <fmt:formatNumber
                                    value="${combo.comboPrice}"
                                    pattern="#,###" />
                                VNĐ
                            </p>

                            <button type="button"
                                    class="sale-button"
                                    data-type="COMBO"
                                    data-id="${combo.comboId}"
                                    data-name="<c:out value='${combo.comboName}' />"
                                    data-price="${combo.comboPrice}"
                                    onclick="addItem(this)">
                                Thêm
                            </button>

                        </div>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>
                <p class="empty-text">
                    Chưa có danh sách combo từ UC-26.
                </p>
            </c:otherwise>

        </c:choose>

    </section>


    <section class="sale-section">

        <h2>2. Đơn hàng tại quầy</h2>

        <form action="${ctx}/staff/food-sale"
              method="post"
              onsubmit="return checkOrder();">

            <div id="hiddenItems"></div>

            <table class="cart-table">

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th style="width:120px;">Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                        <th style="width:90px;"></th>
                    </tr>
                </thead>

                <tbody id="cartBody">

                    <tr id="emptyCartRow">
                        <td colspan="5"
                            class="empty-text">
                            Chưa có sản phẩm trong đơn hàng.
                        </td>
                    </tr>

                </tbody>

            </table>

            <p class="total-row">
                Tổng tiền:
                <strong id="totalText">
                    0 VNĐ
                </strong>
            </p>

            <div class="checkout-row">

                <div>
                    <label for="paymentMethod">
                        Phương thức thanh toán:
                    </label>

                    <select id="paymentMethod"
                            name="paymentMethod"
                            class="payment-select">

                        <option value="CASH">
                            Tiền mặt
                        </option>

                    </select>
                </div>

                <button type="submit"
                        id="payButton"
                        class="pay-button"
                        disabled>
                    Thanh toán
                </button>

            </div>

        </form>

    </section>


    <c:if test="${not empty completedItems}">

        <section class="sale-section">

            <h2>Đơn hàng vừa thanh toán</h2>

            <table class="cart-table">

                <thead>
                    <tr>
                        <th>Loại</th>
                        <th>ID</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="item"
                               items="${completedItems}">

                        <tr>

                            <td>
                                <c:choose>
                                    <c:when test="${item.foodItemId != null}">
                                        Food
                                    </c:when>
                                    <c:otherwise>
                                        Combo
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${item.foodItemId != null}">
                                        <c:out value="${item.foodItemId}" />
                                    </c:when>

                                    <c:otherwise>
                                        <c:out value="${item.comboId}" />
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:out value="${item.quantity}" />
                            </td>

                            <td>
                                <fmt:formatNumber
                                    value="${item.unitPrice}"
                                    pattern="#,###" />
                                VNĐ
                            </td>

                            <td>
                                <fmt:formatNumber
                                    value="${item.lineTotal}"
                                    pattern="#,###" />
                                VNĐ
                            </td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </section>

    </c:if>

</div>


<script>
    var cart = [];

    function addItem(button) {
        var type = button.getAttribute("data-type");
        var id = button.getAttribute("data-id");
        var name = button.getAttribute("data-name");
        var price = Number(button.getAttribute("data-price"));

        for (var i = 0; i < cart.length; i++) {

            if (cart[i].type === type
                    && cart[i].id === id) {

                cart[i].quantity++;
                renderCart();
                return;
            }
        }

        cart.push({
            type: type,
            id: id,
            name: name,
            price: price,
            quantity: 1
        });

        renderCart();
    }

    function changeQuantity(index, value) {
        var quantity = parseInt(value);

        if (isNaN(quantity) || quantity <= 0) {
            cart[index].quantity = 1;
        } else {
            cart[index].quantity = quantity;
        }

        renderCart();
    }

    function removeItem(index) {
        cart.splice(index, 1);
        renderCart();
    }

    function renderCart() {
        var body = document.getElementById("cartBody");
        var hiddenItems = document.getElementById("hiddenItems");
        var total = 0;

        body.innerHTML = "";
        hiddenItems.innerHTML = "";

        if (cart.length === 0) {

            body.innerHTML =
                    '<tr>'
                    + '<td colspan="5" class="empty-text">'
                    + 'Chưa có sản phẩm trong đơn hàng.'
                    + '</td>'
                    + '</tr>';

            document.getElementById("totalText").innerText = "0 VNĐ";
            document.getElementById("payButton").disabled = true;
            return;
        }

        for (var i = 0; i < cart.length; i++) {

            var item = cart[i];
            var lineTotal = item.price * item.quantity;

            total += lineTotal;

            var row = document.createElement("tr");

            row.innerHTML =
                    "<td>" + item.name + "</td>"
                    + "<td>"
                    + '<input type="number" '
                    + 'class="quantity-input" '
                    + 'min="1" '
                    + 'value="' + item.quantity + '" '
                    + 'onchange="changeQuantity(' + i + ', this.value)">'
                    + "</td>"
                    + "<td>" + formatMoney(item.price) + " VNĐ</td>"
                    + "<td>" + formatMoney(lineTotal) + " VNĐ</td>"
                    + "<td>"
                    + '<button type="button" '
                    + 'class="sale-button" '
                    + 'onclick="removeItem(' + i + ')">'
                    + "Xóa"
                    + "</button>"
                    + "</td>";

            body.appendChild(row);

            addHiddenInput(
                    hiddenItems,
                    "itemType",
                    item.type
                    );

            addHiddenInput(
                    hiddenItems,
                    "itemId",
                    item.id
                    );

            addHiddenInput(
                    hiddenItems,
                    "quantity",
                    item.quantity
                    );

            addHiddenInput(
                    hiddenItems,
                    "unitPrice",
                    item.price
                    );
        }

        document.getElementById("totalText").innerText
                = formatMoney(total) + " VNĐ";

        document.getElementById("payButton").disabled = false;
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
        if (cart.length === 0) {
            alert("Vui lòng chọn ít nhất một sản phẩm.");
            return false;
        }

        return confirm("Xác nhận thanh toán đơn hàng?");
    }
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>