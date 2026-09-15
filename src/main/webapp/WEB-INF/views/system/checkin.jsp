<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.Map"%><%@include file="/WEB-INF/include/header.jsp"%>
<section class="panel checkin-layout"><span class="eyebrow">STAFF · ADMISSION</span><h1 class="page-title">Soát vé tại rạp</h1><p class="muted">Quét mã QR hoặc nhập mã vé để kiểm tra trước khi cho khách vào phòng.</p>
<%if(request.getAttribute("error")!=null){%><p class="alert alert-error" role="alert"><%=Html.e(request.getAttribute("error"))%></p><%}%><%if(request.getAttribute("success")!=null){%><p class="alert alert-success" role="status"><%=Html.e(request.getAttribute("success"))%></p><%}%>
<form accept-charset="UTF-8" method="post" class="search-form" id="lookup"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>"><input id="ticketInput" name="ticketID" aria-label="Mã vé" placeholder="TK…" required autofocus autocomplete="off"><button class="btn btn-primary">Kiểm tra vé</button></form>
<div class="camera-tools">
    <button type="button" id="cameraButton" class="btn btn-soft" aria-pressed="false" aria-controls="qrReader">Mở camera quét QR</button>
    <label for="cameraSelect">Camera<select id="cameraSelect" disabled><option value="">Tự chọn camera</option></select></label>
</div>
<p id="cameraStatus" class="muted" role="status" aria-live="polite"></p>
<div id="qrReader" aria-label="Khung hình camera quét mã vé"></div>
<p class="camera-help">Nếu không dùng được camera, chọn ảnh QR hoặc nhập mã vé. QR được đọc ngay trong trình duyệt bằng html5-qrcode; hình ảnh không gửi lên máy chủ. Giữ mã rõ nét, đủ sáng và nằm trọn trong khung hình.</p>
<label for="qrImage">Chọn ảnh chứa mã QR</label>
<input id="qrImage" class="qr-image-input" type="file" accept="image/jpeg,image/png,image/webp">
<%Map<String,Object>t=(Map<String,Object>)request.getAttribute("ticket");if(t!=null&&t.get("checkedInAt")==null){%><article class="panel" style="margin-top:24px"><h2><%=Html.e(t.get("title"))%></h2><p><%=Html.e(t.get("roomName"))%> · Ghế <strong><%=Html.e(t.get("seatID"))%></strong></p><p><%=Html.e(t.get("showDate"))%> · <%=Html.e(t.get("startTime"))%></p><form accept-charset="UTF-8" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>"><input type="hidden" name="ticketID" value="<%=Html.e(t.get("ticketID"))%>"><input type="hidden" name="action" value="confirm"><label><input type="checkbox" name="ageVerified" value="true" required> Đã kiểm tra khách đáp ứng độ tuổi: <%=Html.e(t.get("ageRating"))%>+</label><p><button class="btn btn-primary">Xác nhận vào rạp</button></p></form></article><%}%></section>
<script src="<%=headerCtx%>/assets/vendor/html5-qrcode/html5-qrcode-2.3.8.min.js" defer></script>
<script src="<%=headerCtx%>/assets/js/qr-scanner.js?v=20260912-fix4" defer></script><%@include file="/WEB-INF/include/footer.jsp"%>
