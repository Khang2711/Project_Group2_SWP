<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.Showtime"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Suất chiếu");
    String ctx = request.getContextPath();
    List<Showtime> showtimes = (List<Showtime>) request.getAttribute("showtimes");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <div class="toolbar">
        <h1 class="page-title">Suất chiếu</h1>
        <a class="btn btn-primary" href="<%= ctx%>/admin/showtimes?action=add">Thêm suất chiếu</a>
    </div>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã suất</th>
                    <th>Phim</th>
                    <th>Phòng</th>
                    <th>Ngày</th>
                    <th>Giờ</th>
                    <th>Giá vé</th>
                    <th>Trạng thái</th>
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
                    <td><%= st.getMovieTitle()%></td>
                    <td><%= st.getRoomName()%></td>
                    <td><%= st.getShowDate()%></td>
                    <td><%= st.getStartTime()%> - <%= st.getEndTime()%></td>
                    <td><%= df.format(st.getBasePrice())%> VNĐ</td>
                    <td>
                        <% if (!st.isRoomStatus()) { %>
                        <span class="status status-off">Phòng bảo trì</span>
                        <% } else { %>
                        <span class="status <%= st.isStatus() ? "status-on" : "status-off"%>"><%= st.isStatus() ? "Mở bán" : "Đã khóa"%></span>
                        <% } %>
                    </td>
                    <td>
                        <a class="btn btn-blue" href="<%= ctx%>/admin/showtimes?action=edit&showtimeID=<%= st.getShowtimeID()%>">Sửa</a>
                        <form accept-charset="UTF-8" action="<%= ctx%>/admin/showtimes" method="post" style="display:inline;"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="showtimeID" value="<%= st.getShowtimeID()%>">
                            <button class="btn btn-danger" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="8" style="text-align:center;color:var(--muted);">Chưa có suất chiếu.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
