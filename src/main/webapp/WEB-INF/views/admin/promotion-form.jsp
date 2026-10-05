<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:formatDate
    value="${promotion.startDate}"
    pattern="yyyy-MM-dd"
    var="startDateIso"
    />

<fmt:formatDate
    value="${promotion.startDate}"
    pattern="dd/MM/yyyy"
    var="startDateDisplay"
    />

<fmt:formatDate
    value="${promotion.endDate}"
    pattern="yyyy-MM-dd"
    var="endDateIso"
    />

<fmt:formatDate
    value="${promotion.endDate}"
    pattern="dd/MM/yyyy"
    var="endDateDisplay"
    />

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    * {
        box-sizing: border-box;
    }

    .promotion-page {
        max-width: 1450px;
        margin: 0 auto;
        padding: 40px 55px 60px;
    }

    .page-title {
        font-size: 48px;
        font-weight: 700;
        margin: 0;
        color: #ffffff;
    }

    .title-line {
        width: 85px;
        height: 6px;
        border-radius: 4px;
        background: #ff3045;
        margin: 20px 0 14px;
    }

    .page-description {
        margin: 0 0 38px;
        color: #b6b6b6;
        font-size: 18px;
    }

    .error-box {
        border: 1px solid #ff3045;
        background: #40191d;
        color: #ffffff;
        padding: 18px;
        border-radius: 8px;
        margin-bottom: 25px;
        font-size: 16px;
    }

    .form-card {
        background: #171719;
        border: 1px solid #303036;
        border-radius: 8px;
        padding: 30px;
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

    .form-group.full-width {
        grid-column: 1 / -1;
    }

    .form-group label {
        color: #ffffff;
        font-size: 17px;
        font-weight: 700;
    }

    .form-group input,
    .form-group select,
    .form-group textarea {
        width: 100%;
        background: #101012;
        color: #ffffff;
        border: 1px solid #393941;
        border-radius: 7px;
        padding: 15px 16px;
        font-size: 16px;
        outline: none;
    }

    .form-group input:focus,
    .form-group select:focus,
    .form-group textarea:focus {
        border-color: #ff3045;
    }

    .form-group textarea {
        min-height: 120px;
        resize: vertical;
    }

    .date-wrapper {
        position: relative;
        width: 100%;
        height: 52px;
        cursor: pointer;
    }

    .date-wrapper:hover .date-display {
        border-color: #777780;
    }

    .date-display {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 52px;
        background: #101012;
        color: #ffffff;
        border: 1px solid #393941;
        border-radius: 7px;
        padding: 15px 52px 15px 16px;
        font-size: 16px;
        font-weight: 500;
        pointer-events: none;
        cursor: pointer;
    }

    .native-date {
        position: absolute;
        width: 1px !important;
        height: 1px !important;
        opacity: 0;
        padding: 0 !important;
        border: 0 !important;
        pointer-events: none;
    }

    .calendar-icon {
        position: absolute;
        right: 16px;
        top: 50%;
        transform: translateY(-50%);
        pointer-events: none;
        font-size: 20px;
        z-index: 2;
    }

    .date-format {
        color: #9f9fa5;
        font-size: 13px;
        margin-top: 2px;
    }

    .form-actions {
        display: flex;
        gap: 12px;
        margin-top: 30px;
    }

    .btn-promotion {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 13px 24px;
        border-radius: 7px;
        font-size: 16px;
        font-weight: 700;
        text-decoration: none;
        cursor: pointer;
    }

    .btn-save {
        border: 1px solid #ff3045;
        background: #ff3045;
        color: #ffffff;
    }

    .btn-save:hover {
        background: #e9273b;
    }

    .btn-back {
        border: 1px solid #55555d;
        background: transparent;
        color: #ffffff;
    }

    .btn-back:hover {
        border-color: #ffffff;
        color: #ffffff;
    }

    @media (max-width: 850px) {
        .promotion-page {
            padding: 30px 20px;
        }

        .form-grid {
            grid-template-columns: 1fr;
        }

        .form-group.full-width {
            grid-column: auto;
        }

        .page-title {
            font-size: 36px;
        }
    }
</style>

<div class="promotion-page">

    <h1 class="page-title">
        <c:choose>
            <c:when test="${promotion.promotionId > 0}">
                Sửa khuyến mãi
            </c:when>
            <c:otherwise>
                Thêm khuyến mãi
            </c:otherwise>
        </c:choose>
    </h1>

    <div class="title-line"></div>

    <p class="page-description">
        <c:choose>
            <c:when test="${promotion.promotionId > 0}">
                Cập nhật thông tin khuyến mãi.
            </c:when>
            <c:otherwise>
                Nhập thông tin khuyến mãi.
            </c:otherwise>
        </c:choose>
    </p>

    <c:if test="${not empty error}">
        <div class="error-box">
            ${error}
        </div>
    </c:if>

    <div class="form-card">

        <form
            action="${pageContext.request.contextPath}/admin/promotion-form"
            method="post"
            >

            <c:if test="${promotion.promotionId > 0}">
                <input
                    type="hidden"
                    name="promotionId"
                    value="${promotion.promotionId}"
                    >
            </c:if>

            <div class="form-grid">

                <div class="form-group">

                    <label for="promoCode">
                        Promo Code
                    </label>

                    <input
                        type="text"
                        id="promoCode"
                        name="promoCode"
                        maxlength="50"
                        value="${promotion.promoCode}"
                        required
                        >

                </div>

                <div class="form-group">

                    <label for="discountType">
                        Loại giảm giá
                    </label>

                    <select
                        id="discountType"
                        name="discountType"
                        required
                        >

                        <option value="">
                            -- Chọn loại --
                        </option>

                        <c:choose>

                            <c:when test="${promotion.discountType == 'PERCENT'}">

                                <option value="PERCENT" selected>
                                    PERCENT
                                </option>

                                <option value="FIXED">
                                    FIXED
                                </option>

                            </c:when>

                            <c:when test="${promotion.discountType == 'FIXED'}">

                                <option value="PERCENT">
                                    PERCENT
                                </option>

                                <option value="FIXED" selected>
                                    FIXED
                                </option>

                            </c:when>

                            <c:otherwise>

                                <option value="PERCENT">
                                    PERCENT
                                </option>

                                <option value="FIXED">
                                    FIXED
                                </option>

                            </c:otherwise>

                        </c:choose>

                    </select>

                </div>

                <div class="form-group">

                    <label for="discountValue">
                        Giá trị giảm
                    </label>

                    <input
                        type="text"
                        id="discountValue"
                        name="discountValue"
                        inputmode="decimal"
                        value="${promotion.discountValue}"
                        required
                        >

                </div>

                <div class="form-group">

                    <label for="minOrderAmount">
                        Đơn hàng tối thiểu
                    </label>

                    <input
                        type="text"
                        id="minOrderAmount"
                        name="minOrderAmount"
                        inputmode="decimal"
                        value="${promotion.minOrderAmount}"
                        >

                </div>

                <div class="form-group">

                    <label for="maxDiscountAmount">
                        Giảm tối đa
                    </label>

                    <input
                        type="text"
                        id="maxDiscountAmount"
                        name="maxDiscountAmount"
                        inputmode="decimal"
                        value="${promotion.maxDiscountAmount}"
                        >

                </div>

                <div class="form-group">

                    <label for="usageLimit">
                        Giới hạn sử dụng
                    </label>

                    <input
                        type="text"
                        id="usageLimit"
                        name="usageLimit"
                        inputmode="numeric"
                        value="${promotion.usageLimit}"
                        >

                </div>

                <div class="form-group">

                    <label>
                        Ngày bắt đầu
                    </label>

                    <div
                        class="date-wrapper"
                        id="startDateWrapper"
                        >

                        <input
                            type="text"
                            id="startDateDisplay"
                            class="date-display"
                            value="${startDateDisplay}"
                            placeholder="DD/MM/YYYY"
                            readonly
                            >

                        <span class="calendar-icon">
                            📅
                        </span>

                        <input
                            type="date"
                            id="startDatePicker"
                            name="startDate"
                            class="native-date"
                            required
                            >

                    </div>

                    <div class="date-format">

                    </div>

                </div>

                <div class="form-group">

                    <label>
                        Ngày kết thúc
                    </label>

                    <div
                        class="date-wrapper"
                        id="endDateWrapper"
                        >

                        <input
                            type="text"
                            id="endDateDisplay"
                            class="date-display"
                            value="${endDateDisplay}"
                            placeholder="DD/MM/YYYY"
                            readonly
                            >

                        <span class="calendar-icon">
                            📅
                        </span>

                        <input
                            type="date"
                            id="endDatePicker"
                            name="endDate"
                            class="native-date"
                            required
                            >

                    </div>

                    <div class="date-format">

                    </div>

                </div>

                <div class="form-group">

                    <label for="status">
                        Trạng thái
                    </label>

                    <select
                        id="status"
                        name="status"
                        required
                        >

                        <c:choose>

                            <c:when test="${promotion.status == 'INACTIVE'}">

                                <option value="ACTIVE">
                                    ACTIVE
                                </option>

                                <option value="INACTIVE" selected>
                                    INACTIVE
                                </option>

                            </c:when>

                            <c:otherwise>

                                <option value="ACTIVE" selected>
                                    ACTIVE
                                </option>

                                <option value="INACTIVE">
                                    INACTIVE
                                </option>

                            </c:otherwise>

                        </c:choose>

                    </select>

                </div>

                <div class="form-group full-width">

                    <label for="description">
                        Mô tả
                    </label>

                    <textarea
                        id="description"
                        name="description"
                        maxlength="255"
                        >${promotion.description}</textarea>

                </div>

            </div>

            <div class="form-actions">

                <button
                    type="submit"
                    class="btn-promotion btn-save"
                    >
                    Lưu khuyến mãi
                </button>

                <a
                    href="${pageContext.request.contextPath}/admin/promotions"
                    class="btn-promotion btn-back"
                    >
                    Quay lại
                </a>

            </div>

        </form>

    </div>

</div>

<script>
    function syncDateDisplay(picker, display) {

        if (!picker.value) {
            display.value = "";
            return;
        }

        const parts = picker.value.split("-");

        if (parts.length !== 3) {
            display.value = "";
            return;
        }

        const year = parts[0];
        const month = parts[1];
        const day = parts[2];

        display.value =
                day + "/" +
                month + "/" +
                year;
    }

    function openDatePicker(picker) {

        if (typeof picker.showPicker === "function") {
            picker.showPicker();
        } else {
            picker.focus();
            picker.click();
        }
    }

    document.addEventListener(
            "DOMContentLoaded",
            function () {

                const startWrapper =
                        document.getElementById(
                                "startDateWrapper"
                                );

                const startPicker =
                        document.getElementById(
                                "startDatePicker"
                                );

                const startDisplay =
                        document.getElementById(
                                "startDateDisplay"
                                );

                const endWrapper =
                        document.getElementById(
                                "endDateWrapper"
                                );

                const endPicker =
                        document.getElementById(
                                "endDatePicker"
                                );

                const endDisplay =
                        document.getElementById(
                                "endDateDisplay"
                                );

                const startInitial =
                        "${startDateIso}";

                const endInitial =
                        "${endDateIso}";

                if (startInitial) {

                    startPicker.value =
                            startInitial;

                    syncDateDisplay(
                            startPicker,
                            startDisplay
                            );
                }

                if (endInitial) {

                    endPicker.value =
                            endInitial;

                    syncDateDisplay(
                            endPicker,
                            endDisplay
                            );
                }

                startWrapper.addEventListener(
                        "click",
                        function () {

                            openDatePicker(
                                    startPicker
                                    );
                        }
                );

                endWrapper.addEventListener(
                        "click",
                        function () {

                            openDatePicker(
                                    endPicker
                                    );
                        }
                );

                startPicker.addEventListener(
                        "change",
                        function () {

                            syncDateDisplay(
                                    startPicker,
                                    startDisplay
                                    );
                        }
                );

                endPicker.addEventListener(
                        "change",
                        function () {

                            syncDateDisplay(
                                    endPicker,
                                    endDisplay
                                    );
                        }
                );

            }
    );
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>