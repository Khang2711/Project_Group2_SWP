<%@page import="model.User,utils.Html,security.Access" contentType="text/html" pageEncoding="UTF-8"%>
<% User headerLoginUser=(User)session.getAttribute("LOGIN_USER"); String headerCtx=request.getContextPath(); String hr=headerLoginUser==null?"":headerLoginUser.getRoleID(); %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title><%= Html.e(request.getAttribute("pageTitle")==null?"F-Cinema":request.getAttribute("pageTitle")) %></title>
<link rel="stylesheet" href="<%=headerCtx%>/assets/css/fcinema.css?v=20260912-fix4"></head>
<body><div class="shell"><header class="topbar"><a class="brand" href="<%=headerCtx%><%=headerLoginUser==null?"/movies":Access.home(hr)%>"><span class="brand-mark">F</span><span>F-CINEMA</span></a>
<nav class="nav" aria-label="Điều hướng chính">
<% if(hr.isEmpty()||"US".equals(hr)){ %><a href="<%=headerCtx%>/movies">Khám phá</a><% } %>
<% if("US".equals(hr)){ %><a href="<%=headerCtx%>/cart">Giỏ vé</a><a href="<%=headerCtx%>/booking/history">Vé của tôi</a><a href="<%=headerCtx%>/booking/orders">Đơn đặt vé</a><a href="<%=headerCtx%>/profile">Tài khoản</a><% } %>
<% if("MG".equals(hr)){ %><a href="<%=headerCtx%>/admin/reports">Tổng quan</a><a href="<%=headerCtx%>/admin/movies">Phim</a><a href="<%=headerCtx%>/admin/cinemas">Rạp</a><a href="<%=headerCtx%>/admin/rooms">Phòng</a><a href="<%=headerCtx%>/admin/seats">Ghế</a><a href="<%=headerCtx%>/admin/showtimes">Suất chiếu</a><a href="<%=headerCtx%>/admin/pricing">Giá vé</a><a href="<%=headerCtx%>/admin/promotions">Ưu đãi</a><% } %>
<% if("AD".equals(hr)){ %><a href="<%=headerCtx%>/system/users">Tài khoản & phân quyền</a><% } %>
<% if("ST".equals(hr)){ %><a href="<%=headerCtx%>/staff/checkin">Soát vé</a><% } %>
</nav><div class="account-nav"><% if(headerLoginUser!=null){ %><span class="hello-user"><%=Html.e(headerLoginUser.getFullName())%></span><a href="<%=headerCtx%>/auth?action=logout">Đăng xuất</a><% }else{ %><a class="btn btn-primary" href="<%=headerCtx%>/auth">Đăng nhập</a><% } %></div></header>
