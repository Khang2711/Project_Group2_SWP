<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.Movie"%>
<%@page import="model.Showtime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Chi tiết phim");
    String ctx = request.getContextPath();
    Movie movie = (Movie) request.getAttribute("movie");
    List<Showtime> showtimes = (List<Showtime>) request.getAttribute("showtimes");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <% if (movie != null) { %>
    <div class="movie-detail">
        <div class="poster movie-detail-poster">
            <% if (movie.getImageURL() != null && !movie.getImageURL().trim().isEmpty()) { %>
            <img src="<%= movie.getImageURL()%>" alt="<%= movie.getTitle()%>"
                 onerror="this.style.display='none';this.nextElementSibling.style.display='grid';">
            <span class="poster-fallback" style="display:none;"><%= movie.getMovieID()%></span>
            <% } else { %>
            <span class="poster-fallback"><%= movie.getMovieID()%></span>
            <% } %>
        </div>
        <div class="movie-detail-info">
            <h1 class="page-title"><%= movie.getTitle()%></h1>
            <div class="movie-detail-meta">
                <span><%= movie.getDuration()%> phút</span>
                <strong><%= df.format(movie.getBasePrice())%> VNĐ</strong>
            </div>
            <p class="movie-description"><%= movie.getDescription()%></p>
        </div>
    </div>

    <h2>Suất chiếu</h2>
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã suất</th>
                    <th>Phòng</th>
                    <th>Ngày</th>
                    <th>Giờ</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (showtimes != null && !showtimes.isEmpty()) {
                        for (Showtime st : showtimes) {
                %>
                <tr>
                    <td><strong><%= st.getShowtimeID()%></strong></td>
                    <td><%= st.getRoomName()%></td>
                    <td><%= st.getShowDate()%></td>
                    <td><%= st.getStartTime()%> - <%= st.getEndTime()%></td>
                    <td><a class="btn btn-primary" href="<%= ctx%>/booking/seats?showtimeID=<%= st.getShowtimeID()%>">Chọn ghế</a></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5" style="text-align:center;color:#64748b;">Chưa có suất chiếu.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <% } else { %>
    <div class="alert alert-error">Không tìm thấy phim.</div>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
