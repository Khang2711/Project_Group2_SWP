<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>
<section class="profile-page">
    <h1>Thông tin cá nhân</h1>

    <c:if test="${not empty error}">
        <p class="profile-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <c:if test="${not empty success}">
        <p class="profile-success" role="status">
            <c:out value="${success}" />
        </p>
    </c:if>

    <c:if test="${not empty profile}">
        <div class="profile-card">
            <div class="profile-summary">
                <c:choose>
                    <c:when test="${not empty profile.avatarUrl}">
                        <c:url var="profileAvatarSrc"
                               value="${profile.avatarUrl}" />

                        <img class="profile-avatar"
                             src="<c:out value='${profileAvatarSrc}' />"
                             alt="Ảnh đại diện"
                             referrerpolicy="no-referrer">
                    </c:when>

                    <c:otherwise>
                        <span class="profile-avatar profile-avatar-empty">
                            F
                        </span>
                    </c:otherwise>
                </c:choose>

                <h2>
                    <c:out value="${profile.fullName}" />
                </h2>

                <span>Customer</span>
            </div>

            <dl class="profile-details">
                <dt>Họ và tên</dt>
                <dd>
                    <c:out value="${profile.fullName}" />
                </dd>

                <dt>Email đăng nhập</dt>
                <dd>
                    <c:out value="${profile.email}" />
                </dd>

                <dt>Số điện thoại</dt>
                <dd>
                    <c:out value="${profile.phone}"
                           default="Chưa cập nhật" />
                </dd>
            </dl>

            <div class="profile-actions">
                <a class="profile-primary"
                   href="${pageContext.request.contextPath}/user/edit-profile">
                    Chỉnh sửa thông tin
                </a>

                <a class="profile-secondary"
                   href="${pageContext.request.contextPath}/home">
                    Về trang chủ
                </a>
            </div>
        </div>
    </c:if>
</section>
<%@ include file="/WEB-INF/include/footer.jsp" %>
