<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<style>
    .checkin-page {
        max-width: 1050px;
        margin: 0 auto;
        padding: 40px 30px 60px;
    }

    .checkin-heading h1 {
        margin: 0;
        padding-left: 15px;
        border-left: 5px solid #ff243e;
        font-size: 28px;
        line-height: 1.3;
    }

    .checkin-heading p {
        color: #cccccc;
    }

    .checkin-box {
        margin-top: 25px;
        padding: 25px;
        background: #18181c;
        border: 1px solid #33333a;
        border-radius: 8px;
    }

    .checkin-box h2 {
        margin: 0 0 20px;
        font-size: 22px;
    }

    .checkin-field {
        margin-bottom: 18px;
    }

    .checkin-field label {
        display: block;
        margin-bottom: 8px;
        font-weight: bold;
    }

    .checkin-field input {
        width: 100%;
        box-sizing: border-box;
        padding: 11px;
        background: #101012;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
    }

    .checkin-field input:focus {
        outline: none;
        border-color: #ff3045;
    }

    .checkin-button {
        padding: 11px 20px;
        border: none;
        border-radius: 5px;
        background: #ff3045;
        color: white;
        font-weight: bold;
        cursor: pointer;
    }

    .checkin-button:hover {
        background: #e6293d;
    }

    .checkin-button:disabled {
        background: #55555f;
        color: #bbbbbb;
        cursor: not-allowed;
    }

    .checkin-button-secondary {
        background: #34343c;
    }

    .checkin-button-secondary:hover {
        background: #454550;
    }

    .checkin-message {
        margin-top: 20px;
        padding: 14px;
        border-radius: 5px;
    }

    .checkin-error {
        border: 1px solid #ff3045;
        background: #40191d;
    }

    .checkin-success {
        border: 1px solid #2fa66a;
        background: #153827;
    }

    .checkin-note {
        color: #aaaaaa;
        font-size: 13px;
        line-height: 1.6;
    }

    .checkin-ticket-card {
        grid-template-columns: minmax(0, 1fr);
        margin: 25px auto 0;
    }

    .checkin-ticket-card .cinema-ticket-main {
        padding: 30px;
    }

    .checkin-ticket-card .cinema-ticket-grid strong {
        overflow-wrap: anywhere;
    }

    .checkin-food-section {
        margin-top: 24px;
        padding-top: 20px;
        border-top: 1px dashed #55555f;
    }

    .checkin-food-section h3 {
        margin: 0 0 16px;
        font-size: 18px;
    }

    .checkin-food-list {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    .checkin-food-item {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 20px;
    }

    .checkin-food-item strong {
        display: block;
        color: #f1f1f3;
        font-size: 15px;
    }

    .checkin-food-item p {
        margin: 5px 0 0;
        color: #85858d;
        font-size: 12px;
    }

    .checkin-food-summary {
        min-width: 125px;
        text-align: right;
    }

    .checkin-food-summary span {
        display: block;
        margin-bottom: 5px;
        color: #a5a5ad;
        font-size: 13px;
    }

    .checkin-food-summary strong {
        color: #ff4652;
        white-space: nowrap;
    }

    .checkin-ticket-action {
        margin-top: 24px;
        padding-top: 20px;
        border-top: 1px solid #303038;
    }

    .checkin-ticket-action .checkin-button {
        min-width: 210px;
    }

    .qr-section {
        margin-top: 25px;
        padding-top: 20px;
        border-top: 1px solid #33333a;
    }

    .qr-section h3 {
        margin: 0 0 15px;
        font-size: 18px;
    }

    .qr-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
    }

    #qrReader {
        max-width: 500px;
        margin: 16px auto;
    }

    #qrReader:not(:empty) {
        padding: 10px;
        background: white;
        color: #151515;
        border-radius: 6px;
    }

    #qrMessage {
        margin-bottom: 0;
    }

    @media (max-width: 600px) {
        .checkin-page {
            padding: 30px 15px;
        }

    }
</style>

