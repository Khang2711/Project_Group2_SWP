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

        <style>
            .site-header {
                position: relative;
                z-index: 1000;
            }

            .site-header .header-container {
                max-width: 1400px;
                margin: 0 auto;
                display: flex;
                align-items: center;
                gap: 28px;
                padding: 14px 22px;
            }

            .site-header .logo {
                flex-shrink: 0;
                white-space: nowrap;
            }

            .site-header .main-nav {
                flex: 1;
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 24px;
                min-width: 0;
            }

            .site-header .header-actions {
                margin-left: auto;
                display: flex;
                align-items: center;
                gap: 12px;
                flex-shrink: 0;
            }

            .account-menu {
                position: relative;
            }

            .account-menu summary {
                list-style: none;
            }

            .account-menu summary::-webkit-details-marker {
                display: none;
            }

            .account-menu-button {
                min-width: 170px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                padding: 10px 15px;
                background: #1c1c21;
                color: white;
                border: 1px solid #3b3b42;
                border-radius: 9px;
                cursor: pointer;
                font-size: 14px;
                font-weight: 600;
                user-select: none;
                transition: 0.18s;
            }

            .account-menu-button:hover {
                border-color: #ff1744;
                background: #242429;
            }

            .account-info {
                display: flex;
                align-items: center;
                min-width: 0;
                gap: 9px;
            }

            .account-avatar {
                width: 30px;
                height: 30px;
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                background: #ff1744;
                color: white;
                font-size: 13px;
                font-weight: 700;
            }

            .account-name {
                max-width: 130px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }

            .account-arrow {
                color: #aaa;
                font-size: 11px;
                transition: 0.2s;
            }

            .account-menu[open] .account-arrow {
                transform: rotate(180deg);
            }

            .account-dropdown {
                position: absolute;
                top: calc(100% + 10px);
                right: 0;
                width: 275px;
                overflow: hidden;
                background: #1b1b20;
                border: 1px solid #34343b;
                border-radius: 11px;
                box-shadow: 0 18px 45px rgba(0, 0, 0, 0.48);
                z-index: 9999;
            }

            .account-dropdown-header {
                padding: 15px 17px;
                border-bottom: 1px solid #303036;
                color: #888;
                font-size: 11px;
                font-weight: 700;
                letter-spacing: 1px;
            }

            .account-dropdown a {
                display: flex;
                align-items: center;
                gap: 12px;
                width: 100%;
                box-sizing: border-box;
                padding: 13px 17px;
                color: #ddd;
                text-decoration: none;
                font-size: 14px;
                transition: 0.15s;
            }

            .account-dropdown a:hover {
                background: #29292f;
                color: white;
            }

            .menu-icon {
                width: 22px;
                flex-shrink: 0;
                text-align: center;
                color: #999;
            }

            .account-dropdown a:hover .menu-icon {
                color: #ff1744;
            }

            .menu-divider {
                height: 1px;
                margin: 5px 0;
                background: #303036;
            }

            .account-dropdown .logout-menu {
                color: #ff718b;
            }

            .account-dropdown .logout-menu:hover {
                background: #351a20;
                color: #ff879b;
            }

            .account-dropdown .logout-menu .menu-icon {
                color: #ff526f;
            }

            .guest-actions {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            @media (max-width: 800px) {
                .site-header .header-container {
                    flex-wrap: wrap;
                }

                .site-header .main-nav {
                    order: 3;
                    flex-basis: 100%;
                    flex-wrap: wrap;
                }
            }

            @media (max-width: 550px) {
                .account-menu {
                    width: 100%;
                }

                .account-menu-button {
                    width: 100%;
                }

                .account-dropdown {
                    left: 0;
                    right: auto;
                    width: 100%;
                }
            }
        </style>

        <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
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

                                        <c:if test="${not empty sessionScope.user.fullName}">
                                            <c:out value="${fn:substring(sessionScope.user.fullName, 0, 1)}" />
                                        </c:if>

                                        <c:if test="${empty sessionScope.user.fullName}">
                                            U
                                        </c:if>

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

                                    <div class="menu-divider"></div>

                                </c:if>

                                <a href="${pageContext.request.contextPath}/auth/logout"
                                   class="logout-menu">

                                    <span class="menu-icon">↪</span>
                                    Đăng xuất
                                </a>

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