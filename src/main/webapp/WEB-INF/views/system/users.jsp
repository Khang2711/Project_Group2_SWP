<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .account-page-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 20px;
        margin-bottom: 25px;
    }

    .account-page-title p {
        color: #aaaaaa;
        margin-top: 8px;
    }

    .account-button {
        display: inline-block;
        padding: 10px 16px;
        border: none;
        border-radius: 6px;
        background-color: #ff1f2d;
        color: #ffffff;
        font-weight: bold;
        cursor: pointer;
    }

    .account-button:hover {
        background-color: #d91622;
    }

    .account-button-secondary {
        background-color: #303038;
    }

    .account-button-danger {
        background-color: #a3131d;
    }

    .account-message {
        margin-bottom: 20px;
        padding: 12px 15px;
        border: 1px solid #3a3a42;
        border-radius: 6px;
        background-color: #18181c;
    }

    .account-error {
        border-color: #a3131d;
        color: #ff8088;
    }

    .account-success {
        border-color: #2f6f44;
        color: #8ee0a9;
    }

    .account-table-wrapper {
        overflow-x: auto;
        border: 1px solid #303038;
        border-radius: 8px;
        background-color: #18181c;
    }

    .account-table {
        width: 100%;
        border-collapse: collapse;
    }

    .account-table th,
    .account-table td {
        padding: 14px 12px;
        border-bottom: 1px solid #303038;
        text-align: left;
        vertical-align: middle;
    }

    .account-table th {
        color: #dddddd;
        background-color: #202026;
    }

    .account-table td {
        color: #eeeeee;
    }

    .account-table tr:last-child td {
        border-bottom: none;
    }

    .account-status-active {
        color: #8ee0a9;
        font-weight: bold;
    }

    .account-status-inactive {
        color: #ff8088;
        font-weight: bold;
    }

    .account-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    .account-actions form {
        margin: 0;
    }

    .account-empty {
        padding: 30px;
        color: #aaaaaa;
        text-align: center;
    }

    @media (max-width: 700px) {
        .account-page-title {
            align-items: flex-start;
            flex-direction: column;
        }
    }
</style>

<section>
    <div class="account-page-title">
        <div>
            <h1>Quản lý tài khoản</h1>
            <p>Xem, tạo, cập nhật và vô hiệu hóa tài khoản trong hệ thống.</p>
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
            Cập nhật tài khoản thành công.
        </div>
    </c:if>

    <c:if test="${param.deactivated eq '1'}">
        <div class="account-message account-success">
            Đã vô hiệu hóa tài khoản.
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
                                <a class="account-button account-button-secondary"
                                   href="${pageContext.request.contextPath}/system/user-form?id=${account.userId}">
                                    Sửa
                                </a>

                                <c:if test="${account.status eq 'ACTIVE'
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
