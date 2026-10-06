<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/users.css">
<section class="user-admin ua-form-card">
    <h1>${formUser.userId == 0 ? 'Tạo tài khoản' : 'Thông tin tài khoản'}</h1>
    <c:if test="${not empty error}"><p class="ua-error" role="alert"><c:out value="${error}" /></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/system/user-form" class="ua-form">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.userCsrfToken}' />">
        <input type="hidden" name="id" value="${formUser.userId == 0 ? '' : formUser.userId}">
        <label for="fullName">Họ tên *</label>
        <input id="fullName" name="fullName" maxlength="100" required value="<c:out value='${formUser.fullName}' />">
        <label for="email">Email *</label>
        <input id="email" name="email" type="email" maxlength="100" required value="<c:out value='${formUser.email}' />">
        <label for="phone">Điện thoại</label>
        <input id="phone" name="phone" type="tel" maxlength="16" value="<c:out value='${formUser.phone}' />">
        <label for="password">${formUser.userId == 0 ? 'Mật khẩu *' : 'Mật khẩu mới'}</label>
        <input id="password" name="password" type="password" autocomplete="new-password" minlength="6" maxlength="100" ${formUser.userId == 0 ? 'required' : ''}>
        <p class="ua-help">6–100 ký tự. Khi sửa tài khoản, để trống để giữ mật khẩu hiện tại.</p>
        <label for="roleId">Vai trò *</label>
        <select id="roleId" name="roleId" required>
            <option value="">-- Chọn vai trò --</option>
            <c:forEach var="role" items="${roles}">
                <c:if test="${role.status == 'ACTIVE' or role.roleId == formUser.roleId}">
                    <option value="${role.roleId}" data-role="<c:out value='${role.roleName}' />" ${role.roleId == formUser.roleId ? 'selected' : ''}>
                        <c:out value="${role.roleName}" />${role.status != 'ACTIVE' ? ' (Ngừng hoạt động)' : ''}
                    </option>
                </c:if>
            </c:forEach>
        </select>
        <label for="cinemaId">Chi nhánh rạp</label>
        <select id="cinemaId" name="cinemaId">
            <option value="">-- Không áp dụng / Chọn chi nhánh --</option>
            <c:forEach var="cinema" items="${cinemas}">
                <c:if test="${cinema.status == 'ACTIVE' or cinema.cinemaId == formUser.cinemaId}">
                    <option value="${cinema.cinemaId}" ${cinema.cinemaId == formUser.cinemaId ? 'selected' : ''}>
                        <c:out value="${cinema.cinemaName}" />${cinema.status != 'ACTIVE' ? ' (Ngừng hoạt động)' : ''}
                    </option>
                </c:if>
            </c:forEach>
        </select>
        <p class="ua-help">Bắt buộc với STAFF và CINEMA_MANAGER. ADMIN và CUSTOMER không cần chi nhánh.</p>
        <label for="status">Trạng thái *</label>
        <select id="status" name="status" required>
            <option value="ACTIVE" ${formUser.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
            <option value="INACTIVE" ${formUser.status == 'INACTIVE' ? 'selected' : ''}>Vô hiệu hóa</option>
        </select>
        <c:if test="${formUser.userId == sessionScope.user.userId}"><p class="ua-help">Bạn không thể tự gỡ quyền admin hoặc vô hiệu hóa chính mình.</p></c:if>
        <div class="ua-actions"><button class="ua-button primary" type="submit">Lưu tài khoản</button>
            <a class="ua-button" href="${pageContext.request.contextPath}/system/users">Quay lại</a></div>
    </form>
</section>
<script>
    const roleSelect = document.getElementById('roleId');
    const cinemaSelect = document.getElementById('cinemaId');
    function updateCinemaField() {
        const role = roleSelect.options[roleSelect.selectedIndex].getAttribute('data-role');
        const needsCinema = role === 'STAFF' || role === 'CINEMA_MANAGER';
        cinemaSelect.disabled = !needsCinema;
        cinemaSelect.required = needsCinema;
    }
    roleSelect.addEventListener('change', updateCinemaField);
    updateCinemaField();
</script>
<%@ include file="/WEB-INF/include/footer.jsp" %>
