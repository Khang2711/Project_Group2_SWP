# FCinema MVC — Khung dự án PRJ

Dự án Maven Web Application dùng Java Servlet/JSP, SQL Server. Đầu vào: Group2_RDS Document (2)(2).docx và DBcontext(1).txt. Không có file mô tả nghiệp vụ riêng hoặc script SQL đính kèm; phạm vi theo RDS đã gửi.

## Đã làm và chưa làm

- 14 JavaBean model đầy đủ thuộc tính, constructor rỗng/đủ tham số, getter/setter.
- DBContext kết nối FCinemaDB, giữ địa chỉ localhost:1433 và tài khoản mẫu sa/123 theo file gửi.
- Controller chỉ forward tới JSP để xem khung. Service và repository (DAO) chỉ chứa TODO.
- JSP có header/footer dùng chung, phân thư mục admin/auth/system/user và thêm staff cho check-in.
- Chưa có CRUD, đăng nhập thật, phân quyền, đặt vé, thanh toán hoặc dữ liệu mẫu. Trang Đăng xuất cũng chỉ là placeholder, chưa hủy session.
- Không tạo database hoặc bảng. Phải có FCinemaDB trên SQL Server trước khi kiểm tra kết nối.

## Cách mở bằng NetBeans

1. Giải nén. File → Open Project → chọn thư mục FCinema-MVC có pom.xml.
2. Chọn JDK 11 (hoặc JDK 17), thêm Apache Tomcat 10.1 vào Servers.
3. Project Properties → Run → chọn Tomcat, context path /FCinema.
4. Đợi Maven tải dependencies, Clean and Build rồi Run.
5. Mở http://localhost:8080/FCinema/home để xem các trang khung. Trang này không cần DB.

Dòng lệnh tương đương: `mvn clean package`, sau đó chép target/FCinema.war vào thư mục webapps của Tomcat 10.1 và khởi động server.
Không dùng Tomcat 9 cho cấu hình jakarta này. Không trộn javaee-web-api javax với Jakarta như danh sách thư viện mẫu.
Thư viện JDBC/JSTL được Maven tải theo pom.xml; ZIP không chứa JAR, JDK, Maven hoặc Tomcat.

## Kiểm tra kết nối database

1. Trong SQL Server, bảo đảm database FCinemaDB tồn tại; bật TCP/IP và cổng thực tế (mẫu dùng 1433).
2. Sửa DB_USER/DB_PWD và địa chỉ trong src/main/java/db/DBContext.java theo máy bạn.
3. Với đăng nhập sa, SQL Server cần bật SQL Server Authentication và tài khoản phải được phép truy cập DB.
4. Chạy main của util.DBConnectionCheck trong IDE. Kết quả thành công phải ghi FCinemaDB.

DBContext dùng JDBC tự nạp driver từ dependency, không cần Class.forName. getConnection() trả kết nối mới và ném SQLException để DAO xử lý lỗi; không âm thầm trả null. Đây là khác biệt có chủ đích so với mẫu. Khi viết DAO, dùng try-with-resources để đóng connection.

```java
try (java.sql.Connection connection = new db.DBContext().getConnection()) {
    // TODO: tạo PreparedStatement và ResultSet trong try-with-resources.
}
```

URL encrypt=false và tài khoản mẫu chỉ dành cho thực hành local. Bản khung chưa có bảo vệ các trang quản trị; cần hoàn thiện security trước khi triển khai thật.

## Hiểu luồng MVC

Trình duyệt gửi request → controller nhận request → service kiểm tra nghiệp vụ → repository truy vấn bằng DBContext → model giữ dữ liệu → controller đặt request attribute → JSP hiển thị bằng EL/JSTL.
Ở bản khung, controller chỉ mở JSP, chưa gọi service. Không viết SQL hoặc nghiệp vụ trong JSP.
repository chính là tầng DAO quen thuộc ở PRJ; giữ tên repository và service để khớp mục I.3.2 của RDS. Các package dto/security/integration/config/exception chỉ có mô tả để dành, chưa cần thêm tầng phức tạp.

## Quy ước model

Tên Java camelCase, tên SQL snake_case; xem docs/MODEL_MAPPING.md. ID bắt buộc dùng int, ID cho phép NULL như User.cinemaId và Booking.promotionId dùng Integer; Promotion.usageLimit cũng dùng Integer. Tiền dùng BigDecimal để không sai số; ngày/giờ dùng java.sql.Date, Time, Timestamp cho dễ làm JDBC.
Model chỉ lưu khóa ngoại dạng ID. CinemaMovie và BookingSeat có khóa chính ghép. Không dùng JPA hoặc Lombok.
Khi đọc Integer nullable từ JDBC, không dùng getInt đơn lẻ vì SQL NULL sẽ thành 0; dùng getObject hoặc kiểm tra wasNull.
Bảng User cần viết [User] trong SQL Server. Constructor đầy đủ được tạo để thuận tiện, thường nên dùng constructor rỗng và setter với model nhiều thuộc tính.

## Điểm cần thống nhất trong RDS

Database Schema dùng duration_minutes và poster_url; một số SQL minh họa dùng duration và poster. Model lấy Database Schema làm chuẩn: durationMinutes, posterUrl.
Schema có BookingSeat.reserved_at, Booking.reservation_expired_at và không có Ticket.showtime_id. Không tự thêm combo, đồ ăn, bảng OTP hoặc bảng phân quyền mới.
Độ dài cột, khóa, giá trị status và các ràng buộc phải đối chiếu script SQL thực tế khi nhóm cung cấp; model không tự bảo đảm constraint DB.

## Tài liệu để chia việc

- docs/STRUCTURE.txt: cây thư mục đầy đủ.
- docs/MODEL_MAPPING.md: đối chiếu từng cột với model.
- docs/UC_TASK_MAP.md: ánh xạ 24 use case tới file và gợi ý chia 6 người.
- docs/VALIDATION.md: phạm vi kiểm tra bản bàn giao.
