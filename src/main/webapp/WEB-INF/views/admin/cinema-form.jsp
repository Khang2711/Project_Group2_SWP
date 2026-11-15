<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="form-page" data-css-page="admin-cinema-form">

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