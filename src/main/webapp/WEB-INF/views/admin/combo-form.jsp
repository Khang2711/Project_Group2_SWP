<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .form-page { max-width:950px; margin:0 auto; padding:28px 10px 48px; }
    .form-card { padding:25px; background:#18181c; border:1px solid #33333a; border-radius:9px; }
    .form-card p { color:#aaa; }
    .form-grid { display:grid; grid-template-columns:1fr 1fr; gap:18px; }
    .field { display:flex; flex-direction:column; gap:7px; }
    .field.full { grid-column:1 / -1; }
    .field label { font-weight:600; }
    .field input, .field select, .field textarea { width:100%; box-sizing:border-box; padding:11px 12px; color:#f5f5f5; background:#101012; border:1px solid #494951; border-radius:5px; }
    .field textarea { min-height:85px; resize:vertical; }
    .image-preview { display:flex; align-items:flex-start; gap:14px; flex-wrap:wrap; margin-top:10px; }
    .image-preview img { display:block; max-width:240px; max-height:160px; object-fit:contain; background:#101012; border:1px solid #494951; border-radius:6px; padding:5px; }
    .image-help { color:#aaa; font-size:13px; margin:0; }
    .items-table { width:100%; border-collapse:collapse; margin-top:15px; }
    .items-table th, .items-table td { padding:11px; border:1px solid #3b3b44; text-align:left; }
    .items-table th { background:#222228; }
    .items-table input[type=number] { width:85px; padding:8px; color:white; background:#101012; border:1px solid #494951; border-radius:4px; }
    .form-actions { display:flex; gap:10px; margin-top:22px; flex-wrap:wrap; }
    .primary-button, .secondary-link { padding:10px 15px; border-radius:5px; border:0; text-decoration:none; font-weight:700; cursor:pointer; }
    .primary-button { background:#e50914; color:white; }
    .secondary-link { background:#303038; color:white; border:1px solid #494951; }
    .notice { padding:12px 14px; margin:15px 0; border-radius:6px; background:#40191d; border:1px solid #e50914; color:#ffd5d8; }
    .inactive-row { color:#888; }
    .muted { color:#aaa; }
    @media(max-width:650px) { .form-grid { grid-template-columns:1fr; } .field.full { grid-column:auto; } .form-card { padding:16px; } }
</style>

<div class="form-page">
    <h1><c:choose><c:when test="${combo.comboId gt 0}">Cập nhật combo</c:when><c:otherwise>Thêm combo</c:otherwise></c:choose></h1>
    <p>Chọn các món đang kinh doanh và nhập số lượng từng món trong combo.</p>
    <c:if test="${not empty error}"><div class="notice"><c:out value="${error}" /></div></c:if>

    <div class="form-card">
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/combo-form">
            <input type="hidden" name="id" value="${combo.comboId}">
            <div class="form-grid">
                <div class="field full">
                    <label for="comboName">Tên combo *</label>
                    <input id="comboName" name="comboName" maxlength="100" required value="${combo.comboName}">
                </div>
                <div class="field">
                    <label for="comboPrice">Giá bán combo (VNĐ) *</label>
                    <input id="comboPrice" name="comboPrice" type="number" min="0" step="0.01" required value="${combo.comboPrice}">
                </div>
                <div class="field">
                    <label for="status">Trạng thái</label>
                    <select id="status" name="status">
                        <option value="ACTIVE" ${combo.status eq 'ACTIVE' ? 'selected' : ''}>Đang bán</option>
                        <option value="INACTIVE" ${combo.status eq 'INACTIVE' ? 'selected' : ''}>Ngừng bán</option>
                    </select>
                </div>
                <div class="field full">
                    <label for="description">Mô tả</label>
                    <textarea id="description" name="description" maxlength="255"><c:out value="${combo.description}" /></textarea>
                </div>
                <div class="field full">
                    <label for="comboImage">Ảnh combo (JPG, PNG hoặc GIF; tối đa 5 MB)</label>
                    <input id="comboImage" name="comboImage" type="file"
                           accept=".jpg,.jpeg,.png,.gif,image/jpeg,image/png,image/gif">
                    <p class="image-help">Chọn ảnh từ máy tính. Nếu đang sửa combo, để trống sẽ giữ ảnh hiện tại.</p>
                    <c:if test="${not empty combo.imageUrl}">
                        <c:set var="currentImagePath" value="${fn:trim(combo.imageUrl)}" />
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
                                 alt="Ảnh combo hiện tại"
                                 onerror="this.style.display='none';">
                        </div>
                    </c:if>
                </div>
            </div>

            <h2 style="margin-top:28px;">Các món trong combo</h2>
            <p class="muted">Đánh dấu món cần thêm vào combo và nhập số lượng. Món đã ngừng bán không thể chọn.</p>
            <table class="items-table">
                <thead><tr><th style="width:80px;">Chọn</th><th>Tên món</th><th>Loại</th><th>Giá lẻ</th><th>Số lượng</th><th>Trạng thái</th></tr></thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty foodItems}">
                            <c:forEach var="item" items="${foodItems}">
                                <tr class="${item.status ne 'ACTIVE' ? 'inactive-row' : ''}">
                                    <td><input type="checkbox" name="foodItemId" value="${item.foodItemId}" ${selectedItems[item.foodItemId] ne null ? 'checked' : ''} ${item.status ne 'ACTIVE' ? 'disabled' : ''}></td>
                                    <td><c:out value="${item.itemName}" /></td>
                                    <td><c:choose><c:when test="${item.itemType eq 'DRINK'}">Thức uống</c:when><c:otherwise>Món ăn</c:otherwise></c:choose></td>
                                    <td><c:out value="${item.price}" /> VNĐ</td>
                                    <td><input type="number" name="quantity_${item.foodItemId}" min="1" value="${selectedItems[item.foodItemId] ne null ? selectedItems[item.foodItemId] : 1}" ${item.status ne 'ACTIVE' ? 'disabled' : ''}></td>
                                    <td><c:choose><c:when test="${item.status eq 'ACTIVE'}">Đang bán</c:when><c:otherwise>Ngừng bán</c:otherwise></c:choose></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise><tr><td colspan="6" class="muted">Chưa có món ăn/thức uống. Hãy tạo sản phẩm trước khi tạo combo.</td></tr></c:otherwise>
                    </c:choose>
                </tbody>
            </table>

            <div class="form-actions">
                <button class="primary-button" type="submit">Lưu combo</button>
                <a class="secondary-link" href="${pageContext.request.contextPath}/admin/combos">Quay lại danh sách</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
