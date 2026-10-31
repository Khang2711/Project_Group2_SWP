<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />

<style>
    .promo-page {
        max-width: 700px;
        margin: 0 auto;
        padding: 40px 30px 60px;
    }

    .page-title h1 {
        margin: 0;
        font-size: 30px;
        font-weight: bold;
    }

    .title-line {
        width: 50px;
        height: 4px;
        margin-top: 12px;
        margin-bottom: 18px;
        background: #ff3045;
        border-radius: 2px;
    }

    .promo-subtitle {
        margin: 0;
        color: #cccccc;
    }

    .promo-box {
        margin-top: 25px;
        padding: 25px;
        background: #18181c;
        border: 1px solid #33333a;
        border-radius: 8px;
    }

    .promo-field {
        margin-bottom: 18px;
    }

    .promo-field label {
        display: block;
        margin-bottom: 7px;
        font-weight: bold;
    }

    .promo-field input {
        width: 100%;
        box-sizing: border-box;
        padding: 10px;
        background: #101012;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
    }

    .promo-field input:focus {
        outline: none;
        border-color: #ff3045;
    }

    .apply-button {
        padding: 10px 18px;
        border: none;
        border-radius: 5px;
        background: #ff3045;
        color: white;
        font-weight: bold;
        cursor: pointer;
    }

    .apply-button:hover {
        background: #e6293d;
    }

    .message-error {
        margin-top: 20px;
        padding: 12px;
        border: 1px solid #ff3045;
        background: #40191d;
        border-radius: 5px;
    }

    .message-success {
        margin-top: 20px;
        padding: 12px;
        border: 1px solid #4caf50;
        background: #16351a;
        border-radius: 5px;
    }

    .result-table {
        width: 100%;
        margin-top: 15px;
        border-collapse: collapse;
    }

    .result-table td {
        padding: 11px;
        border-bottom: 1px solid #33333a;
    }

    .result-table td:last-child {
        text-align: right;
        font-weight: bold;
    }

    .final-amount {
        color: #ff3045;
        font-size: 18px;
    }
</style>

<div class="promo-page">

    <div class="page-title">
        <h1>Áp dụng mã giảm giá</h1>
        <div class="title-line"></div>
    </div>

    <p class="promo-subtitle">
        Nhập mã khuyến mãi để kiểm tra số tiền được giảm.
    </p>

    <c:if test="${not empty error}">
        <div class="message-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="message-success">
            <c:out value="${success}" />
        </div>
    </c:if>

    <div class="promo-box">

        <form action="${ctx}/user/apply-promo"
              method="post">

            <div class="promo-field">

                <label for="promoCode">
                    Mã khuyến mãi
                </label>

                <input type="text"
                       id="promoCode"
                       name="promoCode"
                       maxlength="50"
                       value="${fn:escapeXml(promoCode)}"
                       placeholder="VD: SALE20"
                       required />

            </div>
            <input type="hidden"
                   name="showtimeId"
                   value="${showtimeId}">
            <div class="promo-field">

                <label for="orderAmount">
                    Giá trị đơn hàng
                </label>

                <input type="text"
                       id="orderAmount"
                       name="orderAmount"
                       inputmode="numeric"
                       pattern="[0-9]+"
                       value="${orderAmount}"
                       placeholder="VD: 200,000VNĐ"
                       required />

            </div>

            <button type="submit"
                    class="apply-button">
                Áp dụng mã
            </button>

        </form>

    </div>

    <c:if test="${not empty appliedPromotion}">

        <div class="promo-box">

            <h2>Kết quả áp dụng</h2>

            <table class="result-table">

                <tr>
                    <td>Mã khuyến mãi</td>

                    <td>
                        <c:out value="${appliedPromotion.promoCode}" />
                    </td>
                </tr>

                <tr>
                    <td>Ưu đãi</td>

                    <td>

                        <c:choose>

                            <c:when test="${appliedPromotion.discountType == 'PERCENT'}">

                                <fmt:formatNumber
                                    value="${appliedPromotion.discountValue}"
                                    pattern="#,##0.##" />%

                            </c:when>

                            <c:otherwise>

                                <fmt:formatNumber
                                    value="${appliedPromotion.discountValue}"
                                    pattern="#,###" />
                                VNĐ

                            </c:otherwise>

                        </c:choose>

                    </td>
                </tr>

                <c:if test="${not empty appliedPromotion.description}">

                    <tr>
                        <td>Mô tả</td>

                        <td>
                            <c:out value="${appliedPromotion.description}" />
                        </td>
                    </tr>

                </c:if>

                <tr>
                    <td>Giá trị đơn hàng</td>

                    <td>
                        <fmt:formatNumber
                            value="${orderAmount}"
                            pattern="#,###" />
                        VNĐ
                    </td>
                </tr>

                <tr>
                    <td>Số tiền giảm</td>

                    <td>
                        -
                        <fmt:formatNumber
                            value="${discountAmount}"
                            pattern="#,###" />
                        VNĐ
                    </td>
                </tr>

                <tr>
                    <td>Thành tiền</td>

                    <td class="final-amount">

                        <fmt:formatNumber
                            value="${finalAmount}"
                            pattern="#,###" />
                        VNĐ

                    </td>
                </tr>

            </table>

        </div>

    </c:if>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>