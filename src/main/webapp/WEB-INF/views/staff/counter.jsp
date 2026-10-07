<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<h1>Bán hàng tại quầy</h1>
<p>Staff Counter · Bán vé và food/combo tại rạp</p>

<section>
    <h2>Chức năng tại quầy</h2>

    <p>
        Chọn chức năng cần xử lý. Đây là trang sườn, chưa có nghiệp vụ ghi dữ liệu vào database.
    </p>

    <div style="display:flex; gap:20px; flex-wrap:wrap; margin-top:20px;">

        <a href="${ctx}/staff/ticket-sale"
           style="display:block; width:280px; padding:22px; border:1px solid #3a3a42; border-radius:8px; background:#18181c;">
            <h3 style="margin-bottom:10px;">Bán vé tại quầy</h3>
            <p style="color:#aaaaaa;">
                Chọn phim, suất chiếu, ghế và tạo vé cho khách mua trực tiếp tại rạp.
            </p>
        </a>

        <a href="${ctx}/staff/food-sale"
           style="display:block; width:280px; padding:22px; border:1px solid #3a3a42; border-radius:8px; background:#18181c;">
            <h3 style="margin-bottom:10px;">Bán Food / Combo</h3>
            <p style="color:#aaaaaa;">
                Chọn món ăn, nước uống, combo và tạo đơn hàng tại quầy.
            </p>
        </a>

        <a href="${ctx}/staff/check-in"
           style="display:block; width:280px; padding:22px; border:1px solid #3a3a42; border-radius:8px; background:#18181c;">
            <h3 style="margin-bottom:10px;">Check-in vé</h3>
            <p style="color:#aaaaaa;">
                Kiểm tra mã vé và xác nhận khách đã vào phòng chiếu.
            </p>
        </a>

    </div>
</section>

<%-- TODO:
     - Chỉ cho STAFF truy cập.
     - Hiển thị cinema hiện tại của staff.
     - Có thể bổ sung số đơn/vé đã bán trong ca làm việc nếu requirement cần.
--%>

<%@ include file="/WEB-INF/include/footer.jsp" %>
