<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .cart-page {
        max-width: 1050px;
        margin: 0 auto;
    }

    .cart-title {
        margin-bottom: 30px;
    }

    .cart-title h1 {
        font-size: 36px;
        margin-bottom: 10px;
    }

    .cart-title p {
        color: #aaaaaa;
    }

    .cart-title-line {
        width: 65px;
        height: 5px;
        margin-bottom: 14px;
        background-color: #ff1f2d;
        border-radius: 5px;
    }

    .cart-layout {
        display: grid;
        grid-template-columns: 1fr 320px;
        gap: 24px;
        align-items: start;
    }

    .cart-list,
    .cart-summary,
    .cart-empty {
        background-color: #18181c;
        border: 1px solid #303038;
        border-radius: 10px;
    }

    .cart-list {
        overflow: hidden;
    }

    .cart-item {
        display: flex;
        justify-content: space-between;
        gap: 20px;
        padding: 22px;
        border-bottom: 1px solid #303038;
    }

    .cart-item:last-child {
        border-bottom: none;
    }

    .cart-item-type {
        display: inline-block;
        margin-bottom: 8px;
        color: #ff4a55;
        font-size: 13px;
        font-weight: bold;
        text-transform: uppercase;
    }

    .cart-item h3 {
        margin-bottom: 8px;
        font-size: 19px;
    }

    .cart-item-description {
        color: #999999;
        font-size: 14px;
        line-height: 1.5;
    }

    .cart-item-price {
        min-width: 145px;
        text-align: right;
    }

    .cart-item-price span {
        display: block;
        margin-bottom: 8px;
        color: #aaaaaa;
        font-size: 13px;
    }

    .cart-item-price strong {
        color: #ffffff;
        font-size: 18px;
    }

    .cart-summary {
        padding: 22px;
    }

    .cart-summary h2 {
        margin-bottom: 20px;
        font-size: 22px;
    }

    .cart-summary-row {
        display: flex;
        justify-content: space-between;
        gap: 15px;
        margin-bottom: 14px;
        color: #bbbbbb;
        font-size: 14px;
    }

    .cart-summary-total {
        display: flex;
        justify-content: space-between;
        gap: 15px;
        margin-top: 18px;
        padding-top: 18px;
        border-top: 1px solid #3a3a42;
        font-size: 18px;
        font-weight: bold;
    }

    .cart-summary-total strong {
        color: #ff4a55;
    }

    .cart-checkout-button {
        display: block;
        width: 100%;
        margin-top: 22px;
        padding: 13px 16px;
        background-color: #ff1f2d;
        border-radius: 6px;
        color: #ffffff;
        text-align: center;
        font-weight: bold;
    }

    .cart-checkout-button:hover {
        background-color: #d91622;
    }

    .cart-continue-link {
        display: block;
        margin-top: 14px;
        color: #aaaaaa;
        text-align: center;
        font-size: 14px;
    }

    .cart-continue-link:hover {
        color: #ffffff;
    }

    .cart-empty {
        padding: 70px 25px;
        text-align: center;
    }

    .cart-empty-icon {
        margin-bottom: 18px;
        font-size: 46px;
    }

    .cart-empty h2 {
        margin-bottom: 10px;
    }

    .cart-empty p {
        margin-bottom: 24px;
        color: #999999;
    }

    .cart-empty a {
        display: inline-block;
        padding: 11px 24px;
        background-color: #ff1f2d;
        border-radius: 6px;
        color: #ffffff;
        font-weight: bold;
    }

    @media (max-width: 800px) {
        .cart-layout {
            grid-template-columns: 1fr;
        }
    }

    @media (max-width: 550px) {
        .cart-item {
            flex-direction: column;
        }

        .cart-item-price {
            text-align: left;
        }

        .cart-title h1 {
            font-size: 30px;
        }
    }
</style>

<section class="cart-page">

    <div class="cart-title">
        <div class="cart-title-line"></div>
        <h1>Giỏ hàng</h1>
        <p>Kiểm tra vé, món ăn và combo trước khi tiếp tục thanh toán.</p>
    </div>

    <c:choose>

        <c:when test="${empty cartItems}">

            <div class="cart-empty">
                <div class="cart-empty-icon">🛒</div>
                <h2>Giỏ hàng đang trống</h2>
                <p>Bạn chưa thêm vé hoặc sản phẩm nào vào giỏ hàng.</p>

                <a href="${ctx}/user/movies">
                    Chọn phim ngay
                </a>
            </div>

        </c:when>

        <c:otherwise>

            <div class="cart-layout">

                <div class="cart-list">

                    <c:forEach var="item" items="${cartItems}">

                        <article class="cart-item">

                            <div>
                                <span class="cart-item-type">
                                    <c:out value="${item.type}" />
                                </span>

                                <h3>
                                    <c:out value="${item.name}" />
                                </h3>

                                <c:if test="${not empty item.description}">
                                    <p class="cart-item-description">
                                        <c:out value="${item.description}" />
                                    </p>
                                </c:if>
                            </div>

                            <div class="cart-item-price">

                                <span>
                                    Số lượng: <c:out value="${item.quantity}" />
                                </span>

                                <strong>
                                    <fmt:formatNumber value="${item.totalPrice}" pattern="#,###" /> VNĐ
                                </strong>

                            </div>

                        </article>

                    </c:forEach>

                </div>

                <aside class="cart-summary">

                    <h2>Tóm tắt đơn hàng</h2>

                    <div class="cart-summary-row">
                        <span>Tạm tính</span>
                        <span>
                            <fmt:formatNumber value="${cartSubtotal}" pattern="#,###" /> VNĐ
                        </span>
                    </div>

                    <div class="cart-summary-row">
                        <span>Giảm giá</span>
                        <span>
                            - <fmt:formatNumber value="${cartDiscount}" pattern="#,###" /> VNĐ
                        </span>
                    </div>

                    <div class="cart-summary-total">
                        <span>Tổng cộng</span>
                        <strong>
                            <fmt:formatNumber value="${cartTotal}" pattern="#,###" /> VNĐ
                        </strong>
                    </div>

                    <a href="${ctx}/user/checkout" class="cart-checkout-button">
                        Tiếp tục thanh toán
                    </a>

                    <a href="${ctx}/user/movies" class="cart-continue-link">
                        Tiếp tục chọn phim
                    </a>

                </aside>

            </div>

        </c:otherwise>

    </c:choose>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
