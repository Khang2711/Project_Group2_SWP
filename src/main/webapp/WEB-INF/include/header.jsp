<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>F-CINEMA</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/style.css">
    </head>

    <body>

        <header class="site-header">

            <div class="header-container">

                <!-- Logo -->
                <a href="${pageContext.request.contextPath}/home"
                   class="logo">

                    <span class="logo-icon">F</span>
                    <span class="logo-text">F-CINEMA</span>

                </a>


                <!-- Navigation -->
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

                    <a href="${pageContext.request.contextPath}/user/profile">
                        Tài khoản
                    </a>

                </nav>


                <!-- User -->
                <div class="user-area">

                    <span class="username">
                        khangnehihi
                    </span>

                    <a href="${pageContext.request.contextPath}/auth/logout"
                       class="logout-link">
                        Đăng xuất
                    </a>

                </div>

            </div>

        </header>