<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .user-form-page {
        max-width: 720px;
        margin: 0 auto;
    }

    .user-form-page h1 {
        margin-bottom: 8px;
    }

    .user-form-note {
        margin-bottom: 25px;
        color: #aaaaaa;
    }

    .user-form-box {
        padding: 25px;
        border: 1px solid #303038;
        border-radius: 8px;
        background-color: #18181c;
    }

    .user-form-group {
        margin-bottom: 18px;
    }

    .user-form-group label {
        display: block;
        margin-bottom: 8px;
        color: #eeeeee;
        font-weight: bold;
    }

    .user-form-group input,
    .user-form-group select {
        width: 100%;
        height: 42px;
        padding: 0 12px;
        border: 1px solid #3a3a42;
        border-radius: 6px;
        background-color: #0f0f12;
        color: #ffffff;
        outline: none;
    }

    .user-form-group input:focus,
    .user-form-group select:focus {
        border-color: #ff1f2d;
    }

    .user-form-help {
        display: block;
        margin-top: 6px;
        color: #888888;
        font-size: 13px;
    }

    .user-form-error {
        margin-bottom: 20px;
        padding: 12px 15px;
        border: 1px solid #a3131d;
        border-radius: 6px;
        color: #ff8088;
        background-color: #18181c;
    }

    .user-form-actions {
        display: flex;
        gap: 10px;
        margin-top: 25px;
    }

    .user-form-button {
        padding: 11px 18px;
        border: none;
        border-radius: 6px;
        background-color: #ff1f2d;
        color: #ffffff;
        font-weight: bold;
        cursor: pointer;
    }

    .user-form-button:hover {
        background-color: #d91622;
    }

    .user-form-cancel {
        padding: 11px 18px;
        border-radius: 6px;
        background-color: #303038;
        color: #ffffff;
        font-weight: bold;
    }
</style>

<section class="user-form-page">

    <c:choose>
        <c:when test="${not empty user and user.userId > 0}">
            <h1>Cập nhật tài khoản</h1>
            <p class="user-form-note">
                Chỉnh sửa thông tin, vai trò và rạp được phân công.
            </p>
        </c:when>

        <c:otherwise>
            <h1>Tạo tài khoản</h1>
            <p class="user-form-note">
                Tạo tài khoản mới cho người dùng trong hệ thống.
            </p>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
        <div class="user-form-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <form class="user-form-box"
          method="post"
          action="${pageContext.request.contextPath}/system/user-form">

        <c:if test="${not empty user and user.userId > 0}">
            <input type="hidden"
                   name="userId"
                   value="${user.userId}">
        </c:if>

        <div class="user-form-group">
            <label for="fullName">Họ tên *</label>
            <input type="text"
                   id="fullName"
                   name="fullName"
                   maxlength="100"
                   value="<c:out value='${user.fullName}' />"
                   required>
        </div>

        <div class="user-form-group">
            <label for="email">Email *</label>
            <input type="email"
                   id="email"
                   name="email"
                   maxlength="100"
                   value="<c:out value='${user.email}' />"
                   required>
        </div>

        <div class="user-form-group">
            <label for="phone">Số điện thoại</label>
            <input type="text"
                   id="phone"
                   name="phone"
                   maxlength="20"
                   value="<c:out value='${user.phone}' />">
        </div>

        <div class="user-form-group">
            <label for="password">
                Mật khẩu
                <c:if test="${empty user or user.userId == 0}">
                    *
                </c:if>
            </label>

            <input type="password"
                   id="password"
                   name="password">

            <c:choose>
                <c:when test="${not empty user and user.userId > 0}">
                    <span class="user-form-help">
                        Để trống nếu không muốn đổi mật khẩu.
                    </span>
                </c:when>

                <c:otherwise>
                    <span class="user-form-help">
                        Mật khẩu sẽ được mã hóa MD5 trước khi lưu.
                    </span>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="user-form-group">
            <label for="roleId">Vai trò *</label>

            <select id="roleId"
                    name="roleId"
                    required>

                <option value="">-- Chọn vai trò --</option>

                <c:forEach var="role" items="${roles}">
                    <option value="${role.roleId}"
                            <c:if test="${not empty user
                                         and user.roleId == role.roleId}">
                                selected
                            </c:if>>
                        <c:out value="${role.roleName}" />
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="user-form-group">
            <label for="cinemaId">Rạp áp dụng</label>

            <select id="cinemaId"
                    name="cinemaId">

                <option value="">-- Không áp dụng --</option>

                <c:forEach var="cinema" items="${cinemas}">
                    <option value="${cinema.cinemaId}"
                            <c:if test="${not empty user.cinemaId
                                         and user.cinemaId == cinema.cinemaId}">
                                selected
                            </c:if>>
                        <c:out value="${cinema.cinemaName}" />
                    </option>
                </c:forEach>
            </select>

            <span class="user-form-help">
                Bắt buộc chọn rạp khi vai trò là STAFF hoặc CINEMA_MANAGER.
            </span>
        </div>

        <c:if test="${not empty user and user.userId > 0}">
            <div class="user-form-group">
                <label>Trạng thái hiện tại</label>
                <span>
                    <c:out value="${user.status}" />
                </span>
            </div>
        </c:if>

        <div class="user-form-actions">
            <button type="submit"
                    class="user-form-button">
                <c:choose>
                    <c:when test="${not empty user and user.userId > 0}">
                        Lưu thay đổi
                    </c:when>
                    <c:otherwise>
                        Tạo tài khoản
                    </c:otherwise>
                </c:choose>
            </button>

            <a class="user-form-cancel"
               href="${pageContext.request.contextPath}/system/users">
                Hủy
            </a>
        </div>

    </form>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
