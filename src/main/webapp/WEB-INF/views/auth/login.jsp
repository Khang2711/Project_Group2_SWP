<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .auth-card {
        max-width: 420px;
        margin: 40px auto;
        padding: 24px;
        border: 1px solid #ccc;
        border-radius: 12px;
    }

    .auth-card h1 {
        margin-bottom: 20px;
    }

    .auth-field {
        margin-bottom: 16px;
    }

    .auth-field label {
        display: block;
        margin-bottom: 6px;
    }

    .auth-field input {
        box-sizing: border-box;
        width: 100%;
        padding: 10px;
        border: 1px solid #aaa;
        border-radius: 6px;
    }

    .auth-submit {
        width: 100%;
        padding: 12px;
        background: #e50914;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
    }

    .auth-error {
        color: #e53935;
        margin-bottom: 16px;
    }

    .auth-success {
        color: #2e9d54;
        margin-bottom: 16px;
    }

    .auth-links {
        display: flex;
        flex-wrap: wrap;
        gap: 14px;
        margin-top: 18px;
    }
</style>

<section class="auth-card">
    <h1>Đăng nhập</h1>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <c:if test="${param.logout eq '1'}">
        <p class="auth-success">
            Bạn đã đăng xuất.
        </p>
    </c:if>

    <c:if test="${param.reset eq '1'}">
        <p class="auth-success">
            Đổi mật khẩu thành công. Bạn có thể đăng nhập lại.
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/login"
          method="post">

        <div class="auth-field">
            <label for="email">Email</label>

            <input type="email"
                   id="email"
                   name="email"
                   value="<c:out value='${email}' />"
                   maxlength="100"
                   autocomplete="username"
                   required>
        </div>

        <div class="auth-field">
            <label for="password">Mật khẩu</label>

            <input type="password"
                   id="password"
                   name="password"
                   autocomplete="current-password"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            Đăng nhập
        </button>
    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/auth/register">
            Đăng ký
        </a>

        <a href="${pageContext.request.contextPath}/auth/forgot-password">
            Quên mật khẩu?
        </a>

        <a href="${pageContext.request.contextPath}/user/movies">
            Xem phim
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>