<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="isEdit"
       value="${not empty requestScope.managedUser
                and requestScope.managedUser.userId > 0}" />



<section class="user-form-page" data-css-page="system-user-form">

    <c:choose>
        <c:when test="${isEdit}">
            <h1>Chỉnh sửa thông tin tài khoản</h1>
            <p class="user-form-note">
                Admin có thể chỉnh sửa họ tên, email, số điện thoại và rạp làm việc.
                Vai trò hiện tại sẽ được giữ nguyên.
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

        <c:if test="${isEdit}">
            <input type="hidden"
                   name="userId"
                   value="${requestScope.managedUser.userId}">
        </c:if>

        <div class="user-form-group">
            <label for="fullName">Họ tên *</label>
            <input type="text"
                   id="fullName"
                   name="fullName"
                   maxlength="100"
                   value="${fn:escapeXml(requestScope.managedUser.fullName)}"
                   required>
        </div>

        <div class="user-form-group">
            <label for="email">Email *</label>
            <input type="email"
                   id="email"
                   name="email"
                   maxlength="100"
                   value="${fn:escapeXml(requestScope.managedUser.email)}"
                   required>
        </div>

        <div class="user-form-group">
            <label for="phone">Số điện thoại</label>
            <input type="text"
                   id="phone"
                   name="phone"
                   maxlength="20"
                   value="${fn:escapeXml(requestScope.managedUser.phone)}">
        </div>

        <div class="user-form-group">
            <label for="password">Mật khẩu</label>

            <c:choose>
                <c:when test="${isEdit}">
                    <input type="password"
                           id="password"
                           value="********"
                           disabled>

                    <span class="user-form-help">
                        Admin không được thay đổi mật khẩu của tài khoản này.
                    </span>
                </c:when>

                <c:otherwise>
                    <input type="password"
                           id="password"
                           name="password"
                           minlength="6"
                           maxlength="128"
                           autocomplete="new-password"
                           required>

                    <span class="user-form-help">
                        Mật khẩu tối thiểu 6 ký tự và được mã hóa MD5 trước khi lưu.
                    </span>
                </c:otherwise>
            </c:choose>
        </div>

        <c:choose>
            <c:when test="${isEdit}">
                <div class="user-form-group">
                    <label for="roleName">Vai trò</label>
                    <input type="text"
                           id="roleName"
                           value="${fn:escapeXml(requestScope.managedRoleName)}"
                           readonly>
                    <span class="user-form-help">
                        Không thể thay đổi vai trò của tài khoản đã tạo.
                    </span>
                </div>
            </c:when>

            <c:otherwise>
                <div class="user-form-group">
                    <label for="roleId">Vai trò *</label>
                    <select id="roleId"
                            name="roleId"
                            required>
                        <option value="">-- Chọn vai trò --</option>
                        <c:forEach var="role" items="${roles}">
                            <c:choose>
                                <c:when test="${not empty requestScope.managedUser
                                                and requestScope.managedUser.roleId == role.roleId}">
                                    <option value="${role.roleId}" selected>
                                        <c:out value="${role.roleName}" />
                                    </option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${role.roleId}">
                                        <c:out value="${role.roleName}" />
                                    </option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="user-form-group">
            <label for="cinemaId">Rạp áp dụng</label>

            <select id="cinemaId"
                    name="cinemaId">

                <option value="">-- Không áp dụng --</option>

                <c:forEach var="cinema" items="${cinemas}">
                    <c:choose>
                        <c:when test="${not empty requestScope.managedUser.cinemaId
                                        and requestScope.managedUser.cinemaId == cinema.cinemaId}">
                            <option value="${cinema.cinemaId}" selected>
                                <c:out value="${cinema.cinemaName}" />
                            </option>
                        </c:when>

                        <c:otherwise>
                            <option value="${cinema.cinemaId}">
                                <c:out value="${cinema.cinemaName}" />
                            </option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select>

            <span class="user-form-help">
                Bắt buộc chọn rạp cho tài khoản STAFF hoặc CINEMA_MANAGER.
            </span>
        </div>

        <c:if test="${isEdit}">
            <div class="user-form-group">
                <label>Trạng thái hiện tại</label>
                <span>
                    <c:out value="${requestScope.managedUser.status}" />
                </span>
            </div>
        </c:if>

        <div class="user-form-actions">
            <button type="submit"
                    class="user-form-button">
                <c:choose>
                    <c:when test="${isEdit}">
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
