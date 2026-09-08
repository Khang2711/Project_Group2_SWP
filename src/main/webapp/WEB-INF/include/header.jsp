<%@page import="java.net.URLDecoder"%>
<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="jakarta.servlet.http.Cookie"%>
<%@page import="model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%!
    private String escapeHeaderText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    String headerCtx = request.getContextPath();
    String headerTitle = (String) request.getAttribute("pageTitle");
    if (headerTitle == null) {
        headerTitle = "F-Cinema";
    }

    User headerLoginUser = null;
    if (session != null) {
        headerLoginUser = (User) session.getAttribute("LOGIN_USER");
    }

    String displayName = null;
    Cookie[] headerCookies = request.getCookies();
    if (headerCookies != null) {
        for (Cookie cookie : headerCookies) {
            if ("DISPLAY_NAME".equals(cookie.getName())) {
                try {
                    displayName = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    displayName = null;
                }
                break;
            }
        }
    }

    if ((displayName == null || displayName.trim().isEmpty()) && headerLoginUser != null) {
        displayName = headerLoginUser.getFullName();
    }

    boolean fullWidthPage = Boolean.TRUE.equals(request.getAttribute("fullWidthPage"));
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><%= headerTitle%></title>
        <link rel="stylesheet" href="<%= headerCtx%>/assets/css/fcinema.css?v=20260719-1">
    </head>
    <body class="<%= fullWidthPage ? "full-width-page" : ""%>">
        <div class="shell <%= fullWidthPage ? "shell-full" : ""%>">
            <header class="topbar">
                <div class="brand">
                    <span class="brand-mark">F</span>
                    <span>F-Cinema</span>
                     <span class="hello-user" title="username">Hi, <%= escapeHeaderText(displayName)%></span>
                    
                </div>
                <nav class="nav">
                    <a href="<%= headerCtx%>/movies">Phim</a>
                    <% if (headerLoginUser != null && "AD".equals(headerLoginUser.getRoleID())) { %>
                    <a href="<%= headerCtx%>/admin/movies">Quản lí phim</a>
                    <a href="<%= headerCtx%>/admin/rooms">Phòng</a>
                    <a href="<%= headerCtx%>/admin/showtimes">Suất chiếu</a>
                    <a href="<%= headerCtx%>/admin/reports">Báo cáo</a>
                    <% } %>
                    <% if (headerLoginUser != null) { %>
                   
                    <a href="<%= headerCtx%>/cart">Giá vé</a>
                    <a href="<%= headerCtx%>/booking/history">Vé của tôi</a>
                    <a href="<%= headerCtx%>/profile">Tài khoản</a>
                    <a href="<%= headerCtx%>/auth?action=logout">Đăng xuất</a>
                    <% } else { %>
                    <a href="<%= headerCtx%>/auth">Đăng nhập</a>
                    <% } %>
                </nav>
            </header>
