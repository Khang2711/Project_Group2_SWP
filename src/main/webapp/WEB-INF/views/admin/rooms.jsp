<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

    .room-name {
        color: white;
        font-weight: 600;
    }

    .badge {
        padding: 5px 10px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }

    .active {
        color: #6ee49b;
        background: #173322;
    }

    .inactive {
        color: #aaa;
        background: #29292d;
    }

    .action-link {
        color: #ff4265;
        text-decoration: none;
        margin-right: 12px;
        font-weight: 600;
        font-size: 13px;
    }

    .seat-link {
        color: #ddd;
        text-decoration: none;
        border: 1px solid #444;
        padding: 6px 10px;
        border-radius: 5px;
        font-size: 13px;
    }

    .seat-link:hover {
        border-color: #ff1744;
        color: #ff4265;
    }

    .empty {
        text-align: center;
        padding: 40px !important;
        color: #888 !important;
    }
</style>

<div class="admin-page">

    <div class="page-header">

        <div>
            <h1>Phòng chiếu</h1>
            <p>Quản lý phòng chiếu và sơ đồ ghế của rạp.</p>
        </div>

        <a class="btn-primary"
           href="${pageContext.request.contextPath}/admin/room-form">
            + Thêm phòng
        </a>

    </div>

    <div class="table-card">

        <table class="admin-table">

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

                <c:forEach var="room" items="${rooms}">

                    <tr>

                        <td>#${room.roomId}</td>

                        <td class="room-name">
                            <c:out value="${room.roomName}" />
                        </td>

                        <td>
                            <c:out value="${room.roomType}" />
                        </td>

                        <td>
                            ${room.capacity} ghế
                        </td>

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

                        <td>

                            <a class="action-link"
                               href="${pageContext.request.contextPath}/admin/room-form?id=${room.roomId}">
                                Sửa
                            </a>

                            <a class="seat-link"
                               href="${pageContext.request.contextPath}/admin/seats?roomId=${room.roomId}">
                                Quản lý ghế
                            </a>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty rooms}">
                    <tr>
                        <td colspan="6" class="empty">
                            Chưa có phòng chiếu.
                        </td>
                    </tr>
                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>