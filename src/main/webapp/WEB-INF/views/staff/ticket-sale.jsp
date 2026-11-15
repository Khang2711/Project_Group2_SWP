<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="saleMode"
       value="${empty counterStep or counterStep eq 'sale'}" />
<fmt:setLocale value="vi_VN" />



<section class="booking-page counter-page" data-css-page="staff-ticket-sale">

    <div class="booking-heading">
        <h1>Bán vé tại quầy</h1>
    </div>

    <div class="ticket-print-actions">
        <a href="${ctx}/staff/counter">Về quầy</a>
        <a href="${ctx}/staff/ticket-sale?newSale=1">Tạo đơn mới</a>
        <a href="${ctx}/staff/counter?tab=tickets">Vé đã bán</a>
    </div>

    <c:if test="${not empty error}">
        <div class="booking-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:if test="${not empty draftMessage}">
        <div class="booking-error">
            <c:out value="${draftMessage}" />
        </div>
    </c:if>

    <c:if test="${not empty printError}">
        <div class="booking-error">
            <c:out value="${printError}" />
        </div>
    </c:if>

    <c:if test="${not empty completedBooking}">
        <div class="counter-card">
            <h2>Thanh toán thành công</h2>
            <p>Đơn đã lưu. Có thể in vé bên dưới.</p>
        </div>
    </c:if>

    <c:if test="${counterStep eq 'draft-error'}">
        <div class="counter-card">
            <p>
                Chưa thể tiếp tục đơn.
                Hãy tải lại trang hoặc chọn Tạo đơn mới.
            </p>
        </div>
    </c:if>

    <%-- Thông tin đơn chưa thanh toán. --%>
    <c:if test="${counterStep eq 'extras' or counterStep eq 'review'}">
        <div class="counter-card">
            <h2>Đơn đang chuẩn bị</h2>

            <p>
                Phim:
                <strong><c:out value="${draftMovie.title}" /></strong>
            </p>

            <p>
                Suất chiếu:
                <fmt:formatDate value="${draftShowtime.startTime}"
                                pattern="dd/MM/yyyy HH:mm" />

                · Phòng:
                <c:out value="${draftRoom.roomName}" />
            </p>



            <p>
                Ghế:
                <c:forEach var="seat"
                           items="${draftSeats}"
                           varStatus="loop">
                    H<c:out value="${seat.seatRow}" />-<c:out value="${seat.seatNumber}" />${loop.last ? '' : ', '}
                </c:forEach>
            </p>

            <p>
                Tiền vé:
                <strong>
                    <fmt:formatNumber value="${draftTicketTotal}"
                                      pattern="#,##0" /> đ
                </strong>
            </p>
        </div>
    </c:if>

    <%-- Chỉ hỏi mua thêm một lần, trước bước thanh toán. --%>
    <c:if test="${counterStep eq 'extras'}">
        <div class="counter-card">
            <h2>Khách có muốn mua thêm bắp nước/combo không?</h2>

            <div class="ticket-print-actions">
                <a class="booking-primary-button"
                   href="${ctx}/staff/food-sale?draft=1">
                    Có, chọn bắp nước
                </a>

                <form method="post" action="${ctx}/staff/ticket-sale">
                    <input type="hidden" name="action" value="skipFood" />
                    <input type="hidden"
                           name="saleToken"
                           value="${sessionScope.counterSaleToken}" />

                    <button type="submit" class="booking-primary-button">
                        Không, tiếp tục
                    </button>
                </form>
            </div>
        </div>
    </c:if>

    <c:if test="${counterStep eq 'review'}">
        <div class="counter-card">
            <h2>Kiểm tra đơn và thanh toán</h2>

            <c:choose>
                <c:when test="${not empty draftFoodItems}">
                    <div class="counter-table-wrap">
                        <table class="counter-order-table">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Số lượng</th>
                                    <th>Đơn giá</th>
                                    <th>Thành tiền</th>
                                </tr>
                            </thead>

                            <tbody>
                                <c:forEach var="item" items="${draftFoodItems}">
                                    <c:choose>
                                        <c:when test="${not empty item.foodItemId}">
                                            <c:set var="productKey"
                                                   value="FOOD-${item.foodItemId}" />
                                        </c:when>

                                        <c:otherwise>
                                            <c:set var="productKey"
                                                   value="COMBO-${item.comboId}" />
                                        </c:otherwise>
                                    </c:choose>

                                    <tr>
                                        <td>
                                            <c:out value="${draftFoodNames[productKey]}" />
                                        </td>
                                        <td>
                                            <c:out value="${item.quantity}" />
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${item.unitPrice}"
                                                              pattern="#,##0" /> đ
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${item.lineTotal}"
                                                              pattern="#,##0" /> đ
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>

                <c:otherwise>
                    <p>Không mua bắp nước/combo.</p>
                </c:otherwise>
            </c:choose>

            <p>
                Tiền vé:
                <fmt:formatNumber value="${draftTicketTotal}"
                                  pattern="#,##0" /> đ
            </p>

            <p>
                Tiền bắp nước/combo:
                <fmt:formatNumber value="${draftFoodTotal}"
                                  pattern="#,##0" /> đ
            </p>

            <p>
                Tổng cần thu:
                <strong class="counter-total">
                    <fmt:formatNumber value="${draftTotal}"
                                      pattern="#,##0" /> đ
                </strong>
            </p>

            <div class="ticket-print-actions">
                <a href="${ctx}/staff/food-sale?draft=1">
                    Chọn / sửa bắp nước
                </a>

                <c:if test="${not empty draftFoodItems}">
                    <form method="post" action="${ctx}/staff/ticket-sale">
                        <input type="hidden" name="action" value="skipFood" />
                        <input type="hidden"
                               name="saleToken"
                               value="${sessionScope.counterSaleToken}" />

                        <button type="submit"
                                class="booking-primary-button">
                            Bỏ bắp nước
                        </button>
                    </form>
                </c:if>
            </div>

            <form method="post"
                  action="${ctx}/staff/ticket-sale"
                  onsubmit="return confirmCounterPayment(this)">

                <input type="hidden" name="action" value="pay" />
                <input type="hidden"
                       name="saleToken"
                       value="${sessionScope.counterSaleToken}" />

                <p>
                    <label>
                        <input type="checkbox"
                               name="cashReceived"
                               value="yes"
                               required
                               autocomplete="off" />

                        Tôi xác nhận đã thu đủ tổng tiền mặt
                    </label>
                </p>

                <button type="submit" class="booking-primary-button">
                    Xác nhận thanh toán
                </button>
            </form>
        </div>
    </c:if>

    <%-- Chỉ hiển thị vé và QR sau khi lưu thành công. --%>
    <c:if test="${not empty printedTickets}">
        <div class="counter-card" id="counterTicketPrint">
            <div class="ticket-no-print">
                <h2>
                    Vé của đơn #<c:out value="${printedBooking.bookingId}" />
                </h2>

                <p>
                    Tiền vé:
                    <strong>
                        <fmt:formatNumber value="${printedBooking.totalAmount}"
                                          pattern="#,##0" /> đ
                    </strong>
                </p>

                <div class="ticket-print-actions">
                    <button type="button"
                            id="printTicketButton"
                            class="booking-primary-button"
                            onclick="printCounterTickets()"
                            disabled>
                        In vé
                    </button>

                    <span>Mỗi ghế có một vé và một mã QR riêng.</span>
                </div>

                <p id="ticketQrError" class="booking-error" hidden></p>
            </div>

            <c:forEach var="ticket" items="${printedTickets}">
                <c:set var="ticketSeat"
                       value="${printedSeats[ticket.seatId]}" />

                <div class="ticket-paper">
                    <div class="ticket-details">
                        <p class="ticket-brand">F-CINEMA</p>
                        <h3 class="ticket-title">VÉ XEM PHIM</h3>

                        <p>
                            Phim:
                            <strong>
                                <c:out value="${printedMovie.title}" />
                            </strong>
                        </p>

                        <p>
                            Suất chiếu:
                            <strong>
                                <fmt:formatDate value="${printedShowtime.startTime}"
                                                pattern="dd/MM/yyyy HH:mm" />
                            </strong>
                        </p>

                        <p>
                            Rạp: <c:out value="${printedRoom.cinemaId}" />
                            · Phòng:
                            <strong>
                                <c:out value="${printedRoom.roomName}" />
                            </strong>
                        </p>

                        <p>
                            Ghế:
                            <strong>
                                H<c:out value="${ticketSeat.seatRow}" />-<c:out value="${ticketSeat.seatNumber}" />
                            </strong>

                            · Loại:
                            <c:out value="${ticketSeat.seatType}" />
                        </p>

                        <p>
                            Giá vé:
                            <strong>
                                <fmt:formatNumber value="${ticket.ticketPrice}"
                                                  pattern="#,##0" /> đ
                            </strong>
                        </p>

                        <p>
                            Mã đơn:
                            #<c:out value="${printedBooking.bookingId}" />

                            · Trạng thái vé:
                            <c:out value="${ticket.status}" />
                        </p>

                        <p>
                            Mã vé:
                            <span class="ticket-code">
                                <c:out value="${ticket.ticketCode}" />
                            </span>
                        </p>
                    </div>

                    <div class="ticket-qr-area">
                        <div class="ticket-qr"
                             data-ticket-code="<c:out value='${ticket.ticketCode}' />">
                        </div>

                        <p>Quét mã khi vào rạp</p>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${not empty printedBooking}">
        <div class="counter-card" id="purchaseSummary">
            <h2>
                Tổng thanh toán đơn
                #<c:out value="${printedBooking.bookingId}" />
            </h2>

            <p>
                Tiền vé:
                <fmt:formatNumber value="${printedBooking.totalAmount}"
                                  pattern="#,##0" /> đ
            </p>

            <c:choose>
                <c:when test="${not empty linkedFoodError}">
                    <p class="booking-error">
                        <c:out value="${linkedFoodError}" />
                    </p>
                </c:when>

                <c:otherwise>
                    <c:forEach var="order" items="${linkedFoodOrders}">
                        <p>
                            <a href="${ctx}/staff/food-sale?orderId=${order.foodOrderId}">
                                Xem hóa đơn bắp nước
                                #<c:out value="${order.foodOrderId}" />
                            </a>
                        </p>

                        <c:forEach var="item"
                                   items="${linkedFoodItems[order.foodOrderId]}">
                            <p>
                                <c:out value="${linkedFoodNames[item.foodOrderItemId]}" />
                                × <c:out value="${item.quantity}" />:
                                <fmt:formatNumber value="${item.lineTotal}"
                                                  pattern="#,##0" /> đ
                            </p>
                        </c:forEach>
                    </c:forEach>

                    <p>
                        Tiền bắp nước/combo:
                        <fmt:formatNumber value="${linkedFoodTotal}"
                                          pattern="#,##0" /> đ
                    </p>

                    <p>
                        Tổng tiền đã thanh toán cho vé và bắp nước:
                        <strong class="counter-total">
                            <fmt:formatNumber value="${purchaseTotal}"
                                              pattern="#,##0" /> đ
                        </strong>
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <c:if test="${saleMode}">
        <div class="counter-card">
            <h2>1. Chọn phim và suất chiếu</h2>

            <form method="get"
                  action="${ctx}/staff/ticket-sale"
                  class="counter-filter">

                <div class="counter-field">
                    <label for="movieId">Phim</label>

                    <select id="movieId"
                            name="movieId"
                            onchange="reloadTicketForm(this)">
                        <option value="">-- Chọn phim --</option>

                        <c:forEach var="movie" items="${movies}">
                            <option value="${movie.movieId}"
                                    <c:if test="${selectedMovieId == movie.movieId}">
                                        selected
                                    </c:if>>
                                <c:out value="${movie.title}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="counter-field">
                    <label for="date">Ngày chiếu</label>

                    <input type="date"
                           id="date"
                           name="date"
                           value="${selectedDate}"
                           min="${today}"
                           onchange="reloadTicketForm(this)" />
                </div>

                <div class="counter-field">
                    <label for="showtimeId">Suất chiếu</label>

                    <select id="showtimeId"
                            name="showtimeId"
                            <c:if test="${empty showtimes}">disabled</c:if>>
                                <option value="">-- Chọn suất chiếu --</option>

                            <c:forEach var="showtime" items="${showtimes}">
                                <option value="${showtime.showtimeId}"
                                        <c:if test="${selectedShowtimeId == showtime.showtimeId}">
                                            selected
                                        </c:if>>
                                    <fmt:formatDate value="${showtime.startTime}"
                                                    pattern="HH:mm" />
                                    - <c:out value="${rooms[showtime.roomId].roomName}" />
                                </option>
                            </c:forEach>
                    </select>
                </div>

                <button type="submit" class="booking-primary-button">
                    Xem ghế
                </button>
            </form>

            <c:if test="${empty movies and empty error}">
                <p class="counter-note">
                    Rạp hiện chưa có phim để bán vé.
                </p>
            </c:if>

            <c:if test="${not empty selectedMovieId
                          and empty showtimes and empty error}">
                  <p class="counter-note">
                      Phim chưa có suất chiếu sắp tới trong ngày đã chọn.
                  </p>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${not empty seatRows}">
                <div class="showtime-info-bar">
                    <div>
                        <span>Phim</span>
                        <strong>
                            <c:out value="${selectedMovie.title}" />
                        </strong>
                    </div>

                    <div>
                        <span>Phòng</span>
                        <strong>
                            <c:out value="${selectedRoom.roomName}" />
                        </strong>
                    </div>

                    <div>
                        <span>Thời gian</span>
                        <strong>
                            <fmt:formatDate value="${selectedShowtime.startTime}"
                                            pattern="dd/MM/yyyy HH:mm" />
                        </strong>
                    </div>
                </div>

                <form method="post"
                      action="${ctx}/staff/ticket-sale"
                      onsubmit="return prepareTicketSale()">

                    <input type="hidden" name="action" value="prepare" />
                    <input type="hidden"
                           name="movieId"
                           value="${selectedMovieId}" />
                    <input type="hidden"
                           name="date"
                           value="${selectedDate}" />
                    <input type="hidden"
                           name="showtimeId"
                           value="${selectedShowtimeId}" />
                    <input type="hidden"
                           name="saleToken"
                           value="${sessionScope.counterSaleToken}" />

                    <div class="cinema-seat-map">
                        <h2>2. Chọn ghế</h2>

                        <div class="cinema-screen-area">
                            <div class="cinema-screen-curve"></div>
                            <div class="cinema-screen-text">MÀN HÌNH</div>
                        </div>

                        <div class="cinema-rows">
                            <c:forEach var="row" items="${seatRows}">
                                <div class="cinema-row">
                                    <div class="cinema-row-name">
                                        H<c:out value="${row.key}" />
                                    </div>

                                    <div class="cinema-row-seats">
                                        <c:forEach var="seat" items="${row.value}">
                                            <c:set var="sold"
                                                   value="${occupiedSeatIds.contains(seat.seatId)}" />
                                            <c:set var="price"
                                                   value="${seatPrices[seat.seatId]}" />
                                            <c:set var="blocked"
                                                   value="${seat.status ne 'ACTIVE'
                                                            or sold or empty price}" />

                                            <label class="movie-seat
                                                   ${seat.seatType eq 'VIP' ? 'movie-seat-vip' : ''}
                                                   ${seat.seatType eq 'COUPLE' ? 'counter-couple' : ''}
                                                   ${sold ? 'movie-seat-occupied' : ''}
                                                   ${blocked and not sold ? 'movie-seat-disabled' : ''}">

                                                <input type="checkbox"
                                                       name="seatIds"
                                                       value="${seat.seatId}"
                                                       data-price="${price}"
                                                       onchange="updateTicketTotal()"
                                                       autocomplete="off"
                                                       <c:if test="${blocked}">disabled</c:if> />

                                                       <span class="movie-seat-box">
                                                       <c:out value="${seat.seatNumber}" />
                                                </span>

                                                <span class="counter-seat-price">
                                                    <c:choose>
                                                        <c:when test="${sold}">
                                                            Đã bán
                                                        </c:when>

                                                        <c:when test="${seat.status ne 'ACTIVE'}">
                                                            Ngừng sử dụng
                                                        </c:when>

                                                        <c:when test="${empty price}">
                                                            Chưa có giá
                                                        </c:when>

                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${price}"
                                                                              pattern="#,##0" /> đ
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </label>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="cinema-seat-legend">
                            <div>
                                <span class="legend-seat legend-available"></span>
                                Ghế thường
                            </div>
                            <div>
                                <span class="legend-seat legend-vip-seat"></span>
                                VIP
                            </div>
                            <div>
                                <span class="legend-seat counter-couple-color"></span>
                                Ghế đôi
                            </div>
                            <div>
                                <span class="legend-seat legend-selected"></span>
                                Đang chọn
                            </div>
                            <div>
                                <span class="legend-seat legend-occupied"></span>
                                Đã bán
                            </div>
                        </div>

                        <p class="counter-note">
                            Ghế ngừng sử dụng hoặc chưa có giá sẽ không chọn được.
                        </p>
                    </div>

                    <div class="counter-card counter-summary counter-summary-offset"
                         >
                        <div>
                            <h2>3. Xác nhận đơn tại quầy</h2>

                            <p>
                                Đã chọn:
                                <strong id="selectedSeatCount">0</strong> ghế
                            </p>

                            <p>
                                Tạm tính tiền vé:
                                <strong id="ticketTotal"
                                        class="counter-total">0 đ</strong>
                            </p>
                        </div>

                        <button type="submit"
                                id="createTicketButton"
                                class="booking-primary-button">
                            Tiếp tục
                        </button>
                    </div>
                </form>
            </c:when>

            <c:otherwise>
                <div class="counter-card">
                    <h2>2. Chọn ghế</h2>
                    <p class="counter-note">
                        Chọn phim, ngày và suất chiếu rồi bấm Xem ghế.
                    </p>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

