package controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.util.*;import service.*;
@WebServlet(urlPatterns={"/payment/ipn","/payment/return"})
public class PaymentServlet extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  boolean ipn=q.getServletPath().equals("/payment/ipn");String code="99";
  try{Map<String,String>p=new TreeMap<>();boolean duplicate=false;for(Map.Entry<String,String[]>e:q.getParameterMap().entrySet())if(e.getKey().startsWith("vnp_")){if(e.getValue().length!=1)duplicate=true;p.put(e.getKey(),e.getValue()[0]);}
   if(duplicate||!VNPay.verify(p))code="97";
   else if(ipn)code=new BookingService().confirm(p.get("vnp_TxnRef"),p.get("vnp_Amount"),p.get("vnp_TransactionNo"),"00".equals(p.get("vnp_ResponseCode"))&&"00".equals(p.get("vnp_TransactionStatus")),p.get("vnp_ResponseCode"));
   else code="00";
  }catch(Exception e){getServletContext().log("Payment callback",e);}
  if(ipn){r.setContentType("application/json");r.getWriter().write("{\"RspCode\":\""+code+"\",\"Message\":\""+(code.equals("00")?"Confirm Success":"Not confirmed")+"\"}");return;}
  q.setAttribute("notice",code.equals("00")?"Đã trở về từ cổng thanh toán. Vé chỉ xuất hiện sau khi rạp nhận xác nhận giao dịch. Kiểm tra trạng thái đơn đặt vé bên dưới.":"Không xác minh được kết quả thanh toán. Vui lòng kiểm tra trạng thái đơn đặt vé.");q.getRequestDispatcher("/WEB-INF/views/user/paymentReturn.jsp").forward(q,r);
 }
}
