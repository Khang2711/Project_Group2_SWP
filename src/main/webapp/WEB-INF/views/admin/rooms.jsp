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
    }

    .page-header p {
        color: #999;
        margin: 0;
    }

    .btn-primary {
        display: inline-block;

        background: #ff1744;
        color: white;

        padding: 11px 19px;

        border-radius: 7px;

        text-decoration: none;
        font-weight: 600;
    }

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

        background: #1d1d20;
        color: #999;

        text-align: left;

        font-size: 13px;
        text-transform: uppercase;

        border-bottom: 1px solid #303034;
    }

    .admin-table td {
        padding: 17px;

        border-bottom: 1px solid #28282c;

        color: #ddd;
    }

    .admin-table tbody tr:hover {
        background: #1c1c1f;
    }

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

    .action-group {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
    }

    .btn-edit,
    .btn-seat {
        display: inline-block;

        padding: 7px 12px;

        border-radius: 5px;

        text-decoration: none;

        font-size: 13px;
    }

    .btn-edit {
        color: #ff4265;
        border: 1px solid #ff1744;
    }

    .btn-seat {
        color: #ddd;
        border: 1px solid #444;
    }

    .delete-form {
        margin: 0;
    }

    .btn-delete {
        padding: 7px 12px;

        color: #ff6b81;
        background: transparent;

        border: 1px solid #5d3038;
        border-radius: 5px;

        cursor: pointer;
    }

    .empty {
        text-align: center;
        padding: 45px !important;
        color: #888 !important;
    }

    @media (max-width: 900px) {

        .table-card {
            overflow-x: auto;
        }

        .admin-table {
            min-width: 850px;
        }

    }

</style>

<div class="admin-page">

    <div class="page-header">

        <div>

            <h1>
                Phòng chiếu
            </h1>

            <p>
                Quản lý phòng chiếu và sơ đồ ghế của rạp.
            </p>

        </div>

        <a class="btn-primary"
           href="${pageContext.request.contextPath}/admin/room-form">

            + Thêm phòng

        </a>

    </div>

    <c:if test="${param.saved == '1'}">

        <div class="success-message">
            ✓ Lưu thông tin phòng thành công.
        </div>

    </c:if>

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">
            ✓ Xóa phòng thành công.
        </div>

    </c:if>

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <div class="table-card">

        <table class="admin-table"
               data-paginate="true"
               data-page-size="10">

            <thead>

                <tr>

                    <th>ID</th>
                    <th>Tên phòng</th>
                    <th>Loại phòng</th>
                    <th>Sức chứa</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="room"
                           items="${rooms}">

                    <tr>

                        <td class="room-id">

                            #<c:out value="${room.roomId}" />

                        </td>

                        <td class="room-name">

                            <c:out value="${room.roomName}" />

                        </td>

                        <td>

                            <c:if test="${not empty room.roomType}">

                                <span class="room-type">

                                    <c:out value="${room.roomType}" />

                                </span>

                            </c:if>

                            <c:if test="${empty room.roomType}">
                                -
                            </c:if>

                        </td>

                        <td>

                            <c:out value="${room.capacity}" />
                            ghế

                        </td>

                        <td>

                            <c:if test="${room.status == 'ACTIVE'}">

                                <span class="badge active">
                                    ACTIVE
                                </span>

                            </c:if>

                            <c:if test="${room.status != 'ACTIVE'}">

                                <span class="badge inactive">

                                    <c:out value="${room.status}" />

                                </span>

                            </c:if>

                        </td>

                        <td>

                            <div class="action-group">

                                <a class="btn-edit"
                                   href="${pageContext.request.contextPath}/admin/room-form?id=${room.roomId}">

                                    Sửa

                                </a>

                                <a class="btn-seat"
                                   href="${pageContext.request.contextPath}/admin/seats?roomId=${room.roomId}">

                                    Quản lý ghế

                                </a>

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

<%@ include file="/WEB-INF/include/table-pagination.jsp" %>

<%@ include file="/WEB-INF/include/footer.jsp" %>