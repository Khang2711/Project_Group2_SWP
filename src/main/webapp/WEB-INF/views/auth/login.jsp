<%@page import="utils.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String lastUser = (String) request.getAttribute("lastUser");
    if (lastUser == null) {
        lastUser = "";
    }
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>F-Cinema | Login</title>
        <link rel="stylesheet" href="<%= ctx%>/assets/css/fcinema.css?v=20260911-fix2">
    </head>
    <body class="auth-page">
        <div class="auth-card">
            <h1>F-Cinema</h1>

            <% if (error != null) { %>
            <div class="alert alert-error"><%=Html.e(error)%></div>
            <% } %>

            <form accept-charset="UTF-8" class="grid" action="<%= ctx%>/auth" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
                <input type="hidden" name="action" value="signin">
                <div class="field">
                    <label>Tài khoản</label>
                    <input name="userID" value="<%=Html.e(lastUser)%>" required>
                </div>
                <div class="field">
                    <label>Mật khẩu</label>
                    <input type="password" name="password" minlength="6" required>
                </div>
                <button class="btn btn-primary" type="submit">Đăng nhập</button>
                <a class="btn btn-blue" href="<%= ctx%>/auth?action=signup">Đăng ký</a>
            <a href="<%=ctx%>/reset">Quên mật khẩu?</a><a href="<%=ctx%>/movies">Khám phá phim</a></form>
        </div>
    </body>
</html>
