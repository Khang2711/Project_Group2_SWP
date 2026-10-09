<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<section>
    <div class="account-page-title">
        <div>
            <h1>Quản lý tài khoản</h1>
            <p>Xem, tạo, phân quyền, phân rạp và vô hiệu hóa tài khoản.</p>
        </div>

        <a class="account-button"
           href="${pageContext.request.contextPath}/system/user-form">
            + Tạo tài khoản
        </a>
    </div>

    <c:if test="${not empty error}">
        <div class="account-message account-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:if test="${param.created eq '1'}">
        <div class="account-message account-success">
            Tạo tài khoản thành công.
        </div>
    </c:if>

    <c:if test="${param.updated eq '1'}">
        <div class="account-message account-success">
            Cập nhật vai trò và rạp thành công.
        </div>
    </c:if>

    <c:if test="${param.deactivated eq '1'}">
        <div class="account-message account-success">
            Đã vô hiệu hóa tài khoản.
        </div>
    </c:if>

    <c:if test="${param.activated eq '1'}">
        <div class="account-message account-success">
            Đã kích hoạt lại tài khoản.
        </div>
    </c:if>

    <div class="account-table-wrapper">
        <table class="account-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Vai trò</th>
                    <th>Rạp</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>

            <tbody>
                <c:forEach var="account" items="${users}">
                    <c:set var="roleName"
                           value="${roleNames[account.roleId]}" />

                    <tr>
                        <td>
                            <c:out value="${account.userId}" />
                        </td>

                        <td>
                            <c:out value="${account.fullName}" />
                        </td>

                        <td>
                            <c:out value="${account.email}" />
                        </td>

                        <td>
                            <c:out value="${roleName}" />
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${(roleName eq 'STAFF'
                                                or roleName eq 'CINEMA_MANAGER')
                                                and not empty account.cinemaId}">
                                    <c:out value="${cinemaNames[account.cinemaId]}" />
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${account.status eq 'ACTIVE'}">
                                    <span class="account-status-active">
                                        ACTIVE
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="account-status-inactive">
                                        <c:out value="${account.status}" />
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <div class="account-actions">

                                

                                <c:choose>
                                    <c:when test="${account.status eq 'ACTIVE'
                                                    and account.userId ne sessionScope.user.userId}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/system/users"
                                              onsubmit="return confirm('Vô hiệu hóa tài khoản này?');">

                                            <input type="hidden"
                                                   name="action"
                                                   value="deactivate">

                                            <input type="hidden"
                                                   name="userId"
                                                   value="${account.userId}">

                                            <button type="submit"
                                                    class="account-button account-button-danger">
                                                Vô hiệu hóa
                                            </button>
                                        </form>
                                    </c:when>

                                    <c:when test="${account.status eq 'INACTIVE'}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/system/users"
                                              onsubmit="return confirm('Kích hoạt lại tài khoản này?');">

                                            <input type="hidden"
                                                   name="action"
                                                   value="activate">

                                            <input type="hidden"
                                                   name="userId"
                                                   value="${account.userId}">

                                            <button type="submit"
                                                    class="account-button account-button-activate">
                                                Kích hoạt lại
                                            </button>
                                        </form>
                                    </c:when>
                                </c:choose>
                                <c:if test="${roleName eq 'STAFF'
                                              or roleName eq 'CINEMA_MANAGER'}">
                                    <a class="account-button account-button-secondary"
                                       href="${pageContext.request.contextPath}/system/user-form?id=${account.userId}">
                                        Sửa
                                    </a>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty users}">
                    <tr>
                        <td colspan="7" class="account-empty">
                            Chưa có tài khoản nào.
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
