<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="startTimeValue" value="" />
<c:set var="endTimeValue" value="" />

<c:if test="${not empty pricing.startTime}">
    <fmt:formatDate var="startTimeValue"
                    value="${pricing.startTime}"
                    pattern="HH:mm" />
</c:if>

<c:if test="${not empty pricing.endTime}">
    <fmt:formatDate var="endTimeValue"
                    value="${pricing.endTime}"
                    pattern="HH:mm" />
</c:if>

<style>

    .pricing-form-page {
        max-width: 950px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .pricing-form-page h1 {
        margin: 0 0 8px;
        font-size: 32px;
    }

    .subtitle {
        color: #999;
        margin: 0 0 28px;
        line-height: 1.5;
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

    .section-title {
        margin: 5px 0 18px;
        padding-bottom: 9px;
        border-bottom: 1px solid #29292d;
        color: white;
        font-size: 17px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0 20px;
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

    .example-box {
        padding: 15px 17px;
        margin-bottom: 25px;
        border-radius: 8px;
        border: 1px solid #34343a;
        background: #1a1a1d;
        color: #aaa;
        line-height: 1.7;
        font-size: 13px;
    }

    .example-box strong {
        color: white;
    }

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

    @media (max-width: 700px) {

        .form-grid {
            grid-template-columns: 1fr;
        }

        .form-group.full {
            grid-column: auto;
        }
    }

</style>

<div class="pricing-form-page">

    <c:set var="isEdit"
           value="${pricing.pricingId > 0}" />

    <c:choose>

        <c:when test="${isEdit}">

            <h1>
                Cập nhật quy tắc giá vé
            </h1>

        </c:when>

        <c:otherwise>

            <h1>
                Thêm quy tắc giá vé
            </h1>

        </c:otherwise>

    </c:choose>

    <p class="subtitle">
        Giá có thể được áp dụng theo phim,
        loại phòng, loại ghế, ngày trong tuần,
        khung giờ hoặc một ngày đặc biệt.
    </p>

    <div class="example-box">

        Ví dụ:

        <strong>
            Tất cả phim + IMAX + VIP +
            WEEKEND + 17:00–23:59 = 150.000đ
        </strong>

        <br>

        Hoặc:

        <strong>
            Avatar + IMAX + VIP +
            24/12 = 180.000đ
        </strong>

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

            <div class="section-title">
                Điều kiện áp dụng
            </div>

            <div class="form-grid">

                <!-- MOVIE -->

                <div class="form-group">

                    <label>
                        Phim
                    </label>

                    <select class="form-control"
                            name="movieId">

                        <option value="">
                            Tất cả phim
                        </option>

                        <c:forEach var="movie"
                                   items="${movies}">

                            <option value="${movie.movieId}"
                                    ${pricing.movieId == movie.movieId
                                      ? 'selected' : ''}>

                                <c:out value="${movie.title}" />

                            </option>

                        </c:forEach>

                    </select>

                    <div class="help-text">

                        Chọn Tất cả phim để tạo
                        giá mặc định.

                    </div>

                </div>

                <!-- ROOM TYPE -->

                <div class="form-group">

                    <label>
                        Loại phòng
                    </label>

                    <select class="form-control"
                            name="roomType">

                        <option value=""
                                ${empty pricing.roomType
                                  ? 'selected' : ''}>

                            Tất cả loại phòng

                        </option>

                        <option value="2D"
                                ${pricing.roomType == '2D'
                                  ? 'selected' : ''}>

                            2D

                        </option>

                        <option value="3D"
                                ${pricing.roomType == '3D'
                                  ? 'selected' : ''}>

                            3D

                        </option>

                        <option value="IMAX"
                                ${pricing.roomType == 'IMAX'
                                  ? 'selected' : ''}>

                            IMAX

                        </option>

                    </select>

                </div>

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

                <!-- DAY TYPE -->

                <div class="form-group">

                    <label>
                        Loại ngày
                    </label>

                    <select class="form-control"
                            name="dayType">

                        <option value="ALL"
                                ${empty pricing.dayType ||
                                  pricing.dayType == 'ALL'
                                  ? 'selected' : ''}>

                            Tất cả các ngày

                        </option>

                        <option value="WEEKDAY"
                                ${pricing.dayType == 'WEEKDAY'
                                  ? 'selected' : ''}>

                            Thứ 2 - Thứ 6

                        </option>

                        <option value="WEEKEND"
                                ${pricing.dayType == 'WEEKEND'
                                  ? 'selected' : ''}>

                            Thứ 7 - Chủ nhật

                        </option>

                    </select>

                </div>

                <!-- SPECIAL DATE -->

                <div class="form-group full">

                    <label>
                        Ngày đặc biệt / ngày lễ
                    </label>

                    <input class="form-control"
                           type="date"
                           name="specialDate"
                           value="${pricing.specialDate}">

                    <div class="help-text">

                        Để trống nếu đây là giá thông thường.
                        Nếu nhập ngày đặc biệt thì hệ thống
                        ưu tiên rule này hơn WEEKDAY/WEEKEND.

                    </div>

                </div>

                <!-- START TIME -->

                <div class="form-group">

                    <label>
                        Giờ bắt đầu
                    </label>

                    <input class="form-control"
                           type="time"
                           name="startTime"
                           value="${startTimeValue}">

                </div>

                <!-- END TIME -->

                <div class="form-group">

                    <label>
                        Giờ kết thúc
                    </label>

                    <input class="form-control"
                           type="time"
                           name="endTime"
                           value="${endTimeValue}">

                    <div class="help-text">

                        Để trống cả hai nếu áp dụng
                        cho toàn bộ ngày.

                    </div>

                </div>

            </div>

            <div class="section-title">
                Giá và thời gian hiệu lực
            </div>

            <div class="form-grid">

                <!-- PRICE -->

                <div class="form-group full">

                    <label>
                        Giá vé (VNĐ)
                    </label>

                    <input class="form-control"
                           type="number"
                           name="price"
                           min="1000"
                           step="1000"
                           required
                           placeholder="Ví dụ: 90000"
                           value="${pricing.price}">

                </div>

                <!-- START DATE -->

                <div class="form-group">

                    <label>
                        Hiệu lực từ ngày
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
                        Hiệu lực đến ngày
                    </label>

                    <input class="form-control"
                           type="date"
                           name="endDate"
                           value="${pricing.endDate}">

                    <div class="help-text">

                        Có thể để trống nếu
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
                            Thêm quy tắc giá
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