<%@page import="model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Tài khoản");
    String ctx = request.getContextPath();
    User user = (User) request.getAttribute("user");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title">Tài khoản</h1>

    <% if (error != null) { %>
    <div class="alert alert-error"><%= error%></div>
    <% } %>
    <% if (success != null) { %>
    <div class="alert alert-success"><%= success%></div>
    <% } %>

    <% if (user != null) { %>
    <form class="form-grid" action="<%= ctx%>/profile" method="post">
        <div class="field">
            <label>Tài khoản</label>
            <input value="<%= user.getUserID()%>" readonly>
        </div>
        <div class="field">
            <label>Vai trò</label>
            <input value="<%= "AD".equals(user.getRoleID()) ? "Admin" : "User"%>" readonly>
        </div>
        <div class="field full">
            <label>Họ tên</label>
            <input name="fullName" value="<%= user.getFullName()%>" required>
        </div>
        <div class="field full">
            <label>Mật khẩu hiện tại</label>
            <input type="password" name="currentPassword" autocomplete="current-password"
                   placeholder="Bắt buộc khi muốn đổi mật khẩu">
        </div>
        <div class="field full">
            <label>Mật khẩu mới</label>
            <input type="password" name="newPassword" minlength="6" autocomplete="new-password"
                   placeholder="Bỏ trống nếu không đổi mật khẩu">
            <span class="muted">Mật khẩu mới phải có ít nhất 6 ký tự và phải nhập đúng mật khẩu hiện tại.</span>
        </div>
        <div class="field full">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-blue" href="<%= ctx%>/movies">Hủy</a>
        </div>
    </form>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
