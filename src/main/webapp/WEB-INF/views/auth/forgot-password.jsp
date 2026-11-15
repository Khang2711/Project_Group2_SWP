<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<section class="auth-card" data-css-page="auth-forgot-password">
    <h1>Quên mật khẩu</h1>

    <p class="auth-note">
        Nhập email tài khoản. Hệ thống sẽ gửi mã OTP có hiệu lực trong 5 phút.
    </p>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/forgot-password"
          method="post">

        <div class="auth-field">
            <label for="email">Email</label>

            <input type="email"
                   id="email"
                   name="email"
                   value="<c:out value='${email}' />"
                   maxlength="100"
                   autocomplete="email"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            Gửi mã OTP
        </button>
    </form>

    <div class="auth-links">
        <a href="${pageContext.request.contextPath}/auth/login">
            Quay lại đăng nhập
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
