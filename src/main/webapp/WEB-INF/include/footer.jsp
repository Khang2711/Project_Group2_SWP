<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

</main>

<footer class="site-footer">

    <div class="footer-container">

        <div class="footer-brand">
            <h3>F-CINEMA</h3>

            <p>Hệ thống quản lý và đặt vé xem phim.</p>
        </div>

        <div class="footer-links">
            <c:choose>
                <c:when test="${empty sessionScope.user
                                or sessionScope.roleName eq 'CUSTOMER'}">

                    <a href="${pageContext.request.contextPath}/user/movies">
                        Khám phá
                    </a>

                    <a href="${pageContext.request.contextPath}/user/showtimes">
                        Lịch chiếu
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
                    <a href="${pageContext.request.contextPath}/staff/check-in">
                        Check-in vé
                    </a>
                </c:when>

                <c:when test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard">
                        Dashboard
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
        </div>

    </div>

    <div class="footer-bottom">
        © 2026 F-CINEMA. All rights reserved.
    </div>

</footer>

</body>
</html>