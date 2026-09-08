<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.List"%>
<%@page import="model.Ticket"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "F-Cinema | Vé của tôi");
    List<Ticket> tickets = (List<Ticket>) request.getAttribute("tickets");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<%@include file="/WEB-INF/include/header.jsp" %>

<section class="panel">
    <h1 class="page-title">Vé của tôi</h1>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã vé</th>
                    <th>Phim</th>
                    <th>Phòng</th>
                    <th>Ngày chiếu</th>
                    <th>Giờ</th>
                    <th>Ghế</th>
                    <th>Giá vé</th>
                    <th>Ngày đặt</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (tickets != null && !tickets.isEmpty()) {
                        for (Ticket ticket : tickets) {
                %>
                <tr>
                    <td><strong><%= ticket.getTicketID()%></strong></td>
                    <td><%= ticket.getMovieTitle()%></td>
                    <td><%= ticket.getRoomName()%></td>
                    <td><%= ticket.getShowDate()%></td>
                    <td><%= ticket.getStartTime()%></td>
                    <td><%= ticket.getSeatID()%></td>
                    <td><%= df.format(ticket.getPrice())%> VNĐ</td>
                    <td><%= ticket.getOrderDate()%></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="8" style="text-align:center;color:#64748b;">Chưa có vé nào.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</section>

<%@include file="/WEB-INF/include/footer.jsp" %>
