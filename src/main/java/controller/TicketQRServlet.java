package controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import model.User;import dao.Sql;
import com.google.zxing.BarcodeFormat;import com.google.zxing.qrcode.QRCodeWriter;import com.google.zxing.common.BitMatrix;
@WebServlet(urlPatterns={"/tickets/qr"}) public class TicketQRServlet extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  try{User u=(User)q.getSession().getAttribute("LOGIN_USER");String id=q.getParameter("ticketID");if(Sql.rows("SELECT t.ticketID FROM tblTickets t JOIN tblOrders o ON o.orderID=t.orderID WHERE t.ticketID=? AND o.userID=? AND o.status=1 AND t.status=1",id,u.getUserID()).isEmpty()){r.sendError(404);return;}
   BitMatrix b=new QRCodeWriter().encode(id,BarcodeFormat.QR_CODE,240,240);java.awt.image.BufferedImage img=new java.awt.image.BufferedImage(240,240,java.awt.image.BufferedImage.TYPE_INT_RGB);for(int y=0;y<240;y++)for(int x=0;x<240;x++)img.setRGB(x,y,b.get(x,y)?0x000000:0xffffff);r.setContentType("image/png");r.setHeader("Cache-Control","private,no-store");javax.imageio.ImageIO.write(img,"png",r.getOutputStream());
  }catch(Exception e){getServletContext().log("Ticket QR",e);r.sendError(500);}
 }
}
