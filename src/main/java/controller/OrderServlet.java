package controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.util.*;import dao.Sql;import model.User;import service.*;
@WebServlet(urlPatterns={"/booking/orders"})
public class OrderServlet extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  q.setAttribute("paymentConfigured",VNPay.isConfigured());
  q.setAttribute("demoPaymentEnabled",BookingService.demoPaymentEnabled());
  q.setAttribute("success",q.getSession().getAttribute("DEMO_PAYMENT_NOTICE"));
  q.getSession().removeAttribute("DEMO_PAYMENT_NOTICE");
  try{BookingService.expire();User u=(User)q.getSession().getAttribute("LOGIN_USER");q.setAttribute("orders",Sql.rows("SELECT o.*,CASE WHEN EXISTS(SELECT 1 FROM tblPayments p WHERE p.orderID=o.orderID AND p.resultCode='DEMO') THEN 1 ELSE 0 END demoPayment,m.title,s.showDate,s.startTime,r.roomName FROM tblOrders o LEFT JOIN tblShowtimes s ON s.showtimeID=o.showtimeID LEFT JOIN tblMovies m ON m.movieID=s.movieID LEFT JOIN tblRooms r ON r.roomID=s.roomID WHERE o.userID=? ORDER BY o.orderDate DESC",u.getUserID()));}
  catch(Exception e){getServletContext().log("Orders",e);q.setAttribute("error","Không thể tải đơn đặt vé.");}q.getRequestDispatcher("/WEB-INF/views/user/orders.jsp").forward(q,r);
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  try{User u=(User)q.getSession().getAttribute("LOGIN_USER");
   if("simulate".equals(q.getParameter("action"))){
    String code=new BookingService().simulatePayment(q.getParameter("orderID"),u.getUserID());
    if(!"00".equals(code))throw new IllegalArgumentException("Không thể giả lập: đơn không thuộc tài khoản này, đã xử lý hoặc hết hạn giữ ghế. Hãy kiểm tra trạng thái đơn.");
    q.getSession().setAttribute("DEMO_PAYMENT_NOTICE","Giả lập thanh toán thành công. Vé đã được phát hành để kiểm thử; không có tiền thật được thu.");
    r.sendRedirect(q.getContextPath()+"/booking/orders");return;
   }
   List<Map<String,Object>>rows=Sql.rows("SELECT * FROM tblOrders WHERE orderID=? AND userID=? AND paymentState='PENDING' AND expiresAt>SYSDATETIME()",q.getParameter("orderID"),u.getUserID());if(rows.isEmpty())throw new IllegalArgumentException("Đơn đã hết hạn hoặc không còn chờ thanh toán.");r.sendRedirect(VNPay.url(rows.get(0),q.getRemoteAddr()));return;}
  catch(Exception e){getServletContext().log("Payment URL",e);q.setAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không thể mở thanh toán.");}doGet(q,r);
 }
}
