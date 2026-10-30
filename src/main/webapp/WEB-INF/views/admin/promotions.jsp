<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .promo-tools {
        display: flex;
        justify-content: space-between;
        align-items: end;
        gap: 15px;
        margin-bottom: 20px;
        flex-wrap: wrap;
    }

    .promo-filter {
        display: flex;
        gap: 10px;
        align-items: end;
        flex-wrap: wrap;
    }

    .promo-filter label {
        display: block;
        margin-bottom: 5px;
    }

    .promo-filter input,
    .promo-filter select {
        padding: 9px 10px;
        background: #18181c;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
    }

    .small-button {
        padding: 9px 14px;
        border: 1px solid #55555f;
        border-radius: 5px;
        background: #222228;
        color: white;
        cursor: pointer;
        text-decoration: none;
    }

    .small-button:hover {
        background: #303038;
    }

    .promo-table {
        width: 100%;
        border-collapse: collapse;
        background: #18181c;
    }

    .promo-table th,
    .promo-table td {
        padding: 12px;
        border: 1px solid #303038;
    }

    .promo-table th {
        background: #222228;
        text-align: left;
    }

    .id-column {
        width: 55px;
        text-align: center !important;
    }

    .menu-column {
        width: 100px;
        text-align: center !important;
    }

    .status-active {
        color: #4caf50;
        font-weight: bold;
    }

    .status-upcoming {
        color: #2196f3;
        font-weight: bold;
    }

    .status-expired {
        color: #ff9800;
        font-weight: bold;
    }

    .status-inactive {
        color: #aaaaaa;
        font-weight: bold;
    }

    .menu-cell {
        width: 100px;
        text-align: center;
        position: relative;
    }

    .action-menu {
        position: relative;
        display: inline-block;
    }

    .action-menu summary {
        cursor: pointer;
        list-style: none;
        font-size: 18px;
        font-weight: bold;
        padding: 4px 9px;
        border-radius: 4px;
        user-select: none;
    }

    .action-menu summary:hover {
        background: #2a2a30;
    }

    .action-menu summary::-webkit-details-marker {
        display: none;
    }

    .menu-content {
        position: absolute;
        right: 0;
        top: 30px;
        width: 150px;
        padding: 6px;
        background: #222228;
        border: 1px solid #44444d;
        border-radius: 5px;
        z-index: 20;
    }

    .menu-content a,
    .menu-content button {
        display: block;
        width: 100%;
        padding: 8px 10px;
        margin: 0;
        background: transparent;
        color: white;
        border: none;
        border-radius: 3px;
        text-decoration: none;
        text-align: left;
        cursor: pointer;
        box-sizing: border-box;
        font-size: 14px;
    }

    .menu-content a:hover,
    .menu-content button:hover {
        background: #33333a;
    }

    .menu-content form {
        margin: 0;
    }

    .detail-modal {
        display: none;
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.7);
        z-index: 100;
        align-items: center;
        justify-content: center;
    }

    .detail-box {
        width: 520px;
        max-width: 90%;
        max-height: 80vh;
        overflow-y: auto;
        background: #18181c;
        border: 1px solid #44444d;
        border-radius: 8px;
        padding: 22px;
    }

    .detail-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 12px;
        margin-bottom: 15px;
        border-bottom: 1px solid #33333a;
    }

    .detail-header h3 {
        margin: 0;
    }

    .close-button {
        background: transparent;
        color: white;
        border: none;
        font-size: 25px;
        cursor: pointer;
    }

    .detail-row {
        display: flex;
        padding: 8px 0;
        border-bottom: 1px solid #29292f;
    }

    .detail-label {
        width: 180px;
        font-weight: bold;
    }

    .detail-value {
        flex: 1;
    }

    .detail-footer {
        margin-top: 20px;
        text-align: right;
    }
</style>

<div class="movie-page-title">
    <h1>Quản lý khuyến mãi</h1>
    <div class="title-line"></div>
</div>

