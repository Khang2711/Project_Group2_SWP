<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    * {
        box-sizing: border-box;
    }

    .promo-page {
        max-width: 1100px;
        margin: 0 auto;
        padding: 45px 30px 70px;
    }

    .promo-title {
        margin: 0;
        color: #ffffff;
        font-size: 44px;
        font-weight: 700;
    }

    .promo-line {
        width: 85px;
        height: 5px;
        margin: 18px 0;
        background: #ff3045;
        border-radius: 5px;
    }

    .promo-description {
        color: #a9a9b0;
        font-size: 17px;
        margin-bottom: 32px;
    }

    .promo-card {
        background: #171719;
        border: 1px solid #303036;
        border-radius: 10px;
        padding: 30px;
    }

    .promo-form {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 24px;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .form-group label {
        color: #ffffff;
        font-size: 16px;
        font-weight: 700;
    }

    .form-group input {
        width: 100%;
        height: 52px;
        padding: 0 16px;
        color: #ffffff;
        background: #101012;
        border: 1px solid #3b3b43;
        border-radius: 7px;
        font-size: 16px;
        outline: none;
    }

    .form-group input:focus {
        border-color: #ff3045;
    }

    .promo-action {
        grid-column: 1 / -1;
        display: flex;
        justify-content: flex-end;
    }

    .apply-button {
        min-width: 170px;
        height: 50px;
        padding: 0 24px;
        color: #ffffff;
        background: #ff3045;
        border: 1px solid #ff3045;
        border-radius: 7px;
        font-size: 16px;
        font-weight: 700;
        cursor: pointer;
    }

    .apply-button:hover {
        background: #e4273b;
    }

    .message {
        margin-bottom: 24px;
        padding: 16px 18px;
        border-radius: 7px;
        font-size: 15px;
    }

    .message-error {
        color: #ffffff;
        background: #41191e;
        border: 1px solid #ff3045;
    }

    .message-success {
        color: #d7ffe4;
        background: #12321f;
        border: 1px solid #27ae60;
    }

    .result-card {
        margin-top: 30px;
        background: #171719;
        border: 1px solid #303036;
        border-radius: 10px;
        overflow: hidden;
    }

    .result-header {
        padding: 22px 28px;
        border-bottom: 1px solid #303036;
    }

    .result-header h2 {
        margin: 0;
        color: #ffffff;
        font-size: 24px;
    }

    .result-body {
        padding: 28px;
    }

    .result-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 30px;
        padding: 14px 0;
        border-bottom: 1px solid #2a2a30;
    }

    .result-row:last-child {
        border-bottom: none;
    }

    .result-label {
        color: #a9a9b0;
        font-size: 16px;
    }

    .result-value {
        color: #ffffff;
        font-size: 17px;
        font-weight: 700;
        text-align: right;
    }

    .discount-value {
        color: #49d17d;
    }

    .final-value {
        color: #ff4054;
        font-size: 24px;
    }

    .promo-code {
        color: #ffd166;
    }

    .status-active {
        color: #49d17d;
    }

    .promo-note {
        margin-top: 24px;
        padding: 16px;
        background: #101012;
        border-radius: 7px;
        color: #b7b7bd;
        line-height: 1.6;
    }

    @media (max-width: 750px) {
        .promo-form {
            grid-template-columns: 1fr;
        }

        .promo-action {
            grid-column: auto;
        }

        .apply-button {
            width: 100%;
        }

        .result-row {
            align-items: flex-start;
            flex-direction: column;
            gap: 6px;
        }

        .result-value {
            text-align: left;
        }

        .promo-title {
            font-size: 34px;
        }
    }
</style>

