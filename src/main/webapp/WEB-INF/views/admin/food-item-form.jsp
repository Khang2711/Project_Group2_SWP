<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="form-page" data-css-page="admin-food-item-form">
    <h1><c:choose><c:when test="${foodItem.foodItemId gt 0}">Cập nhật sản phẩm</c:when><c:otherwise>Thêm sản phẩm</c:otherwise></c:choose></h1>
    <p>Thông tin sản phẩm chỉ áp dụng cho rạp đang được phân công.</p>
    <c:if test="${not empty error}"><div class="notice"><c:out value="${error}" /></div></c:if>

    <div class="form-card">
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/food-item-form">
            <input type="hidden" name="id" value="${foodItem.foodItemId}">
            <div class="form-grid">
                <div class="field full">
                    <label for="itemName">Tên sản phẩm *</label>
                    <input id="itemName" name="itemName" maxlength="100" required value="${foodItem.itemName}">
                </div>
                <div class="field">
                    <label for="itemType">Loại sản phẩm *</label>
                    <select id="itemType" name="itemType" required>
                        <option value="FOOD" ${foodItem.itemType eq 'FOOD' ? 'selected' : ''}>Món ăn</option>
                        <option value="DRINK" ${foodItem.itemType eq 'DRINK' ? 'selected' : ''}>Thức uống</option>
                    </select>
                </div>
                <div class="field">
                    <label for="price">Giá bán (VNĐ) *</label>
                    <input id="price" name="price" type="number" min="0" step="0.01" required value="${foodItem.price}">
                </div>
                <div class="field full">
                    <label for="description">Mô tả</label>
                    <textarea id="description" name="description" maxlength="255"><c:out value="${foodItem.description}" /></textarea>
                </div>
                <div class="field full">
                    <label for="foodImage">Ảnh sản phẩm (JPG, PNG hoặc GIF; tối đa 5 MB)</label>
                    <input id="foodImage" name="foodImage" type="file"
                           accept=".jpg,.jpeg,.png,.gif,image/jpeg,image/png,image/gif">
                    <p class="image-help">Chọn ảnh từ máy tính. Nếu đang sửa sản phẩm, để trống sẽ giữ ảnh hiện tại.</p>
                    <c:if test="${not empty foodItem.imageUrl}">
                        <c:set var="currentImagePath" value="${fn:trim(foodItem.imageUrl)}" />
                        <c:choose>
                            <c:when test="${fn:startsWith(currentImagePath, 'http://') or fn:startsWith(currentImagePath, 'https://') or fn:startsWith(currentImagePath, '/')}">
                                <c:url var="currentImageSrc" value="${currentImagePath}" />
                            </c:when>
                            <c:otherwise>
                                <c:url var="currentImageSrc" value="/${currentImagePath}" />
                            </c:otherwise>
                        </c:choose>
                        <div class="image-preview">
                            <img src="<c:out value='${currentImageSrc}' />"
                                 alt="Ảnh sản phẩm hiện tại"
                                 onerror="this.style.display='none';">
                        </div>
                    </c:if>
                </div>
                <div class="field">
                    <label for="status">Trạng thái</label>
                    <select id="status" name="status">
                        <option value="ACTIVE" ${foodItem.status eq 'ACTIVE' ? 'selected' : ''}>Đang bán</option>
                        <option value="INACTIVE" ${foodItem.status eq 'INACTIVE' ? 'selected' : ''}>Ngừng bán</option>
                    </select>
                </div>
            </div>
            <div class="form-actions">
                <button class="primary-button" type="submit">Lưu sản phẩm</button>
                <a class="secondary-link" href="${pageContext.request.contextPath}/admin/food-items">Quay lại danh sách</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
