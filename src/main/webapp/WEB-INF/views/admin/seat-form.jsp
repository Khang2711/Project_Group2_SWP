<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .form-page {
        max-width: 720px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .form-page h1 {
        margin: 0 0 8px;
        font-size: 32px;
    }

    .subtitle {
        color: #999;
        margin-bottom: 28px;
    }

    .form-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
        padding: 28px;
    }

    .room-name {
        background: #1c1c1f;
        padding: 13px 15px;
        border-radius: 7px;
        margin-bottom: 24px;
        color: #aaa;
    }

    .room-name strong {
        color: white;
    }

    .form-group {
        margin-bottom: 21px;
    }

    label {
        display: block;
        color: #ccc;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .form-control {
        width: 100%;
        box-sizing: border-box;
        background: #1c1c1f;
        color: white;
        border: 1px solid #35353a;
        border-radius: 7px;
        padding: 12px 13px;
        outline: none;
    }

    .form-control:focus {
        border-color: #ff1744;
    }

    .error {
        background: #32181d;
        border: 1px solid #6b2935;
        color: #ff8095;
        padding: 12px 15px;
        border-radius: 7px;
        margin-bottom: 20px;
    }

    .btn-primary {
        border: none;
        background: #ff1744;
        color: white;
        padding: 11px 20px;
        border-radius: 7px;
        cursor: pointer;
        font-weight: 600;
    }

    .btn-secondary {
        margin-left: 14px;
        color: #aaa;
        text-decoration: none;
    }
</style>

<div class="form-page">

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