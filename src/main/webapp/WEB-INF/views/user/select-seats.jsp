<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>


<c:set var="ctx"
       value="${pageContext.request.contextPath}" />


<section class="booking-page">


    <div class="booking-heading">

        <h1>Chọn ghế</h1>

        <div class="title-line"></div>

        <p>
            Chọn ghế bạn muốn hoặc đặt toàn bộ phòng chiếu.
        </p>

    </div>


    <c:if test="${not empty error}">

        <div class="booking-error">

            <c:out value="${error}" />

        </div>

    </c:if>


    <div class="showtime-info-bar">

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


    <form action="${ctx}/user/select-seats"
          method="post">


        <input type="hidden"
               name="showtimeId"
               value="${showtime.showtimeId}">


        <div class="cinema-seat-map">


            <!-- SCREEN -->

            <div class="cinema-screen-area">

                <div class="cinema-screen-curve"></div>

                <div class="cinema-screen-text">
                    MÀN HÌNH
                </div>

            </div>


            <!-- SEAT ROWS -->

            <div class="cinema-rows">


                <c:forEach var="rowEntry"
                           items="${seatRows}">


                    <div class="cinema-row">


                        <div class="cinema-row-name">

                            HÀNG ${rowEntry.key}

                        </div>


                        <div class="cinema-row-seats">


                            <c:forEach var="seat"
                                       items="${rowEntry.value}">


                                <c:set
                                    var="occupied"
                                    value="${occupiedSeatIds.contains(seat.seatId)}" />


                                <c:set
                                    var="hasPrice"
                                    value="${seatPrices[seat.seatId] != null}" />


                                <c:set
                                    var="disabledSeat"
                                    value="${seat.status ne 'ACTIVE'
                                             or occupied
                                             or not hasPrice}" />


                                <label class="
                                       movie-seat
                                       ${seat.seatType eq 'VIP'
                                         ? 'movie-seat-vip' : ''}
                                       ${occupied
                                         ? 'movie-seat-occupied' : ''}
                                       ${seat.status ne 'ACTIVE'
                                         ? 'movie-seat-disabled' : ''}">


                                    <input
                                        type="checkbox"
                                        name="seatId"
                                        value="${seat.seatId}"
                                        ${disabledSeat
                                          ? 'disabled' : ''}>


                                    <span class="movie-seat-box">

                                        ${seat.seatNumber}

                                    </span>


                                    <span class="movie-seat-tooltip">


                                        Hàng ${seat.seatRow}
                                        - Ghế ${seat.seatNumber}


                                        <br>


                                        <c:out
                                            value="${seat.seatType}" />


                                        <br>


                                        <c:choose>


                                            <c:when test="${occupied}">

                                                Đã bán

                                            </c:when>


                                            <c:when test="${seat.status ne 'ACTIVE'}">

                                                Không khả dụng

                                            </c:when>


                                            <c:when test="${not hasPrice}">

                                                Chưa có giá

                                            </c:when>


                                            <c:otherwise>


                                                <fmt:formatNumber
                                                    value="${seatPrices[seat.seatId]}"
                                                    pattern="#,###" />

                                                VNĐ


                                            </c:otherwise>


                                        </c:choose>


                                    </span>


                                </label>


                            </c:forEach>


                        </div>


                    </div>


                </c:forEach>


            </div>


            <!-- LEGEND -->

            <div class="cinema-seat-legend">


                <div>

                    <span class="
                          legend-seat
                          legend-available">
                    </span>

                    Ghế thường

                </div>


                <div>

                    <span class="
                          legend-seat
                          legend-vip-seat">
                    </span>

                    VIP

                </div>


                <div>

                    <span class="
                          legend-seat
                          legend-selected">
                    </span>

                    Đang chọn

                </div>


                <div>

                    <span class="
                          legend-seat
                          legend-occupied">
                    </span>

                    Đã bán

                </div>


            </div>


        </div>


        <div class="booking-actions">


            <button type="submit"
                    name="bookingMode"
                    value="SELECTED"
                    class="booking-primary-button">

                Tiếp tục

            </button>


            <button type="submit"
                    name="bookingMode"
                    value="FULL_ROOM"
                    class="
                    booking-secondary-button
                    booking-button-element">

                Đặt toàn phòng

            </button>


        </div>


    </form>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>