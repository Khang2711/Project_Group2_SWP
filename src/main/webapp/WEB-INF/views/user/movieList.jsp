<%@page import="java.net.URLEncoder"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="model.Movie"%>
<%@page import="model.MovieSchedule"%>
<%@page import="model.Showtime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Lịch chiếu");
    String ctx = request.getContextPath();
    Map<String, List<MovieSchedule>> scheduleByDate
            = (Map<String, List<MovieSchedule>>) request.getAttribute("scheduleByDate");
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
    DateTimeFormatter inputDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel schedule-page">
    <div class="toolbar schedule-toolbar">
        <div>
            <h1 class="page-title">Chọn phim và suất chiếu</h1>
            <p class="muted">Chỉ hiển thị các phim còn lịch chiếu trong tương lai.</p>
        </div>
    </div>

    <form class="search-form" action="<%= ctx%>/movies" method="get">
        <input type="text" name="q" value="<%= keyword%>" placeholder="Tìm tên phim">
        <button class="btn btn-primary" type="submit">Tìm kiếm</button>
        <% if (!keyword.isEmpty()) { %>
        <a class="btn btn-soft" href="<%= ctx%>/movies">Xóa tìm kiếm</a>
        <% } %>
    </form>

    <% if (error != null) { %>
    <div class="alert alert-error"><%= error%></div>
    <% } %>

    <p class="muted schedule-result-count">Có <strong><%= totalRecords%></strong> vị trí phim theo ngày chiếu.</p>

    <%
        if (scheduleByDate != null && !scheduleByDate.isEmpty()) {
            for (Map.Entry<String, List<MovieSchedule>> entry : scheduleByDate.entrySet()) {
                String rawDate = entry.getKey();
                String formattedDate = rawDate;
                try {
                    formattedDate = LocalDate.parse(rawDate, inputDate).format(displayDate);
                } catch (Exception ignored) {
                }
    %>
    <section class="schedule-day-section">
        <div class="schedule-date-heading">
            <span class="schedule-date-icon">📅</span>
            <div>
                <span class="schedule-date-label">Ngày chiếu</span>
                <h2><%= formattedDate%></h2>
            </div>
        </div>

        <div class="schedule-movie-grid">
            <%
                for (MovieSchedule schedule : entry.getValue()) {
                    Movie movie = schedule.getMovie();
                    List<Showtime> showtimes = schedule.getShowtimes();
                    Showtime firstShowtime = showtimes == null || showtimes.isEmpty()
                            ? null : showtimes.get(0);
            %>
            <article class="schedule-movie-card">
                <div class="poster schedule-poster">
                    <% if (movie.getImageURL() != null && !movie.getImageURL().trim().isEmpty()) { %>
                    <img src="<%= movie.getImageURL()%>" alt="<%= movie.getTitle()%>"
                         onerror="this.style.display='none';this.nextElementSibling.style.display='grid';">
                    <span class="poster-fallback" style="display:none;"><%= movie.getMovieID()%></span>
                    <% } else { %>
                    <span class="poster-fallback"><%= movie.getMovieID()%></span>
                    <% } %>
                </div>

                <div class="schedule-card-content">
                    <div>
                        <h3 class="schedule-movie-title"><%= movie.getTitle()%></h3>
                        <div class="schedule-movie-meta">
                            <span><%= movie.getDuration()%> phút</span>
                            <strong><%= df.format(movie.getBasePrice())%> VNĐ</strong>
                        </div>
                    </div>

                    <p class="schedule-description"><%= movie.getDescription()%></p>

                    <div class="showtime-area">
                        <span class="showtime-label">Chọn giờ chiếu:</span>
                        <div class="showtime-buttons">
                            <%
                                if (showtimes != null) {
                                    for (Showtime showtime : showtimes) {
                            %>
                            <a class="showtime-button"
                               href="<%= ctx%>/booking/seats?showtimeID=<%= showtime.getShowtimeID()%>"
                               title="Đặt vé lúc <%= showtime.getStartTime()%> tại <%= showtime.getRoomName()%>">
                                <strong><%= showtime.getStartTime()%></strong>
                                <small><%= showtime.getRoomName()%></small>
                            </a>
                            <%
                                    }
                                }
                            %>
                        </div>
                    </div>

                    <% if (firstShowtime != null) { %>
                    <a class="btn btn-primary schedule-book-button"
                       href="<%= ctx%>/booking/seats?showtimeID=<%= firstShowtime.getShowtimeID()%>">
                        Đặt vé
                    </a>
                    <% } %>
                </div>
            </article>
            <%
                }
            %>
        </div>
    </section>
    <%
            }
        } else {
    %>
    <div class="empty-schedule">
        <strong>Hiện chưa có lịch chiếu phù hợp.</strong>
        <span>Vui lòng quay lại sau hoặc thử từ khóa khác.</span>
    </div>
    <% } %>

    <% if (totalPages > 1) { %>
    <div class="pagination">
        <% if (currentPage > 1) { %>
        <a href="<%= ctx%>/movies?q=<%= encodedKeyword%>&page=<%= currentPage - 1%>">Trước</a>
        <% } %>
        <% for (int i = 1; i <= totalPages; i++) { %>
        <a class="<%= i == currentPage ? "active" : ""%>"
           href="<%= ctx%>/movies?q=<%= encodedKeyword%>&page=<%= i%>"><%= i%></a>
        <% } %>
        <% if (currentPage < totalPages) { %>
        <a href="<%= ctx%>/movies?q=<%= encodedKeyword%>&page=<%= currentPage + 1%>">Sau</a>
        <% } %>
    </div>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
