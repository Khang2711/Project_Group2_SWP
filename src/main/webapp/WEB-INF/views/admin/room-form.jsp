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

    .form-group {
        margin-bottom: 22px;
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

    /* ==========================
       ROOM TYPE CARDS
       Chỉ hiện khi tạo phòng
       ========================== */

    .room-types {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        margin-bottom: 24px;
    }

    .type-card {
        background: #1b1b1e;
        border: 1px solid #303034;
        border-radius: 9px;
        padding: 15px;
    }

    .type-card strong {
        display: block;
        color: white;
        margin-bottom: 5px;
        font-size: 16px;
    }

    .type-card span {
        color: #999;
        font-size: 13px;
    }

    .capacity-info {
        margin-top: 8px;
        color: #888;
        font-size: 13px;
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

    .btn-primary:hover {
        background: #e3133b;
    }

    .btn-secondary {
        color: #aaa;
        margin-left: 14px;
        text-decoration: none;
    }

    .btn-secondary:hover {
        color: white;
    }

    @media (max-width: 700px) {

        .room-types {
            grid-template-columns: 1fr;
        }
    }

</style>

<div class="form-page">

    <c:set var="isEdit"
           value="${room.roomId > 0}" />

    <!-- ==========================
         TIÊU ĐỀ
         ========================== -->

    <c:choose>

        <c:when test="${isEdit}">

            <h1>
                Cập nhật phòng chiếu
            </h1>

            <p class="subtitle">
                Cập nhật tên và trạng thái hoạt động của phòng.
            </p>

        </c:when>

        <c:otherwise>

            <h1>
                Thêm phòng chiếu
            </h1>

            <p class="subtitle">
                Chọn loại phòng. Hệ thống sẽ tự xác định
                sức chứa và tạo toàn bộ sơ đồ ghế.
            </p>

        </c:otherwise>

    </c:choose>

    <!-- ==========================
         LOẠI PHÒNG
         CHỈ HIỆN KHI TẠO MỚI
         ========================== -->

    <c:if test="${not isEdit}">

        <div class="room-types">

            <div class="type-card">

                <strong>
                    2D
                </strong>

                <span>
                    5 hàng × 8 ghế = 40 ghế
                </span>

            </div>

            <div class="type-card">

                <strong>
                    3D
                </strong>

                <span>
                    5 hàng × 10 ghế = 50 ghế
                </span>

            </div>

            <div class="type-card">

                <strong>
                    IMAX
                </strong>

                <span>
                    6 hàng × 10 ghế = 60 ghế
                </span>

            </div>

        </div>

    </c:if>

    <!-- ==========================
         FORM
         ========================== -->

    <div class="form-card">

        <c:if test="${not empty error}">

            <div class="error-box">

                <c:out value="${error}" />

            </div>

        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/room-form">

            <!-- ID khi update -->

            <c:if test="${isEdit}">

                <input type="hidden"
                       name="id"
                       value="${room.roomId}">

            </c:if>

            <!-- ==========================
                 TÊN PHÒNG
                 ========================== -->

            <div class="form-group">

                <label>
                    Tên phòng
                </label>

                <input class="form-control"
                       type="text"
                       name="roomName"
                       maxlength="100"
                       required
                       placeholder="Ví dụ: Room 3"
                       value="<c:out value='${room.roomName}' />">

            </div>

            <!-- ==========================
                 LOẠI PHÒNG
                 CHỈ HIỆN KHI TẠO MỚI
                 ========================== -->

            <c:if test="${not isEdit}">

                <div class="form-group">

                    <label>
                        Loại phòng
                    </label>

                    <select id="roomType"
                            class="form-control"
                            name="roomType"
                            onchange="updateCapacity()">

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

                </div>

                <!-- ==========================
                     SỨC CHỨA
                     CHỈ HIỆN KHI TẠO
                     ========================== -->

                <div class="form-group">

                    <label>
                        Sức chứa
                    </label>

                    <input id="capacity"
                           class="form-control"
                           type="number"
                           readonly
                           value="${room.capacity > 0
                                    ? room.capacity
                                    : 40}">

                    <div id="layoutText"
                         class="capacity-info">

                        Sơ đồ: 5 hàng × 8 ghế.

                    </div>

                </div>

            </c:if>

            <!-- ==========================
                 TRẠNG THÁI
                 ========================== -->

            <div class="form-group">

                <label>
                    Trạng thái
                </label>

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

            <!-- ==========================
                 BUTTON
                 ========================== -->

            <button class="btn-primary"
                    type="submit">

                <c:choose>

                    <c:when test="${isEdit}">
                        Lưu thay đổi
                    </c:when>

                    <c:otherwise>
                        Tạo phòng và sơ đồ ghế
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

<!-- ==========================
     JS chỉ dùng khi TẠO PHÒNG
     ========================== -->

<c:if test="${not isEdit}">

    <script>

        function updateCapacity() {

            const roomType =
                    document.getElementById(
                            "roomType"
                    ).value;

            const capacity =
                    document.getElementById(
                            "capacity"
                    );

            const layoutText =
                    document.getElementById(
                            "layoutText"
                    );

            if (roomType === "2D") {

                capacity.value = 40;

                layoutText.textContent =
                        "Sơ đồ: 5 hàng × 8 ghế.";

            } else if (roomType === "3D") {

                capacity.value = 50;

                layoutText.textContent =
                        "Sơ đồ: 5 hàng × 10 ghế.";

            } else if (roomType === "IMAX") {

                capacity.value = 60;

                layoutText.textContent =
                        "Sơ đồ: 6 hàng × 10 ghế.";
            }
        }

        updateCapacity();

    </script>

</c:if>

<%@ include file="/WEB-INF/include/footer.jsp" %>