<div class="promo-tools">

    <form action="${ctx}/admin/promotions"
          method="get"
          class="promo-filter">

        <div>
            <label for="keyword">Tìm mã</label>

            <input type="text"
                   id="keyword"
                   name="keyword"
                   value="${fn:escapeXml(keyword)}"
                   placeholder="Nhập mã khuyến mãi">
        </div>

        <div>
            <label for="filter">Tình trạng</label>

            <select id="filter" name="filter">
                <option value="ALL"
                        ${filter eq 'ALL' ? 'selected' : ''}>
                    Tất cả
                </option>

                <option value="AVAILABLE"
                        ${filter eq 'AVAILABLE' ? 'selected' : ''}>
                    Đang hiệu lực
                </option>

                <option value="UPCOMING"
                        ${filter eq 'UPCOMING' ? 'selected' : ''}>
                    Sắp diễn ra
                </option>

                <option value="EXPIRED"
                        ${filter eq 'EXPIRED' ? 'selected' : ''}>
                    Hết hạn
                </option>

                <option value="INACTIVE"
                        ${filter eq 'INACTIVE' ? 'selected' : ''}>
                    Vô hiệu hóa
                </option>
            </select>
        </div>

        <button type="submit" class="small-button">
            Lọc
        </button>

        <a href="${ctx}/admin/promotions" class="small-button">
            Xóa lọc
        </a>
    </form>

    <a href="${ctx}/admin/promotion-form" class="login-button">
        Thêm khuyến mãi
    </a>

</div>

<p>
    Tổng số:
    <strong><c:out value="${totalRecords}" /></strong>
    khuyến mãi
</p>

<c:if test="${empty promotions}">
    <p>Không tìm thấy khuyến mãi phù hợp.</p>
</c:if>

<c:if test="${not empty promotions}">

    <table class="promo-table"
           data-paginate="true"
           data-page-size="10">

        <thead>
            <tr>
                <th class="id-column">ID</th>
                <th>Promo Code</th>
                <th>Ưu đãi</th>
                <th>Thời hạn</th>
                <th>Trạng thái</th>
                <th class="menu-column">Thao tác</th>
            </tr>
        </thead>

        <tbody>
            <c:forEach var="promotion" items="${promotions}">

                <c:set var="displayStatus"
                       value="${displayStatuses[promotion.promotionId]}" />

                <tr>
                    <td class="id-column">
                        <c:out value="${promotion.promotionId}" />
                    </td>

                    <td>
                        <strong>
                            <c:out value="${promotion.promoCode}" />
                        </strong>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${promotion.discountType eq 'PERCENT'}">
                                Giảm
                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    maxFractionDigits="2" />%
                            </c:when>

                            <c:otherwise>
                                Giảm
                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    groupingUsed="true"
                                    maxFractionDigits="0" />
                                VNĐ
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>
                        <fmt:formatDate value="${promotion.startDate}"
                                        pattern="dd/MM/yyyy" />
                        -
                        <fmt:formatDate value="${promotion.endDate}"
                                        pattern="dd/MM/yyyy" />
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${displayStatus eq 'AVAILABLE'}">
                                <span class="status-active">ACTIVE</span>
                            </c:when>

                            <c:when test="${displayStatus eq 'UPCOMING'}">
                                <span class="status-upcoming">UPCOMING</span>
                            </c:when>

                            <c:when test="${displayStatus eq 'EXPIRED'}">
                                <span class="status-expired">EXPIRED</span>
                            </c:when>

                            <c:otherwise>
                                <span class="status-inactive">INACTIVE</span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td class="menu-cell">
                        <details class="action-menu">

                            <summary>...</summary>

                            <div class="menu-content">
                                <button type="button"
                                        onclick="openDetail(${promotion.promotionId})">
                                    Xem chi tiết
                                </button>

                                <a href="${ctx}/admin/promotion-form?id=${promotion.promotionId}">
                                    Chỉnh sửa
                                </a>

                                <c:if test="${promotion.status eq 'ACTIVE'}">
                                    <form action="${ctx}/admin/promotions"
                                          method="post"
                                          onsubmit="return confirm('Bạn có chắc muốn vô hiệu hóa mã này?');">

                                        <input type="hidden"
                                               name="promotionId"
                                               value="${promotion.promotionId}">

                                        <input type="hidden"
                                               name="status"
                                               value="INACTIVE">

                                        <button type="submit">
                                            Vô hiệu hóa
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${promotion.status eq 'INACTIVE'}">
                                    <form action="${ctx}/admin/promotions"
                                          method="post">

                                        <input type="hidden"
                                               name="promotionId"
                                               value="${promotion.promotionId}">

                                        <input type="hidden"
                                               name="status"
                                               value="ACTIVE">

                                        <button type="submit">
                                            Kích hoạt
                                        </button>
                                    </form>
                                </c:if>
                            </div>

                        </details>
                    </td>
                </tr>

            </c:forEach>
        </tbody>
    </table>

