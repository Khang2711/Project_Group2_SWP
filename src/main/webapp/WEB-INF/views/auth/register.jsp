<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<section class="auth-card auth-card-wide">
    <h1>Đăng ký tài khoản</h1>

    <c:if test="${not empty error}">
        <p class="auth-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/auth/register"
          method="post">

        <div class="auth-field">
            <label for="fullName">Họ và tên</label>

            <input type="text"
                   id="fullName"
                   name="fullName"
                   value="<c:out value='${fullName}' />"
                   maxlength="100"
                   placeholder="Nhập họ và tên"
                   autocomplete="name"
                   required>
        </div>

        <div class="auth-field">
            <label for="email">Email</label>

            <input type="email"
                   id="email"
                   name="email"
                   value="<c:out value='${email}' />"
                   maxlength="100"
                   placeholder="Ví dụ: ten@gmail.com"
                   autocomplete="email"
                   required>
        </div>

        <div class="auth-field">
            <label for="phone">Số điện thoại</label>

            <input type="tel"
                   id="phone"
                   name="phone"
                   value="<c:out value='${phone}' />"
                   inputmode="numeric"
                   pattern="[0-9]{10}"
                   minlength="10"
                   maxlength="10"
                   placeholder="Nhập đúng 10 chữ số"
                   title="Số điện thoại phải gồm đúng 10 chữ số."
                   autocomplete="tel"
                   required>
        </div>

        <div class="auth-field">
            <label for="password">Mật khẩu</label>

            <input type="password"
                   id="password"
                   name="password"
                   minlength="6"
                   placeholder="Nhập ít nhất 6 ký tự"
                   autocomplete="new-password"
                   required>
        </div>

        <div class="auth-field">
            <label for="confirmPassword">Xác nhận mật khẩu</label>

            <input type="password"
                   id="confirmPassword"
                   name="confirmPassword"
                   minlength="6"
                   placeholder="Nhập lại mật khẩu"
                   autocomplete="new-password"
                   required>
        </div>

        <button type="submit" class="auth-submit">
            Đăng ký
        </button>
    </form>

    <div class="auth-links">
        Đã có tài khoản?
        <a href="${pageContext.request.contextPath}/auth/login">
            Đăng nhập
        </a>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>