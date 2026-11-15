<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />



<section class="cart-page" data-css-page="user-cart">

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
