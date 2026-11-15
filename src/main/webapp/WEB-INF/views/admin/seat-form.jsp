<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="form-page" data-css-page="admin-seat-form">

    <c:choose>
        <c:when test="${seat.seatId > 0}">
            <h1>Cập nhật ghế</h1>
        </c:when>

        <c:otherwise>
            <h1>Thêm ghế</h1>
        </c:otherwise>
    </c:choose>

    <p class="subtitle">
        Cấu hình vị trí, loại và trạng thái ghế.
    </p>

    <div class="form-card">

        <div class="room-name">
            Phòng:
            <strong>
                <c:out value="${room.roomName}" />
            </strong>
        </div>

        <c:if test="${not empty error}">
            <div class="error">
                <c:out value="${error}" />
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/seat-form">

            <input type="hidden"
                   name="roomId"
                   value="${room.roomId}">

            <c:if test="${seat.seatId > 0}">
                <input type="hidden"
                       name="id"
                       value="${seat.seatId}">
            </c:if>

            <div class="form-group">
                <label>Hàng ghế</label>

                <input class="form-control"
                       type="number"
                       name="seatRow"
                       min="1"
                       required
                       value="${seat.seatRow > 0 ? seat.seatRow : ''}">
            </div>

            <div class="form-group">
                <label>Số ghế</label>

                <input class="form-control"
                       type="number"
                       name="seatNumber"
                       min="1"
                       required
                       value="${seat.seatNumber > 0 ? seat.seatNumber : ''}">
            </div>

            <div class="form-group">
                <label>Loại ghế</label>

                <select class="form-control" name="seatType">

                    <option value="STANDARD"
                            ${empty seat.seatType ||
                              seat.seatType == 'STANDARD'
                              ? 'selected' : ''}>
                        STANDARD
                    </option>

                    <option value="VIP"
                            ${seat.seatType == 'VIP'
                              ? 'selected' : ''}>
                        VIP
                    </option>

                    <option value="COUPLE"
                            ${seat.seatType == 'COUPLE'
                              ? 'selected' : ''}>
                        COUPLE
                    </option>

                </select>
            </div>

            <div class="form-group">
                <label>Trạng thái</label>

                <select class="form-control" name="status">

                    <option value="ACTIVE"
                            ${empty seat.status ||
                              seat.status == 'ACTIVE'
                              ? 'selected' : ''}>
                        ACTIVE
                    </option>

                    <option value="INACTIVE"
                            ${seat.status == 'INACTIVE'
                              ? 'selected' : ''}>
                        INACTIVE
                    </option>

                </select>
            </div>

            <button class="btn-primary" type="submit">
                Lưu ghế
            </button>

            <a class="btn-secondary"
               href="${pageContext.request.contextPath}/admin/seats?roomId=${room.roomId}">
                Hủy
            </a>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>