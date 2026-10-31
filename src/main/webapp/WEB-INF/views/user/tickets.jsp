<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>


<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="ticket-page">


    <div class="booking-heading">

        <h1>Vé của tôi</h1>

        <div class="title-line"></div>

        <p>
            Các vé đã được xác nhận
            sau khi thanh toán thành công.
        </p>

    </div>


    <c:choose>


        <c:when test="${empty tickets}">

            <div class="ticket-empty">

                <div class="ticket-empty-icon">
                    🎫
                </div>

                <h2>
                    Chưa có vé đã xác nhận
                </h2>

                <p>
                    Vé sẽ xuất hiện tại đây
                    sau khi đơn đặt vé được
                    thanh toán và xác nhận.
                </p>

                <a href="${ctx}/user/movies"
                   class="booking-primary-button">

                    Chọn phim

                </a>

            </div>

        </c:when>


        <c:otherwise>

            <div class="ticket-list">


                <c:forEach var="ticket"
                           items="${tickets}">


                    <c:set
                        var="showtime"
                        value="${showtimeMap[ticket.ticketId]}" />


                    <c:set
                        var="movie"
                        value="${movieMap[ticket.ticketId]}" />


                    <c:set
                        var="seat"
                        value="${seatMap[ticket.ticketId]}" />

                    <c:set
                        var="ticketExpired"
                        value="${expiredMap[ticket.ticketId]}" />


                    <article class="ticket-card">


                        <div class="ticket-card-main">


                            <div class="ticket-card-icon">
                                🎟
                            </div>


                            <div class="ticket-card-info">

                                <span class="ticket-label">
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


                                <c:if test="${not empty showtime}">

                                    <p>

                                        <fmt:formatDate
                                            value="${showtime.startTime}"
                                            pattern="dd/MM/yyyy HH:mm" />

                                    </p>

                                </c:if>

                            </div>


                        </div>


                        <div class="ticket-card-meta">


                            <div>

                                <span>
                                    Ghế
                                </span>

                                <strong>

                                    <c:choose>

                                        <c:when test="${not empty seat}">

                                            ${seat.seatRow}-${seat.seatNumber}

                                        </c:when>

                                        <c:otherwise>

                                            #${ticket.seatId}

                                        </c:otherwise>

                                    </c:choose>

                                </strong>

                            </div>


                            <div>

                                <span>
                                    Giá vé
                                </span>

                                <strong>

                                    <fmt:formatNumber
                                        value="${ticket.ticketPrice}"
                                        pattern="#,###" />

                                    VNĐ

                                </strong>

                            </div>


                            <div>

                                <span>
                                    Trạng thái
                                </span>


                                <c:choose>

                                    <c:when test="${ticket.status eq 'USED'}">

                                        <strong class="
                                                ticket-status
                                                ticket-status-used">

                                            Đã sử dụng

                                        </strong>

                                    </c:when>


                                    <c:when test="${ticketExpired}">

                                        <strong class="
                                                ticket-status
                                                ticket-status-expired">

                                            Hết hiệu lực

                                        </strong>

                                    </c:when>


                                    <c:otherwise>

                                        <strong class="
                                                ticket-status
                                                ticket-status-valid">

                                            Có hiệu lực

                                        </strong>

                                    </c:otherwise>

                                </c:choose>

                            </div>


                        </div>


                        <div class="ticket-card-code">

                            <span>
                                Mã vé
                            </span>

                            <strong>

                                <c:out
                                    value="${ticket.ticketCode}" />

                            </strong>

                        </div>


                        <div class="ticket-card-action">

                            <a href="${ctx}/user/ticket-detail?id=${ticket.ticketId}"
                               class="booking-detail-button">

                                Xem vé

                            </a>

                        </div>


                    </article>


                </c:forEach>


            </div>

        </c:otherwise>


    </c:choose>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>