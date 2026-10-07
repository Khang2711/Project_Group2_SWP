<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .admin-page {
        max-width: 1100px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 25px;
    }

    .page-header h1 {
        margin: 0 0 7px;
        font-size: 32px;
    }

    .page-header p {
        color: #999;
        margin: 0;
    }

    .btn-primary {
        background: #ff1744;
        color: white;
        text-decoration: none;
        padding: 11px 18px;
        border-radius: 7px;
        font-weight: 600;
    }

    .room-info {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 9px;
        padding: 16px 20px;
        margin-bottom: 20px;
        color: #bbb;
    }

    .room-info strong {
        color: white;
    }

    .success {
        background: #13291e;
        border: 1px solid #225c3c;
        color: #70d99a;
        border-radius: 7px;
        padding: 12px 15px;
        margin-bottom: 20px;
    }

    .table-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 10px;
        overflow: hidden;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th {
        background: #1d1d20;
        color: #999;
        padding: 14px 17px;
        text-align: left;
        font-size: 13px;
        text-transform: uppercase;
    }

    td {
        padding: 16px 17px;
        border-top: 1px solid #29292d;
        color: #ddd;
    }

    tbody tr:hover {
        background: #1c1c1f;
    }

    .seat-number {
        font-weight: 700;
        color: white;
    }

    .type-badge {
        display: inline-block;
        padding: 5px 9px;
        border-radius: 5px;
        background: #29292d;
        color: #ddd;
        font-size: 12px;
    }

    .status-active {
        color: #6ee49b;
    }

    .status-inactive {
        color: #999;
    }

    .edit-link {
        color: #ff4265;
        text-decoration: none;
        font-weight: 600;
    }

    .back-link {
        display: inline-block;
        margin-top: 20px;
        color: #aaa;
        text-decoration: none;
    }

    .empty {
        text-align: center;
        color: #888;
        padding: 40px;
    }
</style>

<div class="admin-page">

    <div class="page-header">

        <div>
            <h1>Sơ đồ ghế</h1>
            <p>Quản lý ghế trong phòng chiếu.</p>
        </div>

        <a class="btn-primary"
           href="${pageContext.request.contextPath}/admin/seat-form?roomId=${room.roomId}">
            + Thêm ghế
        </a>

    </div>

    <div class="room-info">
        <strong>
            <c:out value="${room.roomName}" />
        </strong>
        &nbsp; • &nbsp;
        Sức chứa:
        <strong>${room.capacity} ghế</strong>
    </div>

    <c:if test="${param.saved == '1'}">
        <div class="success">
            ✓ Lưu thông tin ghế thành công.
        </div>
    </c:if>

    <div class="table-card">

        <table>

            <thead>
                <tr>
                    <th>ID</th>
                    <th>Hàng</th>
                    <th>Số ghế</th>
                    <th>Loại</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach var="seat" items="${seats}">

                    <tr>
                        <td>#${seat.seatId}</td>

                        <td>
                            Hàng ${seat.seatRow}
                        </td>

                        <td class="seat-number">
                            ${seat.seatNumber}
                        </td>

                        <td>
                            <span class="type-badge">
                                <c:out value="${seat.seatType}" />
                            </span>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${seat.status == 'ACTIVE'}">
                                    <span class="status-active">
                                        ● ACTIVE
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="status-inactive">
                                        ● <c:out value="${seat.status}" />
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <a class="edit-link"
                               href="${pageContext.request.contextPath}/admin/seat-form?roomId=${room.roomId}&id=${seat.seatId}">
                                Sửa
                            </a>
                        </td>
                    </tr>

                </c:forEach>

                <c:if test="${empty seats}">
                    <tr>
                        <td colspan="6" class="empty">
                            Phòng này chưa có ghế.
                        </td>
                    </tr>
                </c:if>

            </tbody>

        </table>

    </div>

    <a class="back-link"
       href="${pageContext.request.contextPath}/admin/rooms">
        ← Quay lại phòng chiếu
    </a>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>