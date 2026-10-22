<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .showtime-page h1 {
        margin-bottom: 8px;
    }

    .showtime-subtitle {
        color: #aaa;
        margin-bottom: 24px;
    }

    .showtime-toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 15px;
        margin-bottom: 20px;
    }

    .add-showtime-button {
        padding: 11px 18px;
        background: #ff1744;
        color: white;
        border-radius: 6px;
        text-decoration: none;
        font-weight: bold;
    }

    .showtime-table-wrap {
        overflow-x: auto;
        background: #18181d;
        border: 1px solid #303039;
        border-radius: 10px;
    }

    .showtime-table {
        width: 100%;
        border-collapse: collapse;
    }

    .showtime-table th,
    .showtime-table td {
        padding: 14px 15px;
        border-bottom: 1px solid #303039;
        text-align: left;
        white-space: nowrap;
    }

    .showtime-table th {
        color: #aaa;
        background: #202027;
    }

    .showtime-table tr:last-child td {
        border-bottom: none;
    }

    .status-active {
        color: #45d483;
        font-weight: bold;
    }

    .status-inactive {
        color: #999;
        font-weight: bold;
    }

    .showtime-actions {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .edit-button,
    .delete-button {
        padding: 7px 12px;
        border-radius: 5px;
        font-size: 13px;
        text-decoration: none;
        cursor: pointer;
    }

    .edit-button {
        background: #292932;
        color: white;
        border: 1px solid #444;
    }

    .delete-button {
        background: transparent;
        color: #ff637d;
        border: 1px solid #73303b;
    }

    .message-success,
    .message-error {
        margin-bottom: 18px;
        padding: 13px 15px;
        border-radius: 6px;
    }

    .message-success {
        background: #163725;
        color: #75e7a3;
        border: 1px solid #28583b;
    }

    .message-error {
        background: #36191f;
        color: #ffb9c1;
        border: 1px solid #71343e;
    }

    .empty-showtime {
        padding: 35px;
        text-align: center;
        color: #aaa;
    }

</style>


<section class="showtime-page">

    <div class="showtime-toolbar">

        <div>

            <h1>
                Quản lý lịch chiếu
            </h1>

            <p class="showtime-subtitle">
                Quản lý các suất chiếu của rạp.
            </p>

        </div>

        <a class="add-showtime-button"
           href="${pageContext.request.contextPath}/admin/showtime-form">

            + Thêm lịch chiếu

        </a>

    </div>


    <c:if test="${param.saved eq '1'}">

        <div class="message-success">
            Lưu lịch chiếu thành công.
        </div>

    </c:if>


    <c:if test="${param.deleted eq '1'}">

        <div class="message-success">
            Xử lý lịch chiếu thành công.
        </div>

    </c:if>


    <c:if test="${not empty error}">

        <div class="message-error">

            <c:out value="${error}" />

        </div>

    </c:if>


    <div class="showtime-table-wrap">

        <c:if test="${empty showtimes}">

            <div class="empty-showtime">

                Chưa có lịch chiếu nào.

            </div>

        </c:if>


        <c:if test="${not empty showtimes}">

            <table class="showtime-table">

                <thead>

                    <tr>

                        <th>ID</th>

                        <th>Phim</th>

                        <th>Phòng</th>

                        <th>Bắt đầu</th>

                        <th>Kết thúc</th>

                        <th>Trạng thái</th>

                        <th>Thao tác</th>

                    </tr>

                </thead>


                <tbody>

                    <c:forEach items="${showtimes}"
                               var="showtime">

                        <tr>

                            <td>
                                ${showtime.showtimeId}
                            </td>

                            <td>

                                <c:out value="${movieById[showtime.movieId].title}" />

                            </td>

                            <td>

                                <c:out value="${roomById[showtime.roomId].roomName}" />

                            </td>

                            <td>

                                <fmt:formatDate
                                    value="${showtime.startTime}"
                                    pattern="dd/MM/yyyy HH:mm" />

                            </td>

                            <td>

                                <fmt:formatDate
                                    value="${showtime.endTime}"
                                    pattern="dd/MM/yyyy HH:mm" />

                            </td>

                            <td>

                                <c:if test="${showtime.status eq 'ACTIVE'}">

                                    <span class="status-active">
                                        ACTIVE
                                    </span>

                                </c:if>

                                <c:if test="${showtime.status ne 'ACTIVE'}">

                                    <span class="status-inactive">
                                        INACTIVE
                                    </span>

                                </c:if>

                            </td>

                            <td>

                                <div class="showtime-actions">

                                    <a class="edit-button"
                                       href="${pageContext.request.contextPath}/admin/showtime-form?id=${showtime.showtimeId}">

                                        Sửa

                                    </a>

                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/showtimes"
                                          onsubmit="return confirm('Bạn có chắc muốn xóa lịch chiếu này?');">

                                        <input type="hidden"
                                               name="action"
                                               value="delete">

                                        <input type="hidden"
                                               name="id"
                                               value="${showtime.showtimeId}">

                                        <button type="submit"
                                                class="delete-button">

                                            Xóa

                                        </button>

                                    </form>

                                </div>

                            </td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </c:if>

    </div>

</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>