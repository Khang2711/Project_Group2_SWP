<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .pricing-page {
        max-width: 1450px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 20px;
        margin-bottom: 28px;
    }

    .page-header h1 {
        margin: 0 0 7px;
        font-size: 32px;
    }

    .page-header p {
        margin: 0;
        color: #999;
    }

    .btn-add {
        padding: 11px 18px;
        background: #ff1744;
        color: white;
        border-radius: 7px;
        text-decoration: none;
        font-weight: 600;
        white-space: nowrap;
    }

    .success-message {
        margin-bottom: 20px;
        padding: 13px 16px;
        background: #13291e;
        border: 1px solid #225c3c;
        border-radius: 7px;
        color: #70d99a;
    }

    .error-message {
        margin-bottom: 20px;
        padding: 13px 16px;
        background: #32181d;
        border: 1px solid #6b2935;
        border-radius: 7px;
        color: #ff8095;
    }

    .table-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;
        overflow-x: auto;
    }

    table {
        width: 100%;
        min-width: 1300px;
        border-collapse: collapse;
    }

    th {
        padding: 14px 13px;
        text-align: left;
        background: #1d1d20;
        color: #999;
        font-size: 11px;
        text-transform: uppercase;
    }

    td {
        padding: 16px 13px;
        border-top: 1px solid #29292d;
        color: #ddd;
        vertical-align: middle;
        font-size: 13px;
    }

    tbody tr:hover {
        background: #1c1c1f;
    }

    .muted {
        color: #888;
    }

    .price {
        color: white;
        font-weight: 700;
        font-size: 15px;
        white-space: nowrap;
    }

    .tag {
        display: inline-block;
        padding: 5px 8px;
        border-radius: 5px;
        background: #29292e;
        color: #ddd;
        font-size: 11px;
        font-weight: 600;
    }

    .tag-special {
        background: #44242b;
        color: #ff8ca1;
    }

    .tag-vip {
        background: #3a3016;
        color: #e6c65a;
    }

    .tag-couple {
        background: #342238;
        color: #d792e0;
    }

    .badge {
        display: inline-block;
        padding: 5px 9px;
        border-radius: 15px;
        font-size: 11px;
        font-weight: 600;
    }

    .active {
        color: #6ee49b;
        background: #173322;
    }

    .inactive {
        color: #aaa;
        background: #29292d;
    }

    .actions {
        display: flex;
        align-items: center;
        gap: 7px;
    }

    .btn-edit {
        padding: 7px 11px;
        color: #ff4265;
        border: 1px solid #ff1744;
        border-radius: 5px;
        text-decoration: none;
    }

    .btn-delete {
        padding: 7px 11px;
        color: #ff6b81;
        background: transparent;
        border: 1px solid #5d3038;
        border-radius: 5px;
        cursor: pointer;
    }

    .empty {
        text-align: center;
        color: #888;
        padding: 45px !important;
    }

</style>

<div class="pricing-page">

    <div class="page-header">

        <div>

            <h1>
                Quản lý giá vé
            </h1>

            <p>
                Thiết lập giá theo phim, phòng,
                ghế, ngày và khung giờ.
            </p>

        </div>

        <a class="btn-add"
           href="${pageContext.request.contextPath}/admin/pricing-form">

            + Thêm quy tắc giá

        </a>

    </div>

    <c:if test="${param.saved == '1'}">

        <div class="success-message">
            ✓ Lưu quy tắc giá thành công.
        </div>

    </c:if>

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">
            ✓ Xóa quy tắc giá thành công.
        </div>

    </c:if>

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <div class="table-card">

        <table>

            <thead>

                <tr>

                    <th>Phim</th>
                    <th>Phòng</th>
                    <th>Ghế</th>
                    <th>Ngày</th>
                    <th>Khung giờ</th>
                    <th>Giá</th>
                    <th>Hiệu lực</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="pricing"
                           items="${pricingList}">

                    <tr>

                        <!-- MOVIE -->

                        <td>

                            <c:choose>

                                <c:when test="${empty pricing.movieId}">

                                    <span class="muted">
                                        Tất cả phim
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <c:out value="${pricing.movieTitle}" />

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- ROOM -->

                        <td>

                            <c:choose>

                                <c:when test="${empty pricing.roomType}">

                                    <span class="muted">
                                        Tất cả
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="tag">
                                        ${pricing.roomType}
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- SEAT -->

                        <td>

                            <span class="
                                  tag
                                  ${pricing.seatType == 'VIP'
                                    ? 'tag-vip'
                                    : pricing.seatType == 'COUPLE'
                                    ? 'tag-couple'
                                    : ''}">

                                ${pricing.seatType}

                            </span>

                        </td>

                        <!-- DAY -->

                        <td>

                            <c:choose>

                                <c:when test="${not empty pricing.specialDate}">

                                    <span class="tag tag-special">

                                        <fmt:formatDate
                                            value="${pricing.specialDate}"
                                            pattern="dd/MM/yyyy" />

                                    </span>

                                </c:when>

                                <c:when test="${pricing.dayType == 'WEEKDAY'}">

                                    Thứ 2 - Thứ 6

                                </c:when>

                                <c:when test="${pricing.dayType == 'WEEKEND'}">

                                    Thứ 7 - CN

                                </c:when>

                                <c:otherwise>

                                    Tất cả ngày

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- TIME -->

                        <td>

                            <c:choose>

                                <c:when test="${not empty pricing.startTime}">

                                    <fmt:formatDate
                                        value="${pricing.startTime}"
                                        pattern="HH:mm" />

                                    -

                                    <fmt:formatDate
                                        value="${pricing.endTime}"
                                        pattern="HH:mm" />

                                </c:when>

                                <c:otherwise>

                                    <span class="muted">
                                        Cả ngày
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- PRICE -->

                        <td class="price">

                            <fmt:formatNumber
                                value="${pricing.price}"
                                pattern="#,##0" />

                            đ

                        </td>

                        <!-- EFFECTIVE DATE -->

                        <td>

                            <fmt:formatDate
                                value="${pricing.startDate}"
                                pattern="dd/MM/yyyy" />

                            →

                            <c:choose>

                                <c:when test="${not empty pricing.endDate}">

                                    <fmt:formatDate
                                        value="${pricing.endDate}"
                                        pattern="dd/MM/yyyy" />

                                </c:when>

                                <c:otherwise>

                                    Không giới hạn

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- STATUS -->

                        <td>

                            <c:choose>

                                <c:when test="${pricing.status == 'ACTIVE'}">

                                    <span class="badge active">
                                        ACTIVE
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="badge inactive">
                                        INACTIVE
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </td>

                        <!-- ACTION -->

                        <td>

                            <div class="actions">

                                <a class="btn-edit"
                                   href="${pageContext.request.contextPath}/admin/pricing-form?id=${pricing.pricingId}">

                                    Sửa

                                </a>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/admin/pricing">

                                    <input type="hidden"
                                           name="action"
                                           value="delete">

                                    <input type="hidden"
                                           name="id"
                                           value="${pricing.pricingId}">

                                    <button class="btn-delete"
                                            type="submit"
                                            onclick="return confirm('Xóa quy tắc giá này?');">

                                        Xóa

                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty pricingList}">

                    <tr>

                        <td colspan="9"
                            class="empty">

                            Chưa có quy tắc giá vé.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>