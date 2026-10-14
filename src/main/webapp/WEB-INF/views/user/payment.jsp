<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="booking-page">
    <div class="booking-heading">
        <h1>Thanh toán VNPay</h1>
        <div class="title-line"></div>
        <p>Đơn đặt vé vẫn ở trạng thái chờ thanh toán cho đến khi máy chủ xác minh kết quả từ VNPay.</p>
    </div>

    <div class="checkout-card">
        <c:if test="${not empty error}">
            <div class="booking-notice">
                <strong>Chưa thể mở cổng thanh toán</strong>
                <p><c:out value="${error}" /></p>
            </div>
        </c:if>

        <p>
            Nếu bạn đang chạy môi trường phát triển, hãy kiểm tra cấu hình
            <code>VNPAY_TMN_CODE</code>, <code>VNPAY_HASH_SECRET</code> và
            <code>VNPAY_RETURN_URL</code>, sau đó khởi động lại Tomcat.
        </p>

        <a class="booking-primary-button" href="${ctx}/user/bookings">
            Quay lại đơn đặt vé
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
