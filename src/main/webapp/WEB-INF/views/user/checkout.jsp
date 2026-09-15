<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.CartItem"%>
<%@page import="model.ComboItem"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Thanh toán");
    String ctx = request.getContextPath();
    List<CartItem> cart = (List<CartItem>) request.getAttribute("cart");
    List<ComboItem> comboCart = (List<ComboItem>) request.getAttribute("comboCart");
    Float total = (Float) request.getAttribute("total");
    if (total == null) {
        total = 0f;
    }
    String error = (String) request.getAttribute("error");
    DecimalFormat df = new DecimalFormat("#,###");
    int ticketCount = cart == null ? 0 : cart.size();
    int comboCount = 0;
    float ticketTotal = 0;
    float comboTotal = 0;
    if (cart != null) {
        for (CartItem item : cart) {
            ticketTotal += item.getPrice();
        }
    }
    if (comboCart != null) {
        for (ComboItem item : comboCart) {
            comboCount += item.getQuantity();
            comboTotal += item.getTotal();
        }
    }
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title">Kiểm tra đơn đặt vé</h1>
    <%if(Boolean.FALSE.equals(request.getAttribute("paymentConfigured"))){%><p class="alert alert-error">Rạp chưa kết nối thanh toán trực tuyến. Bạn vẫn có thể tạo đơn giữ ghế; đơn chưa thanh toán sẽ hết hạn sau 10 phút và chưa có vé vào rạp.</p><%}%>

    <% if (error != null) { %>
    <div class="alert alert-error"><%=Html.e(error)%></div>
    <% } %>

    <% if (cart != null && !cart.isEmpty()) { %>
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Nội dung</th>
                    <th>Số lượng</th>
                    <th>Thành tiền</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Vé xem phim</td>
                    <td><%= ticketCount%></td>
                    <td><%= df.format(ticketTotal)%> VNĐ</td>
                </tr>
                <tr>
                    <td>Bắp nước combo</td>
                    <td><%= comboCount%></td>
                    <td><%= df.format(comboTotal)%> VNĐ</td>
                </tr>
            </tbody>
        </table>
    </div>

    <h2 style="color:#ff4450;margin-top:18px;">Tổng tiền: <%= df.format(total)%> VNĐ</h2>

    <form accept-charset="UTF-8" action="<%= ctx%>/checkout" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>">
        <label>Mã ưu đãi<input name="promoCode" value="<%=Html.e(request.getParameter("promoCode"))%>" maxlength="30" placeholder="Nhập mã giảm giá"></label><p class="muted">Ghế được giữ 10 phút kể từ bước tiếp theo. Giá và ưu đãi được kiểm tra lại khi tạo đơn.</p><button class="btn btn-primary" type="submit">Giữ ghế & xem thanh toán</button>
        <a class="btn btn-blue" href="<%= ctx%>/cart">Quay lại giỏ hàng</a>
    </form>
    <% } else { %>
    <div class="alert alert-error">Giỏ hàng đang trống.</div>
    <a class="btn btn-blue" href="<%= ctx%>/movies">Chọn phim</a>
    <% } %>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
