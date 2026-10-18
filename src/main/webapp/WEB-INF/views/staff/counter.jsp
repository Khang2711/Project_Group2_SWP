<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .counter-home {
        min-height: 55vh;
        padding-bottom: 30px;
    }

    .counter-home-heading {
        padding-bottom: 24px;
        margin-bottom: 30px;
        border-bottom: 1px solid #2d2d35;
    }

    .counter-home-label {
        margin-bottom: 12px;
        color: #ff1f2d;
        font-size: 12px;
        font-weight: bold;
        letter-spacing: 2px;
    }

    .counter-home-heading h1 {
        margin: 0;
        padding-left: 15px;
        border-left: 4px solid #ff1f2d;
        color: #ffffff;
        font-size: 30px;
        line-height: 1.3;
        text-transform: uppercase;
    }

    .counter-home-description {
        margin-top: 15px;
        color: #b8b8c2;
        font-size: 15px;
        line-height: 1.7;
    }

    .counter-home-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 22px;
    }

    .counter-home-card {
        display: flex;
        flex-direction: column;
        min-width: 0;
        min-height: 290px;
        padding: 26px;
        background: #18181c;
        border: 1px solid #33333d;
        border-radius: 8px;
    }

    .counter-home-card:hover {
        background: #1d1d23;
        border-color: #55555f;
    }

    .counter-home-tag {
        align-self: flex-start;
        padding: 5px 9px;
        margin-bottom: 22px;
        background: #2b171c;
        border: 1px solid #683039;
        border-radius: 3px;
        color: #ff6472;
        font-size: 11px;
        font-weight: bold;
        letter-spacing: 0.5px;
    }

    .counter-home-card h2 {
        margin: 0 0 14px;
        color: #ffffff;
        font-size: 23px;
        line-height: 1.3;
    }

    .counter-home-card p {
        margin: 0 0 26px;
        color: #b8b8c2;
        font-size: 15px;
        line-height: 1.7;
    }

    .counter-home-button {
        display: block;
        margin-top: auto;
        padding: 13px 18px;
        background: #e50914;
        border: 1px solid #e50914;
        border-radius: 4px;
        color: #ffffff;
        font-size: 15px;
        font-weight: bold;
        text-align: center;
        text-decoration: none;
    }

    .counter-home-button:hover {
        background: #ff1f2d;
        border-color: #ff1f2d;
        color: #ffffff;
    }

    .counter-home-button:focus-visible {
        outline: 2px solid #ffffff;
        outline-offset: 4px;
    }

    /* Màn hình vừa: các ô chuyển thành hai cột. */
    @media (max-width: 900px) {
        .counter-home-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
    }

    /* Điện thoại: mỗi chức năng nằm trên một hàng. */
    @media (max-width: 600px) {
        .counter-home-heading {
            margin-bottom: 22px;
        }

        .counter-home-heading h1 {
            font-size: 25px;
        }

        .counter-home-grid {
            grid-template-columns: 1fr;
            gap: 16px;
        }

        .counter-home-card {
            min-height: 250px;
            padding: 22px;
        }

        .counter-home-card h2 {
            font-size: 22px;
        }
    }
</style>

<section class="counter-home">

    <div class="counter-home-heading">
        <p class="counter-home-label">
            KHU VỰC NHÂN VIÊN
        </p>

        <h1>Bán hàng tại quầy</h1>

        <p class="counter-home-description">
            Chọn chức năng để bán vé, bán food/combo
            hoặc kiểm tra vé vào phòng chiếu.
        </p>
    </div>

    <div class="counter-home-grid">

        <article class="counter-home-card">
            <span class="counter-home-tag">
                VÉ XEM PHIM
            </span>

            <h2>Bán vé tại quầy</h2>

            <p>
                Chọn phim, ngày chiếu, suất chiếu và ghế
                cho khách hàng mua vé trực tiếp tại rạp.
            </p>

            <a href="${ctx}/staff/ticket-sale"
               class="counter-home-button">
                Bán vé
            </a>
        </article>

        <article class="counter-home-card">
            <span class="counter-home-tag">
                ĐỒ ĂN &amp; THỨC UỐNG
            </span>

            <h2>Bán food/combo</h2>

            <p>
                Chọn món ăn, nước uống và combo.
                Nhập số lượng và thanh toán đơn hàng tại quầy.
            </p>

            <a href="${ctx}/staff/food-sale"
               class="counter-home-button">
                Bán food/combo
            </a>
        </article>

        <article class="counter-home-card">
            <span class="counter-home-tag">
                KIỂM SOÁT VÉ
            </span>

            <h2>Check-in vé</h2>

            <p>
                Nhập mã vé hoặc quét QR để kiểm tra thông tin
                và xác nhận khách vào đúng suất chiếu.
            </p>

            <a href="${ctx}/staff/check-in"
               class="counter-home-button">
                Check-in vé
            </a>
        </article>

    </div>
</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>