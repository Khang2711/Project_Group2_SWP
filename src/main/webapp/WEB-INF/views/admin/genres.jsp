
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<div class="genres-page">

    <!-- Page header -->
    <div class="genres-header">

        <div>
            <h1>Quản lý thể loại phim</h1>
            <p>Quản lý danh sách thể loại phim trong hệ thống.</p>
        </div>

        <a class="genres-btn-add"
           href="${pageContext.request.contextPath}/admin/genre-form">
            + Thêm thể loại
        </a>

    </div>

    <!-- Success message -->
    <c:if test="${param.saved == '1'}">
        <div class="genres-success-message">
            Lưu thông tin thể loại thành công.
        </div>
    </c:if>

    <c:if test="${param.statusUpdated == '1'}">
        <div class="genres-success-message">
            Cập nhật trạng thái thể loại thành công.
        </div>
    </c:if>

    <c:if test="${param.deleted == '1'}">
        <div class="genres-success-message">
            Xóa thể loại thành công.
        </div>
    </c:if>

    <!-- Error message -->
    <c:if test="${not empty error}">
        <div class="genres-error-message">
            <c:out value="${error}" />
        </div>
    </c:if>

    <!-- Genre list -->
    <div class="genres-card">

        <table class="genres-table"
               data-paginate="true" 
               data-page-size="10">

            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên thể loại</th>
                    <th>Mô tả</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>

                <c:forEach var="genre" items="${genres}">

                    <tr>

                        <!-- Genre ID -->
                        <td class="genres-id">
                            #<c:out value="${genre.genreId}" />
                        </td>

                        <!-- Genre name -->
                        <td class="genres-name">
                            <c:out value="${genre.genreName}" />
                        </td>

                        <!-- Description -->
                        <td class="genres-description">

                            <c:choose>
                                <c:when test="${not empty genre.description}">
                                    <c:out value="${genre.description}" />
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>
                            </c:choose>

                        </td>

                        <!-- Status -->
                        <td>

                            <c:choose>
                                <c:when test="${genre.status == 'ACTIVE'}">

                                    <span class="genres-badge genres-active">
                                        ACTIVE
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="genres-badge genres-inactive">
                                        INACTIVE
                                    </span>

                                </c:otherwise>
                            </c:choose>

                        </td>

                        <!-- Actions -->
                        <td>

                            <div class="genres-actions">

                                <!-- Edit genre -->
                                <a class="genres-btn-edit"
                                   href="${pageContext.request.contextPath}/admin/genre-form?id=${genre.genreId}">
                                    Sửa
                                </a>

                                <!-- Change genre status -->
                                <form method="post"
                                      class="genres-status-form"
                                      action="${pageContext.request.contextPath}/admin/genres">

                                    <input type="hidden"
                                           name="id"
                                           value="${genre.genreId}">

                                    <c:choose>

                                        <c:when test="${genre.status == 'ACTIVE'}">

                                            <input type="hidden"
                                                   name="status"
                                                   value="INACTIVE">

                                            <button type="submit"
                                                    class="genres-btn-deactivate">
                                                Ngừng sử dụng
                                            </button>

                                        </c:when>

                                        <c:otherwise>

                                            <input type="hidden"
                                                   name="status"
                                                   value="ACTIVE">

                                            <button type="submit"
                                                    class="genres-btn-activate">
                                                Kích hoạt lại
                                            </button>

                                        </c:otherwise>

                                    </c:choose>

                                </form>

                                <form method="post"
                                      class="genres-status-form"
                                      action="${pageContext.request.contextPath}/admin/genres"
                                      onsubmit="return confirm('Bạn có chắc chắn muốn xóa vĩnh viễn thể loại này?');">

                                    <input type="hidden"
                                           name="action"
                                           value="delete">

                                    <input type="hidden"
                                           name="id"
                                           value="${genre.genreId}">

                                    <button type="submit"
                                            class="genres-btn-delete">
                                        Xóa
                                    </button>
                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <!-- Empty list -->
                <c:if test="${empty genres}">
                    <tr>
                        <td colspan="5" class="genres-empty">
                            Chưa có thể loại phim nào.
                        </td>
                    </tr>
                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@include file="/WEB-INF/include/table-pagination.jsp" %>

<%@ include file="/WEB-INF/include/footer.jsp" %>
