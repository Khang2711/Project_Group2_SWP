<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="booking-detail-page">


    <div class="booking-heading">

        <a href="${ctx}/user/bookings"
           class="booking-back-link">

            ← Quay lại đơn đặt vé

        </a>

        <h1>
            Chi tiết đơn #${booking.bookingId}
        </h1>

        <div class="title-line"></div>

    </div>


    <div class="booking-detail-layout">


        <!-- LEFT -->
        <div>


            <div class="booking-detail-card">


                <div class="booking-detail-card-header">

                    <h2>
                        Thông tin suất chiếu
                    </h2>


                    <c:choose>

                        <c:when test="${booking.status eq 'CONFIRMED'}">

                            <span class="
                                  booking-status
                                  booking-status-confirmed">

                                Đã xác nhận

                            </span>

                        </c:when>


                        <c:when test="${booking.status eq 'CANCELLED'}">

                            <span class="
                                  booking-status
                                  booking-status-cancelled">

                                Đã hủy

                            </span>

                        </c:when>


                        <c:otherwise>

                            <span class="
                                  booking-status
                                  booking-status-pending">

                                Chờ thanh toán

                            </span>

                        </c:otherwise>

                    </c:choose>

                </div>


                <div class="booking-movie-info">


                    <div>

                        <span class="booking-info-label">
                            Phim
                        </span>

                        <h2>

                            <c:choose>

                                <c:when test="${not empty movie}">

                                    <c:out
                                        value="${movie.title}" />

                                </c:when>

                                <c:otherwise>

                                    Không có thông tin phim

                                </c:otherwise>

                            </c:choose>

                        </h2>

                    </div>


                    <c:if test="${not empty showtime}">

                        <div class="booking-showtime-grid">


                            <div>

                                <span>
                                    Suất chiếu
                                </span>

                                <strong>
                                    #${showtime.showtimeId}
                                </strong>

                            </div>


                            <div>

                                <span>
                                    Thời gian
                                </span>

                                <strong>

                                    <fmt:formatDate
                                        value="${showtime.startTime}"
                                        pattern="dd/MM/yyyy HH:mm" />

                                </strong>

                            </div>


                        </div>

                    </c:if>


                </div>


            </div>


            <div class="
                 booking-detail-card
                 booking-ticket-section">


                <div class="booking-detail-card-header">

                    <h2>
                        Ghế đã đặt
                    </h2>

                    <span class="booking-ticket-count">

                        ${fn:length(tickets)} ghế

                    </span>

                </div>


                <c:choose>


                    <c:when test="${empty tickets}">

                        <p class="booking-no-ticket">

                            Đơn đặt vé chưa có Ticket.

                        </p>

                    </c:when>


                    <c:otherwise>

                        <div class="booking-ticket-list">


                            <c:forEach var="ticket"
                                       items="${tickets}">


                                <c:set
                                    var="seat"
                                    value="${seatMap[ticket.ticketId]}" />


                                <div class="booking-ticket-item">


                                    <div class="booking-ticket-seat">


                                        <div class="booking-seat-box">

                                            <c:choose>

                                                <c:when test="${not empty seat}">

                                                    ${seat.seatRow}-${seat.seatNumber}

                                                </c:when>

                                                <c:otherwise>

                                                    #${ticket.seatId}

                                                </c:otherwise>

                                            </c:choose>

                                        </div>


                                        <div>


                                            <strong>

                                                <c:choose>

                                                    <c:when test="${not empty seat}">

                                                        Hàng ${seat.seatRow}
                                                        - Ghế ${seat.seatNumber}

                                                    </c:when>

                                                    <c:otherwise>

                                                        Ghế #${ticket.seatId}

                                                    </c:otherwise>

                                                </c:choose>

                                            </strong>


                                            <c:if test="${not empty seat}">

                                                <small>

                                                    <c:out
                                                        value="${seat.seatType}" />

                                                </small>

                                            </c:if>


                                            <small>

                                                <c:choose>

                                                    <c:when test="${not empty ticket.ticketCode}">

                                                        Mã vé:
                                                        <c:out
                                                            value="${ticket.ticketCode}" />

                                                    </c:when>

                                                    <c:otherwise>

                                                        Vé chưa được phát hành

                                                    </c:otherwise>

                                                </c:choose>

                                            </small>


                                        </div>


                                    </div>


                                    <div class="booking-ticket-right">


                                        <strong>

                                            <fmt:formatNumber
                                                value="${ticket.ticketPrice}"
                                                pattern="#,###" />

                                            VNĐ

                                        </strong>


                                        <span>
                                            <c:out value="${ticket.status}" />
                                        </span>


                                    </div>


                                </div>


                            </c:forEach>


                        </div>

                    </c:otherwise>


                </c:choose>


            </div>


        </div>


        <!-- RIGHT -->
        <aside class="booking-detail-summary">


            <h2>
                Tóm tắt đơn hàng
            </h2>


            <div class="booking-summary-row">

                <span>
                    Mã đơn
                </span>

                <strong>
                    #${booking.bookingId}
                </strong>

            </div>


            <div class="booking-summary-row">

                <span>
                    Ngày đặt
                </span>

                <strong>

                    <fmt:formatDate
                        value="${booking.bookingTime}"
                        pattern="dd/MM/yyyy HH:mm" />

                </strong>

            </div>


            <div class="booking-summary-row">

                <span>
                    Tạm tính
                </span>

                <strong>

                    <fmt:formatNumber
                        value="${booking.subtotal}"
                        pattern="#,###" />

                    VNĐ

                </strong>

            </div>


            <div class="booking-summary-row">

                <span>
                    Giảm giá
                </span>

                <strong>

                    <fmt:formatNumber
                        value="${booking.discountAmount}"
                        pattern="#,###" />

                    VNĐ

                </strong>

            </div>


            <div class="booking-summary-line"></div>


            <div class="
                 booking-summary-row
                 booking-summary-total">

                <span>
                    Tổng tiền
                </span>

                <strong>

                    <fmt:formatNumber
                        value="${booking.totalAmount}"
                        pattern="#,###" />

                    VNĐ

                </strong>

            </div>


            <c:if test="${booking.status eq 'CONFIRMED'}">
                <div class="booking-policy-notice">
                    <strong>Lưu ý về hoàn tiền</strong>
                    <p>Đơn đặt vé đã thanh toán thành công. F-Cinema không hỗ trợ hủy đơn hoặc hoàn tiền. Vui lòng liên hệ nhân viên rạp nếu cần được hỗ trợ về thông tin vé.</p>
                </div>
            </c:if>

            <c:if test="${booking.status eq 'PENDING'}">

                <button type="button"
                        class="booking-primary-button"
                        id="openAgeConfirmation">
                    Thanh toán ngay
                </button>

            </c:if>


        </aside>


    </div>


