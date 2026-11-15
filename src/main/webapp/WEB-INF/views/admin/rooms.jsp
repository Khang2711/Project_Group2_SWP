<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="admin-page" data-css-page="admin-rooms">

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