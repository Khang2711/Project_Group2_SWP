<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .pricing-page {
        max-width: 1180px;
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
    }

    .btn-add:hover {
        background: #e3133b;
    }

    /* MESSAGE */

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

    /* TABLE */

    .table-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;
        overflow: hidden;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th {
        padding: 14px 16px;
        text-align: left;
        background: #1d1d20;
        color: #999;
        font-size: 12px;
        text-transform: uppercase;
    }

    td {
        padding: 17px 16px;
        border-top: 1px solid #29292d;
        color: #ddd;
        vertical-align: middle;
    }

    tbody tr:hover {
        background: #1c1c1f;
    }

    .pricing-id {
        color: #777;
    }

    /* SEAT TYPE */

    .seat-type {
        display: inline-block;
        padding: 6px 10px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 700;
    }

    .standard {
        background: #29292e;
        color: #ddd;
    }

    .vip {
        background: #3a3016;
        color: #e6c65a;
    }

    .couple {
        background: #342238;
        color: #d792e0;
    }

    /* PRICE */

    .price {
        color: white;
        font-size: 16px;
        font-weight: 700;
    }

    /* STATUS */

    .badge {
        display: inline-block;
        padding: 5px 10px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }

    .active {
        color: #6ee49b;
        background: #173322;
        border: 1px solid #28573a;
    }

    .inactive {
        color: #aaa;
        background: #29292d;
        border: 1px solid #3b3b40;
    }

    /* ACTION */

    .actions {
        display: flex;
        gap: 8px;
        align-items: center;
    }

    .btn-edit {
        padding: 7px 12px;
        color: #ff4265;
        border: 1px solid #ff1744;
        border-radius: 5px;
        text-decoration: none;
        font-size: 13px;
        font-weight: 600;
    }

    .btn-edit:hover {
        background: #ff1744;
        color: white;
    }

    .delete-form {
        margin: 0;
    }

    .btn-delete {
        padding: 7px 12px;
        background: transparent;
        color: #ff6b81;
        border: 1px solid #5d3038;
        border-radius: 5px;
        cursor: pointer;
    }

    .btn-delete:hover {
        background: #ff1744;
        color: white;
        border-color: #ff1744;
    }

    .empty {
        text-align: center;
        color: #888;
        padding: 45px !important;
    }

    @media (max-width: 900px) {

        .page-header {
            flex-direction: column;
            align-items: flex-start;
        }

        .table-card {
            overflow-x: auto;
        }

        table {
            min-width: 900px;
        }
    }

</style>

<div class="pricing-page">

    <div class="page-header">

        <div>

            <h1>
                Quản lý giá vé
            </h1>

            <p>
                Thiết lập giá vé theo loại ghế
                và khoảng thời gian áp dụng.
            </p>

        </div>

        <a class="btn-add"
           href="${pageContext.request.contextPath}/admin/pricing-form">

            + Thêm giá vé

        </a>

    </div>

    <!-- SAVED -->

    <c:if test="${param.saved == '1'}">

        <div class="success-message">

            ✓ Lưu giá vé thành công.

        </div>

    </c:if>

    <!-- DELETED -->

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">

            ✓ Xóa giá vé thành công.

        </div>

    </c:if>

    <!-- ERROR -->

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

                    <th>ID</th>

                    <th>Loại ghế</th>

                    <th>Giá vé</th>

                    <th>Ngày bắt đầu</th>

                    <th>Ngày kết thúc</th>

                    <th>Trạng thái</th>

                    <th>Thao tác</th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="pricing"
                           items="${pricingList}">

                    <tr>

                        <td class="pricing-id">

                            #${pricing.pricingId}

                        </td>

                        <!-- SEAT TYPE -->

                        <td>

                            <span class="
                                  seat-type
                                  ${pricing.seatType == 'VIP'
                                    ? 'vip'
                                    : pricing.seatType == 'COUPLE'
                                    ? 'couple'
                                    : 'standard'}">

                                <c:out value="${pricing.seatType}" />

                            </span>

                        </td>

                        <!-- PRICE -->

                        <td class="price">

                            <fmt:formatNumber
                                value="${pricing.price}"
                                pattern="#,##0" />

                            đ

                        </td>

                        <!-- START -->

                        <td>

                            <fmt:formatDate
                                value="${pricing.startDate}"
                                pattern="dd/MM/yyyy" />

                        </td>

                        <!-- END -->

                        <td>

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

                                        <c:out value="${pricing.status}" />

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

                                <form class="delete-form"
                                      method="post"
                                      action="${pageContext.request.contextPath}/admin/pricing">

                                    <input type="hidden"
                                           name="action"
                                           value="delete">

                                    <input type="hidden"
                                           name="id"
                                           value="${pricing.pricingId}">

                                    <button class="btn-delete"
                                            type="submit"
                                            onclick="return confirm('Bạn có chắc muốn xóa giá vé này?');">

                                        Xóa

                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty pricingList}">

                    <tr>

                        <td colspan="7"
                            class="empty">

                            Rạp chưa có thông tin giá vé.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>