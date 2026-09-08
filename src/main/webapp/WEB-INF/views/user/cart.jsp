<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="model.CartItem"%>
<%@page import="model.ComboItem"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Giỏ hàng");
    String ctx = request.getContextPath();
    List<CartItem> cart = (List<CartItem>) request.getAttribute("cart");
    List<ComboItem> comboCart = (List<ComboItem>) request.getAttribute("comboCart");
    List<ComboItem> combos = (List<ComboItem>) request.getAttribute("combos");
    String error = (String) request.getAttribute("error");
    Float total = (Float) request.getAttribute("total");
    if (total == null) {
        total = 0f;
    }

    float ticketTotal = 0;
    if (cart != null) {
        for (CartItem item : cart) {
            ticketTotal += item.getPrice();
        }
    }

    float comboTotal = 0;
    Map<String, Integer> comboQuantityMap = new HashMap<>();
    if (comboCart != null) {
        for (ComboItem item : comboCart) {
            comboTotal += item.getTotal();
            comboQuantityMap.put(item.getComboID(), item.getQuantity());
        }
    }

    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="grid grid-2">
    <div class="panel">
        <div class="toolbar">
            <h1 class="page-title" style="font-size:26px;">Vé đã chọn</h1>
            <a class="btn btn-blue" href="<%= ctx%>/movies">Tiếp tục đặt vé</a>
        </div>

        <% if (error != null) { %>
        <div class="alert alert-error"><%= error%></div>
        <% } %>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Phim</th>
                        <th>Suất</th>
                        <th>Ghế</th>
                        <th>Giá</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (cart != null && !cart.isEmpty()) {
                            for (CartItem item : cart) {
                    %>
                    <tr>
                        <td><%= item.getMovieTitle()%></td>
                        <td><%= item.getShowDate()%> <%= item.getStartTime()%></td>
                        <td><strong><%= item.getSeatID()%></strong></td>
                        <td><%= df.format(item.getPrice())%> VNĐ</td>
                        <td>
                            <a class="btn btn-danger" href="<%= ctx%>/cart?action=remove&key=<%= item.getKey()%>">Xóa</a>
                        </td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr>
                        <td colspan="5" style="text-align:center;color:#64748b;">Chưa có vé trong giỏ.</td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h2 class="page-title" style="font-size:26px;">Combo bắp nước</h2>
        <p class="muted" style="margin-bottom:14px;">Mỗi combo đều ghi rõ số lượng bắp và nước nhận được.</p>

        <form action="<%= ctx%>/cart" method="post" class="grid">
            <input type="hidden" name="action" value="add-combo">
            <%
                if (combos != null && !combos.isEmpty()) {
                    for (ComboItem combo : combos) {
                        Integer quantity = comboQuantityMap.get(combo.getComboID());
                        if (quantity == null) {
                            quantity = 0;
                        }
            %>
            <div class="field combo-field clear-combo-field">
                <label><%= combo.getComboName()%></label>
                <span class="combo-price"><%= df.format(combo.getPrice())%> VNĐ</span>
                <input type="number" name="<%= combo.getComboID()%>" min="0" value="<%= quantity%>">
            </div>
            <%
                    }
                }
            %>
            <button class="btn btn-blue" type="submit">Cập nhật combo</button>
        </form>

        <hr style="border:0;border-top:1px solid #e2e8f0;margin:18px 0;">
        <h2 style="margin:0 0 10px;">Tổng tiền</h2>
        <p class="muted">Vé: <strong><%= df.format(ticketTotal)%> VNĐ</strong></p>
        <p class="muted">Combo: <strong><%= df.format(comboTotal)%> VNĐ</strong></p>
        <h2 style="color:#ea580c;"><%= df.format(total)%> VNĐ</h2>

        <div class="checkout-actions">
            <form action="<%= ctx%>/cart" method="post" style="display:inline;">
                <input type="hidden" name="action" value="clear">
                <button class="btn btn-soft" type="submit">Xóa giỏ hàng</button>
            </form>
            <form action="<%= ctx%>/checkout" method="post" style="display:inline;">
                <button class="btn btn-primary" type="submit">Thanh toán</button>
            </form>
        </div>
    </div>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
