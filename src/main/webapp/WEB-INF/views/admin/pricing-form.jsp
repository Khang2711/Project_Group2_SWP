<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .pricing-form-page {
        max-width: 800px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .pricing-form-page h1 {
        margin: 0 0 8px;
        font-size: 32px;
    }

    .subtitle {
        margin: 0 0 28px;
        color: #999;
    }

    .form-card {
        padding: 28px;
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
    }

    .error-message {
        padding: 13px 16px;
        margin-bottom: 20px;
        border: 1px solid #6b2935;
        background: #32181d;
        color: #ff8095;
        border-radius: 7px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
    }

    .form-group {
        margin-bottom: 21px;
    }

    .form-group.full {
        grid-column: 1 / -1;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        color: #ccc;
        font-weight: 600;
    }

    .form-control {
        width: 100%;
        box-sizing: border-box;
        padding: 12px 13px;
        background: #1c1c1f;
        color: white;
        border: 1px solid #35353a;
        border-radius: 7px;
        outline: none;
    }

    .form-control:focus {
        border-color: #ff1744;
    }

    .help-text {
        margin-top: 7px;
        color: #888;
        font-size: 12px;
        line-height: 1.5;
    }

    /* TYPE CARDS */

    .type-guide {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 10px;
        margin-bottom: 25px;
    }

    .type-card {
        padding: 13px;
        border: 1px solid #303034;
        background: #1b1b1e;
        border-radius: 8px;
        text-align: center;
    }

    .type-card strong {
        color: white;
    }

    /* BUTTON */

    .buttons {
        margin-top: 8px;
    }

    .btn-save {
        border: 0;
        background: #ff1744;
        color: white;
        padding: 11px 20px;
        border-radius: 7px;
        cursor: pointer;
        font-weight: 600;
    }

    .btn-save:hover {
        background: #e3133b;
    }

    .btn-cancel {
        margin-left: 14px;
        color: #aaa;
        text-decoration: none;
    }

    .btn-cancel:hover {
        color: white;
    }

    @media (max-width: 700px) {

        .form-grid {
            grid-template-columns: 1fr;
        }

        .form-group.full {
            grid-column: auto;
        }

        .type-guide {
            grid-template-columns: 1fr;
        }
    }

</style>

<div class="pricing-form-page">

    <c:set var="isEdit"
           value="${pricing.pricingId > 0}" />

    <c:choose>

        <c:when test="${isEdit}">

            <h1>
                Cập nhật giá vé
            </h1>

            <p class="subtitle">
                Điều chỉnh giá và thời gian
                áp dụng của loại ghế.
            </p>

        </c:when>

        <c:otherwise>

            <h1>
                Thêm giá vé
            </h1>

            <p class="subtitle">
                Tạo mức giá mới cho một
                loại ghế tại rạp.
            </p>

        </c:otherwise>

    </c:choose>

    <div class="type-guide">

        <div class="type-card">
            <strong>STANDARD</strong>
        </div>

        <div class="type-card">
            <strong>VIP</strong>
        </div>

        <div class="type-card">
            <strong>COUPLE</strong>
        </div>

    </div>

    <div class="form-card">

        <c:if test="${not empty error}">

            <div class="error-message">

                ⚠
                <c:out value="${error}" />

            </div>

        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/pricing-form">

            <c:if test="${isEdit}">

                <input type="hidden"
                       name="id"
                       value="${pricing.pricingId}">

            </c:if>

            <div class="form-grid">

                <!-- SEAT TYPE -->

                <div class="form-group">

                    <label>
                        Loại ghế
                    </label>

                    <select class="form-control"
                            name="seatType"
                            required>

                        <option value="STANDARD"
                                ${empty pricing.seatType ||
                                  pricing.seatType == 'STANDARD'
                                  ? 'selected' : ''}>

                            STANDARD

                        </option>

                        <option value="VIP"
                                ${pricing.seatType == 'VIP'
                                  ? 'selected' : ''}>

                            VIP

                        </option>

                        <option value="COUPLE"
                                ${pricing.seatType == 'COUPLE'
                                  ? 'selected' : ''}>

                            COUPLE

                        </option>

                    </select>

                </div>

                <!-- PRICE -->

                <div class="form-group">

                    <label>
                        Giá vé (VNĐ)
                    </label>

                    <input class="form-control"
                           type="number"
                           name="price"
                           min="0"
                           step="1000"
                           required
                           placeholder="Ví dụ: 80000"
                           value="${pricing.price}">

                </div>

                <!-- START DATE -->

                <div class="form-group">

                    <label>
                        Ngày bắt đầu
                    </label>

                    <input class="form-control"
                           type="date"
                           name="startDate"
                           required
                           value="${pricing.startDate}">

                </div>

                <!-- END DATE -->

                <div class="form-group">

                    <label>
                        Ngày kết thúc
                    </label>

                    <input class="form-control"
                           type="date"
                           name="endDate"
                           value="${pricing.endDate}">

                    <div class="help-text">

                        Có thể để trống nếu mức giá
                        không có ngày kết thúc.

                    </div>

                </div>

                <!-- STATUS -->

                <div class="form-group full">

                    <label>
                        Trạng thái
                    </label>

                    <select class="form-control"
                            name="status">

                        <option value="ACTIVE"
                                ${empty pricing.status ||
                                  pricing.status == 'ACTIVE'
                                  ? 'selected' : ''}>

                            ACTIVE

                        </option>

                        <option value="INACTIVE"
                                ${pricing.status == 'INACTIVE'
                                  ? 'selected' : ''}>

                            INACTIVE

                        </option>

                    </select>

                    <div class="help-text">

                        Hai mức giá ACTIVE của cùng loại ghế
                        không được có thời gian áp dụng chồng nhau.

                    </div>

                </div>

            </div>

            <div class="buttons">

                <button class="btn-save"
                        type="submit">

                    <c:choose>

                        <c:when test="${isEdit}">
                            Lưu thay đổi
                        </c:when>

                        <c:otherwise>
                            Thêm giá vé
                        </c:otherwise>

                    </c:choose>

                </button>

                <a class="btn-cancel"
                   href="${pageContext.request.contextPath}/admin/pricing">

                    Hủy

                </a>

            </div>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>