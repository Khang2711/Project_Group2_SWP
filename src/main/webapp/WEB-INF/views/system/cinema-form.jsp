<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<div class="genre-form-page">

    <div class="genre-form-header">
        <c:choose>
            <c:when test="${isEdit}">
                <h1>Chỉnh sửa chi nhánh</h1>
                <p>Cập nhật thông tin chi nhánh trong hệ thống.</p>
            </c:when>
            <c:otherwise>
                <h1>Thêm chi nhánh mới</h1>
                <p>Nhập thông tin rạp chiếu phim mới.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty error}">
        <div class="genres-error-message">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="genre-form-card">
        <form method="post"
              action="${pageContext.request.contextPath}/system/cinema-form">

            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${cinema.cinemaId}">
            </c:if>

            <div class="genre-form-group">
                <label for="cinemaName">
                    Tên chi nhánh <span class="required">*</span>
                </label>

                <input type="text"
                       id="cinemaName"
                       name="cinemaName"
                       value="<c:out value='${cinema.cinemaName}' />"
                       maxlength="100"
                       placeholder="Ví dụ: F-Cinema Cần Thơ"
                       autocomplete="off"
                       required>
            </div>

            <div class="genre-form-group">
                <label for="address">
                    Địa chỉ <span class="required">*</span>
                </label>

                <input type="text"
                       id="address"
                       name="address"
                       value="<c:out value='${cinema.address}' />"
                       maxlength="255"
                       placeholder="Nhập địa chỉ chi nhánh"
                       required>
            </div>

            <div class="genre-form-group">
                <label for="phone">Số điện thoại</label>

                <input type="text"
                       id="phone"
                       name="phone"
                       value="<c:out value='${cinema.phone}' />"
                       maxlength="20"
                       placeholder="Nhập số điện thoại liên hệ">
            </div>

            <div class="genre-form-group">
                <label for="status">
                    Trạng thái <span class="required">*</span>
                </label>

                <select name="status" id="status" required>
                    <option value="ACTIVE"
                            ${cinema.status == 'ACTIVE' ? 'selected' : ''}>
                        ACTIVE
                    </option>

                    <option value="INACTIVE"
                            ${cinema.status == 'INACTIVE' ? 'selected' : ''}>
                        INACTIVE
                    </option>
                </select>
            </div>

            <div class="genre-form-actions">
                <a class="genre-form-btn-cancel"
                   href="${pageContext.request.contextPath}/system/cinemas">
                    Hủy
                </a>

                <button type="submit" class="genre-form-btn-save">
                    <c:choose>
                        <c:when test="${isEdit}">Lưu thay đổi</c:when>
                        <c:otherwise>Thêm chi nhánh</c:otherwise>
                    </c:choose>
                </button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
