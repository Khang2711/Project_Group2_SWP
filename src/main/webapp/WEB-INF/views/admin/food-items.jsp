<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .manage-page { max-width: 1250px; margin: 0 auto; padding: 28px 10px 48px; }
    .manage-heading { display:flex; justify-content:space-between; align-items:center; gap:16px; flex-wrap:wrap; }
    .manage-heading p, .muted { color:#aaa; }
    .primary-link, .action-link, .status-button { display:inline-block; border:0; border-radius:5px; padding:9px 13px; text-decoration:none; cursor:pointer; font-weight:600; }
    .primary-link { background:#e50914; color:#fff; }
    .action-link { background:#303038; color:#fff; border:1px solid #45454f; }
    .status-button { background:#303038; color:#fff; border:1px solid #555; }
    .table-wrap { overflow-x:auto; margin-top:22px; background:#18181c; border:1px solid #33333a; border-radius:8px; }
    .manage-table { width:100%; border-collapse:collapse; min-width:850px; }
    .manage-table th, .manage-table td { padding:13px 14px; border-bottom:1px solid #33333a; text-align:left; vertical-align:middle; }
    .manage-table th { background:#222228; color:#eee; }
    .manage-table tr:last-child td { border-bottom:0; }
    .pill { display:inline-block; padding:4px 9px; border-radius:20px; font-size:12px; font-weight:700; }
    .pill-active { color:#b8f5c2; background:#173b22; }
    .pill-inactive { color:#ffb8bd; background:#411b20; }
    .notice { padding:12px 14px; margin-top:15px; border-radius:6px; background:#25252c; border:1px solid #45454f; }
    .error { border-color:#e50914; color:#ffd5d8; }
    .success { border-color:#2f8f46; color:#c5f4ce; }
    .actions { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
    .empty { text-align:center; color:#aaa; padding:30px !important; }
</style>

<div class="manage-page">
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
                    <th>Mã</th><th>Tên sản phẩm</th><th>Loại</th><th>Mô tả</th><th>Giá bán</th><th>Trạng thái</th><th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty foodItems}">
                        <c:forEach var="item" items="${foodItems}">
                            <tr>
                                <td><c:out value="${item.foodItemId}" /></td>
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
                    <c:otherwise><tr><td colspan="7" class="empty">Chưa có sản phẩm. Hãy thêm món ăn hoặc thức uống đầu tiên.</td></tr></c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