</c:if>

<c:forEach var="promotion" items="${promotions}">

    <div id="detail-${promotion.promotionId}" class="detail-modal">
        <div class="detail-box">

            <div class="detail-header">
                <h3>Chi tiết khuyến mãi</h3>

                <button type="button"
                        class="close-button"
                        onclick="closeDetail(${promotion.promotionId})">
                    ×
                </button>
            </div>

            <div class="detail-row">
                <div class="detail-label">ID</div>
                <div class="detail-value">
                    <c:out value="${promotion.promotionId}" />
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Promo Code</div>
                <div class="detail-value">
                    <strong>
                        <c:out value="${promotion.promoCode}" />
                    </strong>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Mô tả</div>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${not empty promotion.description}">
                            <c:out value="${promotion.description}" />
                        </c:when>

                        <c:otherwise>
                            -
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Loại giảm</div>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${promotion.discountType eq 'PERCENT'}">
                            Phần trăm
                        </c:when>

                        <c:otherwise>
                            Số tiền cố định
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Giá trị giảm</div>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${promotion.discountType eq 'PERCENT'}">
                            <fmt:formatNumber
                                value="${promotion.discountValue}"
                                maxFractionDigits="2" />%
                        </c:when>

                        <c:otherwise>
                            <fmt:formatNumber
                                value="${promotion.discountValue}"
                                groupingUsed="true"
                                maxFractionDigits="0" />
                            VNĐ
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Đơn hàng tối thiểu</div>
                <div class="detail-value">
                    <fmt:formatNumber
                        value="${promotion.minOrderAmount}"
                        groupingUsed="true"
                        maxFractionDigits="0" />
                    VNĐ
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Giảm tối đa</div>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${promotion.maxDiscountAmount != null}">
                            <fmt:formatNumber
                                value="${promotion.maxDiscountAmount}"
                                groupingUsed="true"
                                maxFractionDigits="0" />
                            VNĐ
                        </c:when>

                        <c:otherwise>
                            Không giới hạn
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Giới hạn sử dụng</div>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${promotion.usageLimit != null}">
                            <c:out value="${promotion.usageLimit}" /> lần
                        </c:when>

                        <c:otherwise>
                            Không giới hạn
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Số lượt đã dùng</div>
                <div class="detail-value">
                    <c:out value="${promotion.usedCount}" /> lần
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Ngày bắt đầu</div>
                <div class="detail-value">
                    <fmt:formatDate value="${promotion.startDate}"
                                    pattern="dd/MM/yyyy" />
                </div>
            </div>

            <div class="detail-row">
                <div class="detail-label">Ngày kết thúc</div>
                <div class="detail-value">
                    <fmt:formatDate value="${promotion.endDate}"
                                    pattern="dd/MM/yyyy" />
                </div>
            </div>

            <div class="detail-footer">
                <button type="button"
                        class="small-button"
                        onclick="closeDetail(${promotion.promotionId})">
                    Đóng
                </button>
            </div>

        </div>
    </div>

</c:forEach>

<script>
    function openDetail(id) {
        var modal = document.getElementById("detail-" + id);

        if (modal != null) {
            modal.style.display = "flex";
        }

        closeMenus();
    }

    function closeDetail(id) {
        var modal = document.getElementById("detail-" + id);

        if (modal != null) {
            modal.style.display = "none";
        }
    }

    function closeMenus() {
        var menus = document.querySelectorAll(".action-menu");

        for (var i = 0; i < menus.length; i++) {
            menus[i].removeAttribute("open");
        }
    }

    var menus = document.querySelectorAll(".action-menu");

    for (var i = 0; i < menus.length; i++) {
        menus[i].addEventListener("toggle", function () {
            if (this.open) {
                for (var j = 0; j < menus.length; j++) {
                    if (menus[j] !== this) {
                        menus[j].removeAttribute("open");
                    }
                }
            }
        });
    }
</script>

<%@ include file="/WEB-INF/include/table-pagination.jsp" %>
<%@ include file="/WEB-INF/include/footer.jsp" %>