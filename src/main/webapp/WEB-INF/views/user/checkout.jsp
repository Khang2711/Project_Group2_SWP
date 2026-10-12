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


<section class="booking-page">


    <div class="booking-heading">

        <h1>Xác nhận đặt vé</h1>

        <div class="title-line"></div>

        <p>
            Kiểm tra lại thông tin trước khi tạo đơn đặt vé.
        </p>

    </div>


    <div class="checkout-layout">


        <!-- LEFT -->

        <div class="checkout-card">


            <div class="checkout-card-title">

                <h2>Ghế đã chọn</h2>


                <span class="seat-count">

                    ${fn:length(selectedSeats)} ghế

                </span>

            </div>


            <c:if test="${fullRoom}">

                <div class="booking-notice">

                    Bạn đang chọn hình thức
                    đặt toàn phòng.

                </div>

            </c:if>


            <div class="checkout-seat-list">


                <c:forEach var="seat"
                           items="${selectedSeats}">


                    <div class="checkout-seat-item">


                        <div class="checkout-seat-name">


                            <span class="seat-icon">

                                ${seat.seatRow}-${seat.seatNumber}

                            </span>


                            <div>

                                <strong>

                                    Hàng ${seat.seatRow}
                                    - Ghế ${seat.seatNumber}

                                </strong>


                                <small>

                                    <c:out
                                        value="${seat.seatType}" />

                                </small>

                            </div>


                        </div>


                        <strong class="checkout-seat-price">


                            <fmt:formatNumber
                                value="${seatPrices[seat.seatId]}"
                                pattern="#,###" />

                            VNĐ


                        </strong>


                    </div>


                </c:forEach>


            </div>


        </div>


        <!-- RIGHT -->

        <div class="checkout-summary">


            <h2>Thông tin đặt vé</h2>


            <div class="summary-row">

                <span>
                    Suất chiếu
                </span>

                <strong>
                    #${showtime.showtimeId}
                </strong>

            </div>


            <div class="summary-row">

                <span>
                    Thời gian
                </span>

                <strong>

                    <fmt:formatDate
                        value="${showtime.startTime}"
                        pattern="dd/MM/yyyy HH:mm" />

                </strong>

            </div>


            <div class="summary-row">

                <span>
                    Số lượng ghế
                </span>

                <strong>
                    ${fn:length(selectedSeats)}
                </strong>

            </div>


            <div class="summary-divider"></div>


            <div class="
                 summary-row
                 summary-total">

                <span>
                    Tổng tiền
                </span>

                <strong>

                    <fmt:formatNumber
                        value="${subtotal}"
                        pattern="#,###" />

                    VNĐ

                </strong>

            </div>


            <form action="${ctx}/user/checkout"
                  method="post"
                  class="checkout-confirm-form">


                <input type="hidden"
                       name="showtimeId"
                       value="${showtime.showtimeId}">


                <c:forEach var="seat"
                           items="${selectedSeats}">


                    <input type="hidden"
                           name="seatId"
                           value="${seat.seatId}">


                </c:forEach>


                <button type="submit"
                        class="booking-primary-button">

                    Xác nhận đặt vé

                </button>


            </form>


            <a class="booking-secondary-button"
               href="${ctx}/user/select-seats?showtimeId=${showtime.showtimeId}">

                Quay lại chọn ghế

            </a>


            <p class="checkout-note">

                Sau khi xác nhận,
                hệ thống sẽ tạo Booking
                và Ticket ở trạng thái PENDING.

            </p>


        </div>


    </div>


</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>