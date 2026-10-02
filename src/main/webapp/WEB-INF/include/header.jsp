<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">

    <head>

        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>F-CINEMA</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>

    </head>

    <body>

        <header class="site-header">

            <div class="header-container">

                <!-- Logo -->
                <a href="${pageContext.request.contextPath}/user/movies" class="logo">

                    <span class="logo-box">F</span>

                    <span class="logo-text">F-CINEMA</span>

                </a>


                <nav class="main-nav">

                    <a href="${pageContext.request.contextPath}/user/movies">
                        Khám phá
                    </a>

                    <a href="${pageContext.request.contextPath}/user/showtimes">
                        Giờ vé
                    </a>

                    <a href="${pageContext.request.contextPath}/user/tickets">
                        Vé của tôi
                    </a>

                    <a href="${pageContext.request.contextPath}/user/bookings">
                        Đơn đặt vé
                    </a>

                </nav>


                <div class="header-actions">

                    <c:choose>

                        <c:when test="${not empty sessionScope.user}">

                            <a href="${pageContext.request.contextPath}/user/profile" class="profile-button">
                                ${sessionScope.user.fullName}
                            </a>

                            <a href="${pageContext.request.contextPath}/auth/logout" class="logout-link">
                                Đăng xuất
                            </a>

                        </c:when>


                        <c:otherwise>

                            <a href="${pageContext.request.contextPath}/auth/login" class="login-button">
                                Đăng nhập
                            </a>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </header>

        <main class="page-container">