</section>





<style>
    .booking-policy-notice {
        margin-top: 18px;
        padding: 14px 16px;
        color: #ffe1e4;
        background: rgba(229, 9, 20, .10);
        border: 1px solid rgba(229, 9, 20, .45);
        border-left: 4px solid #e50914;
        border-radius: 8px;
    }
    .booking-policy-notice strong {
        display: block;
        margin-bottom: 6px;
        color: #ff727b;
    }
    .booking-policy-notice p {
        margin: 0;
        font-size: 13px;
        line-height: 1.6;
    }
</style>

<c:if test="${booking.status eq 'PENDING'}">
    <c:set var="movieAgeRating" value="${fn:toUpperCase(fn:trim(movie.ageRating))}" />
    <style>
        .age-confirm-overlay {
            position: fixed;
            inset: 0;
            z-index: 9999;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 20px;
            background: rgba(0, 0, 0, .78);
        }
        .age-confirm-overlay.is-visible { display: flex; }
        .age-confirm-dialog {
            width: 100%;
            max-width: 540px;
            padding: 28px;
            color: #f5f5f5;
            background: #1c1c22;
            border: 1px solid #454550;
            border-top: 4px solid #e50914;
            border-radius: 12px;
            box-shadow: 0 18px 60px rgba(0, 0, 0, .55);
        }
        .age-confirm-dialog h2 {
            margin: 0 0 18px;
            font-size: 23px;
        }
        .age-confirm-dialog p {
            margin: 0;
            color: #dedee3;
            font-size: 15px;
            line-height: 1.75;
        }
        .age-confirm-actions {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 26px;
        }
        .age-confirm-actions button {
            min-width: 110px;
            padding: 11px 18px;
            border: 1px solid transparent;
            border-radius: 6px;
            font: inherit;
            font-weight: 700;
            cursor: pointer;
        }
        .age-confirm-cancel {
            color: #eee;
            background: #34343d;
            border-color: #555560 !important;
        }
        .age-confirm-agree {
            color: #fff;
            background: #e50914;
        }
        .age-confirm-actions button:hover { filter: brightness(1.12); }
        @media (max-width: 480px) {
            .age-confirm-dialog { padding: 22px 18px; }
            .age-confirm-actions { flex-direction: column-reverse; }
            .age-confirm-actions button { width: 100%; }
        }
    </style>

    <div class="age-confirm-overlay"
         id="ageConfirmationModal"
         aria-hidden="true">
        <div class="age-confirm-dialog"
             role="dialog"
             aria-modal="true"
             aria-labelledby="ageConfirmationTitle"
             aria-describedby="ageConfirmationMessage">
            <h2 id="ageConfirmationTitle">Xác nhận thông tin vé</h2>

            <div id="ageConfirmationMessage">
                <strong>1. Quy định độ tuổi</strong>
                <p>
                    <c:choose>
                        <c:when test="${movieAgeRating eq 'P'}">
                            Phim được phân loại P – phù hợp với mọi độ tuổi. Khán giả ở mọi lứa tuổi đều có thể xem phim.
                        </c:when>
                        <c:when test="${movieAgeRating eq 'T13'}">
                            Phim được phân loại T13 – chỉ dành cho khán giả từ đủ 13 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 13 tuổi sẽ không được vào xem.
                        </c:when>
                        <c:when test="${movieAgeRating eq 'T16'}">
                            Phim được phân loại T16 – chỉ dành cho khán giả từ đủ 16 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 16 tuổi sẽ không được vào xem.
                        </c:when>
                        <c:when test="${movieAgeRating eq 'T18'}">
                            Phim được phân loại T18 – chỉ dành cho khán giả từ đủ 18 tuổi trở lên. Người xem cần xuất trình giấy tờ tùy thân để xác thực độ tuổi khi được yêu cầu. Khán giả chưa đủ 18 tuổi sẽ không được vào xem.
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${not empty movie.ageRating}">
                                    Phim có phân loại ${movie.ageRating}. Vui lòng kiểm tra điều kiện độ tuổi của phim và liên hệ nhân viên F-Cinema nếu cần được giải đáp trước khi thanh toán.
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
                    Sau khi thanh toán thành công, đơn đặt vé không được hủy và không được hoàn tiền. Vui lòng kiểm tra kỹ tên phim, ngày giờ chiếu, rạp, ghế và tổng tiền trước khi xác nhận thanh toán. F-Cinema không hoàn tiền trong trường hợp người xem không đáp ứng điều kiện độ tuổi của phim.
                </p>
            </div>

            <div class="age-confirm-actions">
                <button type="button"
                        class="age-confirm-cancel"
                        id="cancelAgeConfirmation">
                    Hủy
                </button>
                <button type="button"
                        class="age-confirm-agree"
                        id="agreeAgeConfirmation">
                    Đồng ý
                </button>
            </div>
        </div>
    </div>

    <script>
        (function () {
            var openButton = document.getElementById("openAgeConfirmation");
            var modal = document.getElementById("ageConfirmationModal");
            var cancelButton = document.getElementById("cancelAgeConfirmation");
            var agreeButton = document.getElementById("agreeAgeConfirmation");

            if (!openButton || !modal || !cancelButton || !agreeButton) {
                return;
            }

            var paymentUrl = "${ctx}/user/payment?bookingId=${booking.bookingId}";

            function closeModal() {
                modal.classList.remove("is-visible");
                modal.setAttribute("aria-hidden", "true");
                openButton.focus();
            }

            openButton.addEventListener("click", function () {
                modal.classList.add("is-visible");
                modal.setAttribute("aria-hidden", "false");
                agreeButton.focus();
            });

            cancelButton.addEventListener("click", closeModal);

            agreeButton.addEventListener("click", function () {
                window.location.href = paymentUrl;
            });

            modal.addEventListener("click", function (event) {
                if (event.target === modal) {
                    closeModal();
                }
            });

            document.addEventListener("keydown", function (event) {
                if (event.key === "Escape" && modal.classList.contains("is-visible")) {
                    closeModal();
                }
            });
        })();
    </script>
</c:if>


<%@ include file="/WEB-INF/include/footer.jsp" %>