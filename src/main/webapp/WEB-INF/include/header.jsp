<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>F-CINEMA</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    </head>

    <body>

        <header class="site-header">

            <div class="header-container">

                <!-- Logo -->
                <a href="${pageContext.request.contextPath}/user/movies" class="logo"> F-CINEMA </a>

                <!-- Navigation -->
                <nav class="main-nav">
                    <a href="${pageContext.request.contextPath}/user/movies"> Khám phá </a>

                    <a href="${pageContext.request.contextPath}/user/showtimes"> Giỏ vé </a>

                    <a href="${pageContext.request.contextPath}/user/tickets"> Vé của tôi </a>

                    <a href="${pageContext.request.contextPath}/user/bookings"> Đơn đặt vé </a>

                    <!-- Account / Username -->
                    <a href="${pageContext.request.contextPath}/user/profile">

                        <c:choose>
                            <!-- Logged in -->
                            <c:when test="${not empty sessionScope.user}">
                                ${sessionScope.user.fullName}
                            </c:when>

                            <!-- Not logged in -->
                            <c:otherwise>
                                Tài khoản
                            </c:otherwise>
                        </c:choose>
                    </a>
                </nav>

                <!-- Right side -->
                <div class="header-actions">
                    <c:choose>
                        <!-- Logged in -->
                        <c:when test="${not empty sessionScope.user}">
                            <span class="welcome-user">
                                ${sessionScope.user.fullName}
                            </span>

                            <a href="${pageContext.request.contextPath}/auth/logout" class="logout-link"> Đăng xuất </a>
                        </c:when>

                        <!-- Not logged in -->
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/auth/login" class="login-button"> Đăng nhập </a>
                        </c:otherwise>
                    </c:choose>

                </div>

            </div>

        </header>

        <main class="page-container">