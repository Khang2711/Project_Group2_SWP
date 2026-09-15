<%@page pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/include/header.jsp"%><section class="panel"><h1 class="page-title">Kết quả giao dịch</h1><p><%=Html.e(request.getAttribute("notice"))%></p><a class="btn btn-primary" href="<%=headerCtx%>/booking/orders">Xem đơn đặt vé</a></section><%@include file="/WEB-INF/include/footer.jsp"%>
