<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">

        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>F-CINEMA</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/style.css">

        

        <script defer src="${pageContext.request.contextPath}/assets/js/main.js?v=profile-avatar-backfix"></script>
    </head>

    <body>

        <header class="site-header">
            <div class="header-container">
                <c:set var="logoPath" value="/home" />

                <c:choose>
                    <c:when test="${sessionScope.roleName eq 'STAFF'}">
                        <c:set var="logoPath" value="/staff/counter" />
                    </c:when>

                    <c:when test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">
                        <c:set var="logoPath" value="/admin/dashboard" />
                    </c:when>

                    <c:when test="${sessionScope.roleName eq 'ADMIN'}">
                        <c:set var="logoPath" value="/system/users" />
                    </c:when>
                </c:choose>

                <a href="${pageContext.request.contextPath}${logoPath}"
                   class="logo">

                    <span class="logo-box">F</span>
                    <span class="logo-text">F-CINEMA</span>
                </a>

                <nav class="main-nav">
                    <c:if test="${empty sessionScope.user}">
                        <a href="${pageContext.request.contextPath}/user/movies">
                            Phim
                        </a>
                    </c:if>
                </nav>

                <div class="header-actions">

                    <c:if test="${not empty sessionScope.user}">

                        <details class="account-menu" id="accountMenu">

                            <summary class="account-menu-button">

                                <span class="account-info">
                                    <span class="account-avatar">

                                        <c:choose>
                                            <c:when test="${not empty sessionScope.user.avatarUrl and fn:startsWith(sessionScope.user.avatarUrl, '/avatars/')}">
                                                <c:url var="headerAvatarSrc"
                                                       value="${sessionScope.user.avatarUrl}" />
                                                <img src="<c:out value='${headerAvatarSrc}' />"
                                                     alt="Ảnh đại diện"
                                                     referrerpolicy="no-referrer">
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${not empty sessionScope.user.fullName}">
                                                        <c:out value="${fn:substring(sessionScope.user.fullName, 0, 1)}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        U
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>

                                    <span class="account-name">
                                        <c:out value="${sessionScope.user.fullName}" />
                                    </span>
                                </span>

                                <span class="account-arrow">▼</span>

                            </summary>

                            <div class="account-dropdown">

                                <c:if test="${sessionScope.roleName eq 'CUSTOMER'}">

                                    <div class="account-dropdown-header">
                                        KHÁCH HÀNG
                                    </div>

                                    <a href="${pageContext.request.contextPath}/user/movies">
                                        <span class="menu-icon">▶</span>
                                        Phim
                                    </a>

                                    <a href="${pageContext.request.contextPath}/user/profile">
                                        <span class="menu-icon">●</span>
                                        Hồ sơ cá nhân
                                    </a>

                                    <a href="${pageContext.request.contextPath}/user/tickets">
                                        <span class="menu-icon">🎫</span>
                                        Vé của tôi
                                    </a>

                                    <a href="${pageContext.request.contextPath}/user/bookings">
                                        <span class="menu-icon">▦</span>
                                        Đơn đặt vé
                                    </a>

                                    <div class="menu-divider"></div>

                                </c:if>

                                <c:if test="${sessionScope.roleName eq 'STAFF'}">

                                    <div class="account-dropdown-header">
                                        NHÂN VIÊN
                                    </div>

                                    <a href="${pageContext.request.contextPath}/staff/counter">
                                        <span class="menu-icon">⌂</span>
                                        Quầy bán hàng
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/ticket-sale">
                                        <span class="menu-icon">$</span>
                                        Bán vé tại quầy
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/food-sale">
                                        <span class="menu-icon">+</span>
                                        Bán bắp nước/combo
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/check-in">
                                        <span class="menu-icon">✓</span>
                                        Check-in vé
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/counter?tab=statistics">
                                        <span class="menu-icon">▥</span>
                                        Thống kê của tôi
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/counter?tab=tickets">
                                        <span class="menu-icon">▤</span>
                                        Vé đã bán
                                    </a>

                                    <a href="${pageContext.request.contextPath}/staff/counter?tab=food">
                                        <span class="menu-icon">▤</span>
                                        Bắp nước/combo đã bán
                                    </a>

                                    <div class="menu-divider"></div>

                                </c:if>

                                <c:if test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">

                                    <div class="account-dropdown-header">
                                        CINEMA MANAGER
                                    </div>

                                    <a href="${pageContext.request.contextPath}/admin/dashboard">
                                        <span class="menu-icon">▣</span>
                                        Dashboard
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/cinemas">
                                        <span class="menu-icon">★</span>
                                        Thông tin rạp
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/rooms">
                                        <span class="menu-icon">▦</span>
                                        Quản lý phòng chiếu
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/movies">
                                        <span class="menu-icon">▶</span>
                                        Quản lý phim
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/genres">
                                        <span class="menu-icon">🎬︎</span>
                                        Quản lý thể loại
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/pricing">
                                        <span class="menu-icon">$</span>
                                        Quản lý giá vé
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/promotions">
                                        <span class="menu-icon">%</span>
                                        Quản lý khuyến mãi
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/showtimes">
                                        <span class="menu-icon">◷</span>
                                        Quản lý lịch chiếu
                                    </a>

                                    <div class="menu-divider"></div>

                                    <a href="${pageContext.request.contextPath}/admin/food-items">
                                        <span class="menu-icon">●</span>
                                        Đồ ăn / thức uống
                                    </a>

                                    <a href="${pageContext.request.contextPath}/admin/combos">
                                        <span class="menu-icon">◈</span>
                                        Quản lý combo
                                    </a>

                                    <div class="menu-divider"></div>

                                </c:if>

                                <c:if test="${sessionScope.roleName eq 'ADMIN'}">

                                    <div class="account-dropdown-header">
                                        ADMIN
                                    </div>

                                    <a href="${pageContext.request.contextPath}/system/users">
                                        <span class="menu-icon">♟</span>
                                        Quản lý người dùng
                                    </a>

                                    <a href="${pageContext.request.contextPath}/system/cinemas">
                                        <span class="menu-icon">★</span>
                                        Quản lý chi nhánh
                                    </a>

                                    <div class="menu-divider"></div>

                                </c:if>

                                <form action="${pageContext.request.contextPath}/auth/logout"
                                      method="post"
                                      class="logout-form">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<c:out value='${sessionScope.csrfToken}' />">

                                    <button type="submit" class="logout-menu">
                                        <span class="menu-icon">↪</span>
                                        Đăng xuất
                                    </button>

                                </form>

                            </div>

                        </details>

                    </c:if>

                    <c:if test="${empty sessionScope.user}">

                        <div class="guest-actions">

                            <a href="${pageContext.request.contextPath}/auth/login"
                               class="login-button">
                                Đăng nhập
                            </a>

                            <a href="${pageContext.request.contextPath}/auth/register"
                               class="login-button">
                                Đăng ký
                            </a>

                        </div>

                    </c:if>

                </div>

            </div>
        </header>

        <script>
            document.addEventListener("click", function (event) {

                const accountMenu = document.getElementById("accountMenu");

                if (!accountMenu) {
                    return;
                }

                if (!accountMenu.contains(event.target)) {
                    accountMenu.removeAttribute("open");
                }
            });
        </script>

<main class="page-container">
