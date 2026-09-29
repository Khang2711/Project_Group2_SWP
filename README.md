# FCinema MVC — Khung dự án PRJ

Dự án Maven Web Application dùng Java Servlet/JSP, SQL Server. Đầu vào ban đầu: Group2_RDS Document (2)(2).docx và DBcontext(1).txt; cấu trúc source sau đó được đồng bộ theo quyết định mới của nhóm là bỏ cơ chế giữ ghế 10 phút.

## Đã làm và chưa làm

- JavaBean model theo schema hiện có, constructor rỗng/đủ tham số, getter/setter.
- DBContext kết nối FCinemaDB, giữ địa chỉ localhost:1433 và tài khoản mẫu local.
- Controller chỉ forward tới JSP để xem khung. Service và repository (DAO) phần lớn vẫn là TODO.
- JSP có header/footer dùng chung, phân thư mục admin/auth/system/user và thêm staff cho check-in.
- Chưa có CRUD hoàn chỉnh, đăng nhập thật, phân quyền, đặt vé, thanh toán hoặc dữ liệu mẫu.
- Không tạo database hoặc bảng. Phải có FCinemaDB trên SQL Server trước khi kiểm tra kết nối.

## Cách mở bằng NetBeans

1. File → Open Project → chọn thư mục có pom.xml.
2. Chọn JDK 11 (hoặc JDK 17), thêm Apache Tomcat 10.1 vào Servers.
3. Project Properties → Run → chọn Tomcat, context path /FCinema.
4. Đợi Maven tải dependencies, Clean and Build rồi Run.
5. Mở http://localhost:8080/FCinema/home để xem các trang khung. Trang này không cần DB.

Dòng lệnh tương đương: `mvn clean package`, sau đó chép target/FCinema.war vào thư mục webapps của Tomcat 10.1 và khởi động server.
Không dùng Tomcat 9 cho cấu hình jakarta này. Không trộn javaee-web-api javax với Jakarta.

## Kiểm tra kết nối database

1. Trong SQL Server, bảo đảm database FCinemaDB tồn tại; bật TCP/IP và cổng thực tế.
2. Sửa DB_USER/DB_PWD và địa chỉ trong src/main/java/db/DBContext.java theo máy bạn.
3. Với đăng nhập sa, SQL Server cần bật SQL Server Authentication và tài khoản phải được phép truy cập DB.
4. Chạy main của util.DBConnectionCheck trong IDE. Kết quả thành công phải ghi FCinemaDB.

DBContext dùng JDBC tự nạp driver từ dependency, không cần Class.forName. getConnection() trả kết nối mới và ném SQLException để DAO xử lý lỗi; khi viết DAO dùng try-with-resources để đóng connection.

```java
try (java.sql.Connection connection = new db.DBContext().getConnection()) {
    // TODO: tạo PreparedStatement và ResultSet trong try-with-resources.
}
```

URL encrypt=false và tài khoản mẫu chỉ dành cho thực hành local. Bản khung chưa có bảo vệ các trang quản trị; cần hoàn thiện security trước khi triển khai thật.

## Hiểu luồng MVC

Trình duyệt gửi request → controller nhận request → service kiểm tra nghiệp vụ → repository truy vấn bằng DBContext → model giữ dữ liệu → controller đặt request attribute → JSP hiển thị bằng EL/JSTL.
Ở bản khung, controller chủ yếu mở JSP, chưa gọi đầy đủ service. Không viết SQL hoặc nghiệp vụ trong JSP.
Repository chính là tầng DAO quen thuộc ở PRJ; giữ tên repository và service để khớp tài liệu thiết kế.

## Quy ước model

Tên Java camelCase, tên SQL snake_case; xem docs/MODEL_MAPPING.md. ID bắt buộc dùng int, ID cho phép NULL như User.cinemaId và Booking.promotionId dùng Integer; Promotion.usageLimit cũng dùng Integer. Tiền dùng BigDecimal; ngày/giờ dùng java.sql.Date, Time, Timestamp để thuận tiện JDBC.
Model chỉ lưu khóa ngoại dạng ID. CinemaMovie có khóa chính ghép. Không dùng JPA hoặc Lombok.
Khi đọc Integer nullable từ JDBC, không dùng getInt đơn lẻ vì SQL NULL sẽ thành 0; dùng getObject hoặc kiểm tra wasNull.
Bảng User cần viết [User] trong SQL Server.

## Quyết định nghiệp vụ mới về đặt ghế

- Đã bỏ cơ chế giữ ghế 10 phút.
- Không dùng `BookingSeat` để reserve ghế và không dùng `Booking.reservationExpiredAt`.
- Ghế của vé được lưu qua `Ticket.seat_id`; suất chiếu lấy qua `Booking.showtime_id`.
- Khi xác nhận thanh toán phải kiểm tra lại xung đột ghế và xử lý trong transaction/constraint phù hợp để tránh bán trùng.
- Không tự thêm bảng mới như combo/đồ ăn nếu chưa có schema SQL/RDS mới xác nhận rõ cấu trúc.

## Điểm cần thống nhất với database thật

Database Schema và Java model phải được đối chiếu với script SQL mới nhất trước khi viết repository thật. Nếu script SQL hiện vẫn còn `Booking_Seat` hoặc `reservation_expired_at`, cần sửa script SQL cho đồng bộ với quyết định bỏ giữ ghế trước khi triển khai chức năng booking.
Độ dài cột, khóa, giá trị status và các ràng buộc phải đối chiếu script SQL thực tế; model không tự bảo đảm constraint DB.

## Tài liệu để chia việc

- docs/STRUCTURE.txt: cây thư mục đầy đủ.
- docs/MODEL_MAPPING.md: đối chiếu từng cột với model.
- docs/UC_TASK_MAP.md: ánh xạ use case tới file và gợi ý chia 6 người.
- docs/VALIDATION.md: phạm vi kiểm tra bản bàn giao.
