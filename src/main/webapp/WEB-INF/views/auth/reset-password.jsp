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
        margin-bottom: 10px;
    }

    .auth-note {
        margin-bottom: 20px;
        color: #aaaaaa;
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

    .auth-links {
        margin-top: 18px;
    }
</style>

<section class="auth-card">
    <h1>Đặt lại mật khẩu</h1>

    <p class="auth-note">
        OTP đã được xác nhận. Nhập mật khẩu mới cho tài khoản.
    </p>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/reset-password"
          method="post">

        <div class="auth-field">
            <label for="newPassword">Mật khẩu mới</label>

            <input type="password"
                   id="newPassword"
                   name="newPassword"
                   minlength="6"
                   autocomplete="new-password"
                   required>
        </div>

        <div class="auth-field">
            <label for="confirmPassword">
                Xác nhận mật khẩu mới
            </label>

            <input type="password"
                   id="confirmPassword"
                   name="confirmPassword"
                   minlength="6"
                   autocomplete="new-password"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            Cập nhật mật khẩu
        </button>
    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/auth/login">
            Quay lại đăng nhập
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
