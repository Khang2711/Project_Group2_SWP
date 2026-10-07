<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .form-page {
        max-width: 760px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    .form-page h1 {
        margin: 0 0 8px;
        font-size: 32px;
    }

    .subtitle {
        color: #999;
        margin: 0 0 28px;
    }

    .form-card {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 12px;
        padding: 28px;
    }

    .form-group {
        margin-bottom: 22px;
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
        border: 1px solid #35353a;
        border-radius: 7px;
        background: #1c1c1f;
        color: #fff;
        outline: none;
    }

    .form-control:focus {
        border-color: #ff1744;
    }

    .error-box {
        background: #32181d;
        border: 1px solid #6b2935;
        color: #ff8095;
        padding: 12px 15px;
        border-radius: 7px;
        margin-bottom: 20px;
    }

    .btn-primary {
        border: none;
        background: #ff1744;
        color: white;
        padding: 11px 20px;
        border-radius: 7px;
        cursor: pointer;
        font-weight: 600;
    }

    .btn-secondary {
        color: #aaa;
        margin-left: 15px;
        text-decoration: none;
    }
</style>

<div class="form-page">

    <h1>Chỉnh sửa thông tin rạp</h1>
    <p class="subtitle">
        Cập nhật địa chỉ và thông tin liên hệ của rạp.
    </p>

    <div class="form-card">

        <c:if test="${not empty error}">
            <div class="error-box">
                <c:out value="${error}" />
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/cinema-form">

            <div class="form-group">
                <label>Tên rạp</label>

                <input class="form-control"
                       type="text"
                       value="<c:out value='${cinema.cinemaName}' />"
                       disabled>
            </div>

            <div class="form-group">
                <label>Địa chỉ</label>

                <input class="form-control"
                       type="text"
                       name="address"
                       maxlength="255"
                       required
                       value="<c:out value='${cinema.address}' />">
            </div>

            <div class="form-group">
                <label>Số điện thoại</label>

                <input class="form-control"
                       type="text"
                       name="phone"
                       maxlength="20"
                       value="<c:out value='${cinema.phone}' />">
            </div>

            <button class="btn-primary" type="submit">
                Lưu thay đổi
            </button>

            <a class="btn-secondary"
               href="${pageContext.request.contextPath}/admin/cinemas">
                Hủy
            </a>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>