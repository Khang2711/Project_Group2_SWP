
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<div class="genre-form-page">

    <!-- Page header -->
    <div class="genre-form-header">

        <c:choose>
            <c:when test="${isEdit}">
                <h1>Chỉnh sửa thể loại</h1>
                <p>Cập nhật thông tin thể loại phim.</p>
            </c:when>

            <c:otherwise>
                <h1>Thêm thể loại</h1>
                <p>Thêm thể loại phim mới vào hệ thống.</p>
            </c:otherwise>
        </c:choose>

    </div>

    <!-- Error message -->
    <c:if test="${not empty error}">
        <div class="genres-error-message">
            <c:out value="${error}" />
        </div>
    </c:if>

    <!-- Genre form -->
    <div class="genre-form-card">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/genre-form">

            <!-- Genre ID when editing -->
            <c:if test="${isEdit}">
                <input type="hidden"
                       name="id"
                       value="${genre.genreId}">
            </c:if>

            <!-- Genre name -->
            <div class="genre-form-group">

                <label for="genreName">
                    Tên thể loại <span class="required">*</span>
                </label>

                <input type="text"
                       id="genreName"
                       name="genreName"
                       value="<c:out value='${genre.genreName}' />"
                       maxlength="100"
                       placeholder="Nhập tên thể loại"
                       autocomplete ="off"
                       required>

            </div>

            <!-- Description -->
            <div class="genre-form-group">

                <label for="description">
                    Mô tả
                </label>

                <textarea id="description"
                          name="description"
                          rows="5"
                          maxlength="255"
                          placeholder="Nhập mô tả thể loại"><c:out value="${genre.description}" /></textarea>

            </div>

            <!-- Status -->
            <div class="genre-form-group">

                <label for="status">
                    Trạng thái <span class="required">*</span>
                </label>

                <select name="status" id="status" required>

                    <option value=""
                            disabled
                            ${empty genre.status ? 'selected' : ''}>
                        -- Chọn trạng thái --
                    </option>

                    <option value="ACTIVE"
                            ${genre.status == 'ACTIVE' ? 'selected' : ''}>
                        ACTIVE
                    </option>

                    <option value="INACTIVE"
                            ${genre.status == 'INACTIVE' ? 'selected' : ''}>
                        INACTIVE
                    </option>

                </select>

            </div>

            <!-- Form actions -->
            <div class="genre-form-actions">

                <a class="genre-form-btn-cancel"
                   href="${pageContext.request.contextPath}/admin/genres">
                    Hủy
                </a>

                <button type="submit"
                        class="genre-form-btn-save">

                    <c:choose>
                        <c:when test="${isEdit}">
                            Lưu thay đổi
                        </c:when>

                        <c:otherwise>
                            Thêm thể loại
                        </c:otherwise>
                    </c:choose>

                </button>

            </div>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
