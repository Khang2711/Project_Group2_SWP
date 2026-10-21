<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .form-page {
        max-width: 850px;
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

    .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 18px;
    }

    .form-group {
        margin-bottom: 21px;
    }

    .form-group.full {
        grid-column: 1 / -1;
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

    .form-control[readonly] {
        color: #aaa;
    }

    .help-text {
        margin-top: 7px;
        color: #888;
        font-size: 13px;
        line-height: 1.5;
    }

    .error-box {
        background: #32181d;
        border: 1px solid #6b2935;
        color: #ff8095;
        padding: 12px 15px;
        border-radius: 7px;
        margin-bottom: 20px;
    }

    .layout-preview {
        background: #1a1a1e;
        border: 1px solid #303036;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 21px;
    }

    .layout-preview strong {
        color: white;
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

    .seat-link {
        display: inline-block;
        margin-top: 8px;
        color: #ff4265;
        text-decoration: none;
    }

    @media (max-width: 700px) {

        .form-grid {
            grid-template-columns: 1fr;
        }

        .form-group.full {
            grid-column: auto;
        }
    }

</style>

<div class="form-page">

    <c:set var="isEdit"
           value="${room.roomId > 0}" />

    <c:choose>

        <c:when test="${isEdit}">

            <h1>Cập nhật phòng chiếu</h1>

            <p class="subtitle">
                Chỉnh thông tin phòng. Số lượng ghế
                được quản lý trong sơ đồ ghế.
            </p>

        </c:when>

        <c:otherwise>

            <h1>Thêm phòng chiếu</h1>

            <p class="subtitle">
                Tự chọn loại phòng, số hàng
                và số ghế mỗi hàng.
            </p>

        </c:otherwise>

    </c:choose>

    <div class="form-card">

        <c:if test="${not empty error}">

            <div class="error-box">

                <c:out value="${error}" />

            </div>

        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/room-form">

            <c:if test="${isEdit}">

                <input type="hidden"
                       name="id"
                       value="${room.roomId}">

            </c:if>

            <div class="form-grid">

                <div class="form-group full">

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

                    <select class="form-control"
                            name="roomType">

                        <option value="2D"
                                ${empty room.roomType ||
                                  room.roomType == '2D'
                                  ? 'selected' : ''}>
                            2D
                        </option>

                        <option value="3D"
                                ${room.roomType == '3D'
                                  ? 'selected' : ''}>
                            3D
                        </option>

                        <option value="IMAX"
                                ${room.roomType == 'IMAX'
                                  ? 'selected' : ''}>
                            IMAX
                        </option>

                    </select>

                    <div class="help-text">
                        Loại phòng không còn quyết định
                        số lượng ghế.
                    </div>

                </div>

                <div class="form-group">

                    <label>Trạng thái</label>

                    <select class="form-control"
                            name="status">

                        <option value="ACTIVE"
                                ${empty room.status ||
                                  room.status == 'ACTIVE'
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

                <!-- CREATE -->

                <c:if test="${not isEdit}">

                    <div class="form-group">

                        <label>Số hàng ghế</label>

                        <input id="rowCount"
                               class="form-control"
                               type="number"
                               name="rowCount"
                               min="1"
                               max="30"
                               required
                               value="${empty rowCount ? 5 : rowCount}"
                               oninput="updateCapacity()">

                    </div>

                    <div class="form-group">

                        <label>Số ghế mỗi hàng</label>

                        <input id="seatsPerRow"
                               class="form-control"
                               type="number"
                               name="seatsPerRow"
                               min="1"
                               max="50"
                               required
                               value="${empty seatsPerRow ? 8 : seatsPerRow}"
                               oninput="updateCapacity()">

                    </div>

                    <div class="form-group full">

                        <div class="layout-preview">

                            Sơ đồ ban đầu:

                            <strong id="layoutResult">
                                5 hàng × 8 ghế = 40 ghế
                            </strong>

                        </div>

                    </div>

                </c:if>

                <!-- EDIT -->

                <c:if test="${isEdit}">

                    <div class="form-group full">

                        <label>Sức chứa hiện tại</label>

                        <input class="form-control"
                               type="text"
                               readonly
                               value="${room.capacity} ghế">

                        <div class="help-text">
                            Muốn tăng hoặc giảm số ghế,
                            hãy chỉnh trong Quản lý ghế.
                        </div>

                        <a class="seat-link"
                           href="${pageContext.request.contextPath}/admin/seats?roomId=${room.roomId}">

                            → Mở quản lý ghế

                        </a>

                    </div>

                </c:if>

            </div>

            <button class="btn-primary"
                    type="submit">

                <c:choose>

                    <c:when test="${isEdit}">
                        Lưu thay đổi
                    </c:when>

                    <c:otherwise>
                        Tạo phòng
                    </c:otherwise>

                </c:choose>

            </button>

            <a class="btn-secondary"
               href="${pageContext.request.contextPath}/admin/rooms">

                Hủy

            </a>

        </form>

    </div>

</div>

<c:if test="${not isEdit}">

    <script>

        function updateCapacity() {

            const rows =
                    parseInt(
                        document.getElementById(
                            "rowCount"
                        ).value
                    ) || 0;

            const seats =
                    parseInt(
                        document.getElementById(
                            "seatsPerRow"
                        ).value
                    ) || 0;

            const total =
                    rows * seats;

            document.getElementById(
                    "layoutResult"
            ).textContent =
                    rows
                    + " hàng × "
                    + seats
                    + " ghế = "
                    + total
                    + " ghế";
        }

        updateCapacity();

    </script>

</c:if>

<%@ include file="/WEB-INF/include/footer.jsp" %>