<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="booking-page payment-result-page">

    <div class="booking-heading payment-result-heading">

        <span class="payment-result-label">
            VNPAY
        </span>

        <h1>Kết quả thanh toán</h1>

        <div class="title-line"></div>

        <p>
            Thông tin giao dịch và trạng thái đơn đặt vé của bạn.
        </p>

    </div>


    <%-- Lỗi server / database --%>

    <c:if test="${not empty error}">

        <div class="payment-result-card payment-result-error">

            <div class="payment-result-icon">
                !
            </div>

            <div class="payment-result-content">

                <span class="payment-result-status">
                    GIAO DỊCH CHƯA ĐƯỢC XÁC NHẬN
                </span>

                <h2>Chưa thể xác nhận kết quả</h2>

                <p class="payment-result-message">
                    <c:out value="${error}" />
                </p>


                <div class="payment-result-actions">

                    <a href="${ctx}/user/bookings"
                       class="payment-result-button payment-result-button-primary">

                        Kiểm tra đơn đặt vé

                    </a>

                </div>

            </div>

        </div>

    </c:if>


    <c:if test="${not empty paymentResult}">

        <%-- Xác định giao diện theo trạng thái --%>

        <c:choose>

            <%-- THANH TOÁN THÀNH CÔNG --%>

            <c:when test="${paymentResult.signatureValid
                            and paymentResult.paymentSuccessful
                            and paymentResult.bookingConfirmed
                            and not paymentResult.refundRequired}">

                <c:set var="resultClass"
                       value="payment-result-success" />

                <c:set var="resultIcon"
                       value="✓" />

                <c:set var="resultStatus"
                       value="THANH TOÁN THÀNH CÔNG" />

                <c:set var="resultTitle"
                       value="Đặt vé thành công!" />

                <c:set var="resultDescription"
                       value="Đơn đặt vé đã được xác nhận và vé của bạn đã sẵn sàng sử dụng." />

            </c:when>


            <%-- ĐÃ THU TIỀN NHƯNG CẦN HOÀN TIỀN --%>

            <c:when test="${paymentResult.signatureValid
                            and paymentResult.paymentSuccessful
                            and paymentResult.refundRequired}">

                <c:set var="resultClass"
                       value="payment-result-warning" />

                <c:set var="resultIcon"
                       value="!" />

                <c:set var="resultStatus"
                       value="CẦN KIỂM TRA" />

                <c:set var="resultTitle"
                       value="Giao dịch cần được kiểm tra" />

                <c:set var="resultDescription"
                       value="VNPay đã ghi nhận thanh toán nhưng đơn cần được nhân viên kiểm tra và xử lý." />

            </c:when>


            <%-- THANH TOÁN ĐÃ GHI NHẬN --%>

            <c:when test="${paymentResult.signatureValid
                            and paymentResult.paymentSuccessful}">

                <c:set var="resultClass"
                       value="payment-result-warning" />

                <c:set var="resultIcon"
                       value="!" />

                <c:set var="resultStatus"
                       value="ĐÃ GHI NHẬN" />

                <c:set var="resultTitle"
                       value="Thanh toán đã được ghi nhận" />

                <c:set var="resultDescription"
                       value="Hệ thống đã nhận kết quả giao dịch. Vui lòng kiểm tra trạng thái đơn đặt vé." />

            </c:when>


            <%-- THANH TOÁN THẤT BẠI / HỦY --%>

            <c:when test="${paymentResult.signatureValid}">

                <c:set var="resultClass"
                       value="payment-result-failed" />

                <c:set var="resultIcon"
                       value="×" />

                <c:set var="resultStatus"
                       value="THANH TOÁN CHƯA THÀNH CÔNG" />

                <c:set var="resultTitle"
                       value="Giao dịch chưa hoàn tất" />

                <c:set var="resultDescription"
                       value="Đơn đặt vé và vé vẫn đang ở trạng thái chờ thanh toán." />

            </c:when>


            <%-- CHỮ KÝ KHÔNG HỢP LỆ --%>

            <c:otherwise>

                <c:set var="resultClass"
                       value="payment-result-error" />

                <c:set var="resultIcon"
                       value="!" />

                <c:set var="resultStatus"
                       value="KHÔNG THỂ XÁC MINH" />

                <c:set var="resultTitle"
                       value="Không xác minh được giao dịch" />

                <c:set var="resultDescription"
                       value="Hệ thống không thay đổi trạng thái booking hoặc vé khi giao dịch không hợp lệ." />

            </c:otherwise>

        </c:choose>


        <%-- CARD CHUNG --%>

        <div class="payment-result-card ${resultClass}">


            <div class="payment-result-icon">

                ${resultIcon}

            </div>


            <div class="payment-result-content">


                <span class="payment-result-status">

                    ${resultStatus}

                </span>


                <h2>

                    ${resultTitle}

                </h2>


                <p class="payment-result-description">

                    ${resultDescription}

                </p>


                <c:if test="${not empty paymentResult.message}">

                    <div class="payment-result-system-message">

                        <span>
                            Thông báo hệ thống
                        </span>

                        <p>

                            <c:out value="${paymentResult.message}" />

                        </p>

                    </div>

                </c:if>


                <c:if test="${paymentResult.bookingId gt 0}">

                    <div class="payment-result-order">

                        <span>
                            Mã đơn đặt vé
                        </span>

                        <strong>

                            #${paymentResult.bookingId}

                        </strong>

                    </div>


                    <div class="payment-result-actions">


                        <a href="${ctx}/user/booking-detail?id=${paymentResult.bookingId}"
                           class="payment-result-button payment-result-button-primary">

                            Xem chi tiết đơn

                        </a>


                        <%-- Thành công thì cho xem vé --%>

                        <c:if test="${paymentResult.signatureValid
                                    and paymentResult.paymentSuccessful
                                    and paymentResult.bookingConfirmed
                                    and not paymentResult.refundRequired}">

                            <a href="${ctx}/user/tickets"
                               class="payment-result-button payment-result-button-secondary">

                                Xem vé của tôi

                            </a>

                        </c:if>


                        <%-- Thất bại hợp lệ thì cho thanh toán lại --%>

                        <c:if test="${paymentResult.signatureValid
                                    and not paymentResult.paymentSuccessful}">

                            <a href="${ctx}/user/payment?bookingId=${paymentResult.bookingId}"
                               class="payment-result-button payment-result-button-secondary">

                                Thử thanh toán lại

                            </a>

                        </c:if>


                    </div>

                </c:if>


                <a href="${ctx}/user/bookings"
                   class="payment-result-back">

                    ← Quay lại đơn đặt vé

                </a>


            </div>

        </div>

    </c:if>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>