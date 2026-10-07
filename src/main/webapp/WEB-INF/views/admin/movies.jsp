<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
        transition: 0.2s;
    }

    .btn-add:hover {
        background: #e3133b;
        transform: translateY(-1px);
    }

    .success-message {
        padding: 13px 16px;
        margin-bottom: 22px;
        border: 1px solid #225c3c;
        background: #13291e;
        color: #70d99a;
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
        letter-spacing: 0.4px;
        border-bottom: 1px solid #303034;
    }

    .movie-table td {
        padding: 17px;
        border-bottom: 1px solid #28282c;
        color: #ddd;
        vertical-align: middle;
    }

    .movie-table tbody tr {
        transition: 0.2s;
    }

    .movie-table tbody tr:hover {
        background: #1c1c1f;
    }

    .movie-table tbody tr:last-child td {
        border-bottom: none;
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

    .btn-edit {
        display: inline-block;
        padding: 7px 13px;
        border: 1px solid #ff1744;
        color: #ff4265;
        text-decoration: none;
        border-radius: 5px;
        font-size: 13px;
        transition: 0.2s;
    }

    .btn-edit:hover {
        background: #ff1744;
        color: white;
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
            min-width: 850px;
        }

        .movie-header {
            align-items: flex-start;
            flex-direction: column;
        }
    }
</style>

<div class="movie-page">

    <div class="movie-header">

        <div>
            <h1>Quản lý phim</h1>
            <p>Quản lý danh sách phim đang được áp dụng tại rạp.</p>
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

    <div class="movie-card">

        <table class="movie-table">

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

                <c:forEach var="movie" items="${movies}">

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
                            <span class="age-badge">
                                <c:out value="${movie.ageRating}" />
                            </span>
                        </td>

                        <td>
                            <c:out value="${movie.releaseDate}" />
                        </td>

                        <td>
                            <c:out value="${movie.endDate}" />
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${movie.status == 'ACTIVE'}">
                                    <span class="badge badge-active">
                                        ACTIVE
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="badge badge-inactive">
                                        <c:out value="${movie.status}" />
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </td>

                        <td>
                            <a class="btn-edit"
                               href="${pageContext.request.contextPath}/admin/movie-form?id=${movie.movieId}">
                                Sửa
                            </a>
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

<%@ include file="/WEB-INF/include/footer.jsp" %>