<section class="checkin-page">

    <div class="checkin-heading">
        <h1>Kiểm tra và check-in vé</h1>
        <p>Nhập mã vé hoặc quét QR để tra cứu thông tin vé và suất chiếu.</p>
    </div>

    <c:if test="${not empty success}">
        <div class="checkin-message checkin-success">
            <c:out value="${success}" />
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="checkin-message checkin-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="checkin-box">
        <h2>1. Tra cứu vé</h2>

        <form id="lookupForm"
              action="${ctx}/staff/check-in"
              method="get">

            <div class="checkin-field">
                <label for="ticketCode">Mã vé</label>

                <input type="text"
                       id="ticketCode"
                       name="ticketCode"
                       maxlength="100"
                       required
                       autocomplete="off"
                       value="<c:out value='${ticketCode}' />"
                       placeholder="Nhập mã vé của khách hàng"
                       oninput="hideTicketResult()" />
            </div>

            <button type="submit" class="checkin-button">
                Tra cứu vé
            </button>
        </form>

        <div class="qr-section">
            <h3>Quét mã QR bằng camera</h3>

            <div class="qr-buttons">
                <button type="button"
                        id="startQrButton"
                        class="checkin-button"
                        onclick="startQrScanner()">
                    Mở bộ quét QR
                </button>

                <button type="button"
                        id="stopQrButton"
                        class="checkin-button checkin-button-secondary"
                        onclick="stopQrScanner()"
                        disabled>
                    Tắt bộ quét
                </button>
            </div>

            <div id="qrReader"></div>

            <p id="qrMessage"
               class="checkin-note"
               aria-live="polite"></p>
        </div>
    </div>

    <c:if test="${not empty ticket}">
        <div class="cinema-ticket checkin-ticket-card" id="ticketResult">
            <div class="cinema-ticket-main">
                <div class="cinema-ticket-top">
                    <div>
                        <span class="ticket-label">F-CINEMA TICKET</span>
                        <h2><c:out value="${movie.title}" /></h2>
                    </div>

                    <c:choose>
                        <c:when test="${ticket.status eq 'USED'}">
                            <span class="ticket-status ticket-status-used">Đã sử dụng</span>
                        </c:when>
                        <c:when test="${ticket.status eq 'CANCELLED'}">
                            <span class="ticket-status ticket-status-used">Đã hủy</span>
                        </c:when>
                        <c:when test="${ticket.status eq 'PENDING'}">
                            <span class="ticket-status ticket-status-used">Chưa xác nhận</span>
                        </c:when>
                        <c:otherwise>
                            <span class="ticket-status ticket-status-valid">Có hiệu lực</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="cinema-ticket-grid">
                    <div>
                        <span>Rạp chiếu</span>
                        <strong><c:out value="${cinema.cinemaName}" /></strong>
                    </div>

                    <div>
                        <span>Địa chỉ rạp</span>
                        <strong><c:out value="${cinema.address}" /></strong>
                    </div>

                    <div>
                        <span>Ngày giờ chiếu</span>
                        <strong>
                            <fmt:formatDate value="${showtime.startTime}" pattern="dd/MM/yyyy HH:mm" />
                        </strong>
                    </div>

                    <div>
                        <span>Ghế</span>
                        <strong>Hàng <c:out value="${seat.seatRow}" /> - Ghế <c:out value="${seat.seatNumber}" /></strong>
                    </div>

                    <div>
                        <span>Loại ghế</span>
                        <strong><c:out value="${seat.seatType}" /></strong>
                    </div>

                    <div>
                        <span>Suất chiếu</span>
                        <strong>#<c:out value="${showtime.showtimeId}" /></strong>
                    </div>

                    <div>
                        <span>Phòng chiếu</span>
                        <strong>Phòng #<c:out value="${showtime.roomId}" /></strong>
                    </div>

                    <div>
                        <span>Mã đơn</span>
                        <strong>#<c:out value="${ticket.bookingId}" /></strong>
                    </div>

                    <div>
                        <span>Giá vé</span>
                        <strong class="cinema-ticket-price">
                            <fmt:formatNumber value="${ticket.ticketPrice}" pattern="#,##0" /> VND
                        </strong>
                    </div>
                </div>

                <c:if test="${not empty ticket.checkedInAt}">
                    <div class="ticket-checkin-note">
                        Đã check-in lúc
                        <strong>
                            <fmt:formatDate value="${ticket.checkedInAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                        </strong>
                    </div>
                </c:if>

                <c:if test="${not empty paidFoodItems}">
                    <div class="checkin-food-section">
                        <h3>Bắp nước / Combo</h3>

                        <div class="checkin-food-list">
                            <c:forEach var="foodItem" items="${paidFoodItems}">
                                <div class="checkin-food-item">
                                    <div>
                                        <strong><c:out value="${foodItem.productName}" /></strong>
                                        <p>Sản phẩm đi kèm đơn vé #<c:out value="${ticket.bookingId}" /></p>
                                    </div>

                                    <div class="checkin-food-summary">
                                        <span>Số lượng: <c:out value="${foodItem.quantity}" /></span>
                                        <strong>
                                            <fmt:formatNumber value="${foodItem.lineTotal}" pattern="#,##0" /> VND
                                        </strong>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <c:if test="${canCheckIn}">
                    <div class="checkin-ticket-action">
                        <form action="${ctx}/staff/check-in"
                              method="post"
                              onsubmit="return confirm('Xác nhận cho khách sử dụng vé này?')">

                            <input type="hidden"
                                   name="ticketCode"
                                   value="<c:out value='${ticket.ticketCode}' />" />

                            <input type="hidden"
                                   name="token"
                                   value="${sessionScope.checkInToken}" />

                            <button type="submit" class="checkin-button">
                                Xác nhận check-in
                            </button>
                        </form>
                    </div>
                </c:if>
            </div>
        </div>
    </c:if>

