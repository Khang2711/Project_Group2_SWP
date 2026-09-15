package controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;import java.util.*;import dao.Sql;import utils.DBUtils;import model.User;
@WebServlet(urlPatterns={"/staff/checkin"}) public class CheckinServlet extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{q.getRequestDispatcher("/WEB-INF/views/system/checkin.jsp").forward(q,r);}
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  User u=(User)q.getSession().getAttribute("LOGIN_USER");String code=q.getParameter("ticketID");
  try(Connection c=DBUtils.getConnection()){c.setAutoCommit(false);try{
   Map<String,Object>ticket=Sql.one(c,"SELECT t.ticketID,t.checkedInAt,t.seatID,m.title,s.showDate,s.startTime,s.endTime,r.roomName,m.ageRating FROM tblTickets t WITH(UPDLOCK,HOLDLOCK) JOIN tblOrders o ON o.orderID=t.orderID JOIN tblShowtimes s ON s.showtimeID=t.showtimeID JOIN tblMovies m ON m.movieID=s.movieID JOIN tblRooms r ON r.roomID=s.roomID JOIN tblUsers staff ON staff.cinemaID=r.cinemaID WHERE t.ticketID=? AND t.status=1 AND o.status=1 AND staff.userID=? AND staff.status=1 AND staff.roleID='ST'",code==null?"":code.trim(),u.getUserID());
   if(ticket==null)throw new IllegalArgumentException("Vé không hợp lệ hoặc không thuộc chi nhánh của bạn.");
   q.setAttribute("ticket",ticket);
   if(ticket.get("checkedInAt")!=null)throw new IllegalArgumentException("Vé đã được sử dụng lúc "+ticket.get("checkedInAt"));
   if("confirm".equals(q.getParameter("action"))){
    if(!"true".equals(q.getParameter("ageVerified")))throw new IllegalArgumentException("Cần xác nhận độ tuổi khách theo phân loại phim.");
    int count=Sql.update(c,"UPDATE t SET checkedInAt=SYSDATETIME(),checkedInBy=? FROM tblTickets t JOIN tblShowtimes s ON s.showtimeID=t.showtimeID WHERE t.ticketID=? AND t.checkedInAt IS NULL AND SYSDATETIME() BETWEEN DATEADD(MINUTE,-30,DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),s.startTime),CAST(s.showDate AS datetime2))) AND DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),s.endTime),CAST(s.showDate AS datetime2))",u.getUserID(),code.trim());
    if(count!=1)throw new IllegalArgumentException("Chỉ nhận khách từ 30 phút trước giờ chiếu đến giờ kết thúc.");q.setAttribute("success","Soát vé thành công. Mời khách vào phòng.");q.removeAttribute("ticket");
   }c.commit();
  }catch(Exception e){c.rollback();throw e;}}
  catch(Exception e){getServletContext().log("Check-in",e);q.setAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không thể kiểm tra vé.");}doGet(q,r);
 }
}
