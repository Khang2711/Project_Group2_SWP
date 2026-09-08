# F-Cinema — Group 2

Dự án web đặt vé xem phim, quản lý phim, phòng chiếu, suất chiếu và báo cáo.

## Cấu trúc

- `pom.xml`: cấu hình Maven và thư viện.
- `src/main/java/controller/`: Servlet xử lý yêu cầu.
- `src/main/java/dao/`: truy vấn SQL Server.
- `src/main/java/model/`: các đối tượng dữ liệu.
- `src/main/java/filter/`: kiểm tra quyền truy cập.
- `src/main/java/utils/`: kết nối database và tiện ích.
- `src/main/resources/`: cấu hình mẫu; tự tạo cấu hình local theo hướng dẫn.
- `src/main/webapp/`: JSP, CSS, cấu hình web.
- `database/01_schema.sql`: tạo 8 bảng và khóa ngoại.
- `database/02_seed.sql`: dữ liệu demo.
- `database/legacy/`: bản cập nhật cho database cũ, không cần chạy khi cài mới.
- `docs/GITHUB_GUIDE.md`: đưa code lên GitHub và làm việc nhóm.
- `.gitignore`: loại file build và cấu hình riêng khỏi Git.

## 1. Môi trường

- NetBeans có hỗ trợ Maven/Java Web, Maven hoặc Maven đi kèm IDE.
- JDK 11 trở lên; `pom.xml` đặt mức biên dịch Java 11.
- Server hỗ trợ Jakarta Servlet 6.0, ví dụ Tomcat 10.1. Dự án dùng `jakarta`, không dùng Tomcat 9.
- SQL Server và SSMS để chạy SQL. JDBC driver được Maven tải từ `pom.xml`.
- SQL Server cho phép SQL Server Authentication, TCP/IP; kiểm tra đúng cổng của máy (mẫu dùng 1433).

## 2. Tạo database

**Máy chưa có database:** mở SSMS, kết nối SQL Server; chạy `database/01_schema.sql` trước, sau đó `database/02_seed.sql`. Mỗi file chỉ chạy một lần trên database mới. Script tạo bảng không xóa dữ liệu cũ; chạy lại sẽ báo trùng bảng/khóa.

**Máy đã có FCinemaDB:** không chạy lại hai file trên. Sao lưu DB trước khi cập nhật. `database/legacy/ADD_MOVIE_IMAGE.sql` chỉ bổ sung imageURL nếu thiếu. File `UPDATE_EXISTING_DATABASE_20260716.sql` đổi mật khẩu demo cũ và thông tin/giá combo; đọc nội dung trước khi chạy. Database cũ phải có đủ bảng/cột mà code yêu cầu.

Dữ liệu demo giữ nguyên từ dự án gốc. Đăng nhập web: `admin / 123456` (quản trị), `khang / 123456`, `user01 / 123456` (khách hàng). Đây là tài khoản web, không phải tài khoản SQL Server. Ngày chiếu mẫu được tạo tương đối theo ngày chạy seed; nếu hết lịch, thêm suất chiếu trong quản trị.

## 3. Cấu hình kết nối riêng cho từng máy

1. Sao chép `src/main/resources/db.example.properties` thành `src/main/resources/db.local.properties`.
2. Mở file local, sửa `db.url`, `db.user`, `db.password` theo SQL Server trên máy mình. Thay `CHANGE_ME` bằng mật khẩu SQL Server.
3. Nếu dùng cổng khác, sửa `localhost:1433`. Không thay bằng URL của website.
4. File local được `.gitignore` loại khỏi Git, mỗi thành viên phải tự tạo sau khi clone.
5. Cấu hình dùng định dạng Java Properties: nếu giá trị có dấu gạch chéo ngược, phải viết hai dấu gạch chéo ngược. Cấu hình được đóng gói vào WAR khi build local; không chia sẻ WAR chứa mật khẩu.

## 4. Mở và chạy

1. NetBeans → File → Open Project → chọn thư mục `FCinema` có `pom.xml`.
2. Chọn JDK và server Tomcat tương thích trong Properties/Run theo môi trường IDE.
3. Clean and Build, rồi Run. Maven cần mạng lần đầu để tải thư viện.
4. Mở URL NetBeans báo khi deploy. Nếu context là `/F-Cinema1` và HTTP port 8080: `http://localhost:8080/F-Cinema1/auth`. Context thực tế có thể theo tên WAR `F-Cinema1-1.0-SNAPSHOT`; xem log server.
5. Đăng nhập và thử danh sách phim, thêm/sửa/xóa, đặt vé, báo cáo.

Có thể build bằng `mvn clean package` ở thư mục có `pom.xml`, sau đó deploy `target/F-Cinema1-1.0-SNAPSHOT.war` lên server. Thay cấu hình DB xong phải build/deploy lại.

## 5. Lỗi thường gặp

- Missing db.local.properties: tạo file đúng vị trí, Clean and Build lại.
- Login failed: kiểm tra tài khoản SQL Server và chế độ Authentication.
- Connection refused/timeout: kiểm tra dịch vụ SQL Server, TCP/IP và cổng.
- Invalid object name: chưa tạo bảng hoặc đang kết nối nhầm database.
- Lỗi jakarta/Servlet khi deploy: kiểm tra phiên bản server.

## Thay đổi khi sắp xếp

Giữ Maven/MVC, JSP, CSS, tên artifact và cấu hình web. Loại thư mục `target` chứa WAR/class sinh tự động. Tách script SQL gốc thành schema và seed, bỏ lệnh DROP TABLE để không đưa thao tác xóa bảng vào luồng cài mới. Hai script cập nhật cũ được chuyển vào `database/legacy`. DBUtils giữ nguyên chữ ký phương thức, chuyển thông tin kết nối hard-code sang file local. Không chỉnh nghiệp vụ.

Đã kiểm tra cấu trúc, tính toàn vẹn code/tài nguyên giữ lại và phần SQL được tách. Chưa build hoặc chạy CRUD thực tế vì môi trường xử lý không có JDK/Maven, SQL Server và Tomcat. Nhóm cần chạy kiểm tra tại máy trước khi merge.
