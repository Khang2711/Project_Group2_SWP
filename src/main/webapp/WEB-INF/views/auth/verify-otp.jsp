<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<section class="auth-card" data-css-page="auth-verify-otp">
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
