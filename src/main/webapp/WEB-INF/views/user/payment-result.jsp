<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="\${pageContext.request.contextPath}" />

<section class="booking-page">
    <div class="booking-heading">
        <h1>Kết quả thanh toán</h1>
        <div class="title-line"></div>
    </div>

    <div class="checkout-card">
        <c:if test="\${not empty error}">
            <div class="booking-notice">
                <strong>Chưa thể xác nhận kết quả</strong>
                <p><c:out value="\${error}" /></p>
            </div>
        </c:if>

        <c:if test="\${not empty paymentResult}">
            <c:choose>
                <c:when test="\${paymentResult.signatureValid and paymentResult.paymentSuccessful and paymentResult.bookingConfirmed and not paymentResult.refundRequired}">
                    <h2>Thanh toán thành công</h2>
                    <p><c:out value="\${paymentResult.message}" /></p>
                    <p>Đơn đặt vé đã được xác nhận và vé đã chuyển sang trạng thái hợp lệ.</p>
                </c:when>

                <c:when test="\${paymentResult.signatureValid and paymentResult.paymentSuccessful and paymentResult.refundRequired}">
                    <h2>Đã nhận thanh toán, cần kiểm tra đơn</h2>
                    <p><c:out value="\${paymentResult.message}" /></p>
                    <p>Đơn này chưa được xác nhận thêm lần nữa. Nhân viên cần kiểm tra và xử lý hoàn tiền nếu có thu trùng hoặc ghế không còn khả dụng.</p>
                </c:when>

                <c:when test="\${paymentResult.signatureValid and paymentResult.paymentSuccessful}">
                    <h2>Thanh toán đã được ghi nhận</h2>
                    <p><c:out value="\${paymentResult.message}" /></p>
                </c:when>

                <c:when test="\${paymentResult.signatureValid}">
                    <h2>Thanh toán chưa thành công</h2>
                    <p><c:out value="\${paymentResult.message}" /></p>
                    <p>Booking và vé chưa được xác nhận.</p>
                </c:when>

                <c:otherwise>
                    <h2>Không xác minh được giao dịch</h2>
                    <p><c:out value="\${paymentResult.message}" /></p>
                    <p>Hệ thống không xác nhận booking hoặc vé khi chữ ký không hợp lệ.</p>
                </c:otherwise>
            </c:choose>

            <c:if test="\${paymentResult.bookingId gt 0}">
                <p>Mã đơn đặt vé: <strong>#\${paymentResult.bookingId}</strong></p>
                <a class="booking-primary-button"
                   href="\${ctx}/user/booking-detail?id=\${paymentResult.bookingId}">
                    Xem chi tiết đơn
                </a>

                <c:if test="\${paymentResult.signatureValid and not paymentResult.paymentSuccessful}">
                    <a class="booking-secondary-button"
                       href="\${ctx}/user/payment?bookingId=\${paymentResult.bookingId}">
                        Thử thanh toán lại
                    </a>
                </c:if>
            </c:if>
        </c:if>

        <p>
            <a href="\${ctx}/user/bookings">Quay lại đơn đặt vé</a>
        </p>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