</section>

<c:if test="${not empty printedTickets}">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
</c:if>

<script>
                                                           var ticketQrReady = false;

                                                           function prepareTicketSale() {
                                                               var seats = document.querySelectorAll(
                                                                       'input[name="seatIds"]:checked');

                                                               if (seats.length === 0) {
                                                                   alert("Vui lòng chọn ít nhất một ghế.");
                                                                   return false;
                                                               }

                                                               document.getElementById("createTicketButton").disabled = true;
                                                               return true;
                                                           }

                                                           function confirmCounterPayment(form) {
                                                               if (!confirm("Xác nhận đã thu đủ tổng tiền của đơn này?")) {
                                                                   return false;
                                                               }

                                                               form.querySelector('button[type="submit"]').disabled = true;
                                                               return true;
                                                           }

                                                           function reloadTicketForm(field) {
                                                               // Đổi phim hoặc ngày thì bỏ suất chiếu cũ.
                                                               document.getElementById("showtimeId").value = "";
                                                               field.form.submit();
                                                           }

                                                           function updateTicketTotal() {
                                                               var seats = document.getElementsByName("seatIds");
                                                               var total = 0;
                                                               var count = 0;

                                                               // Chỉ là tiền hiển thị; server kiểm tra giá lại từ database.
                                                               for (var i = 0; i < seats.length; i++) {
                                                                   if (seats[i].checked && !seats[i].disabled) {
                                                                       total += Number(seats[i].getAttribute("data-price"));
                                                                       count++;
                                                                   }
                                                               }

                                                               document.getElementById("selectedSeatCount").textContent = count;

                                                               document.getElementById("ticketTotal").textContent =
                                                                       total.toLocaleString("vi-VN", {
                                                                           maximumFractionDigits: 2
                                                                       }) + " đ";
                                                           }

                                                           function prepareTicketQrCodes() {
                                                               var boxes = document.querySelectorAll(".ticket-qr");

                                                               if (boxes.length === 0) {
                                                                   return;
                                                               }

                                                               var error = document.getElementById("ticketQrError");

                                                               try {
                                                                   if (typeof QRCode === "undefined") {
                                                                       throw new Error("Không tải được thư viện QR.");
                                                                   }

                                                                   // QR chứa mã vé đã lưu để chức năng check-in đọc được.
                                                                   for (var i = 0; i < boxes.length; i++) {
                                                                       boxes[i].innerHTML = "";

                                                                       new QRCode(boxes[i], {
                                                                           text: boxes[i].getAttribute("data-ticket-code"),
                                                                           width: 128,
                                                                           height: 128,
                                                                           colorDark: "#000000",
                                                                           colorLight: "#ffffff",
                                                                           correctLevel: QRCode.CorrectLevel.M
                                                                       });
                                                                   }

                                                                   ticketQrReady = true;
                                                                   document.getElementById("printTicketButton").disabled = false;

                                                               } catch (e) {
                                                                   error.hidden = false;
                                                                   error.textContent =
                                                                           "Chưa tạo được QR. Kiểm tra kết nối mạng rồi tải lại trang. "
                                                                           + "Vé đã lưu, không tạo lại đơn.";
                                                               }
                                                           }

                                                           function printCounterTickets() {
                                                               if (!ticketQrReady) {
                                                                   alert("Vui lòng chờ mã QR hiển thị rồi in vé.");
                                                                   return;
                                                               }

                                                               window.print();
                                                           }

                                                           if (document.getElementById("ticketTotal") != null) {
                                                               updateTicketTotal();
                                                           }

                                                           prepareTicketQrCodes();
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>