<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .admin-page {
        max-width: 1180px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .page-heading {
        margin-bottom: 28px;
    }

    .page-heading h1 {
        margin: 0 0 8px;
        font-size: 32px;
    }

    .page-heading p {
        margin: 0;
        color: #999;
    }

    .cinema-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
        padding: 28px;
    }

    .cinema-title {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: 25px;
    }

    .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 18px;
        margin-bottom: 28px;
    }

    .info-item {
        background: #1c1c1f;
        border: 1px solid #2c2c30;
        border-radius: 8px;
        padding: 18px;
    }

    .info-label {
        display: block;
        color: #888;
        font-size: 13px;
        margin-bottom: 7px;
        text-transform: uppercase;
    }

    .info-value {
        color: #fff;
        font-size: 16px;
    }

    .btn-primary {
        display: inline-block;
        background: #ff1744;
        color: #fff;
        padding: 11px 19px;
        border-radius: 7px;
        text-decoration: none;
        font-weight: 600;
    }

    .btn-primary:hover {
        background: #e3133b;
    }

    @media (max-width: 700px) {
        .info-grid {
            grid-template-columns: 1fr;
        }
    }
</style>

<div class="admin-page">

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