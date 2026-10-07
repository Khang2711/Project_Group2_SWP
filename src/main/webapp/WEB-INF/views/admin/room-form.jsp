<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .form-page {
        max-width: 760px;
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
        margin: 0 0 28px;
    }

    .form-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
        padding: 28px;
    }

    .form-group {
        margin-bottom: 21px;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        color: #ccc;
        font-weight: 600;
    }

    .form-control {
        width: 100%;
        box-sizing: border-box;
        background: #1c1c1f;
        border: 1px solid #35353a;
        border-radius: 7px;
        color: white;
        padding: 12px 13px;
        outline: none;
    }

    .form-control:focus {
        border-color: #ff1744;
    }

    .error-box {
        background: #32181d;
        border: 1px solid #6b2935;
        color: #ff8095;
        padding: 12px 15px;
        border-radius: 7px;
        margin-bottom: 20px;
    }

    .btn-primary {
        border: 0;
        background: #ff1744;
        color: white;
        padding: 11px 20px;
        border-radius: 7px;
        cursor: pointer;
        font-weight: 600;
    }

    .btn-secondary {
        color: #aaa;
        margin-left: 14px;
        text-decoration: none;
    }
</style>

<div class="form-page">

    <c:choose>
        <c:when test="${room.roomId > 0}">
            <h1>Cập nhật phòng chiếu</h1>
        </c:when>
        <c:otherwise>
            <h1>Thêm phòng chiếu</h1>
        </c:otherwise>
    </c:choose>

    <p class="subtitle">
        Thiết lập thông tin và sức chứa của phòng.
    </p>

    <div class="form-card">

        <c:if test="${not empty error}">
            <div class="error-box">
                <c:out value="${error}" />
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/room-form">

            <c:if test="${room.roomId > 0}">
                <input type="hidden"
                       name="id"
                       value="${room.roomId}">
            </c:if>

            <div class="form-group">
                <label>Tên phòng</label>

                <input class="form-control"
                       type="text"
                       name="roomName"
                       maxlength="100"
                       required
                       value="<c:out value='${room.roomName}' />">
            </div>

            <div class="form-group">
                <label>Loại phòng</label>

                <input class="form-control"
                       type="text"
                       name="roomType"
                       maxlength="50"
                       placeholder="Ví dụ: 2D, 3D, IMAX"
                       value="<c:out value='${room.roomType}' />">
            </div>

            <div class="form-group">
                <label>Sức chứa</label>

                <input class="form-control"
                       type="number"
                       name="capacity"
                       min="1"
                       required
                       value="${room.capacity > 0 ? room.capacity : ''}">
            </div>

            <div class="form-group">
                <label>Trạng thái</label>

                <select class="form-control" name="status">
                    <option value="ACTIVE"
                            ${empty room.status || room.status == 'ACTIVE'
                              ? 'selected' : ''}>
                        ACTIVE
                    </option>

                    <option value="INACTIVE"
                            ${room.status == 'INACTIVE'
                              ? 'selected' : ''}>
                        INACTIVE
                    </option>
                </select>
            </div>

            <button class="btn-primary" type="submit">
                Lưu phòng
            </button>

            <a class="btn-secondary"
               href="${pageContext.request.contextPath}/admin/rooms">
                Hủy
            </a>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>