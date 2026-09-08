<%@page import="java.text.DecimalFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Thành công");
    Float total = (Float) request.getAttribute("total");
    if (total == null) total = 0f;
    DecimalFormat df = new DecimalFormat("#,###");
    String ctx = request.getContextPath();
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title">Thanh toán thành công</h1>
    <p>Tổng tiền: <strong><%= df.format(total)%> VNĐ</strong></p>
    <a class="btn btn-primary" href="<%= ctx%>/booking/history">Xem vé của tôi</a>
    <a class="btn btn-blue" href="<%= ctx%>/movies">Tiếp tục đặt vé</a>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
