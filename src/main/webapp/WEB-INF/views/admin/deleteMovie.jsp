<%@page import="model.Movie"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Xóa phim");
    String ctx = request.getContextPath();
    Movie movie = (Movie) request.getAttribute("movie");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title">Xóa phim</h1>

    <% if (movie != null) { %>
    <form accept-charset="UTF-8" action="<%= ctx%>/admin/movies" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="movieID" value="<%=Html.e(movie.getMovieID())%>">
        <p>Bạn có chắc muốn xóa phim <%=Html.e(movie.getTitle())%> không?</p>
        <button class="btn btn-danger" type="submit">Xóa</button>
        <a class="btn btn-blue" href="<%= ctx%>/admin/movies">Hủy</a>
    </form>
    <% } else { %>
    <div class="alert alert-error">Không tìm thấy phim.</div>
    <a class="btn btn-blue" href="<%= ctx%>/admin/movies">Quay lại</a>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
