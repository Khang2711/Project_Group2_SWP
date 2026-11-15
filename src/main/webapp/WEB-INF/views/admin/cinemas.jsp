<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="admin-page" data-css-page="admin-cinemas">

    <div class="page-heading">
        <h1>Thông tin rạp</h1>
        <p>Thông tin rạp chiếu phim bạn đang quản lý.</p>
    </div>

    <div class="cinema-card">

        <div class="cinema-title">
            <c:out value="${cinema.cinemaName}" />
        </div>

        <div class="info-grid">

            <div class="info-item">
                <span class="info-label">Địa chỉ</span>
                <span class="info-value">
                    <c:out value="${cinema.address}" />
                </span>
            </div>

            <div class="info-item">
                <span class="info-label">Số điện thoại</span>
                <span class="info-value">
                    <c:out value="${cinema.phone}" />
                </span>
            </div>

            <div class="info-item">
                <span class="info-label">Số phòng chiếu</span>
                <span class="info-value">
                    <c:out value="${roomCount}" /> phòng
                </span>
            </div>

            <div class="info-item">
                <span class="info-label">Trạng thái</span>
                <span class="info-value">
                    <c:out value="${cinema.status}" />
                </span>
            </div>

        </div>

        <a class="btn-primary"
           href="${pageContext.request.contextPath}/admin/cinema-form">
            Chỉnh sửa thông tin
        </a>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>