<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="form-page" data-css-page="admin-room-form">

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