</section>

<script src="https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js"></script>

<script>
                          var qrScanner = null;
                          var qrHandled = false;
                          var qrStopping = false;

                          function setQrMessage(message) {
                              document.getElementById("qrMessage").textContent = message;
                          }

                          function hideTicketResult() {
                              var result = document.getElementById("ticketResult");

                              if (result != null) {
                                  result.style.display = "none";
                              }
                          }

                          function resetQrButtons() {
                              qrScanner = null;
                              qrStopping = false;

                              document.getElementById("startQrButton").disabled = false;
                              document.getElementById("stopQrButton").disabled = true;
                          }

                          function startQrScanner() {
                              if (qrScanner != null) {
                                  return;
                              }

                              if (typeof Html5QrcodeScanner == "undefined") {
                                  setQrMessage(
                                          "Không tải được thư viện QR. "
                                          + "Kiểm tra Internet hoặc nhập mã vé.");
                                  return;
                              }

                              if (!window.isSecureContext) {
                                  setQrMessage(
                                          "Camera cần localhost hoặc HTTPS. "
                                          + "Hãy mở project bằng localhost.");
                                  return;
                              }

                              qrHandled = false;
                              qrStopping = false;
                              hideTicketResult();

                              document.getElementById("startQrButton").disabled = true;
                              document.getElementById("stopQrButton").disabled = false;

                              try {
                                  qrScanner = new Html5QrcodeScanner(
                                          "qrReader",
                                          {
                                              fps: 10,
                                              formatsToSupport: [
                                                  Html5QrcodeSupportedFormats.QR_CODE
                                              ],
                                              supportedScanTypes: [
                                                  Html5QrcodeScanType.SCAN_TYPE_CAMERA
                                              ]
                                          },
                                          false
                                          );

                                  qrScanner.render(
                                          onQrScanned,
                                          function (error) {
                                              // Chưa thấy QR trong một khung hình là bình thường.
                                          }
                                  );

                                  setQrMessage(
                                          "Cho phép camera, chọn camera "
                                          + "rồi bấm Start Scanning.");

                              } catch (error) {
                                  resetQrButtons();

                                  setQrMessage(
                                          "Không khởi tạo được bộ quét. "
                                          + "Hãy nhập mã vé hoặc tải lại trang.");
                              }
                          }

                          function onQrScanned(decodedText) {
                              // Một QR có thể xuất hiện trong nhiều khung hình.
                              // Chỉ xử lý lần đầu để tránh gửi nhiều yêu cầu tra cứu.
                              if (qrHandled || qrStopping || qrScanner == null) {
                                  return;
                              }

                              var code = decodedText.trim();

                              if (code == "" || code.length > 100) {
                                  setQrMessage(
                                          "QR không chứa mã vé hợp lệ. "
                                          + "Hãy đưa mã QR khác vào camera.");
                                  return;
                              }

                              qrHandled = true;
                              qrStopping = true;

                              document.getElementById("stopQrButton").disabled = true;
                              document.getElementById("ticketCode").value = code;

                              hideTicketResult();
                              setQrMessage("Đã đọc mã vé. Đang tra cứu...");

                              // clear() trả về Promise:
                              // Đợi tắt camera xong mới chuyển sang trang kết quả tra cứu.
                              qrScanner.clear().then(function () {
                                  resetQrButtons();
                                  document.getElementById("lookupForm").submit();

                              }).catch(function (error) {
                                  qrStopping = false;

                                  document.getElementById("stopQrButton").disabled = false;

                                  setQrMessage(
                                          "Đã đọc mã nhưng chưa tắt được camera. "
                                          + "Bấm Tắt bộ quét rồi Tra cứu vé.");
                              });
                          }

                          function stopQrScanner() {
                              if (qrScanner == null || qrStopping) {
                                  return;
                              }

                              qrStopping = true;
                              qrHandled = true;

                              document.getElementById("stopQrButton").disabled = true;

                              qrScanner.clear().then(function () {
                                  resetQrButtons();
                                  setQrMessage("Đã tắt bộ quét.");

                              }).catch(function (error) {
                                  qrStopping = false;

                                  document.getElementById("stopQrButton").disabled = false;

                                  setQrMessage(
                                          "Chưa tắt được bộ quét. "
                                          + "Hãy thử lại hoặc tải lại trang.");
                              });
                          }
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>