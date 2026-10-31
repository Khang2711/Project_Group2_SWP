<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

</main>


<footer class="site-footer">

    <div class="footer-container">


        <div class="footer-brand">

            <a href="${pageContext.request.contextPath}${logoPath}"
               class="footer-brand-logo">

                <span class="footer-logo-mark">
                    F
                </span>

                <span>
                    F-CINEMA
                </span>

            </a>


            <p class="footer-description">

                Hệ thống đặt vé và quản lý rạp chiếu phim.
                Khám phá những bộ phim yêu thích và tận hưởng
                trải nghiệm điện ảnh cùng F-CINEMA.

            </p>

        </div>


        <div class="footer-section">

            <h4>
                Truy cập nhanh
            </h4>


            <nav class="footer-links"
                 aria-label="Footer navigation">


                <c:if test="${empty sessionScope.user
                              or sessionScope.roleName eq 'CUSTOMER'}">


                    <a href="${pageContext.request.contextPath}/home">

                        Trang chủ

                    </a>


                    <a href="${pageContext.request.contextPath}/user/movies">

                        Phim

                    </a>


                    <a href="${pageContext.request.contextPath}/user/showtimes">

                        Lịch chiếu

                    </a>


                    <c:if test="${sessionScope.roleName eq 'CUSTOMER'}">


                        <a href="${pageContext.request.contextPath}/user/profile">

                            Hồ sơ cá nhân

                        </a>


                        <a href="${pageContext.request.contextPath}/user/tickets">

                            Vé của tôi

                        </a>


                        <a href="${pageContext.request.contextPath}/user/bookings">

                            Đơn đặt vé

                        </a>


                    </c:if>


                </c:if>


                <c:if test="${sessionScope.roleName eq 'STAFF'}">


                    <a href="${pageContext.request.contextPath}/staff/counter">

                        Quầy bán hàng

                    </a>


                    <a href="${pageContext.request.contextPath}/staff/ticket-sale">

                        Bán vé

                    </a>


                    <a href="${pageContext.request.contextPath}/staff/food-sale">

                        Bán bắp nước / combo

                    </a>


                    <a href="${pageContext.request.contextPath}/staff/check-in">

                        Check-in vé

                    </a>


                </c:if>


                <c:if test="${sessionScope.roleName eq 'CINEMA_MANAGER'}">


                    <a href="${pageContext.request.contextPath}/admin/dashboard">

                        Dashboard

                    </a>


                    <a href="${pageContext.request.contextPath}/admin/movies">

                        Quản lý phim

                    </a>


                    <a href="${pageContext.request.contextPath}/admin/genres">

                        Quản lý thể loại

                    </a>


                    <a href="${pageContext.request.contextPath}/admin/showtimes">

                        Quản lý lịch chiếu

                    </a>


                </c:if>


                <c:if test="${sessionScope.roleName eq 'ADMIN'}">


                    <a href="${pageContext.request.contextPath}/system/users">

                        Quản lý người dùng

                    </a>


                </c:if>


            </nav>

        </div>


        <div class="footer-section footer-contact">

            <h4>
                Liên hệ & Hỗ trợ
            </h4>


            <div class="footer-contact-list">


                <div class="footer-contact-item">

                    <span class="footer-contact-label">
                        Email
                    </span>

                    <a href="mailto:fcinemasytem@gmail.com">

                        fcinemasytem@gmail.com

                    </a>

                </div>


                <div class="footer-contact-item">

                    <span class="footer-contact-label">
                        Hotline
                    </span>

                    <a href="tel:0334155209">

                        0334155209

                    </a>

                </div>


            </div>

        </div>

    </div>


    <div class="footer-bottom">

        <div class="footer-bottom-inner">

            <p>
                © 2026 F-CINEMA. All rights reserved.
            </p>

            <p>
                Trải nghiệm điện ảnh dễ dàng hơn mỗi ngày.
            </p>

        </div>

    </div>

</footer>


</body>
</html>