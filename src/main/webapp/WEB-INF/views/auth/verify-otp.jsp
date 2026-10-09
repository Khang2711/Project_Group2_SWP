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
        text-align: center;
        letter-spacing: 6px;
        font-size: 20px;
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
        gap: 14px;
        flex-wrap: wrap;
        margin-top: 18px;
    }
</style>

<section class="auth-card">
    <h1>Xác nhận OTP</h1>

    <p class="auth-note">
        Mã OTP đã được gửi đến:
        <strong><c:out value="${resetEmail}" /></strong>.
        Mã có hiệu lực trong 5 phút.
    </p>

    <c:if test="${param.sent eq '1'}">
        <p class="auth-success">
            Đã gửi mã OTP.
        </p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/verify-otp"
          method="post">

        <div class="auth-field">
            <label for="otp">Mã OTP</label>

            <input type="text"
                   id="otp"
                   name="otp"
                   maxlength="6"
                   pattern="[0-9]{6}"
                   inputmode="numeric"
                   autocomplete="one-time-code"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            Xác nhận OTP
        </button>
    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/auth/forgot-password">
            Gửi lại OTP
        </a>

        <a href="${pageContext.request.contextPath}/auth/login">
            Quay lại đăng nhập
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
