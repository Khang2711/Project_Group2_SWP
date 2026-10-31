<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<div class="genres-page">

    <div class="genres-header">
        <div>
            <h1>Quản lý chi nhánh</h1>
            <p>Quản lý các rạp chiếu phim trong hệ thống F-Cinema.</p>
        </div>

        <a class="genres-btn-add"
           href="${pageContext.request.contextPath}/system/cinema-form">
            + Thêm chi nhánh
        </a>
    </div>

    <c:if test="${param.saved == '1'}">
        <div class="genres-success-message">
            Lưu thông tin chi nhánh thành công.
        </div>
    </c:if>

    <c:if test="${param.statusUpdated == '1'}">
        <div class="genres-success-message">
            Cập nhật trạng thái chi nhánh thành công.
        </div>
    </c:if>

    <c:if test="${param.deleted == '1'}">
        <div class="genres-success-message">
            Xóa chi nhánh thành công.
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="genres-error-message">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="genres-card">
        <table class="genres-table"
               data-paginate="true"
               data-page-size="10">

            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên chi nhánh</th>
                    <th>Địa chỉ</th>
                    <th>Số điện thoại</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>
                <c:forEach var="cinema" items="${cinemas}">
                    <tr>
                        <td class="genres-id">
                            #<c:out value="${cinema.cinemaId}" />
                        </td>

                        <td class="genres-name">
                            <c:out value="${cinema.cinemaName}" />
                        </td>

                        <td class="genres-description">
                            <c:out value="${cinema.address}" />
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty cinema.phone}">
                                    <c:out value="${cinema.phone}" />
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${cinema.status == 'ACTIVE'}">
                                    <span class="genres-badge genres-active">ACTIVE</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="genres-badge genres-inactive">INACTIVE</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <div class="genres-actions">
                                <a class="genres-btn-edit"
                                   href="${pageContext.request.contextPath}/system/cinema-form?id=${cinema.cinemaId}">
                                    Sửa
                                </a>

                                <form method="post"
                                      class="genres-status-form"
                                      action="${pageContext.request.contextPath}/system/cinemas">

                                    <input type="hidden"
                                           name="id"
                                           value="${cinema.cinemaId}">

                                    <c:choose>
                                        <c:when test="${cinema.status == 'ACTIVE'}">
                                            <input type="hidden" name="status" value="INACTIVE">
                                            <button type="submit" class="genres-btn-deactivate">
                                                Ngừng hoạt động
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="hidden" name="status" value="ACTIVE">
                                            <button type="submit" class="genres-btn-activate">
                                                Kích hoạt lại
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>

                                <form method="post"
                                      class="genres-status-form"
                                      action="${pageContext.request.contextPath}/system/cinemas"
                                      onsubmit="return confirm('Chỉ xóa được chi nhánh khi chưa có Manager/Staff, phòng chiếu, bảng giá, phim, đồ ăn, combo hoặc đơn bán. Bạn có chắc muốn xóa?');">

                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden"
                                           name="id"
                                           value="${cinema.cinemaId}">

                                    <button type="submit" class="genres-btn-delete">
                                        Xóa
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty cinemas}">
                    <tr>
                        <td colspan="6" class="genres-empty">
                            Chưa có chi nhánh nào trong hệ thống.
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>

</div>

<%@include file="/WEB-INF/include/table-pagination.jsp" %>
<%@ include file="/WEB-INF/include/footer.jsp" %>
