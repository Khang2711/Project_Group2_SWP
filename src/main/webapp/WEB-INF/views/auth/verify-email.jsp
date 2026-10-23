<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .email-verification-card {
        max-width: 460px;
        margin: 40px auto;
        padding: 26px;
        background: #18181c;
        color: #f5f5f5;
        border: 1px solid #303038;
        border-radius: 12px;
    }

    .email-verification-card h1 {
        margin-bottom: 12px;
    }

    .email-verification-note {
        color: #b8b8c0;
        line-height: 1.6;
        margin-bottom: 20px;
    }

    .email-verification-card .auth-field {
        margin-bottom: 16px;
    }

    .email-verification-card .auth-field label {
        display: block;
        margin-bottom: 7px;
    }

    .email-verification-card .auth-field input {
        box-sizing: border-box;
        width: 100%;
        padding: 11px 12px;
        background: #101014;
        color: #ffffff;
        border: 1px solid #484852;
        border-radius: 6px;
    }

    .email-verification-card .otp-input {
        text-align: center;
        letter-spacing: 6px;
        font-size: 21px;
    }

    .email-verification-card .auth-submit {
        width: 100%;
        padding: 12px;
        background: #e50914;
        color: #ffffff;
        border: none;
        border-radius: 6px;
        font-weight: bold;
        cursor: pointer;
    }

    .email-verification-card .auth-error {
        color: #ff7777;
        margin-bottom: 16px;
    }

    .email-verification-card .auth-success {
        color: #55d98a;
        margin-bottom: 16px;
    }

    .email-verification-card .auth-links {
        display: flex;
        gap: 14px;
        flex-wrap: wrap;
        margin-top: 20px;
    }
</style>

<section class="email-verification-card">
    <h1>Xác thực email</h1>

    <p class="email-verification-note">
        Nhập email đã đăng ký để nhận mã OTP. Mã có hiệu lực trong 5 phút.
        Bạn cần xác thực email trước khi đăng nhập.
    </p>

    <c:if test="${param.sent eq '1'}">
        <p class="auth-success" role="status">
            OTP đã được gửi. Hãy kiểm tra hộp thư đến hoặc thư mục Spam.
        </p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/verify-email"
          method="post">
        <input type="hidden" name="action" value="send">

        <div class="auth-field">
            <label for="email">Email đăng ký</label>
            <input type="email"
                   id="email"
                   name="email"
                   value="<c:out value='${emailVerificationEmail}' />"
                   maxlength="100"
                   autocomplete="email"
                   placeholder="Ví dụ: ten@gmail.com"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            <c:choose>
                <c:when test="${otpSent}">Gửi lại OTP</c:when>
                <c:otherwise>Gửi OTP</c:otherwise>
            </c:choose>
        </button>
    </form>

    <c:if test="${otpSent}">
        <hr style="border-color:#303038; margin:22px 0;">

        <p class="email-verification-note">
            Mã OTP đã được gửi đến
            <strong><c:out value="${emailVerificationEmail}" /></strong>.
        </p>

        <form action="${pageContext.request.contextPath}/auth/verify-email"
              method="post">
            <input type="hidden" name="action" value="verify">

            <div class="auth-field">
                <label for="otp">Mã OTP gồm 6 chữ số</label>
                <input class="otp-input"
                       type="text"
                       id="otp"
                       name="otp"
                       maxlength="6"
                       pattern="[0-9]{6}"
                       inputmode="numeric"
                       autocomplete="one-time-code"
                       required>
            </div>

            <button type="submit" class="auth-submit">
                Xác thực email
            </button>
        </form>
    </c:if>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/auth/login">
            Quay lại đăng nhập
        </a>
        <a href="${pageContext.request.contextPath}/auth/register">
            Đăng ký tài khoản khác
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
