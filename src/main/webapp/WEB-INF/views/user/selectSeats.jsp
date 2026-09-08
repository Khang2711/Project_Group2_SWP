<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.Showtime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Chọn ghế");
    String ctx = request.getContextPath();
    Showtime showtime = (Showtime) request.getAttribute("showtime");
    List<String> bookedSeats = (List<String>) request.getAttribute("bookedSeats");
    String error = (String) request.getAttribute("error");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel seat-selection-panel">
    <div class="toolbar">
        <div>
            <h1 class="page-title">Chọn ghế</h1>
            <p class="muted">Ghế màu xám đã được đặt và không thể chọn.</p>
        </div>
    </div>

    <% if (error != null) { %>
    <div class="alert alert-error"><%= error%></div>
    <% } %>

    <% if (showtime != null) { %>
    <div class="showtime-summary">
        <div>
            <strong><%= showtime.getMovieTitle()%></strong>
            <span><%= showtime.getRoomName()%></span>
        </div>
        <div>
            <span><%= showtime.getShowDate()%></span>
            <strong><%= showtime.getStartTime()%> - <%= showtime.getEndTime()%></strong>
        </div>
        <div>
            <span>Giá vé</span>
            <strong><%= df.format(showtime.getBasePrice())%> VNĐ</strong>
        </div>
    </div>

    <form action="<%= ctx%>/cart" method="post" id="seatForm">
        <input type="hidden" name="action" value="add">
        <input type="hidden" name="showtimeID" value="<%= showtime.getShowtimeID()%>">

        <div class="seat-tools">
            <button class="btn btn-soft" type="button" id="selectAllSeatsButton" onclick="toggleAllAvailableSeats()">
                Bao rạp / Chọn tất cả
            </button>
            <span class="selected-seat-count">Đã chọn: <strong id="selectedSeatCount">0</strong> ghế</span>
        </div>

        <div class="seat-stage">MÀN HÌNH</div>
        <div class="seat-map">
            <%
                int totalSeats = showtime.getTotalSeats();
                int seatsPerRow = 10;
                int totalRows = (int) Math.ceil(totalSeats * 1.0 / seatsPerRow);

                for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {
                    char rowName = (char) ('A' + rowIndex);
            %>
            <div class="seat-row">
                <span class="seat-row-name"><%= rowName%></span>
                <%
                    for (int number = 1; number <= seatsPerRow; number++) {
                        int seatPosition = rowIndex * seatsPerRow + number;
                        if (seatPosition > totalSeats) {
                            break;
                        }

                        String seatID = String.valueOf(rowName) + number;
                        boolean booked = bookedSeats != null && bookedSeats.contains(seatID);
                %>
                <% if (booked) { %>
                <label class="seat booked"><%= seatID%></label>
                <% } else { %>
                <label class="seat">
                    <input type="checkbox" name="seatID" value="<%= seatID%>" onchange="updateSelectedSeatCount()">
                    <%= seatID%>
                </label>
                <% } %>
                <% } %>
            </div>
            <% } %>
        </div>

        <div class="seat-actions">
            <button class="btn btn-primary" type="submit">Thêm vé vào giỏ</button>
            <a class="btn btn-blue" href="<%= ctx%>/movies">Quay lại lịch chiếu</a>
        </div>
    </form>
    <% } else { %>
    <div class="alert alert-error">Suất chiếu không tồn tại hoặc đã qua giờ mở bán.</div>
    <a class="btn btn-blue" href="<%= ctx%>/movies">Quay lại lịch chiếu</a>
    <% } %>
</section>

<script>
    function getAvailableSeatCheckboxes() {
        return Array.from(document.querySelectorAll('input[name="seatID"]'));
    }

    function updateSelectedSeatCount() {
        const seats = getAvailableSeatCheckboxes();
        const selectedCount = seats.filter(seat => seat.checked).length;
        const countElement = document.getElementById('selectedSeatCount');
        const selectAllButton = document.getElementById('selectAllSeatsButton');

        if (countElement) {
            countElement.textContent = selectedCount;
        }

        if (selectAllButton) {
            const allSelected = seats.length > 0 && seats.every(seat => seat.checked);
            selectAllButton.textContent = allSelected
                    ? 'Bỏ chọn tất cả'
                    : 'Bao rạp / Chọn tất cả';
        }
    }

    function toggleAllAvailableSeats() {
        const seats = getAvailableSeatCheckboxes();
        const shouldSelectAll = seats.some(seat => !seat.checked);
        seats.forEach(seat => {
            seat.checked = shouldSelectAll;
        });
        updateSelectedSeatCount();
    }

    updateSelectedSeatCount();
</script>

<%@include file="/WEB-INF/include/footer.jsp" %>
