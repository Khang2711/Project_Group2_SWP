<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

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

        /* =========================
           MANAGER DROPDOWN
           ========================= */

        .manager-menu {
            position: relative;
        }

        .manager-menu summary {
            list-style: none;
        }

        .manager-menu summary::-webkit-details-marker {
            display: none;
        }

        .manager-menu-button {
            min-width: 165px;

            display: flex;
            align-items: center;
            justify-content: space-between;

            gap: 12px;

            padding: 10px 16px;

            border: 1px solid #3b3b42;
            border-radius: 9px;

            background: #1c1c21;
            color: white;

            cursor: pointer;

            font-size: 14px;
            font-weight: 600;

            user-select: none;

            transition: 0.18s;
        }

        .manager-menu-button:hover {
            border-color: #ff1744;
            background: #242429;
        }

        .manager-account {
            display: flex;
            align-items: center;

            gap: 9px;

            min-width: 0;
        }

        .manager-avatar {
            width: 28px;
            height: 28px;

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

        .manager-name {
            max-width: 120px;

            overflow: hidden;

            white-space: nowrap;
            text-overflow: ellipsis;
        }

        .manager-arrow {
            color: #aaa;
            font-size: 11px;

            transition: 0.2s;
        }

        .manager-menu[open] .manager-arrow {
            transform: rotate(180deg);
        }

        .manager-dropdown {
            position: absolute;

            top: calc(100% + 10px);
            right: 0;

            width: 270px;

            overflow: hidden;

            background: #1b1b20;

            border: 1px solid #34343b;
            border-radius: 10px;

            box-shadow:
                0 18px 45px
                rgba(0, 0, 0, 0.45);

            z-index: 9999;
        }

        .manager-dropdown-header {
            padding: 14px 17px;

            border-bottom: 1px solid #303036;

            color: #888;

            font-size: 11px;
            font-weight: 700;

            letter-spacing: 1px;
        }

        .manager-dropdown a {
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

        .manager-dropdown a:hover {
            background: #29292f;
            color: white;
        }

        .manager-dropdown-icon {
            width: 20px;

            flex-shrink: 0;

            text-align: center;

            color: #999;
        }

        .manager-dropdown a:hover
        .manager-dropdown-icon {
            color: #ff1744;
        }

        .manager-menu-divider {
            height: 1px;

            margin: 5px 0;

            background: #303036;
        }

        .manager-dropdown .logout-menu {
            color: #ff718b;
        }

        .manager-dropdown .logout-menu:hover {
            background: #351a20;
            color: #ff879b;
        }

        .manager-dropdown .logout-menu
        .manager-dropdown-icon {
            color: #ff526f;
        }

        /* =========================
           NORMAL ACCOUNT
           ========================= */

        .normal-profile-button {
            display: inline-block;

            padding: 9px 14px;

            border-radius: 6px;

            background: #25252a;
            color: white;

            text-decoration: none;
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

            .manager-menu {
                width: 100%;
            }

            .manager-menu-button {
                width: 100%;
            }

            .manager-dropdown {
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

        <a href="${pageContext.request.contextPath}/home"
           class="logo">

            <span class="logo-box">
                F
            </span>

            <span class="logo-text">
                F-CINEMA
            </span>

        </a>


        <nav class="main-nav">

            <c:choose>

                <c:when test="${empty sessionScope.user
                                or sessionScope.roleName eq 'CUSTOMER'}">

                    <a href="${pageContext.request.contextPath}/user/movies">
                        Phim
                    </a>

                    <c:if test="${not empty sessionScope.user
                                  and sessionScope.roleName eq 'CUSTOMER'}">

                        <a href="${pageContext.request.contextPath}/user/tickets">
                            Vé của tôi
                        </a>

                        <a href="${pageContext.request.contextPath}/user/bookings">
                            Đơn đặt vé
                        </a>

                    </c:if>

                </c:when>


                <c:when test="${sessionScope.roleName eq 'STAFF'}">

                    <a href="${pageContext.request.contextPath}/staff/counter">
                        Bán tại quầy
                    </a>

                    <a href="${pageContext.request.contextPath}/staff/check-in">
                        Check-in vé
                    </a>

                </c:when>


                <c:when test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">

                </c:when>


                <c:when test="${sessionScope.roleName eq 'ADMIN'}">

                    <a href="${pageContext.request.contextPath}/system/users">
                        Quản lý người dùng
                    </a>

                </c:when>

            </c:choose>

        </nav>


        <div class="header-actions">

            <c:choose>

                <c:when test="${not empty sessionScope.user}">

                    <c:choose>

                        <c:when test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">

                            <details class="manager-menu"
                                     id="managerMenu">

                                <summary class="manager-menu-button">

                                    <span class="manager-account">

                                        <span class="manager-avatar">

                                            <c:choose>

                                                <c:when test="${not empty sessionScope.user.fullName}">

                                                    <c:out value="${fn:substring(sessionScope.user.fullName, 0, 1)}" />

                                                </c:when>

                                                <c:otherwise>
                                                    M
                                                </c:otherwise>

                                            </c:choose>

                                        </span>

                                        <span class="manager-name">

                                            <c:out value="${sessionScope.user.fullName}" />

                                        </span>

                                    </span>

                                    <span class="manager-arrow">
                                        ▼
                                    </span>

                                </summary>


                                <div class="manager-dropdown">

                                    <div class="manager-dropdown-header">
                                        CINEMA MANAGER
                                    </div>


                                    <a href="${pageContext.request.contextPath}/admin/dashboard">

                                        <span class="manager-dropdown-icon">
                                            ▣
                                        </span>

                                        Dashboard

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/cinemas">

                                        <span class="manager-dropdown-icon">
                                            ★
                                        </span>

                                        Thông tin rạp

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/rooms">

                                        <span class="manager-dropdown-icon">
                                            ▦
                                        </span>

                                        Quản lý phòng chiếu

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/movies">

                                        <span class="manager-dropdown-icon">
                                            ▶
                                        </span>

                                        Quản lý phim

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/pricing">

                                        <span class="manager-dropdown-icon">
                                            $
                                        </span>

                                        Quản lý giá vé

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/showtimes">

                                        <span class="manager-dropdown-icon">
                                            ◷
                                        </span>

                                        Quản lý lịch chiếu

                                    </a>


                                    <div class="manager-menu-divider"></div>


                                    <a href="${pageContext.request.contextPath}/admin/food-items">

                                        <span class="manager-dropdown-icon">
                                            ●
                                        </span>

                                        Đồ ăn / thức uống

                                    </a>


                                    <a href="${pageContext.request.contextPath}/admin/combos">

                                        <span class="manager-dropdown-icon">
                                            ◈
                                        </span>

                                        Quản lý combo

                                    </a>


                                    <div class="manager-menu-divider"></div>


                                    <a href="${pageContext.request.contextPath}/auth/logout"
                                       class="logout-menu">

                                        <span class="manager-dropdown-icon">
                                            ↪
                                        </span>

                                        Đăng xuất

                                    </a>

                                </div>

                            </details>

                        </c:when>


                        <c:when test="${sessionScope.roleName eq 'CUSTOMER'}">

                            <a href="${pageContext.request.contextPath}/user/profile"
                               class="normal-profile-button">

                                <c:out value="${sessionScope.user.fullName}" />

                            </a>

                            <a href="${pageContext.request.contextPath}/auth/logout"
                               class="logout-link">

                                Đăng xuất

                            </a>

                        </c:when>


                        <c:otherwise>

                            <span class="normal-profile-button">

                                <c:out value="${sessionScope.user.fullName}" />

                            </span>

                            <a href="${pageContext.request.contextPath}/auth/logout"
                               class="logout-link">

                                Đăng xuất

                            </a>

                        </c:otherwise>

                    </c:choose>

                </c:when>


                <c:otherwise>

                    <a href="${pageContext.request.contextPath}/auth/login"
                       class="login-button">

                        Đăng nhập

                    </a>

                    <a href="${pageContext.request.contextPath}/auth/register"
                       class="login-button">

                        Đăng ký

                    </a>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</header>


<script>

    document.addEventListener(
        "click",
        function (event) {

            const managerMenu =
                    document.getElementById(
                            "managerMenu"
                    );

            if (!managerMenu) {
                return;
            }

            if (!managerMenu.contains(
                    event.target
            )) {

                managerMenu.removeAttribute(
                        "open"
                );
            }
        }
    );

</script>


<main class="page-container">