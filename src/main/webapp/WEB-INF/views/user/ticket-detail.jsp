<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>


<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="ticket-detail-page">


    <div class="booking-heading">

        <a href="${ctx}/user/tickets"
           class="booking-back-link">

            ← Quay lại vé của tôi

        </a>

        <h1>
            Chi tiết vé
        </h1>

        <div class="title-line"></div>

    </div>


    <div class="cinema-ticket">


        <div class="cinema-ticket-main">


            <div class="cinema-ticket-top">


                <div>

                    <span class="ticket-label">
                        F-CINEMA TICKET
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


                <c:choose>

                    <c:when test="${ticket.status eq 'USED'}">

                        <span class="
                              ticket-status
                              ticket-status-used">

                            Đã sử dụng

                        </span>

                    </c:when>

                    <c:when test="${expired}">
                        <span class="ticket-status ticket-status-expired">
                            Hết hiệu lực
                        </span>
                    </c:when>

                    <c:otherwise>

                        <span class="
                              ticket-status
                              ticket-status-valid">

                            Có hiệu lực

                        </span>

                    </c:otherwise>

                </c:choose>


            </div>


            <div class="cinema-ticket-grid">

                <c:if test="${not empty cinema}">

                    <div>
                        <span>Rạp chiếu</span>
                        <strong>
                            <c:out value="${cinema.cinemaName}" />
                        </strong>
                    </div>

                    <div>
                        <span>Địa chỉ rạp</span>
                        <strong>
                            <c:out value="${cinema.address}" />
                        </strong>
                    </div>

                </c:if>


                <div>

                    <span>
                        Ngày giờ chiếu
                    </span>

                    <strong>

                        <c:choose>

                            <c:when test="${not empty showtime}">

                                <fmt:formatDate
                                    value="${showtime.startTime}"
                                    pattern="dd/MM/yyyy HH:mm" />

                            </c:when>

                            <c:otherwise>
                                Không có dữ liệu
                            </c:otherwise>

                        </c:choose>

                    </strong>

                </div>


                <div>

                    <span>
                        Ghế
                    </span>

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

                </div>


                <div>

                    <span>
                        Loại ghế
                    </span>

                    <strong>

                        <c:choose>

                            <c:when test="${not empty seat}">

                                <c:out
                                    value="${seat.seatType}" />

                            </c:when>

                            <c:otherwise>
                                -
                            </c:otherwise>

                        </c:choose>

                    </strong>

                </div>


                <div>

                    <span>
                        Suất chiếu
                    </span>

                    <strong>

                        <c:choose>

                            <c:when test="${not empty showtime}">

                                #${showtime.showtimeId}

                            </c:when>

                            <c:otherwise>
                                -
                            </c:otherwise>

                        </c:choose>

                    </strong>

                </div>


                <div>

                    <span>
                        Phòng chiếu
                    </span>

                    <strong>

                        <c:choose>

                            <c:when test="${not empty showtime}">

                                Phòng #${showtime.roomId}

                            </c:when>

                            <c:otherwise>
                                -
                            </c:otherwise>

                        </c:choose>

                    </strong>

                </div>


                <div>

                    <span>
                        Mã đơn
                    </span>

                    <strong>
                        #${ticket.bookingId}
                    </strong>

                </div>


                <div>

                    <span>
                        Giá vé
                    </span>

                    <strong class="cinema-ticket-price">

                        <fmt:formatNumber
                            value="${ticket.ticketPrice}"
                            pattern="#,###" />

                        VNĐ

                    </strong>

                </div>


            </div>

            <c:if test="${not empty foodItems}">

                <div class="cinema-ticket-food">

                    <h3>Bắp nước / Combo</h3>

                    <c:forEach var="item" items="${foodItems}">

                        <div class="cinema-ticket-food-item">

                            <div>
                                <strong>
                                    <c:out value="${item.productName}" />
                                </strong>

                                <span>
                                    Số lượng: ${item.quantity}
                                </span>
                            </div>

                            <strong class="cinema-ticket-price">

                                <fmt:formatNumber
                                    value="${item.lineTotal}"
                                    pattern="#,###" />

                                VNĐ

                            </strong>

                        </div>

                    </c:forEach>

                    <small>
                        Sản phẩm đi kèm đơn đặt vé
                        #${ticket.bookingId}
                    </small>

                </div>

            </c:if>




            <c:if test="${ticket.status eq 'USED'
                          and not empty ticket.checkedInAt}">

                  <div class="ticket-checkin-note">

                      Đã check-in lúc

                      <strong>

                          <fmt:formatDate
                              value="${ticket.checkedInAt}"
                              pattern="dd/MM/yyyy HH:mm" />

                      </strong>

                  </div>

            </c:if>


        </div>


        <aside class="cinema-ticket-stub">


            <span>
                QR VÉ
            </span>


            <c:choose>

                <c:when test="${expired}">
                    <p class="ticket-expired-message">
                        Vé đã hết hiệu lực và không thể sử dụng
                        để check-in.
                    </p>
                </c:when>

                <c:otherwise>
                    <img
                        src="${ctx}/user/ticket-qr?id=${ticket.ticketId}"
                        alt="QR code vé"
                        class="cinema-ticket-qr" />
                </c:otherwise>

            </c:choose>


            <span>
                MÃ VÉ
            </span>


            <strong class="cinema-ticket-code">

                <c:out
                    value="${ticket.ticketCode}" />

            </strong>


            <c:if test="${not expired}">
                <small>
                    Xuất trình mã vé này khi check-in
                </small>
                <small>
                    Vui lòng check-in sớm nhất trước 1 tiếng khi phim bắt đầu   
                </small>

            </c:if>


        </aside>


    </div>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>