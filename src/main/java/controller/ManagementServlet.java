package controller;
import dao.Sql;import utils.*;import java.util.*;import java.sql.*;import java.math.BigDecimal;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet(urlPatterns={"/system/users","/admin/cinemas","/admin/seats","/admin/pricing","/admin/promotions","/admin/movie-policy","/admin/room-branch"})
public class ManagementServlet extends HttpServlet {
 public static class Field {
  public final String key,label,type;public final String[] options;
  Field(String k,String l,String t,String...o){key=k;label=l;type=t;options=o;}
 }
 public static class Spec {
  public final String table,title;public final Field[] fields;
  Spec(String t,String n,Field...f){table=t;title=n;fields=f;}
 }
 static Field f(String k,String l,String t,String...o){return new Field(k,l,t,o);}
 public static final Map<String,Spec> SPECS=new LinkedHashMap<>();
 static {
 SPECS.put("/system/users",new Spec("tblUsers","Tài khoản & phân quyền",f("userID","Tên đăng nhập","text"),f("fullName","Họ và tên","text"),f("roleID","Vai trò","select","US:Khách hàng","ST:Nhân viên soát vé","MG:Quản lý rạp","AD:Quản trị tài khoản"),f("email","Email","email"),f("cinemaID","Chi nhánh (bắt buộc với nhân viên)","cinema"),f("status","Trạng thái","select","1:Hoạt động","0:Vô hiệu hóa")));
 SPECS.put("/admin/cinemas",new Spec("tblCinemas","Chi nhánh rạp",f("cinemaID","Mã rạp","text"),f("cinemaName","Tên rạp","text"),f("address","Địa chỉ","text"),f("phone","Điện thoại","text"),f("status","Trạng thái","select","1:Hoạt động","0:Ngừng hoạt động")));
 SPECS.put("/admin/seats",new Spec("tblSeats","Cấu hình ghế",f("seatKey","Mã cấu hình (phòng-ghế)","text"),f("roomID","Phòng","room"),f("seatID","Số ghế (A1–Z10)","text"),f("seatType","Loại ghế","select","Standard:Thường","VIP:VIP","Couple:Đôi"),f("status","Trạng thái","select","1:Sẵn sàng","0:Không sử dụng")));
 SPECS.put("/admin/pricing",new Spec("tblPricing","Chính sách giá vé",f("pricingID","Mã chính sách","text"),f("cinemaID","Rạp","cinema"),f("seatType","Loại ghế","select","Standard:Thường","VIP:VIP","Couple:Đôi"),f("price","Giá vé (VNĐ)","number"),f("validFrom","Từ ngày","date"),f("validTo","Đến ngày","date"),f("status","Trạng thái","select","1:Áp dụng","0:Ngừng áp dụng")));
 SPECS.put("/admin/promotions",new Spec("tblPromotions","Ưu đãi & mã giảm giá",f("promoCode","Mã ưu đãi","text"),f("cinemaID","Rạp áp dụng","cinema"),f("promoName","Tên chương trình","text"),f("discountType","Hình thức giảm","select","PERCENT:Phần trăm","FIXED:Số tiền"),f("discountValue","Giá trị giảm","number"),f("minAmount","Giá trị vé tối thiểu","number"),f("validFrom","Từ ngày","date"),f("validTo","Đến ngày","date"),f("usageLimit","Số lượt tối đa","number"),f("status","Trạng thái","select","1:Áp dụng","0:Ngừng áp dụng")));
 SPECS.put("/admin/room-branch",new Spec("tblRooms","Chi nhánh của phòng",f("roomID","Mã phòng","text"),f("cinemaID","Rạp","cinema")));
 SPECS.put("/admin/movie-policy",new Spec("tblMovies","Phân loại & thời gian chiếu",f("movieID","Mã phim","text"),f("ageRating","Độ tuổi tối thiểu","select","0:P – Mọi độ tuổi","13:T13","16:T16","18:T18"),f("screeningStart","Bắt đầu công chiếu","date"),f("screeningEnd","Kết thúc công chiếu","date")));
 }
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException {render(q,r);}
 private void render(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  Spec s=SPECS.get(q.getServletPath());q.setAttribute("spec",s);
  try{
   String columns=String.join(",",Arrays.stream(s.fields).map(x->x.key).toArray(String[]::new));
   String search=q.getParameter("q");q.setAttribute("records",Sql.rows("SELECT "+columns+" FROM "+s.table+" WHERE "+s.fields[0].key+" LIKE ? ORDER BY "+s.fields[0].key,"%"+(search==null?"":search)+"%"));
   if(q.getParameter("edit")!=null){List<Map<String,Object>> a=Sql.rows("SELECT "+columns+" FROM "+s.table+" WHERE "+s.fields[0].key+"=?",q.getParameter("edit"));if(!a.isEmpty())q.setAttribute("record",a.get(0));}
   q.setAttribute("cinemas",Sql.rows("SELECT cinemaID,cinemaName FROM tblCinemas ORDER BY cinemaName"));q.setAttribute("rooms",Sql.rows("SELECT roomID,roomName FROM tblRooms ORDER BY roomName"));
  }catch(Exception e){getServletContext().log("Management load",e);q.setAttribute("error","Không tải được dữ liệu. Kiểm tra kết nối và bản cập nhật SQL.");}
  q.getRequestDispatcher("/WEB-INF/views/system/manage.jsp").forward(q,r);
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  Spec s=SPECS.get(q.getServletPath());
  try(Connection c=DBUtils.getConnection()){
   c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try{
    boolean editing="update".equals(q.getParameter("mode"));List<Object> values=new ArrayList<>();String key=q.getParameter(s.fields[0].key);
    if(key==null||!key.matches("[A-Za-z0-9_-]{1,30}"))throw new IllegalArgumentException("Mã chỉ gồm chữ, số, dấu gạch; tối đa 30 ký tự.");
    for(Field field:s.fields){String v=q.getParameter(field.key);v=v==null?"":v.trim();
     if(field.type.equals("select")&&Arrays.stream(field.options).noneMatch(o->o.split(":",2)[0].equals(q.getParameter(field.key))))throw new IllegalArgumentException("Lựa chọn không hợp lệ: "+field.label);
     boolean optional=field.key.equals("email")||field.key.equals("phone")||(s.table.equals("tblUsers")&&field.key.equals("cinemaID"));
     if(v.isEmpty()&&!optional)throw new IllegalArgumentException("Vui lòng nhập "+field.label);
     if(v.length()>250)throw new IllegalArgumentException("Nội dung quá dài: "+field.label);
     if(field.type.equals("number")){BigDecimal n=new BigDecimal(v);if(n.signum()<0||n.scale()>0)throw new IllegalArgumentException("Giá trị phải là số nguyên không âm.");values.add(n);}
     else if(field.type.equals("date"))values.add(java.sql.Date.valueOf(v));else values.add(v.isEmpty()?null:v);
    }
    if(s.table.equals("tblUsers")){
     if("ST".equals(q.getParameter("roleID"))&&(q.getParameter("cinemaID")==null||q.getParameter("cinemaID").isBlank()))throw new IllegalArgumentException("Nhân viên phải thuộc một chi nhánh.");
     model.User self=(model.User)q.getSession().getAttribute("LOGIN_USER");
     if(key.equals(self.getUserID())&&(!"AD".equals(q.getParameter("roleID"))||!"1".equals(q.getParameter("status"))))throw new IllegalArgumentException("Không thể tự khóa hoặc hạ quyền tài khoản đang đăng nhập.");
    }
    if(s.table.equals("tblSeats")){
     if(!q.getParameter("seatID").matches("[A-Z](10|[1-9])"))throw new IllegalArgumentException("Số ghế phải từ A1 đến Z10.");
     if(!editing){Map<String,Object> room=Sql.one(c,"SELECT totalSeats FROM tblRooms WHERE roomID=?",q.getParameter("roomID"));int count=((Number)Sql.one(c,"SELECT COUNT(*) n FROM tblSeats WHERE roomID=?",q.getParameter("roomID")).get("n")).intValue();if(room==null||count>=((Number)room.get("totalSeats")).intValue())throw new IllegalArgumentException("Số ghế đã đạt sức chứa phòng.");}
     if(editing&&Sql.one(c,"SELECT TOP 1 t.ticketID FROM tblTickets t JOIN tblShowtimes st ON st.showtimeID=t.showtimeID JOIN tblSeats se ON se.roomID=st.roomID AND se.seatID=t.seatID WHERE se.seatKey=? AND st.showDate>=CAST(GETDATE() AS date)",key)!=null)throw new IllegalArgumentException("Ghế đã có vé cho lịch chiếu chưa kết thúc.");
    }
    if((s.table.equals("tblMovies")||s.table.equals("tblRooms"))&&!editing)throw new IllegalArgumentException("Tạo phim trong mục Phim trước, sau đó cập nhật chính sách.");
    String[] cols=Arrays.stream(s.fields).map(f->f.key).toArray(String[]::new);
    if(editing){List<String> set=new ArrayList<>();for(int i=1;i<cols.length;i++)set.add(cols[i]+"=?");values.remove(0);values.add(key);if(Sql.update(c,"UPDATE "+s.table+" SET "+String.join(",",set)+" WHERE "+cols[0]+"=?",values.toArray())!=1)throw new IllegalArgumentException("Không tìm thấy bản ghi.");}
    else {
     String names=String.join(",",cols);if(s.table.equals("tblUsers")){String pw=q.getParameter("password");if(pw==null||pw.length()<8)throw new IllegalArgumentException("Mật khẩu ban đầu cần ít nhất 8 ký tự.");names+=",password";values.add(PasswordUtils.hash(pw));}
     Sql.update(c,"INSERT INTO "+s.table+"("+names+") VALUES("+String.join(",",Collections.nCopies(values.size(),"?"))+")",values.toArray());
    }
    c.commit();r.sendRedirect(q.getContextPath()+q.getServletPath()+"?saved=1");return;
   }catch(Exception e){c.rollback();throw e;}
  }catch(Exception e){getServletContext().log("Management save",e);q.setAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không lưu được: mã trùng, dữ liệu liên quan hoặc khoảng hiệu lực không hợp lệ.");}
  render(q,r);
 }
}
