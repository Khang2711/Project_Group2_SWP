<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="model.Movie"%>
<%@page import="model.Room"%>
<%@page import="model.Showtime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Suất chiếu");
    String ctx = request.getContextPath();
    Showtime showtime = (Showtime) request.getAttribute("showtime");
    List<Movie> movies = (List<Movie>) request.getAttribute("movies");
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
    String mode = (String) request.getAttribute("mode");
    String error = (String) request.getAttribute("error");
    boolean isUpdate = "update".equals(mode);
    String today = LocalDate.now().toString();
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title"><%= isUpdate ? "Cập nhật suất chiếu" : "Thêm suất chiếu"%></h1>

    <% if (error != null) { %>
    <div class="alert alert-error"><%=Html.e(error)%></div>
    <% } %>

    <form accept-charset="UTF-8" class="form-grid" action="<%= ctx%>/admin/showtimes" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
        <input type="hidden" name="action" value="<%= isUpdate ? "update" : "add"%>">

        <div class="field">
            <label>Mã suất chiếu</label>
            <input name="showtimeID" value="<%= showtime != null ? showtime.getShowtimeID() : ""%>" <%= isUpdate ? "readonly" : ""%> required>
        </div>

        <div class="field">
            <label>Phim</label>
            <select name="movieID" required>
                <%
                    if (movies != null) {
                        for (Movie movie : movies) {
                            String selected = showtime != null && movie.getMovieID().equals(showtime.getMovieID()) ? "selected" : "";
                %>
                <option value="<%=Html.e(movie.getMovieID())%>" <%= selected%>><%=Html.e(movie.getTitle())%><%= movie.isStatus() ? "" : " (Chưa có lịch chiếu)"%></option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <div class="field">
            <label>Phòng</label>
            <select name="roomID" required>
                <%
                    if (rooms != null) {
                        for (Room room : rooms) {
                            String selected = showtime != null && room.getRoomID().equals(showtime.getRoomID()) ? "selected" : "";
                %>
                <option value="<%=Html.e(room.getRoomID())%>" <%= selected%>><%=Html.e(room.getRoomName())%><%= room.isStatus() ? "" : " (Bảo trì)"%></option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <div class="field">
            <label>Ngày chiếu</label>
            <input type="date" name="showDate" value="<%= showtime != null ? showtime.getShowDate() : ""%>" <%= isUpdate ? "" : "min=\"" + today + "\""%> required>
        </div>

        <div class="field">
            <label>Giờ bắt đầu</label>
            <input type="time" name="startTime" value="<%= showtime != null ? showtime.getStartTime() : ""%>" required>
        </div>

        <div class="field">
            <label>Giờ kết thúc dự kiến (máy chủ tính lại theo phim)</label>
            <input type="time" name="endTime" value="<%= showtime != null ? showtime.getEndTime() : ""%>" required>
        </div>

        <div class="field full">
            <label>Trạng thái</label>
            <select name="status">
                <option value="true" <%= showtime == null || showtime.isStatus() ? "selected" : ""%>>Mở bán</option>
                <option value="false" <%= showtime != null && !showtime.isStatus() ? "selected" : ""%>>Đã khóa</option>
            </select>
        </div>

        <div class="field full">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-blue" href="<%= ctx%>/admin/showtimes">Hủy</a>
        </div>
    </form>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
