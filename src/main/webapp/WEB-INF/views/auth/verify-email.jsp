<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<section class="email-verification-card" data-css-page="auth-verify-email">
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
        <hr class="email-verification-divider">

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
