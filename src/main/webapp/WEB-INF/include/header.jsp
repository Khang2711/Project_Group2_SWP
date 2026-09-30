<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

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
                <a href="${pageContext.request.contextPath}/user/movies" class="logo">
                    <span class="logo-mark">F</span>
                    <span class="logo-text">F-CINEMA</span>
                </a>

                <!-- Navigation -->
                <nav class="main-navigation">
                    <a href="${pageContext.request.contextPath}/user/movies" class="nav-link active"> Khám phá </a>

                    <a href="${pageContext.request.contextPath}/user/showtimes" class="nav-link"> Giờ vé </a>

                    <a href="${pageContext.request.contextPath}/user/tickets" class="nav-link"> Vé của tôi </a>

                    <a href="${pageContext.request.contextPath}/user/bookings" class="nav-link"> Đơn đặt vé </a>
                </nav>

                <!-- Account -->
                <div class="account-navigation">

                    <a href="${pageContext.request.contextPath}/user/profile" class="account-link"> Tài khoản </a>

                    <a href="${pageContext.request.contextPath}/auth/logout" class="logout-link"> Đăng xuất </a>
                </div>
            </div>
        </header>