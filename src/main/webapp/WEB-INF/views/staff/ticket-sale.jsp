<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<h1>Bán vé tại quầy</h1>
<p>Staff Counter · Ticket Sale</p>

<section>
    <h2>1. Chọn phim và suất chiếu</h2>

    <form method="get" action="${ctx}/staff/ticket-sale">

        <label for="movieId">Phim</label><br>
        <select id="movieId" name="movieId">
            <option value="">-- Chọn phim --</option>

            <c:forEach var="movie" items="${movies}">
                <option value="${movie.movieId}">
                    <c:out value="${movie.title}" />
                </option>
            </c:forEach>
        </select>

        <br><br>

        <label for="showtimeId">Suất chiếu</label><br>
        <select id="showtimeId" name="showtimeId">
            <option value="">-- Chọn suất chiếu --</option>

            <c:forEach var="showtime" items="${showtimes}">
                <option value="${showtime.showtimeId}">
                    <c:out value="${showtime.startTime}" />
                </option>
            </c:forEach>
        </select>

        <br><br>

        <button type="submit">Xem ghế</button>
    </form>

    <c:if test="${empty movies}">
        <p style="color:#aaaaaa; margin-top:15px;">
            Chưa có dữ liệu phim. Controller/Service sẽ truyền danh sách phim vào sau.
        </p>
    </c:if>
</section>

<section style="margin-top:30px;">
    <h2>2. Chọn ghế</h2>

    <c:choose>
        <c:when test="${not empty seats}">
            <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top:15px;">
                <c:forEach var="seat" items="${seats}">
                    <label style="padding:10px; border:1px solid #3a3a42; border-radius:6px;">
                        <input type="checkbox"
                               name="seatIds"
                               value="${seat.seatId}">
                        Ghế <c:out value="${seat.seatNumber}" />
                    </label>
                </c:forEach>
            </div>
        </c:when>

        <c:otherwise>
            <p style="color:#aaaaaa;">
                Chọn suất chiếu để hiển thị sơ đồ ghế.
            </p>
        </c:otherwise>
    </c:choose>
</section>

<section style="margin-top:30px;">
    <h2>3. Xác nhận bán vé</h2>

    <p>Thông tin vé, tổng tiền và phương thức thanh toán sẽ hiển thị tại đây.</p>

    <button type="button" disabled>
        Tạo vé
    </button>

    <p style="color:#aaaaaa; margin-top:10px;">
        Nút đang khóa vì business logic bán vé tại quầy chưa được triển khai.
    </p>
</section>

<%-- TODO:
     Controller:
     - lấy phim đang chiếu tại cinema của staff
     - lấy showtime theo movie
     - lấy seat theo room/showtime

     Service:
     - kiểm tra seat còn trống
     - tính giá
     - tạo Booking/Ticket phù hợp với business rule hiện tại
     - tạo Payment khi thanh toán thành công

     Không viết SQL trực tiếp trong JSP.
--%>

<%@ include file="/WEB-INF/include/footer.jsp" %>
