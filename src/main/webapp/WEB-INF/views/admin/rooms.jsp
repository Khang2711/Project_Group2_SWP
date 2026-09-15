<%@page import="java.util.List"%>
<%@page import="model.Room"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Phòng chiếu");
    String ctx = request.getContextPath();
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <div class="toolbar">
        <a class="btn btn-soft" href="<%=ctx%>/admin/room-branch">Gán phòng vào chi nhánh</a><h1 class="page-title">Phòng chiếu</h1>
    </div>

    <form accept-charset="UTF-8" class="form-grid" action="<%= ctx%>/admin/rooms" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
        <input type="hidden" name="action" value="create">
        <div class="field">
            <label>Mã phòng</label>
            <input name="roomID" required>
        </div>
        <div class="field">
            <label>Tên phòng</label>
            <input name="roomName" required>
        </div>
        <div class="field">
            <label>Số ghế</label>
            <input type="number" name="totalSeats" min="1" value="60" required>
        </div>
        <div class="field">
            <label>Trạng thái</label>
            <select name="status">
                <option value="true">Hoạt động</option>
                <option value="false">Bảo trì</option>
            </select>
        </div>
        <div class="field full">
            <button class="btn btn-primary" type="submit">Thêm phòng</button>
        </div>
    </form>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã phòng</th>
                    <th>Tên phòng</th>
                    <th>Số ghế</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (rooms != null && !rooms.isEmpty()) {
                        for (Room room : rooms) {
                            String formID = "roomForm" + room.getRoomID();
                %>
                <tr>
                    <td>
                        <form accept-charset="UTF-8" id="<%= formID%>" action="<%= ctx%>/admin/rooms" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>"></form>
                        <input form="<%= formID%>" type="hidden" name="action" value="update">
                        <input form="<%= formID%>" name="roomID" value="<%=Html.e(room.getRoomID())%>" readonly>
                    </td>
                    <td><input form="<%= formID%>" name="roomName" value="<%=Html.e(room.getRoomName())%>" required></td>
                    <td><input form="<%= formID%>" type="number" name="totalSeats" min="1" value="<%= room.getTotalSeats()%>" required></td>
                    <td>
                        <select form="<%= formID%>" name="status">
                            <option value="true" <%= room.isStatus() ? "selected" : ""%>>Hoạt động</option>
                            <option value="false" <%= !room.isStatus() ? "selected" : ""%>>Bảo trì</option>
                        </select>
                    </td>
                    <td><button form="<%= formID%>" class="btn btn-blue" type="submit">Lưu</button></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5" style="text-align:center;color:var(--muted);">Chưa có phòng chiếu.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
