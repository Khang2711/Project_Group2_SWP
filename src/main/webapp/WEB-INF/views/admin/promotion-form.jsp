<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${not empty promotion and promotion.promotionId > 0}" />

<fmt:formatDate value="${promotion.startDate}"
                pattern="yyyy-MM-dd"
                var="startDateIso" />

<fmt:formatDate value="${promotion.startDate}"
                pattern="dd/MM/yyyy"
                var="startDateDisplay" />

<fmt:formatDate value="${promotion.endDate}"
                pattern="yyyy-MM-dd"
                var="endDateIso" />

<fmt:formatDate value="${promotion.endDate}"
                pattern="dd/MM/yyyy"
                var="endDateDisplay" />

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .promotion-page {
        max-width: 1450px;
        margin: 0 auto;
        padding: 40px 55px 60px;
    }

    .page-title {
        margin: 0;
        color: white;
        font-size: 48px;
    }

    .title-line {
        width: 85px;
        height: 6px;
        margin: 20px 0 14px;
        background: #ff3045;
        border-radius: 4px;
    }

    .page-description {
        margin: 0 0 38px;
        color: #b6b6b6;
        font-size: 18px;
    }

    .error-box {
        margin-bottom: 20px;
        padding: 15px;
        color: white;
        background: #40191d;
        border: 1px solid #ff3045;
        border-radius: 7px;
    }

    .form-card {
        padding: 30px;
        background: #171719;
        border: 1px solid #303036;
        border-radius: 8px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 24px 30px;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .full-width {
        grid-column: 1 / -1;
    }

    .form-group label {
        color: white;
        font-size: 17px;
        font-weight: bold;
    }

    .form-group input,
    .form-group select,
    .form-group textarea {
        width: 100%;
        padding: 15px 16px;
        color: white;
        background: #101012;
        border: 1px solid #393941;
        border-radius: 7px;
        font-size: 16px;
        box-sizing: border-box;
    }

    .form-group input:focus,
    .form-group select:focus,
    .form-group textarea:focus {
        outline: none;
        border-color: #ff3045;
    }

    .form-group textarea {
        min-height: 120px;
        resize: vertical;
    }

    .date-picker {
        position: relative;
        height: 52px;
        cursor: pointer;
    }

    .date-display {
        height: 52px;
        padding-right: 50px !important;
        cursor: pointer;
        pointer-events: none;
    }

    .native-date {
        position: absolute;
        width: 1px !important;
        height: 1px;
        left: 50%;
        top: 50%;
        opacity: 0;
        pointer-events: none;
    }

    .calendar-icon {
        position: absolute;
        right: 16px;
        top: 50%;
        transform: translateY(-50%);
        pointer-events: none;
    }

    .form-actions {
        display: flex;
        gap: 12px;
        margin-top: 30px;
    }

    .btn-promotion {
        padding: 13px 24px;
        color: white;
        border-radius: 7px;
        font-size: 16px;
        font-weight: bold;
        text-decoration: none;
        cursor: pointer;
    }

    .btn-save {
        background: #ff3045;
        border: 1px solid #ff3045;
    }

    .btn-save:hover {
        background: #e9273b;
    }

    .btn-back {
        background: transparent;
        border: 1px solid #55555d;
    }

    .btn-back:hover {
        border-color: white;
    }

    @media (max-width: 850px) {
        .promotion-page {
            padding: 30px 20px;
        }

        .form-grid {
            grid-template-columns: 1fr;
        }

        .full-width {
            grid-column: auto;
        }

        .page-title {
            font-size: 36px;
        }
    }
</style>

<div class="promotion-page">

    <h1 class="page-title">
        ${isEdit ? 'Sửa khuyến mãi' : 'Thêm khuyến mãi'}
    </h1>

    <div class="title-line"></div>

    <p class="page-description">
        ${isEdit ? 'Cập nhật thông tin khuyến mãi.' : 'Nhập thông tin khuyến mãi.'}
    </p>

    <c:if test="${not empty error}">
        <div class="error-box">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="form-card">

        <form action="${ctx}/admin/promotion-form"
              method="post">

            <c:if test="${isEdit}">
                <input type="hidden"
                       name="promotionId"
                       value="${promotion.promotionId}">
            </c:if>

            <div class="form-grid">

                <div class="form-group">
                    <label for="promoCode">
                        Promo Code
                    </label>

                    <input type="text"
                           id="promoCode"
                           name="promoCode"
                           maxlength="50"
                           value="${promotion.promoCode}"
                           required>
                </div>

                <div class="form-group">
                    <label for="discountType">
                        Loại giảm giá
                    </label>

                    <select id="discountType"
                            name="discountType"
                            required>

                        <option value="">
                            -- Chọn loại --
                        </option>

                        <option value="PERCENT"
                                ${promotion.discountType eq 'PERCENT' ? 'selected' : ''}>
                            PERCENT
                        </option>

                        <option value="FIXED"
                                ${promotion.discountType eq 'FIXED' ? 'selected' : ''}>
                            FIXED
                        </option>

                    </select>
                </div>

                <div class="form-group">
                    <label for="discountValue">
                        Giá trị giảm
                    </label>

                    <input type="text"
                           id="discountValue"
                           name="discountValue"
                           inputmode="decimal"
                           value="${promotion.discountValue}"
                           required>
                </div>

                <div class="form-group">
                    <label for="minOrderAmount">
                        Đơn hàng tối thiểu
                    </label>

                    <input type="text"
                           id="minOrderAmount"
                           name="minOrderAmount"
                           inputmode="decimal"
                           value="${promotion.minOrderAmount}">
                </div>

                <div class="form-group">
                    <label for="maxDiscountAmount">
                        Giảm tối đa
                    </label>

                    <input type="text"
                           id="maxDiscountAmount"
                           name="maxDiscountAmount"
                           inputmode="decimal"
                           value="${promotion.maxDiscountAmount}">
                </div>

                <div class="form-group">
                    <label for="usageLimit">
                        Giới hạn sử dụng
                    </label>

                    <input type="text"
                           id="usageLimit"
                           name="usageLimit"
                           inputmode="numeric"
                           value="${promotion.usageLimit}">
                </div>

                <div class="form-group">
                    <label for="startDatePicker">
                        Ngày bắt đầu
                    </label>

                    <div class="date-picker"
                         onclick="openDatePicker('startDatePicker')">

                        <input type="text"
                               id="startDateDisplay"
                               class="date-display"
                               value="${startDateDisplay}"
                               placeholder="DD/MM/YYYY"
                               readonly>

                        <span class="calendar-icon">
                            📅
                        </span>

                        <input type="date"
                               id="startDatePicker"
                               class="native-date"
                               name="startDate"
                               value="${startDateIso}"
                               onchange="showDate(this, 'startDateDisplay')"
                               required>

                    </div>
                </div>

                <div class="form-group">
                    <label for="endDatePicker">
                        Ngày kết thúc
                    </label>

                    <div class="date-picker"
                         onclick="openDatePicker('endDatePicker')">

                        <input type="text"
                               id="endDateDisplay"
                               class="date-display"
                               value="${endDateDisplay}"
                               placeholder="DD/MM/YYYY"
                               readonly>

                        <span class="calendar-icon">
                            📅
                        </span>

                        <input type="date"
                               id="endDatePicker"
                               class="native-date"
                               name="endDate"
                               value="${endDateIso}"
                               onchange="showDate(this, 'endDateDisplay')"
                               required>

                    </div>
                </div>

                <div class="form-group">
                    <label for="status">
                        Trạng thái
                    </label>

                    <select id="status"
                            name="status"
                            required>

                        <option value="ACTIVE"
                                ${promotion.status ne 'INACTIVE' ? 'selected' : ''}>
                            ACTIVE
                        </option>

                        <option value="INACTIVE"
                                ${promotion.status eq 'INACTIVE' ? 'selected' : ''}>
                            INACTIVE
                        </option>

                    </select>
                </div>

                <div class="form-group full-width">
                    <label for="description">
                        Mô tả
                    </label>

                    <textarea id="description"
                              name="description"
                              maxlength="255"><c:out value="${promotion.description}" /></textarea>
                </div>

            </div>

            <div class="form-actions">

                <button type="submit"
                        class="btn-promotion btn-save">
                    Lưu khuyến mãi
                </button>

                <a href="${ctx}/admin/promotions"
                   class="btn-promotion btn-back">
                    Quay lại
                </a>

            </div>

        </form>

    </div>

</div>

<script>
    function openDatePicker(id) {
        var picker = document.getElementById(id);

        if (picker.showPicker) {
            picker.showPicker();
        } else {
            picker.click();
        }
    }

    // Convert yyyy-MM-dd to dd/MM/yyyy for display
    function showDate(input, displayId) {
        if (input.value === "") {
            document.getElementById(displayId).value = "";
            return;
        }

        var parts = input.value.split("-");

        document.getElementById(displayId).value =
                parts[2] + "/" + parts[1] + "/" + parts[0];
    }
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>