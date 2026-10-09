<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>F-CINEMA</title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/assets/css/style.css">

        <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    </head>

    <body>
        <header class="site-header">
            <div class="header-container">

                <a href="${pageContext.request.contextPath}/home"
                   class="logo">
                    <span class="logo-box">F</span>
                    <span class="logo-text">F-CINEMA</span>
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

                            <a href="${pageContext.request.contextPath}/admin/dashboard">
                                Dashboard
                            </a>

                            <a href="${pageContext.request.contextPath}/admin/cinemas">
                                Thông tin rạp
                            </a>

                            <a href="${pageContext.request.contextPath}/admin/rooms">
                                Phòng chiếu
                            </a>

                            <a href="${pageContext.request.contextPath}/admin/movies">
                                Quản lý phim
                            </a>

                            <a href="${pageContext.request.contextPath}/admin/showtimes">
                                Quản lý lịch chiếu
                            </a>

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
                                <c:when test="${sessionScope.roleName eq 'CUSTOMER'}">
                                    <a href="${pageContext.request.contextPath}/user/profile"
                                       class="profile-button">
                                        <c:out value="${sessionScope.user.fullName}" />
                                    </a>
                                </c:when>

                                <c:otherwise>
                                    <span class="profile-button">
                                        <c:out value="${sessionScope.user.fullName}" />
                                    </span>
                                </c:otherwise>
                            </c:choose>

                            <a href="${pageContext.request.contextPath}/auth/logout"
                               class="logout-link">
                                Đăng xuất
                            </a>
                        </c:when>

                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/auth/login"
                               class="login-button">
                                Đăng nhập
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>
        </header>

        <main class="page-container">