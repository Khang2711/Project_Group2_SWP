<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .admin-page {
        max-width: 1180px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 20px;
        margin-bottom: 28px;
    }

    .page-header h1 {
        margin: 0 0 8px;
        font-size: 32px;
        font-weight: 700;
    }

    .page-header p {
        color: #999;
        margin: 0;
        font-size: 15px;
    }

    /* ==========================
       BUTTON THÊM PHÒNG
       ========================== */

    .btn-primary {
        display: inline-block;
        background: #ff1744;
        color: white;
        padding: 11px 19px;
        border-radius: 7px;
        text-decoration: none;
        font-weight: 600;
        transition: 0.2s;
    }

    .btn-primary:hover {
        background: #e3133b;
        transform: translateY(-1px);
    }

    /* ==========================
       MESSAGE
       ========================== */

    .success-message {
        padding: 13px 16px;
        margin-bottom: 22px;
        border: 1px solid #225c3c;
        background: #13291e;
        color: #70d99a;
        border-radius: 7px;
    }

    .error-message {
        padding: 13px 16px;
        margin-bottom: 22px;
        border: 1px solid #6b2935;
        background: #32181d;
        color: #ff8095;
        border-radius: 7px;
    }

    /* ==========================
       TABLE CARD
       ========================== */

    .table-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;
        overflow: hidden;
    }

    .admin-table {
        width: 100%;
        border-collapse: collapse;
    }

    .admin-table th {
        padding: 15px 17px;
        text-align: left;
        background: #1d1d20;
        color: #999;
        font-size: 13px;
        text-transform: uppercase;
        letter-spacing: 0.4px;
        border-bottom: 1px solid #303034;
    }

    .admin-table td {
        padding: 17px;
        border-bottom: 1px solid #28282c;
        color: #ddd;
        vertical-align: middle;
    }

    .admin-table tbody tr {
        transition: 0.2s;
    }

    .admin-table tbody tr:hover {
        background: #1c1c1f;
    }

    .admin-table tbody tr:last-child td {
        border-bottom: none;
    }

    /* ==========================
       ROOM INFO
       ========================== */

    .room-id {
        color: #777;
    }

    .room-name {
        color: white;
        font-weight: 600;
    }

    .room-type {
        display: inline-block;
        padding: 5px 9px;
        background: #29292d;
        border-radius: 5px;
        color: #ddd;
        font-size: 12px;
    }

    .capacity {
        color: #ddd;
    }

    /* ==========================
       STATUS
       ========================== */

    .badge {
        display: inline-block;
        padding: 5px 10px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }

    .active {
        color: #6ee49b;
        background: #173322;
        border: 1px solid #28573a;
    }

    .inactive {
        color: #aaa;
        background: #29292d;
        border: 1px solid #3b3b40;
    }

    /* ==========================
       ACTION
       ========================== */

    .action-group {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
    }

    .btn-edit {
        display: inline-block;
        padding: 7px 12px;
        color: #ff4265;
        background: transparent;
        border: 1px solid #ff1744;
        border-radius: 5px;
        text-decoration: none;
        font-size: 13px;
        font-weight: 600;
        transition: 0.2s;
    }

    .btn-edit:hover {
        background: #ff1744;
        color: white;
    }

    .btn-seat {
        display: inline-block;
        padding: 7px 12px;
        color: #ddd;
        background: transparent;
        border: 1px solid #444;
        border-radius: 5px;
        text-decoration: none;
        font-size: 13px;
        transition: 0.2s;
    }

    .btn-seat:hover {
        border-color: #ff1744;
        color: #ff4265;
    }

    .delete-form {
        margin: 0;
        display: inline;
    }

    .btn-delete {
        padding: 7px 12px;
        color: #ff6b81;
        background: transparent;
        border: 1px solid #5d3038;
        border-radius: 5px;
        font-size: 13px;
        cursor: pointer;
        transition: 0.2s;
    }

    .btn-delete:hover {
        background: #ff1744;
        border-color: #ff1744;
        color: white;
    }

    /* ==========================
       EMPTY
       ========================== */

    .empty {
        text-align: center;
        padding: 45px !important;
        color: #888 !important;
    }

    /* ==========================
       RESPONSIVE
       ========================== */

    @media (max-width: 900px) {

        .table-card {
            overflow-x: auto;
        }

        .admin-table {
            min-width: 850px;
        }

        .page-header {
            flex-direction: column;
            align-items: flex-start;
        }
    }

