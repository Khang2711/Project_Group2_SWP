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


            <c:if test="${booking.status eq 'PENDING'}">

                <a class="booking-primary-button"
                   href="${ctx}/user/payment?bookingId=${booking.bookingId}">
                    Thanh toán ngay
                </a>

            </c:if>


        </aside>


    </div>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>