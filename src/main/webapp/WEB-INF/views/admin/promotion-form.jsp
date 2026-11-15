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



<div class="promotion-page" data-css-page="admin-promotion-form">

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