<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/users.css">
<section class="user-admin">
    <div class="user-heading"><div><h1>Quản lý tài khoản</h1><p>Tài khoản, vai trò và chi nhánh rạp.</p></div>
        <a class="ua-button primary" href="${pageContext.request.contextPath}/system/user-form">+ Tạo tài khoản</a>
    </div>
    <c:if test="${not empty sessionScope.userMessage}">
        <p class="ua-success" role="status"><c:out value="${sessionScope.userMessage}" /></p>
        <c:remove var="userMessage" scope="session" />
    </c:if>
    <c:if test="${not empty error}"><p class="ua-error" role="alert"><c:out value="${error}" /></p></c:if>
    <div class="ua-table-wrap"><table class="ua-table">
        <thead><tr><th>ID</th><th>Họ tên / Email</th><th>Điện thoại</th><th>Vai trò</th><th>Chi nhánh</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
        <tbody>
        <c:forEach var="u" items="${users}">
            <tr>
                <td><c:out value="${u.userId}" /></td>
                <td><strong><c:out value="${u.fullName}" /></strong><br><span class="ua-muted"><c:out value="${u.email}" /></span></td>
                <td><c:out value="${u.phone}" default="—" /></td>
                <td><c:out value="${u.roleName}" /></td>
                <td><c:out value="${u.cinemaName}" default="—" /></td>
                <td><span class="ua-badge"><c:out value="${u.status == 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu hóa'}" /></span></td>
                <td><div class="ua-actions">
                    <a class="ua-button" href="${pageContext.request.contextPath}/system/user-form?id=${u.userId}">Xem / Sửa</a>
                    <c:if test="${u.status == 'ACTIVE' and u.userId != sessionScope.user.userId}">
                        <form method="post" action="${pageContext.request.contextPath}/system/users" onsubmit="return confirm('Vô hiệu hóa tài khoản này?');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.userCsrfToken}' />">
                            <input type="hidden" name="id" value="${u.userId}">
                            <input type="hidden" name="action" value="deactivate">
                            <button class="ua-button danger" type="submit">Vô hiệu hóa</button>
                        </form>
                    </c:if>
                </div></td>
            </tr>
        </c:forEach>
        <c:if test="${empty users}"><tr><td colspan="7">Chưa có tài khoản.</td></tr></c:if>
        </tbody>
    </table></div>
</section>
<%@ include file="/WEB-INF/include/footer.jsp" %>
