<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="booking-history-page">

    <div class="booking-heading">

        <h1>Đơn đặt vé</h1>

        <div class="title-line"></div>

        <p>
            Theo dõi các đơn đặt vé của bạn tại F-CINEMA.
        </p>

    </div>


    <c:choose>

        <c:when test="${empty bookings}">

            <div class="booking-history-empty">

                <div class="booking-empty-icon">
                    F
                </div>

                <h2>
                    Chưa có đơn đặt vé
                </h2>

                <p>
                    Các đơn đặt vé của bạn
                    sẽ xuất hiện tại đây.
                </p>

                <a href="${ctx}/user/movies"
                   class="booking-primary-button">

                    Khám phá phim

                </a>

            </div>

        </c:when>


        <c:otherwise>

            <div class="booking-history-list">


                <c:forEach var="booking"
                           items="${bookings}">


                    <c:set
                        var="showtime"
                        value="${showtimeMap[booking.bookingId]}" />


                    <c:set
                        var="movie"
                        value="${movieMap[booking.bookingId]}" />


                    <article class="booking-history-card">


                        <div class="booking-history-main">


                            <div class="booking-history-id">

                                <span>
                                    Đơn đặt vé
                                </span>

                                <strong>
                                    #${booking.bookingId}
                                </strong>

                            </div>


                            <div class="booking-history-movie">

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


                                <c:if test="${not empty showtime}">

                                    <p>

                                        <fmt:formatDate
                                            value="${showtime.startTime}"
                                            pattern="dd/MM/yyyy HH:mm" />

                                    </p>

                                </c:if>

                            </div>


                        </div>


                        <div class="booking-history-info">


                            <div>

                                <span>
                                    Ngày đặt
                                </span>

                                <strong>

                                    <fmt:formatDate
                                        value="${booking.bookingTime}"
                                        pattern="dd/MM/yyyy HH:mm" />

                                </strong>

                            </div>


                            <div>

                                <span>
                                    Tổng tiền
                                </span>

                                <strong class="booking-history-price">

                                    <fmt:formatNumber
                                        value="${booking.totalAmount}"
                                        pattern="#,###" />

                                    VNĐ

                                </strong>

                            </div>


                            <div>

                                <span>
                                    Trạng thái
                                </span>


                                <c:choose>

                                    <c:when test="${booking.status eq 'CONFIRMED'}">

                                        <strong class="
                                                booking-status
                                                booking-status-confirmed">

                                            Đã xác nhận

                                        </strong>

                                    </c:when>


                                    <c:when test="${booking.status eq 'CANCELLED'}">

                                        <strong class="
                                                booking-status
                                                booking-status-cancelled">

                                            Đã hủy

                                        </strong>

                                    </c:when>


                                    <c:otherwise>

                                        <strong class="
                                                booking-status
                                                booking-status-pending">

                                            Chờ thanh toán

                                        </strong>

                                    </c:otherwise>

                                </c:choose>

                            </div>


                        </div>


                        <div class="booking-history-action">

                            <a href="${ctx}/user/booking-detail?id=${booking.bookingId}"
                               class="booking-detail-button">

                                Xem chi tiết

                            </a>

                        </div>


                    </article>


                </c:forEach>


            </div>

        </c:otherwise>

    </c:choose>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>