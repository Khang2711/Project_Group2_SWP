<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="movie-form-page" data-css-page="admin-movie-form">

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

        <form method="post" enctype="multipart/form-data"
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

                    <div class="form-group full">

                        <label for="moviePoster">
                            Poster phim (JPG, PNG hoặc GIF; tối đa 5 MB)
                        </label>

                        <input class="form-control"
                               id="moviePoster"
                               type="file"
                               name="moviePoster"
                               accept=".jpg,.jpeg,.png,.gif,image/jpeg,image/png,image/gif">

                        <div class="help-text">
                            Chọn ảnh từ máy tính. Khi sửa phim, để trống sẽ giữ poster hiện tại.
                            Ảnh mới được lưu vào assets/images/movies để có thể push lên GitHub.
                        </div>

                        <c:if test="${not empty movie.posterUrl}">
                            <c:set var="posterPath" value="${fn:trim(movie.posterUrl)}" />
                            <c:choose>
                                <c:when test="${fn:startsWith(posterPath, 'http://') or fn:startsWith(posterPath, 'https://')}">
                                    <c:set var="posterPreviewSrc" value="${posterPath}" />
                                </c:when>
                                <c:when test="${fn:startsWith(posterPath, '/')}">
                                    <c:url var="posterPreviewSrc" value="${posterPath}" />
                                </c:when>
                                <c:otherwise>
                                    <c:url var="posterPreviewSrc" value="/${posterPath}" />
                                </c:otherwise>
                            </c:choose>
                        </c:if>

                        <div class="poster-preview"
                             id="posterPreviewBox"
                             style="${empty movie.posterUrl ? 'display:none;' : ''}">
                            <img id="posterPreview"
                                 src="<c:out value='${posterPreviewSrc}' />"
                                 alt="Poster phim hiện tại"
                                 onerror="this.style.display='none';">
                        </div>

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

<script>
(function () {
    var input = document.getElementById('moviePoster');
    var preview = document.getElementById('posterPreview');
    var previewBox = document.getElementById('posterPreviewBox');
    if (!input || !preview || !previewBox) {
        return;
    }
    input.addEventListener('change', function () {
        var file = this.files && this.files.length ? this.files[0] : null;
        if (!file) {
            return;
        }
        var validType = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type);
        var validExtension = /\\.(jpe?g|png|gif)$/i.test(file.name);
        if (!validType && !validExtension) {
            alert('Vui lòng chọn ảnh JPG, PNG hoặc GIF.');
            this.value = '';
            return;
        }
        preview.src = URL.createObjectURL(file);
        preview.style.display = 'block';
        previewBox.style.display = 'block';
    });
})();
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>