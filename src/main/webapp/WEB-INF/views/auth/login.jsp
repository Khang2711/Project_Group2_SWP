<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<section class="auth-card">
    <h1>Đăng nhập</h1>

    <c:if test="${not empty success}">
        <p class="auth-success" role="status">
            <c:out value="${success}" />
        </p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/login"
          method="post">

        <div class="auth-field">
            <label for="email">Email</label>

            <input type="email"
                   id="email"
                   name="email"
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