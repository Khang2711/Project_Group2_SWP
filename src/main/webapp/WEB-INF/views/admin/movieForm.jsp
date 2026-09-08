<%@page import="model.Movie"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Phim");
    String ctx = request.getContextPath();
    Movie movie = (Movie) request.getAttribute("movie");
    String mode = (String) request.getAttribute("mode");
    String error = (String) request.getAttribute("error");
    boolean isUpdate = "update".equals(mode);
    String imageURL = movie != null && movie.getImageURL() != null ? movie.getImageURL() : "";
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title"><%= isUpdate ? "Cập nhật phim" : "Thêm phim"%></h1>

    <% if (error != null) { %>
    <div class="alert alert-error"><%= error%></div>
    <% } %>

    <form class="form-grid" action="<%= ctx%>/admin/movies" method="post">
        <input type="hidden" name="action" value="<%= isUpdate ? "update" : "create"%>">

        <div class="field">
            <label>Mã phim</label>
            <input name="movieID" value="<%= movie != null ? movie.getMovieID() : ""%>" <%= isUpdate ? "readonly" : ""%> required>
        </div>
        <div class="field">
            <label>Tên phim</label>
            <input name="title" value="<%= movie != null ? movie.getTitle() : ""%>" required>
        </div>
        <div class="field">
            <label>Thời lượng</label>
            <input type="number" name="duration" min="1" value="<%= movie != null ? movie.getDuration() : 90%>" required>
        </div>
        <div class="field">
            <label>Giá vé</label>
            <input type="number" name="basePrice" min="0" value="<%= movie != null ? movie.getBasePrice() : 80000%>" required>
        </div>
        <div class="field full">
            <label>Mô tả</label>
            <textarea name="description"><%= movie != null ? movie.getDescription() : ""%></textarea>
        </div>
        <div class="field full">
            <label>Ảnh phim</label>
            <div class="movie-image-editor">
                <div class="movie-image-controls">
                    <label class="image-sub-label" for="imageURL">Link ảnh (URL)</label>
                    <input id="imageURL" type="url" name="imageURL" value="<%= imageURL%>"
                           placeholder="Ví dụ: https://example.com/poster.jpg">
                    <small class="muted">Dán link ảnh từ Internet. Ảnh sẽ tự cắt ở giữa theo tỉ lệ poster 2:3.</small>
                </div>
                <div class="movie-image-preview-box">
                    <span class="image-sub-label">Xem trước</span>
                    <div class="poster form-poster-preview">
                        <img id="imagePreview" src="<%= imageURL%>" alt="Ảnh xem trước">
                        <span id="imageFallback" class="poster-fallback"><%= movie != null ? movie.getMovieID() : "ẢNH"%></span>
                    </div>
                </div>
            </div>
        </div>
        <div class="field full">
            <label>Trạng thái</label>
            <select name="status">
                <option value="true" <%= movie == null || movie.isStatus() ? "selected" : ""%>>Đang chiếu</option>
                <option value="false" <%= movie != null && !movie.isStatus() ? "selected" : ""%>>Ngừng chiếu</option>
            </select>
        </div>
        <div class="field full">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-blue" href="<%= ctx%>/admin/movies">Hủy</a>
        </div>
    </form>
</section>

<script>
    const imageURLInput = document.getElementById('imageURL');
    const imagePreview = document.getElementById('imagePreview');
    const imageFallback = document.getElementById('imageFallback');

    function showFallback() {
        imagePreview.style.display = 'none';
        imageFallback.style.display = 'grid';
    }

    function updatePreview() {
        const url = imageURLInput.value.trim();
        if (url === '') {
            showFallback();
            return;
        }
        imagePreview.style.display = 'block';
        imageFallback.style.display = 'none';
        imagePreview.src = url;
    }

    imagePreview.addEventListener('error', showFallback);
    imageURLInput.addEventListener('input', updatePreview);
    updatePreview();
</script>

<%@include file="/WEB-INF/include/footer.jsp" %>
