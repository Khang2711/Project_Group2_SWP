<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="manage-page" data-css-page="admin-combos">
    <div class="manage-heading">
        <div>
            <h1>Quản lý combo</h1>
            <p>Tạo combo, đặt giá bán và cấu hình các món cùng số lượng trong mỗi combo.</p>
        </div>
        <a class="primary-link" href="${pageContext.request.contextPath}/admin/combo-form">+ Thêm combo</a>
    </div>

    <c:if test="${not empty error}"><div class="notice error"><c:out value="${error}" /></div></c:if>
    <c:if test="${param.saved eq '1'}"><div class="notice success">Đã lưu combo.</div></c:if>
    <c:if test="${param.statusUpdated eq '1'}"><div class="notice success">Đã cập nhật trạng thái combo.</div></c:if>

    <div class="table-wrap">
        <table class="manage-table">
            <thead><tr><th>Mã</th><th>Tên combo</th><th>Mô tả</th><th>Các món trong combo</th><th>Giá bán</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty combos}">
                        <c:forEach var="combo" items="${combos}">
                            <tr>
                                <td><c:out value="${combo.comboId}" /></td>
                                <td><strong><c:out value="${combo.comboName}" /></strong></td>
                                <td><c:out value="${combo.description}" /></td>
                                <td class="item-list">
                                    <c:choose>
                                        <c:when test="${not empty comboItemsByComboId[combo.comboId]}">
                                            <c:forEach var="comboItem" items="${comboItemsByComboId[combo.comboId]}">
                                                <div><c:out value="${foodItemNames[comboItem.foodItemId]}" /> × <c:out value="${comboItem.quantity}" /></div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise><span class="muted">Chưa cấu hình món</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><strong><fmt:formatNumber value="${combo.comboPrice}" pattern="#,##0.##" /> VNĐ</strong></td>
                                <td><c:choose><c:when test="${combo.status eq 'ACTIVE'}"><span class="pill pill-active">Đang bán</span></c:when><c:otherwise><span class="pill pill-inactive">Ngừng bán</span></c:otherwise></c:choose></td>
                                <td>
                                    <div class="actions">
                                        <a class="action-link" href="${pageContext.request.contextPath}/admin/combo-form?id=${combo.comboId}">Sửa</a>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/combo-status">
                                            <input type="hidden" name="id" value="${combo.comboId}">
                                            <c:choose>
                                                <c:when test="${combo.status eq 'ACTIVE'}">
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
                    <c:otherwise><tr><td colspan="7" class="empty">Chưa có combo. Hãy tạo combo đầu tiên.</td></tr></c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