<div class="promo-page">

    <h1 class="promo-title">
        Áp dụng mã khuyến mãi
    </h1>

    <div class="promo-line"></div>

    <div class="promo-description">
        Nhập mã khuyến mãi để kiểm tra và tính lại tổng tiền đơn hàng.
    </div>

    <c:if test="${not empty error}">
        <div class="message message-error">
            ${error}
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message message-success">
            ${success}
        </div>
    </c:if>

    <div class="promo-card">

        <form
            action="${pageContext.request.contextPath}/user/apply-promo"
            method="post"
            class="promo-form"
        >

           <div class="form-group">

    <label for="orderAmount">
        Tổng tiền trước giảm
    </label>

    <input
        type="text"
        id="orderAmount"
        name="orderAmount"
        inputmode="numeric"
        value="${orderAmountInput}"
        placeholder="Ví dụ: 300000"
        required
    >

</div>

            <div class="form-group">

                <label for="promoCode">
                    Mã khuyến mãi
                </label>

                <input
                    type="text"
                    id="promoCode"
                    name="promoCode"
                    maxlength="50"
                    value="${promoCode}"
                    placeholder="Ví dụ: WELCOME10"
                    required
                >

            </div>

            <div class="promo-action">

                <button
                    type="submit"
                    class="apply-button"
                >
                    Áp dụng
                </button>

            </div>

        </form>

    </div>

    <c:if test="${not empty success}">

        <div class="result-card">

            <div class="result-header">
                <h2>Kết quả áp dụng</h2>
            </div>

            <div class="result-body">

                <div class="result-row">

                    <span class="result-label">
                        Mã khuyến mãi
                    </span>

                    <span class="result-value promo-code">
                        ${promotion.promoCode}
                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Loại giảm giá
                    </span>

                    <span class="result-value">
                        ${promotion.discountType}
                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Giá trị giảm
                    </span>

                    <span class="result-value">

                        <c:choose>

                            <c:when test="${promotion.discountType == 'PERCENT'}">

                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    maxFractionDigits="2"
                                />%

                            </c:when>

                            <c:otherwise>

                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    type="number"
                                    maxFractionDigits="0"
                                />
                                VNĐ

                            </c:otherwise>

                        </c:choose>

                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Đơn hàng tối thiểu
                    </span>

                    <span class="result-value">

                        <fmt:formatNumber
                            value="${promotion.minOrderAmount}"
                            type="number"
                            maxFractionDigits="0"
                        />
                        VNĐ

                    </span>

                </div>

                <c:if test="${not empty promotion.maxDiscountAmount}">

                    <div class="result-row">

                        <span class="result-label">
                            Giảm tối đa
                        </span>

                        <span class="result-value">

                            <fmt:formatNumber
                                value="${promotion.maxDiscountAmount}"
                                type="number"
                                maxFractionDigits="0"
                            />
                            VNĐ

                        </span>

                    </div>

                </c:if>

                <div class="result-row">

                    <span class="result-label">
                        Ngày bắt đầu
                    </span>

                    <span class="result-value">

                        <fmt:formatDate
                            value="${promotion.startDate}"
                            pattern="dd/MM/yyyy"
                        />

                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Ngày kết thúc
                    </span>

                    <span class="result-value">

                        <fmt:formatDate
                            value="${promotion.endDate}"
                            pattern="dd/MM/yyyy"
                        />

                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Trạng thái
                    </span>

                    <span class="result-value status-active">
                        ${promotion.status}
                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Tổng tiền ban đầu
                    </span>

                    <span class="result-value">

                        <fmt:formatNumber
                            value="${orderAmount}"
                            type="number"
                            maxFractionDigits="0"
                        />
                        VNĐ

                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Số tiền được giảm
                    </span>

                    <span class="result-value discount-value">

                        -
                        <fmt:formatNumber
                            value="${discountAmount}"
                            type="number"
                            maxFractionDigits="0"
                        />
                        VNĐ

                    </span>

                </div>

                <div class="result-row">

                    <span class="result-label">
                        Thành tiền
                    </span>

                    <span class="result-value final-value">

                        <fmt:formatNumber
                            value="${finalAmount}"
                            type="number"
                            maxFractionDigits="0"
                        />
                        VNĐ

                    </span>

                </div>

                <c:if test="${not empty promotion.description}">

                    <div class="promo-note">
                        ${promotion.description}
                    </div>

                </c:if>

            </div>

        </div>

    </c:if>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>