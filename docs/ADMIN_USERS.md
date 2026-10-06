# UC-03: Quản lý tài khoản

Triển khai tiếp trên main `426a929877a2b46f8b4ae5666bd007259df02f82`.

## Chạy

1. Giữ cấu hình SQL Server của nhóm trong `db.DBContext`, DB `FCinemaDB`.
2. Clean and Build bằng Maven/NetBeans, chạy Tomcat 10.1 (POM hiện dùng Servlet 6), JDK 11 trở lên.
3. Đăng nhập tài khoản ADMIN bằng trang đăng nhập hiện có. AuthController tự chuyển tới `/system/users`.
4. Chọn **Tạo tài khoản**, hoặc **Xem / Sửa**, **Vô hiệu hóa** trên danh sách.

Không cần đổi schema hay chạy lại script tạo DB. Header/footer, AuthController, AuthService và menu theo vai trò hiện tại được giữ nguyên.

## Quy tắc

- ADMIN/CUSTOMER không gán chi nhánh. STAFF/CINEMA_MANAGER bắt buộc có chi nhánh.
- Role lấy từ DB, không hardcode role_id. Chỉ dùng vai trò ACTIVE.
- Chi nhánh lưu trong Staff.cinema_id, không thêm cột vào User. Chỉ gán mới vào rạp ACTIVE; cho phép giữ chi nhánh cũ đã ngừng hoạt động.
- User.roleName và cinemaName là dữ liệu hiển thị từ JOIN.
- Tạo/sửa User và bản ghi Customer/Staff chạy cùng transaction. Rollback nếu lỗi.
- Giữ bản ghi Customer/Staff lịch sử khi chuyển vai trò vì Booking/Food_Order có khóa ngoại. Quyền hiện tại luôn dựa trên User.role_id.
- Mật khẩu dùng PasswordUtil.md5 hiện có. Khi sửa, để trống để giữ mật khẩu cũ.
- Vô hiệu hóa dùng trạng thái INACTIVE; không xóa dữ liệu. Có thể kích hoạt lại qua form sửa.
- Không cho admin tự vô hiệu hóa hoặc tự gỡ quyền.
- AuthFilter đọc lại User/Role từ DB cho các trang cần đăng nhập, cập nhật session user/roleName. Tài khoản bị khóa bị hủy session ở request được bảo vệ tiếp theo; thay đổi vai trò/chi nhánh có hiệu lực mà không phải đăng xuất thủ công. Trang công khai vẫn truy cập bình thường.
- Không giữ password_hash trong session. POST cần CSRF token; SQL dùng PreparedStatement; dữ liệu JSP dùng c:out.

## Cấu trúc

- UserController: nhận request, kiểm tra quyền/token, mở JSP.
- UserService: kiểm tra dữ liệu và quy tắc tài khoản.
- UserRepository: truy vấn, lưu User/Customer/Staff; giữ findByEmail cho phần đăng nhập hiện tại.
- system/users.jsp và system/user-form.jsp: danh sách, tạo/xem/sửa.
- assets/css/users.css: giao diện riêng cho trang quản lý, dùng header/footer chung.
- security/AuthFilter: cập nhật quyền/trạng thái từ DB trước khi cho truy cập.

## Kiểm tra

Đã build toàn bộ project bằng Maven; biên dịch hai JSP với Jasper Tomcat 10.1 (0 lỗi); chạy 26 kiểm tra service và 12 kiểm tra filter thành công. Filter được kiểm tra với session cũ sau khi bị khóa/đổi vai trò, lỗi DB, khách chưa đăng nhập và cả 4 vai trò.

Chưa kết nối SQL Server thật của nhóm. Cần kiểm tra trực tiếp: tạo CUSTOMER/STAFF/MANAGER; trùng email; đổi vai trò; giữ/đổi mật khẩu; khóa/kích hoạt; dữ liệu lịch sử; hai session khi một admin khóa hoặc đổi quyền tài khoản còn lại.

Để chạy các kiểm tra không dùng DB sau `mvn package` (Windows; thay đường dẫn Servlet JAR theo máy):

```bat
mkdir test-classes
javac --release 11 -encoding UTF-8 -cp "target/classes;%USERPROFILE%/.m2/repository/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar" -d test-classes tests/UserServiceTest.java tests/AuthFilterTest.java
java -cp "test-classes;target/classes" UserServiceTest
java -cp "test-classes;target/classes;%USERPROFILE%/.m2/repository/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar" security.AuthFilterTest
```

Repository giả chỉ dùng trong tests; không có đường tắt đăng nhập admin trong ứng dụng.
