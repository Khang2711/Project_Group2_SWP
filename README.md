# FCinema MVC — Khung dự án PRJ

VNPay Sandbox setup and manual verification: [docs/VNPAY_SANDBOX_SETUP.md](docs/VNPAY_SANDBOX_SETUP.md)

Email verification OTP setup and database migration: [docs/EMAIL_VERIFICATION_SETUP.md](docs/EMAIL_VERIFICATION_SETUP.md)


## Cách mở bằng NetBeans

1. File → Open Project → chọn thư mục có pom.xml.
2. Chọn JDK 11 (hoặc JDK 17), thêm Apache Tomcat 10.1 vào Servers.
3. Project Properties → Run → chọn Tomcat, context path /FCinema.
4. Đợi Maven tải dependencies, Clean and Build rồi Run.
5. Mở http://localhost:8080/FCinema/home để xem các trang khung. Trang này không cần DB.

Dòng lệnh tương đương: `mvn clean package`, sau đó chép target/FCinema.war vào thư mục webapps của Tomcat 10.1 và khởi động server.
Không dùng Tomcat 9 cho cấu hình jakarta này. Không trộn javaee-web-api javax với Jakarta.

