<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<h1>Bán Food / Combo tại quầy</h1>
<p>Staff Counter · Food & Combo Sale</p>

<section>
    <h2>1. Chọn sản phẩm</h2>

    <h3 style="margin-top:20px;">Món ăn / Nước uống</h3>

    <c:choose>
        <c:when test="${not empty foodItems}">
            <div style="display:flex; gap:15px; flex-wrap:wrap; margin-top:15px;">

                <c:forEach var="item" items="${foodItems}">
                    <div style="width:230px; padding:16px; border:1px solid #3a3a42; border-radius:8px; background:#18181c;">

                        <strong>
                            <c:out value="${item.itemName}" />
                        </strong>

                        <p style="color:#aaaaaa; margin:8px 0;">
                            <c:out value="${item.itemType}" />
                        </p>

                        <p>
                            <fmt:formatNumber value="${item.price}" pattern="#,###" /> VNĐ
                        </p>

                        <button type="button" disabled style="margin-top:10px;">
                            Thêm
                        </button>
                    </div>
                </c:forEach>

            </div>
        </c:when>

        <c:otherwise>
            <p style="color:#aaaaaa;">
                Chưa có dữ liệu Food Item từ Controller.
            </p>
        </c:otherwise>
    </c:choose>

    <h3 style="margin-top:30px;">Combo</h3>

    <c:choose>
        <c:when test="${not empty combos}">
            <div style="display:flex; gap:15px; flex-wrap:wrap; margin-top:15px;">

                <c:forEach var="combo" items="${combos}">
                    <div style="width:230px; padding:16px; border:1px solid #3a3a42; border-radius:8px; background:#18181c;">

                        <strong>
                            <c:out value="${combo.comboName}" />
                        </strong>

                        <p style="color:#aaaaaa; margin:8px 0;">
                            <c:out value="${combo.description}" />
                        </p>

                        <p>
                            <fmt:formatNumber value="${combo.comboPrice}" pattern="#,###" /> VNĐ
                        </p>

                        <button type="button" disabled style="margin-top:10px;">
                            Thêm
                        </button>
                    </div>
                </c:forEach>

            </div>
        </c:when>

        <c:otherwise>
            <p style="color:#aaaaaa;">
                Chưa có dữ liệu Combo từ Controller.
            </p>
        </c:otherwise>
    </c:choose>
</section>

<section style="margin-top:35px;">
    <h2>2. Đơn hàng tại quầy</h2>

    <c:choose>
        <c:when test="${not empty orderItems}">
            <table border="1" cellpadding="10" cellspacing="0" style="width:100%; margin-top:15px;">
                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="orderItem" items="${orderItems}">
                        <tr>
                            <td><c:out value="${orderItem.name}" /></td>
                            <td><c:out value="${orderItem.quantity}" /></td>
                            <td>
                                <fmt:formatNumber value="${orderItem.unitPrice}" pattern="#,###" /> VNĐ
                            </td>
                            <td>
                                <fmt:formatNumber value="${orderItem.lineTotal}" pattern="#,###" /> VNĐ
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:otherwise>
            <p style="color:#aaaaaa;">
                Chưa có sản phẩm trong đơn hàng.
            </p>
        </c:otherwise>
    </c:choose>

    <p style="margin-top:20px;">
        Tổng tiền:
        <strong>
            <fmt:formatNumber value="${totalAmount}" pattern="#,###" /> VNĐ
        </strong>
    </p>

    <button type="button" disabled style="margin-top:15px;">
        Thanh toán
    </button>

    <p style="color:#aaaaaa; margin-top:10px;">
        Nút đang khóa vì FoodOrderService và Repository chưa được nối.
    </p>
</section>

<%-- TODO:
     Controller:
     - lấy Food_Item ACTIVE theo cinema của staff
     - lấy Combo ACTIVE theo cinema của staff

     Service:
     - thêm/xóa/thay đổi số lượng sản phẩm trong đơn tạm
     - tính subtotal/total
     - tạo Food_Order
     - tạo Food_Order_Item
     - kiểm tra sản phẩm và staff thuộc cùng cinema

     Không viết SQL trực tiếp trong JSP.
--%>

<%@ include file="/WEB-INF/include/footer.jsp" %>
