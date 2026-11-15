<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="manage-page" data-css-page="admin-food-items">
    <div class="manage-heading">
        <div>
            <h1>Quản lý đồ ăn / thức uống</h1>
            <p>Quản lý sản phẩm và giá bán tại rạp của bạn.</p>
        </div>
        <a class="primary-link" href="${pageContext.request.contextPath}/admin/food-item-form">+ Thêm sản phẩm</a>
    </div>

    <c:if test="${not empty error}">
        <div class="notice error"><c:out value="${error}" /></div>
    </c:if>
    <c:if test="${param.saved eq '1'}">
        <div class="notice success">Đã lưu sản phẩm.</div>
    </c:if>
    <c:if test="${param.statusUpdated eq '1'}">
        <div class="notice success">Đã cập nhật trạng thái sản phẩm.</div>
    </c:if>

    <div class="table-wrap">
        <table class="manage-table">
            <thead>
                <tr>
                    <th>Mã</th><th>Ảnh</th><th>Tên sản phẩm</th><th>Loại</th><th>Mô tả</th><th>Giá bán</th><th>Trạng thái</th><th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty foodItems}">
                        <c:forEach var="item" items="${foodItems}">
                            <tr>
                                <td><c:out value="${item.foodItemId}" /></td>
                                <td class="food-thumb">
                                    <c:if test="${not empty item.imageUrl}">
                                        <c:set var="imagePath" value="${fn:trim(item.imageUrl)}" />
                                        <c:choose>
                                            <c:when test="${fn:startsWith(imagePath, 'http://') or fn:startsWith(imagePath, 'https://') or fn:startsWith(imagePath, '/')}">
                                                <c:url var="imageSrc" value="${imagePath}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:url var="imageSrc" value="/${imagePath}" />
                                            </c:otherwise>
                                        </c:choose>
                                        <img src="<c:out value='${imageSrc}' />"
                                             alt="<c:out value='${item.itemName}' />"
                                             loading="lazy"
                                             onerror="this.style.display='none';">
                                    </c:if>
                                </td>
                                <td><strong><c:out value="${item.itemName}" /></strong></td>
                                <td><c:choose><c:when test="${item.itemType eq 'DRINK'}">Thức uống</c:when><c:otherwise>Món ăn</c:otherwise></c:choose></td>
                                <td><c:out value="${item.description}" /></td>
                                <td><strong><fmt:formatNumber value="${item.price}" pattern="#,##0.##" /> VNĐ</strong></td>
                                <td><c:choose><c:when test="${item.status eq 'ACTIVE'}"><span class="pill pill-active">Đang bán</span></c:when><c:otherwise><span class="pill pill-inactive">Ngừng bán</span></c:otherwise></c:choose></td>
                                <td>
                                    <div class="actions">
                                        <a class="action-link" href="${pageContext.request.contextPath}/admin/food-item-form?id=${item.foodItemId}">Sửa</a>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/food-item-status">
                                            <input type="hidden" name="id" value="${item.foodItemId}">
                                            <c:choose>
                                                <c:when test="${item.status eq 'ACTIVE'}">
                                                    <input type="hidden" name="status" value="INACTIVE">
                                                    <button class="status-button" type="submit">Ngừng bán</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <input type="hidden" name="status" value="ACTIVE">
                                                    <button class="status-button" type="submit">Kích hoạt</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td colspan="8" class="empty">Chưa có sản phẩm. Hãy thêm món ăn hoặc thức uống đầu tiên.</td></tr></c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
