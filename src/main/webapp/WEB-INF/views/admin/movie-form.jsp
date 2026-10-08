<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .movie-form-page {
        max-width: 1050px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .movie-form-header {
        margin-bottom: 28px;
    }

    .movie-form-header h1 {
        margin: 0 0 8px;
        font-size: 32px;
        font-weight: 700;
    }

    .movie-form-header p {
        margin: 0;
        color: #8f8f95;
        font-size: 15px;
    }

    .movie-form-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
        overflow: hidden;
    }

    .movie-form-body {
        padding: 30px;
    }

    .error-box {
        margin-bottom: 24px;
        padding: 13px 16px;
        border-radius: 7px;
        background: #32181d;
        border: 1px solid #6b2935;
        color: #ff8ca0;
    }

    .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 22px;
    }

    .form-group {
        margin-bottom: 22px;
    }

    .form-group.full {
        grid-column: 1 / -1;
    }

    .form-group label,
    .group-title {
        display: block;
        margin-bottom: 8px;
        color: #d7d7da;
        font-size: 14px;
        font-weight: 600;
    }

    .required {
        color: #ff1744;
    }

    .form-control {
        width: 100%;
        box-sizing: border-box;
        padding: 12px 14px;
        background: #1c1c1f;
        border: 1px solid #35353a;
        border-radius: 7px;
        color: #fff;
        font-size: 14px;
        outline: none;
        transition: 0.2s;
    }

    .form-control:focus {
        border-color: #ff1744;
        box-shadow: 0 0 0 3px rgba(255, 23, 68, 0.08);
    }

    textarea.form-control {
        min-height: 120px;
        resize: vertical;
        font-family: inherit;
    }

    .genre-box {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 10px;
        padding: 16px;
        background: #1c1c1f;
        border: 1px solid #35353a;
        border-radius: 8px;
    }

    .genre-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 9px 11px;
        background: #222225;
        border: 1px solid #303034;
        border-radius: 6px;
        color: #ddd;
        cursor: pointer;
        transition: 0.2s;
    }

    .genre-item:hover {
        border-color: #ff1744;
        color: #fff;
    }

    .genre-item input {
        accent-color: #ff1744;
    }

    .status-box {
        display: flex;
        gap: 12px;
    }

    .status-option {
        flex: 1;
    }

    .status-option input {
        display: none;
    }

    .status-option label {
        display: block;
        padding: 12px 15px;
        margin: 0;
        border-radius: 7px;
        border: 1px solid #35353a;
        background: #1c1c1f;
        text-align: center;
        cursor: pointer;
        transition: 0.2s;
    }

    .status-option input:checked + label {
        border-color: #ff1744;
        background: rgba(255, 23, 68, 0.09);
        color: #ff5574;
    }

    .help-text {
        margin-top: 7px;
        color: #777;
        font-size: 12px;
    }

    .movie-form-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 30px;
        background: #111113;
        border-top: 1px solid #29292d;
    }

    .btn-back {
        color: #aaa;
        text-decoration: none;
        font-weight: 500;
    }

    .btn-back:hover {
        color: white;
    }

    .btn-save {
        border: 0;
        background: #ff1744;
        color: white;
        padding: 11px 24px;
        border-radius: 7px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: 0.2s;
    }

    .btn-save:hover {
        background: #e3133b;
        transform: translateY(-1px);
    }

    .section-title {
        grid-column: 1 / -1;
        padding-bottom: 10px;
        margin-top: 5px;
        border-bottom: 1px solid #29292d;
        color: #fff;
        font-size: 17px;
        font-weight: 700;
    }

    @media (max-width: 800px) {
        .form-grid {
            grid-template-columns: 1fr;
        }

        .form-group.full,
        .section-title {
            grid-column: auto;
        }

        .genre-box {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    @media (max-width: 520px) {
        .genre-box {
            grid-template-columns: 1fr;
        }

        .movie-form-footer {
            flex-direction: column-reverse;
            gap: 15px;
            align-items: stretch;
        }

        .btn-save,
        .btn-back {
            text-align: center;
        }
    }
</style>

<div class="movie-form-page">

    <div class="movie-form-header">

        <c:choose>
            <c:when test="${movie.movieId > 0}">
                <h1>Cập nhật phim</h1>
                <p>
                    Chỉnh sửa thông tin phim đang được quản lý tại rạp.
                </p>
            </c:when>

            <c:otherwise>
                <h1>Thêm phim mới</h1>
                <p>
                    Nhập thông tin phim và chọn thể loại phù hợp.
                </p>
            </c:otherwise>
        </c:choose>

    </div>

    <div class="movie-form-card">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/movie-form">

            <div class="movie-form-body">

                <c:if test="${not empty error}">
                    <div class="error-box">
                        ⚠ <c:out value="${error}" />
                    </div>
                </c:if>

                <c:if test="${movie.movieId > 0}">
                    <input type="hidden"
                           name="id"
                           value="${movie.movieId}">
                </c:if>

                <div class="form-grid">

                    <div class="section-title">
                        Thông tin cơ bản
                    </div>

                    <div class="form-group full">

                        <label>
                            Tên phim
                            <span class="required">*</span>
                        </label>

                        <input class="form-control"
                               type="text"
                               name="title"
                               maxlength="255"
                               required
                               placeholder="Nhập tên phim"
                               value="<c:out value='${movie.title}' />">

                    </div>

                    <div class="form-group">

                        <label>
                            Thời lượng
                            <span class="required">*</span>
                        </label>

                        <input class="form-control"
                               type="number"
                               name="durationMinutes"
                               min="1"
                               required
                               placeholder="Ví dụ: 120"
                               value="${movie.durationMinutes > 0
                                        ? movie.durationMinutes : ''}">

                        <div class="help-text">
                            Đơn vị: phút
                        </div>

                    </div>

                    <div class="form-group">

                        <label>
                            Phân loại độ tuổi
                        </label>

                        <input class="form-control"
                               type="text"
                               name="ageRating"
                               maxlength="20"
                               placeholder="Ví dụ: P, T13, T16, T18"
                               value="<c:out value='${movie.ageRating}' />">

                    </div>

                    <div class="form-group full">

                        <label>
                            Mô tả phim
                        </label>

                        <textarea class="form-control"
                                  name="description"
                                  placeholder="Nhập nội dung hoặc mô tả ngắn về phim"><c:out value="${movie.description}" /></textarea>

                    </div>

                    <div class="section-title">
                        Thời gian phát hành
                    </div>

                    <div class="form-group">

                        <label>
                            Ngày phát hành
                        </label>

                        <input class="form-control"
                               type="date"
                               name="releaseDate"
                               value="${movie.releaseDate}">

                    </div>

                    <div class="form-group">

                        <label>
                            Ngày kết thúc
                        </label>

                        <input class="form-control"
                               type="date"
                               name="endDate"
                               value="${movie.endDate}">

                    </div>

                    <div class="section-title">
                        Hình ảnh & Trailer
                    </div>

                    <div class="form-group">

                        <label>
                            Poster URL
                        </label>

                        <input class="form-control"
                               type="text"
                               name="posterUrl"
                               maxlength="500"
                               placeholder="https://..."
                               value="<c:out value='${movie.posterUrl}' />">

                    </div>

                    <div class="form-group">

                        <label>
                            Trailer URL
                        </label>

                        <input class="form-control"
                               type="text"
                               name="trailerUrl"
                               maxlength="500"
                               placeholder="https://..."
                               value="<c:out value='${movie.trailerUrl}' />">

                    </div>

                    <div class="section-title">
                        Thể loại
                    </div>

                    <div class="form-group full">

                        <span class="group-title">
                            Chọn ít nhất một thể loại
                            <span class="required">*</span>
                        </span>

                        <div class="genre-box">

                            <c:forEach var="genre"
                                       items="${genres}">

                                <label class="genre-item">

                                    <input type="checkbox"
                                           name="genreId"
                                           value="${genre.genreId}"
                                           <c:if test="${selectedGenreIds.contains(genre.genreId)}">
                                               checked
                                           </c:if>>

                                    <span>
                                        <c:out value="${genre.genreName}" />
                                    </span>

                                </label>

                            </c:forEach>

                        </div>

                    </div>

                    <div class="section-title">
                        Trạng thái phim
                    </div>

                    <div class="form-group full">

                        <div class="status-box">

                            <div class="status-option">

                                <input type="radio"
                                       id="movie-active"
                                       name="status"
                                       value="ACTIVE"
                                       ${empty movie.status
                                         || movie.status == 'ACTIVE'
                                         ? 'checked' : ''}>

                                <label for="movie-active">
                                    ● ACTIVE
                                </label>

                            </div>

                            <div class="status-option">

                                <input type="radio"
                                       id="movie-inactive"
                                       name="status"
                                       value="INACTIVE"
                                       ${movie.status == 'INACTIVE'
                                         ? 'checked' : ''}>

                                <label for="movie-inactive">
                                    ● INACTIVE
                                </label>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

            <div class="movie-form-footer">

                <a class="btn-back"
                   href="${pageContext.request.contextPath}/admin/movies">
                    ← Quay lại danh sách phim
                </a>

                <button class="btn-save"
                        type="submit">

                    <c:choose>
                        <c:when test="${movie.movieId > 0}">
                            Lưu thay đổi
                        </c:when>

                        <c:otherwise>
                            Thêm phim
                        </c:otherwise>
                    </c:choose>

                </button>

            </div>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>