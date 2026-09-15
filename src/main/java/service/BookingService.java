package service;
import dao.Sql;import utils.DBUtils;import model.*;import java.sql.*;import java.util.*;import java.math.*;
/** SQL Server transactions serialize seat allocation and payment confirmation. */
public class BookingService {
 public static void expire(Connection c)throws Exception{
  Sql.update(c,"DELETE r FROM tblReservations r JOIN tblOrders o ON o.orderID=r.orderID WHERE o.paymentState='PENDING' AND o.expiresAt<=SYSDATETIME()");
  Sql.update(c,"UPDATE tblOrders SET paymentState='EXPIRED' WHERE paymentState='PENDING' AND expiresAt<=SYSDATETIME()");
  Sql.update(c,"UPDATE tblPromotions SET status=0 WHERE validTo<CAST(GETDATE() AS date)");
 }
 public static void expire()throws Exception{try(Connection c=DBUtils.getConnection()){c.setAutoCommit(false);try{expire(c);c.commit();}catch(Exception e){c.rollback();throw e;}}}
 public static BigDecimal money(Object o){return new BigDecimal(o.toString()).setScale(0,RoundingMode.HALF_UP);}
 public static Map<String,Object> quoteSeat(Connection c,String show,String seat)throws Exception{
  Map<String,Object> x=Sql.one(c,"SELECT se.seatID,se.seatType,COALESCE((SELECT TOP 1 p.price FROM tblPricing p WHERE p.cinemaID=r.cinemaID AND p.seatType=se.seatType AND p.status=1 AND s.showDate BETWEEN p.validFrom AND p.validTo ORDER BY p.validFrom DESC,p.pricingID),m.basePrice) price FROM tblShowtimes s JOIN tblRooms r ON r.roomID=s.roomID JOIN tblCinemas ci ON ci.cinemaID=r.cinemaID JOIN tblMovies m ON m.movieID=s.movieID JOIN tblSeats se ON se.roomID=r.roomID WHERE s.showtimeID=? AND se.seatID=? AND se.status=1 AND r.status=1 AND ci.status=1 AND m.status=1 AND s.status=1 AND DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),s.startTime),CAST(s.showDate AS datetime2))>SYSDATETIME()",show,seat);
  if(x==null)throw new IllegalArgumentException("Ghế hoặc suất chiếu không còn khả dụng.");return x;
 }
 public Map<String,Object> reserve(String user,List<CartItem> cart,List<ComboItem> combos,String promo,boolean full)throws Exception{
  if(cart==null||cart.isEmpty())throw new IllegalArgumentException("Vui lòng chọn vé trước khi thanh toán.");
  String show=cart.get(0).getShowtimeID();Set<String>seatIDs=new LinkedHashSet<>();for(CartItem item:cart)if(!show.equals(item.getShowtimeID())||!seatIDs.add(item.getSeatID()))throw new IllegalArgumentException("Mỗi đơn chỉ gồm một suất chiếu và các ghế khác nhau.");
  try(Connection c=DBUtils.getConnection()){
   c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try{
    // Same showtime lock is taken by reservation, payment and scheduling operations.
    Map<String,Object> schedule=Sql.one(c,"SELECT s.showtimeID,m.ageRating,r.cinemaID,m.screeningStart,m.screeningEnd,s.showDate FROM tblShowtimes s WITH(UPDLOCK,HOLDLOCK) JOIN tblMovies m ON m.movieID=s.movieID JOIN tblRooms r ON r.roomID=s.roomID WHERE s.showtimeID=?",show);
    if(schedule==null)throw new IllegalArgumentException("Không tìm thấy suất chiếu.");expire(c);
    java.time.LocalDate day=java.time.LocalDate.parse(schedule.get("showDate").toString());
    if(schedule.get("screeningStart")!=null&&day.isBefore(java.time.LocalDate.parse(schedule.get("screeningStart").toString()))||schedule.get("screeningEnd")!=null&&day.isAfter(java.time.LocalDate.parse(schedule.get("screeningEnd").toString())))throw new IllegalArgumentException("Suất chiếu ngoài thời gian công chiếu.");
    Map<String,Object> customer=Sql.one(c,"SELECT birthDate FROM tblUsers WHERE userID=? AND status=1 AND roleID='US'",user);
    if(customer==null)throw new IllegalArgumentException("Tài khoản không hợp lệ.");int age=((Number)schedule.get("ageRating")).intValue();
    if(age>0&&(customer.get("birthDate")==null||java.time.Period.between(java.time.LocalDate.parse(customer.get("birthDate").toString()),day).getYears()<age))throw new IllegalArgumentException("Phim yêu cầu đủ "+age+" tuổi. Vui lòng cập nhật ngày sinh trong tài khoản.");
    if(full){int capacity=((Number)Sql.one(c,"SELECT COUNT(*) n FROM tblSeats se JOIN tblShowtimes s ON s.roomID=se.roomID WHERE s.showtimeID=? AND se.status=1",show).get("n")).intValue();if(seatIDs.size()!=capacity)throw new IllegalArgumentException("Bao rạp yêu cầu đặt toàn bộ ghế đang sử dụng và chưa có người đặt.");}
    BigDecimal subtotal=BigDecimal.ZERO;List<BigDecimal>prices=new ArrayList<>();
    for(String seat:seatIDs){Map<String,Object>x=quoteSeat(c,show,seat);if(Sql.one(c,"SELECT seatID FROM tblTickets WHERE showtimeID=? AND seatID=? AND status=1 UNION ALL SELECT seatID FROM tblReservations WHERE showtimeID=? AND seatID=?",show,seat,show,seat)!=null)throw new IllegalArgumentException("Ghế "+seat+" đã được đặt hoặc đang giữ.");BigDecimal price=money(x.get("price"));subtotal=subtotal.add(price);prices.add(price);}
    BigDecimal discount=BigDecimal.ZERO;String code=promo==null?"":promo.trim().toUpperCase(Locale.ROOT);
    if(!code.isEmpty()){
     Map<String,Object>x=Sql.one(c,"SELECT * FROM tblPromotions WITH(UPDLOCK,HOLDLOCK) WHERE promoCode=? AND cinemaID=? AND status=1 AND CAST(GETDATE() AS date) BETWEEN validFrom AND validTo",code,schedule.get("cinemaID"));
     if(x==null||subtotal.compareTo(money(x.get("minAmount")))<0)throw new IllegalArgumentException("Mã ưu đãi không hợp lệ, hết hạn hoặc chưa đủ giá trị vé tối thiểu.");
     int pending=((Number)Sql.one(c,"SELECT COUNT(*) n FROM tblOrders WHERE promoCode=? AND paymentState='PENDING'",code).get("n")).intValue();
     if(((Number)x.get("usedCount")).intValue()+pending>=((Number)x.get("usageLimit")).intValue())throw new IllegalArgumentException("Mã ưu đãi đã hết lượt.");
     discount="PERCENT".equals(x.get("discountType"))?subtotal.multiply(money(x.get("discountValue"))).divide(new BigDecimal(100),0,RoundingMode.HALF_UP):money(x.get("discountValue"));discount=discount.min(subtotal);
    }
    BigDecimal total=subtotal.subtract(discount);List<Map<String,Object>> comboRows=new ArrayList<>();
    for(ComboItem item:combos){if(item.getQuantity()<1||item.getQuantity()>20)throw new IllegalArgumentException("Mỗi combo tối đa 20 phần.");Map<String,Object>x=Sql.one(c,"SELECT comboID,price FROM tblCombos WHERE comboID=? AND status=1",item.getComboID());if(x==null)throw new IllegalArgumentException("Combo không còn bán.");x.put("quantity",item.getQuantity());comboRows.add(x);total=total.add(money(x.get("price")).multiply(BigDecimal.valueOf(item.getQuantity())));}
    if(total.signum()<=0)throw new IllegalArgumentException("Tổng thanh toán phải lớn hơn 0.");
    String order="OD"+UUID.randomUUID().toString().replace("-","").substring(0,26);
    Sql.update(c,"INSERT INTO tblOrders(orderID,userID,orderDate,totalAmount,status,paymentState,expiresAt,showtimeID,promoCode,discount) VALUES(?,?,GETDATE(),?,0,'PENDING',DATEADD(MINUTE,10,SYSDATETIME()),?,?,?)",order,user,total,show,code.isEmpty()?null:code,discount);
    int i=0;for(String seat:seatIDs)Sql.update(c,"INSERT INTO tblReservations(orderID,showtimeID,seatID,price) VALUES(?,?,?,?)",order,show,seat,prices.get(i++));
    for(Map<String,Object>x:comboRows)Sql.update(c,"INSERT INTO tblOrderCombos(orderComboID,orderID,comboID,quantity,price) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),order,x.get("comboID"),x.get("quantity"),x.get("price"));
    Map<String,Object>result=Sql.one(c,"SELECT * FROM tblOrders WHERE orderID=?",order);c.commit();return result;
   }catch(Exception e){c.rollback();throw e;}
  }
 }
 /** Return VNPay acknowledgement code; duplicate callbacks never issue duplicate tickets. */
 public static boolean demoPaymentEnabled(){return Boolean.parseBoolean(DBUtils.config("FCINEMA_DEMO_PAYMENT","true"));}
 public String simulatePayment(String order,String user)throws Exception{
  if(!demoPaymentEnabled())throw new IllegalArgumentException("Chế độ giả lập thanh toán đã tắt.");
  if(user==null||user.isBlank())throw new IllegalArgumentException("Vui lòng đăng nhập lại.");
  return confirmInternal(order,null,UUID.randomUUID().toString(),true,"DEMO",user);
 }
 public String confirm(String order,String amount,String txn,boolean success,String result)throws Exception{
  return confirmInternal(order,amount,txn,success,result,null);
 }
 private String confirmInternal(String order,String amount,String txn,boolean success,String result,String owner)throws Exception{
  try(Connection c=DBUtils.getConnection()){c.setAutoCommit(false);c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
   try{
    Map<String,Object> pre=Sql.one(c,"SELECT showtimeID FROM tblOrders WHERE orderID=?",order);if(pre==null){c.rollback();return "01";}
    Sql.one(c,"SELECT showtimeID FROM tblShowtimes WITH(UPDLOCK,HOLDLOCK) WHERE showtimeID=?",pre.get("showtimeID"));
    Map<String,Object>o=Sql.one(c,"SELECT *,CASE WHEN expiresAt>SYSDATETIME() THEN 1 ELSE 0 END unexpired FROM tblOrders WITH(UPDLOCK,HOLDLOCK) WHERE orderID=?",order);
    if(owner!=null&&!owner.equals(o.get("userID"))){c.rollback();return "01";}
    if(owner==null&&!money(o.get("totalAmount")).multiply(new BigDecimal(100)).toPlainString().equals(amount)){c.rollback();return "04";}
    if(!"PENDING".equals(o.get("paymentState"))){c.rollback();return "02";}
    if(((Number)o.get("unexpired")).intValue()!=1){expire(c);c.commit();return "02";}
    if(success){List<Map<String,Object>>seats=Sql.rows(c,"SELECT * FROM tblReservations WHERE orderID=?",order);if(seats.isEmpty())throw new IllegalStateException("Missing reserved seats");
     for(Map<String,Object>s:seats)Sql.update(c,"INSERT INTO tblTickets(ticketID,orderID,showtimeID,seatID,price,status) VALUES(?,?,?,?,?,1)","TK"+UUID.randomUUID().toString().replace("-",""),order,s.get("showtimeID"),s.get("seatID"),s.get("price"));
     Sql.update(c,"UPDATE tblOrders SET status=1,paymentState='PAID' WHERE orderID=?",order);
     if(o.get("promoCode")!=null)Sql.update(c,"UPDATE tblPromotions SET usedCount=usedCount+1 WHERE promoCode=?",o.get("promoCode"));
    }else Sql.update(c,"UPDATE tblOrders SET paymentState='FAILED' WHERE orderID=?",order);
    Sql.update(c,"INSERT INTO tblPayments(paymentID,orderID,amount,resultCode) VALUES(?,?,?,?)",success?(owner!=null?"DEMO-":"VNP-")+txn:"FAIL-"+UUID.randomUUID().toString(),order,o.get("totalAmount"),result);
    Sql.update(c,"DELETE FROM tblReservations WHERE orderID=?",order);c.commit();return "00";
   }catch(Exception e){c.rollback();throw e;}
  }
 }
}
