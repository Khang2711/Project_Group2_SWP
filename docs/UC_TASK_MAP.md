# Bản đồ chức năng và file

Các file hiện là khung, chưa hoàn thành use case. Một controller có thể mở nhiều trang.

| UC | Chức năng | Controller / Service | JSP trong WEB-INF/views |
|---|---|---|---|
| 01 | Đăng nhập | AuthController / AuthService | auth/login.jsp |
| 02 | Đăng xuất | AuthController / AuthService | auth/logout.jsp |
| 18 | Đăng ký | AuthController / AuthService | auth/register.jsp |
| 20 | Quên mật khẩu | AuthController / AuthService | auth/forgot-password.jsp |
| 20 | Đặt lại mật khẩu | AuthController / AuthService | auth/reset-password.jsp |
| 19 | Thông tin cá nhân | ProfileController / ProfileService | user/profile.jsp |
| 23 | Sửa thông tin cá nhân | ProfileController / ProfileService | user/edit-profile.jsp |
| 10 | Danh sách phim | MovieController / MovieService | user/movies.jsp |
| 24 | Chi tiết phim | MovieController / MovieService | user/movie-detail.jsp |
| 11 | Lịch chiếu | ShowtimeController / ShowtimeService | user/showtimes.jsp |
| 12 | Chọn ghế hoặc đặt toàn phòng | BookingController / BookingService | user/select-seats.jsp |
| 12 | Xác nhận đặt vé | BookingController / BookingService | user/checkout.jsp |
| 22 | Áp dụng mã giảm giá | PromotionController / PromotionService | user/apply-promo.jsp |
| 13 | Thanh toán VNPay | PaymentController / PaymentService | user/payment.jsp |
| 13 | Kết quả thanh toán | PaymentController / PaymentService | user/payment-result.jsp |
| 14 | Đơn đặt vé | BookingController / BookingService | user/bookings.jsp |
| 14 | Chi tiết đơn đặt vé | BookingController / BookingService | user/booking-detail.jsp |
| 15 | Vé của tôi | TicketController / TicketService | user/tickets.jsp |
| 15 | Chi tiết vé | TicketController / TicketService | user/ticket-detail.jsp |
| 03 | Quản lý tài khoản | UserController / UserService | system/users.jsp |
| 03 | Tạo hoặc sửa tài khoản | UserController / UserService | system/user-form.jsp |
| 04 | Quản lý phim | MovieController / MovieService | admin/movies.jsp |
| 04 | Thêm hoặc sửa phim | MovieController / MovieService | admin/movie-form.jsp |
| 05 | Quản lý rạp | CinemaController / CinemaService | admin/cinemas.jsp |
| 05 | Thêm hoặc sửa rạp | CinemaController / CinemaService | admin/cinema-form.jsp |
| 06 | Quản lý phòng | RoomController / RoomService | admin/rooms.jsp |
| 06 | Thêm hoặc sửa phòng | RoomController / RoomService | admin/room-form.jsp |
| 07 | Quản lý ghế | SeatController / SeatService | admin/seats.jsp |
| 07 | Cấu hình ghế | SeatController / SeatService | admin/seat-form.jsp |
| 08 | Quản lý giá vé | PricingController / PricingService | admin/pricing.jsp |
| 08 | Thêm hoặc sửa giá | PricingController / PricingService | admin/pricing-form.jsp |
| 09 | Quản lý lịch chiếu | ShowtimeController / ShowtimeService | admin/showtimes.jsp |
| 09 | Thêm hoặc sửa lịch chiếu | ShowtimeController / ShowtimeService | admin/showtime-form.jsp |
| 21 | Quản lý khuyến mãi | PromotionController / PromotionService | admin/promotions.jsp |
| 21 | Thêm hoặc sửa khuyến mãi | PromotionController / PromotionService | admin/promotion-form.jsp |
| 17 | Thống kê hoạt động | DashboardController / DashboardService | admin/dashboard.jsp |
| 16 | Kiểm tra và check-in vé | CheckInController / CheckInService | staff/check-in.jsp |

## Gợi ý chia 6 người theo lịch sử phụ trách trong RDS

| Thành viên | Phạm vi gợi ý | Điểm phối hợp |
|---|---|---|
| Thịnh | UC-01,02,18,19,20,23; Auth/Profile; User/Role repository | Thống nhất session và mã vai trò với Vy |
| Hoàng | UC-04–09; quản lý phim/rạp/phòng/ghế/giá/lịch chiếu | Dùng chung Movie/Showtime với Kiệt |
| Kiệt | UC-10,11,24; danh sách phim, chi tiết, lịch chiếu khách hàng | Hoàng phụ trách ghi dữ liệu; Kiệt phụ trách đọc |
| Hào | UC-12–15,22; đặt chỗ, VNPay, booking, vé | Vy cung cấp kiểm tra promotion; ticket chia sẻ với check-in |
| Vy | UC-03,16,17,21; tài khoản, check-in, dashboard, promotion | Phối hợp Thịnh về quyền và Hào về trạng thái vé |
| Khang | Tích hợp, security filters, kiểm thử luồng, tài liệu và rà soát RDS | Đây là phân công gợi ý, không phải phân công có sẵn trong RDS |

UC-22 và UC-23 được gợi ý ghép theo luồng để giảm giao tiếp chéo; nhóm có thể giữ người cũ nếu cần.
Mỗi task hoàn thành gồm: JSP → controller → service → repository → kiểm tra DB và quyền.
Không đồng thời sửa một file mà chưa thống nhất nhánh và phần chịu trách nhiệm.

## Thứ tự triển khai

1. Thống nhất SQL thật và model; kiểm tra kết nối.
2. Đăng nhập, session, phân quyền; danh mục rạp/phim/phòng/ghế.
3. Giá vé, lịch chiếu, khuyến mãi và trang khách hàng.
4. Đặt chỗ có transaction, giữ ghế 10 phút, không bán trùng ghế cùng suất.
5. VNPay: xác thực callback, xử lý lặp an toàn, phát hành vé khi thanh toán thành công.
6. Check-in, lịch sử, thống kê và kiểm thử toàn luồng.

Đặt toàn phòng dùng Booking + BookingSeat cho toàn bộ ghế hợp lệ; không tự thêm bảng mới.
BookingSeat giữ lịch sử RESERVED/SOLD/RELEASED. Ticket lấy suất chiếu qua Booking.
Payment có thể có nhiều giao dịch cho một Booking. Dashboard chỉ tính doanh thu hợp lệ.
