<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="movie-page" data-css-page="admin-movies">

    <div class="movie-header">

        <div>

            <h1>
                Quản lý phim
            </h1>

            <p>
                Quản lý danh sách phim đang được áp dụng tại rạp.
            </p>

        </div>

        <a class="btn-add"
           href="${pageContext.request.contextPath}/admin/movie-form">

            + Thêm phim

        </a>

    </div>

    <c:if test="${param.saved == '1'}">

        <div class="success-message">
            ✓ Lưu thông tin phim thành công.
        </div>

    </c:if>

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">
            ✓ Xóa phim khỏi rạp thành công.
        </div>

    </c:if>

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <div class="movie-card">

        <table class="movie-table"
               data-paginate="true"
               data-page-size="10">

            <thead>

                <tr>

                    <th>ID</th>
                    <th>Tên phim</th>
                    <th>Thời lượng</th>
                    <th>Phân loại</th>
                    <th>Ngày phát hành</th>
                    <th>Ngày kết thúc</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="movie"
                           items="${movies}">

                    <tr>

                        <td class="movie-id">

                            #<c:out value="${movie.movieId}" />

                        </td>

                        <td class="movie-title">

                            <c:out value="${movie.title}" />

                        </td>

                        <td>

                            <c:out value="${movie.durationMinutes}" />
                            phút

                        </td>

                        <td>

                            <c:if test="${not empty movie.ageRating}">

                                <span class="age-badge">

                                    <c:out value="${movie.ageRating}" />

                                </span>

                            </c:if>

                            <c:if test="${empty movie.ageRating}">
                                -
                            </c:if>

                        </td>

                        <td>

                            <c:if test="${not empty movie.releaseDate}">

                                <c:out value="${movie.releaseDate}" />

                            </c:if>

                            <c:if test="${empty movie.releaseDate}">
                                -
                            </c:if>

                        </td>

                        <td>

                            <c:if test="${not empty movie.endDate}">

                                <c:out value="${movie.endDate}" />

                            </c:if>

                            <c:if test="${empty movie.endDate}">
                                -
                            </c:if>

                        </td>

                        <td>

                            <c:if test="${movie.status == 'ACTIVE'}">

                                <span class="badge badge-active">
                                    ACTIVE
                                </span>

                            </c:if>

                            <c:if test="${movie.status != 'ACTIVE'}">

                                <span class="badge badge-inactive">

                                    <c:out value="${movie.status}" />

                                </span>

                            </c:if>

                        </td>

                        <td>

                            <div class="action-group">

                                <a class="btn-edit"
                                   href="${pageContext.request.contextPath}/admin/movie-form?id=${movie.movieId}">

                                    Sửa

                                </a>

                                <form class="delete-form"
                                      method="post"
                                      action="${pageContext.request.contextPath}/admin/movies">

                                    <input type="hidden"
                                           name="id"
                                           value="${movie.movieId}">

                                    <button type="submit"
                                            class="btn-delete"
                                            onclick="return confirm('Bạn có chắc muốn xóa phim này khỏi rạp?');">

                                        Xóa

                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty movies}">

                    <tr>

                        <td colspan="8"
                            class="empty-row">

                            Chưa có phim nào tại rạp.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/table-pagination.jsp" %>

<%@ include file="/WEB-INF/include/footer.jsp" %>