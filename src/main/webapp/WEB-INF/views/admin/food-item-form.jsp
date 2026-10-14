<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .form-page { max-width:780px; margin:0 auto; padding:28px 10px 48px; }
    .form-card { padding:25px; background:#18181c; border:1px solid #33333a; border-radius:9px; }
    .form-card p { color:#aaa; }
    .form-grid { display:grid; grid-template-columns:1fr 1fr; gap:18px; }
    .field { display:flex; flex-direction:column; gap:7px; }
    .field.full { grid-column:1 / -1; }
    .field label { font-weight:600; }
    .field input, .field select, .field textarea { width:100%; box-sizing:border-box; padding:11px 12px; color:#f5f5f5; background:#101012; border:1px solid #494951; border-radius:5px; }
    .field textarea { min-height:90px; resize:vertical; }
    .form-actions { display:flex; gap:10px; margin-top:22px; flex-wrap:wrap; }
    .primary-button, .secondary-link { padding:10px 15px; border-radius:5px; border:0; text-decoration:none; font-weight:700; cursor:pointer; }
    .primary-button { background:#e50914; color:white; }
    .secondary-link { background:#303038; color:white; border:1px solid #494951; }
    .notice { padding:12px 14px; margin:15px 0; border-radius:6px; background:#40191d; border:1px solid #e50914; color:#ffd5d8; }
    @media(max-width:650px) { .form-grid { grid-template-columns:1fr; } .field.full { grid-column:auto; } .form-card { padding:16px; } }
</style>

<div class="form-page">
    <h1><c:choose><c:when test="${foodItem.foodItemId gt 0}">Cập nhật sản phẩm</c:when><c:otherwise>Thêm sản phẩm</c:otherwise></c:choose></h1>
    <p>Thông tin sản phẩm chỉ áp dụng cho rạp đang được phân công.</p>
    <c:if test="${not empty error}"><div class="notice"><c:out value="${error}" /></div></c:if>

    <div class="form-card">
        <form method="post" action="${pageContext.request.contextPath}/admin/food-item-form">
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
                    <label for="imageUrl">Đường dẫn hình ảnh</label>
                    <input id="imageUrl" name="imageUrl" maxlength="500" value="${foodItem.imageUrl}">
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
