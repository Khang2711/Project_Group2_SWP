<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .movie-page {
        max-width: 1240px;
        margin: 0 auto;
        padding: 45px 25px 70px;
        color: #f5f5f5;
    }

    .movie-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 28px;
        gap: 20px;
    }

    .movie-header h1 {
        margin: 0 0 7px;
        font-size: 32px;
        font-weight: 700;
    }

    .movie-header p {
        margin: 0;
        color: #999;
        font-size: 15px;
    }

    .btn-add {
        display: inline-block;
        padding: 11px 20px;
        background: #ff1744;
        color: white;
        text-decoration: none;
        border-radius: 7px;
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

    .movie-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 10px;
        overflow: hidden;
    }

    .movie-table {
        width: 100%;
        border-collapse: collapse;
    }

    .movie-table th {
        background: #1d1d20;
        color: #aaa;
        padding: 15px 17px;
        text-align: left;
        font-size: 13px;
        text-transform: uppercase;
        border-bottom: 1px solid #303034;
    }

    .movie-table td {
        padding: 17px;
        border-bottom: 1px solid #28282c;
        color: #ddd;
    }

    .movie-table tbody tr:hover {
        background: #1c1c1f;
    }

    .movie-title {
        color: white;
        font-weight: 600;
    }

    .movie-id {
        color: #777;
    }

    .badge {
        display: inline-block;
        padding: 5px 10px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }

    .badge-active {
        color: #65d890;
        background: #183323;
        border: 1px solid #28573a;
    }

    .badge-inactive {
        color: #aaa;
        background: #29292d;
        border: 1px solid #3b3b40;
    }

    .age-badge {
        display: inline-block;
        padding: 4px 9px;
        border-radius: 5px;
        background: #342024;
        color: #ff637e;
        font-size: 12px;
        font-weight: 700;
    }

    .action-group {
        display: flex;
        gap: 8px;
        align-items: center;
    }

    .btn-edit {
        padding: 7px 13px;
        border: 1px solid #ff1744;
        color: #ff4265;
        background: transparent;
        text-decoration: none;
        border-radius: 5px;
        font-size: 13px;
    }

    .delete-form {
        margin: 0;
    }

    .btn-delete {
        padding: 7px 13px;
        border: 1px solid #5d3038;
        background: transparent;
        color: #ff6b81;
        border-radius: 5px;
        cursor: pointer;
    }

    .empty-row {
        text-align: center;
        color: #888 !important;
        padding: 45px !important;
    }

    @media (max-width: 900px) {

        .movie-card {
            overflow-x: auto;
        }

        .movie-table {
            min-width: 900px;
        }

    }

</style>

<div class="movie-page">

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