<%@page import="java.net.URLEncoder"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.Movie"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Quản lý phim");
    request.setAttribute("fullWidthPage", Boolean.TRUE);
    String ctx = request.getContextPath();
    List<Movie> movies = (List<Movie>) request.getAttribute("movies");
    String error = (String) request.getAttribute("error");
    String keyword = (String) request.getAttribute("keyword");
    if (keyword == null) {
        keyword = "";
    }
    Integer currentPageObj = (Integer) request.getAttribute("page");
    Integer totalPagesObj = (Integer) request.getAttribute("totalPages");
    Integer totalRecordsObj = (Integer) request.getAttribute("totalRecords");
    int currentPage = currentPageObj == null ? 1 : currentPageObj;
    int totalPages = totalPagesObj == null ? 1 : totalPagesObj;
    int totalRecords = totalRecordsObj == null ? 0 : totalRecordsObj;
    String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <div class="toolbar">
        <a class="btn btn-soft" href="<%=ctx%>/admin/movie-policy">Độ tuổi & thời gian công chiếu</a><h1 class="page-title">Quản lý phim</h1>
        <a class="btn btn-primary" href="<%= ctx%>/admin/movies?action=create">Thêm phim</a>
    </div>

    <form accept-charset="UTF-8" class="search-form" action="<%= ctx%>/admin/movies" method="get">
        <input type="text" name="q" value="<%=Html.e(keyword)%>" placeholder="Tìm kiếm phim">
        <button class="btn btn-primary" type="submit">Tìm kiếm</button>
        <% if (!keyword.isEmpty()) { %>
        <a class="btn btn-soft" href="<%= ctx%>/admin/movies">Xóa tìm kiếm</a>
        <% } %>
    </form>

    <% if (error != null) { %>
    <div class="alert alert-error"><%=Html.e(error)%></div>
    <% } %>

    <p class="muted" style="margin-bottom:14px;">Tìm thấy <%= totalRecords%> phim.</p>

    <div class="table-wrap">
        <table class="admin-movie-table">
            <colgroup>
                <col class="col-poster">
                <col class="col-id">
                <col class="col-title">
                <col class="col-description">
                <col class="col-duration">
                <col class="col-price">
                <col class="col-status">
                <col class="col-actions">
            </colgroup>
            <thead>
                <tr>
                    <th>Ảnh</th>
                    <th>Mã phim</th>
                    <th>Tên phim</th>
                    <th>Mô tả</th>
                    <th>Thời lượng</th>
                    <th>Giá vé</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (movies != null && !movies.isEmpty()) {
                        for (Movie movie : movies) {
                %>
                <tr>
                    <td>
                        <div class="movie-thumb">
                            <% if (movie.getImageURL() != null && !movie.getImageURL().trim().isEmpty()) { %>
                            <img src="<%=Html.e(movie.getImageURL())%>" alt="<%=Html.e(movie.getTitle())%>"
                                 width="48" height="72"
                                 style="width:48px;height:72px;max-width:48px;max-height:72px;object-fit:cover;object-position:center;"
                                 onerror="this.style.display='none';this.nextElementSibling.style.display='grid';">
                            <span class="poster-fallback" style="display:none;"><%=Html.e(movie.getMovieID())%></span>
                            <% } else { %>
                            <span class="poster-fallback"><%=Html.e(movie.getMovieID())%></span>
                            <% } %>
                        </div>
                    </td>
                    <td><strong><%=Html.e(movie.getMovieID())%></strong></td>
                    <td><div class="admin-movie-title"><%=Html.e(movie.getTitle())%></div></td>
                    <td><div class="admin-movie-description"><%=Html.e(movie.getDescription())%></div></td>
                    <td><%= movie.getDuration()%> phút</td>
                    <td><%= df.format(movie.getBasePrice())%> VNĐ</td>
                    <td><span class="status <%= movie.isStatus() ? "status-on" : "status-off"%>"><%= movie.isStatus() ? "Đang chiếu" : "Ngừng chiếu"%></span></td>
                    <td>
                        <div class="admin-movie-actions">
                            <a class="btn btn-blue" href="<%= ctx%>/admin/movies?action=edit&movieID=<%=Html.e(movie.getMovieID())%>">Sửa</a>
                            <a class="btn btn-danger" href="<%= ctx%>/admin/movies?action=delete&movieID=<%=Html.e(movie.getMovieID())%>">Xóa</a>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="8" style="text-align:center;color:var(--muted);">Không tìm thấy phim phù hợp.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>

    <% if (totalPages > 1) { %>
    <div class="pagination">
        <% if (currentPage > 1) { %>
        <a href="<%= ctx%>/admin/movies?q=<%= encodedKeyword%>&page=<%= currentPage - 1%>">Trước</a>
        <% } %>
        <%
            for (int i = 1; i <= totalPages; i++) {
        %>
        <a class="<%= i == currentPage ? "active" : ""%>" href="<%= ctx%>/admin/movies?q=<%= encodedKeyword%>&page=<%= i%>"><%= i%></a>
        <% } %>
        <% if (currentPage < totalPages) { %>
        <a href="<%= ctx%>/admin/movies?q=<%= encodedKeyword%>&page=<%= currentPage + 1%>">Sau</a>
        <% } %>
    </div>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