</style>

<div class="admin-page">

    <!-- ==========================
         HEADER
         ========================== -->

    <div class="page-header">

        <div>

            <h1>
                Phòng chiếu
            </h1>

            <p>
                Quản lý phòng chiếu
                và sơ đồ ghế của rạp.
            </p>

        </div>

        <a class="btn-primary"
           href="${pageContext.request.contextPath}/admin/room-form">

            + Thêm phòng

        </a>

    </div>

    <!-- ==========================
         THÊM / SỬA THÀNH CÔNG
         ========================== -->

    <c:if test="${param.saved == '1'}">

        <div class="success-message">

            ✓ Lưu thông tin phòng
            thành công.

        </div>

    </c:if>

    <!-- ==========================
         XÓA THÀNH CÔNG
         ========================== -->

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">

            ✓ Xóa phòng thành công.

        </div>

    </c:if>

    <!-- ==========================
         LỖI KHI XÓA
         ========================== -->

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <!-- ==========================
         TABLE
         ========================== -->

    <div class="table-card">

        <table class="admin-table">

            <thead>

                <tr>

                    <th>
                        ID
                    </th>

                    <th>
                        Tên phòng
                    </th>

                    <th>
                        Loại phòng
                    </th>

                    <th>
                        Sức chứa
                    </th>

                    <th>
                        Trạng thái
                    </th>

                    <th>
                        Thao tác
                    </th>

                </tr>

            </thead>

            <tbody>

                <!-- ==========================
                     DANH SÁCH PHÒNG
                     ========================== -->

                <c:forEach var="room"
                           items="${rooms}">

                    <tr>

                        <!-- ID -->
                        <td class="room-id">

                            #<c:out value="${room.roomId}" />

                        </td>

                        <!-- TÊN PHÒNG -->
                        <td class="room-name">

                            <c:out value="${room.roomName}" />

                        </td>

                        <!-- LOẠI PHÒNG -->
                        <td>

                            <c:choose>

                                <c:when test="${not empty room.roomType}">

                                    <span class="room-type">

                                        <c:out value="${room.roomType}" />

                                    </span>

                                </c:when>

                                <c:otherwise>

                                    -

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- SỨC CHỨA -->
                        <td class="capacity">

                            <c:out value="${room.capacity}" />
                            ghế

                        </td>

                        <!-- TRẠNG THÁI -->
                        <td>

                            <c:choose>

                                <c:when test="${room.status == 'ACTIVE'}">

                                    <span class="badge active">

                                        ACTIVE

                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="badge inactive">

                                        <c:out value="${room.status}" />

                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- ==========================
                             THAO TÁC
                             ========================== -->

                        <td>

                            <div class="action-group">

                                <!-- SỬA -->
                                <a class="btn-edit"
                                   href="${pageContext.request.contextPath}/admin/room-form?id=${room.roomId}">

                                    Sửa

                                </a>

                                <!-- QUẢN LÝ GHẾ -->
                                <a class="btn-seat"
                                   href="${pageContext.request.contextPath}/admin/seats?roomId=${room.roomId}">

                                    Quản lý ghế

                                </a>

                                <!-- XÓA -->
                                <form class="delete-form"
                                      method="post"
                                      action="${pageContext.request.contextPath}/admin/rooms">

                                    <input type="hidden"
                                           name="action"
                                           value="delete">

                                    <input type="hidden"
                                           name="id"
                                           value="${room.roomId}">

                                    <button type="submit"
                                            class="btn-delete"
                                            onclick="return confirm('Bạn có chắc muốn xóa phòng này?');">

                                        Xóa

                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <!-- ==========================
                     KHÔNG CÓ PHÒNG
                     ========================== -->

                <c:if test="${empty rooms}">

                    <tr>

                        <td colspan="6"
                            class="empty">

                            Chưa có phòng chiếu.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>