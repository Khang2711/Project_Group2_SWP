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
    <div class="alert alert-error"><%=Html.e(error)%></div>
    <% } %>
    <% if (success != null) { %>
    <div class="alert alert-success"><%=Html.e(success)%></div>
    <% } %>

    <% if (user != null) { %>
    <form accept-charset="UTF-8" class="form-grid" action="<%= ctx%>/profile" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
        <div class="field">
            <label>Tài khoản</label>
            <input value="<%=Html.e(user.getUserID())%>" readonly>
        </div>
        <div class="field">
            <label>Vai trò</label>
            <input value="<%= "AD".equals(user.getRoleID()) ? "Admin" : "User"%>" readonly>
        </div>
        <div class="field full">
            <label>Họ tên</label>
            <input name="fullName" value="<%=Html.e(user.getFullName())%>" required>
        </div>
        <%java.util.Map<String,Object> pd=(java.util.Map<String,Object>)request.getAttribute("profileData");if(pd==null)pd=java.util.Collections.emptyMap();%>
        <div class="field"><label>Email khôi phục</label><input type="email" name="email" value="<%=Html.e(pd.get("email"))%>"></div>
        <div class="field"><label>Số điện thoại</label><input name="phone" maxlength="20" value="<%=Html.e(pd.get("phone"))%>"></div>
        <div class="field"><label>Ngày sinh</label><input type="date" name="birthDate" value="<%=Html.e(pd.get("birthDate"))%>"></div>
        <div class="field"><label>Ảnh đại diện (HTTPS)</label><input type="url" name="avatar" value="<%=Html.e(pd.get("avatar"))%>"></div>
        <%if(pd.get("avatar")!=null&&!pd.get("avatar").toString().isBlank()){%><img src="<%=Html.e(pd.get("avatar"))%>" alt="Ảnh đại diện" width="80" height="80" style="border-radius:50%;object-fit:cover"><%}%>
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
