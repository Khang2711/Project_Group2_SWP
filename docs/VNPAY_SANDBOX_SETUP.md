# Cấu hình và kiểm thử VNPay Sandbox

Tài liệu này dành cho UC-13 của F-Cinema. Mã nguồn đọc cấu hình từ biến môi trường; **không commit TMN Code hoặc Hash Secret lên GitHub**.

## 1. Biến môi trường

Cần thiết lập các biến sau trước khi mở NetBeans/Tomcat:

- **VNPAY_TMN_CODE**: Terminal/Merchant Code do VNPay Sandbox cấp.
- **VNPAY_HASH_SECRET**: Secret Key do VNPay Sandbox cấp.
- **VNPAY_RETURN_URL**: URL trình duyệt quay về, ví dụ **http://localhost:8080/FCinema/payment/vnpay-return**.

Biến tùy chọn:

- **VNPAY_PAYMENT_URL**: mặc định là **https://sandbox.vnpayment.vn/paymentv2/vpcpay.html**.

Trên Windows, có thể dùng PowerShell (thay giá trị mẫu bằng thông tin Sandbox của nhóm):

~~~powershell
setx VNPAY_TMN_CODE "YOUR_TMN_CODE"
setx VNPAY_HASH_SECRET "YOUR_HASH_SECRET"
setx VNPAY_RETURN_URL "http://localhost:8080/FCinema/payment/vnpay-return"
setx VNPAY_PAYMENT_URL "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
~~~

**setx** chỉ áp dụng cho tiến trình mới. Sau khi chạy các lệnh, đóng và mở lại NetBeans, sau đó restart Tomcat. Nếu ứng dụng có context path khác **/FCinema** hoặc Tomcat dùng port khác, sửa URL cho đúng. Không dùng placeholder làm giá trị thật.

## 2. Cấu hình IPN

Endpoint IPN đã được tạo tại:

**/payment/vnpay-ipn**

Trong trang cấu hình merchant Sandbox, đặt IPN URL thành URL HTTPS công khai trỏ tới endpoint trên, ví dụ:

**https://<public-host>/FCinema/payment/vnpay-ipn**

VNPay không thể gọi **localhost** trực tiếp từ máy chủ của họ. Khi thử nghiệm IPN trên máy cá nhân, cần dùng tunnel HTTPS được nhóm chấp thuận (ví dụ ngrok) và cập nhật IPN URL trong cấu hình Sandbox. URL return của trình duyệt và URL IPN là hai URL khác nhau.

## 3. Luồng xử lý

1. Booking và Ticket được tạo ở trạng thái **PENDING**.
2. PaymentService tạo bản ghi Payment **PENDING** và URL VNPay. Mỗi lần thử mới dùng **merchant_reference** riêng; link chờ quá 15 phút sẽ được thay bằng reference mới.
3. VNPay trả về **/payment/vnpay-return** hoặc gọi **/payment/vnpay-ipn**. Cả hai endpoint dùng chung quy trình xác minh.
4. Server xác minh **vnp_SecureHash** bằng HMAC-SHA512, đối chiếu **vnp_TmnCode**, **vnp_TxnRef**, tiền (**vnp_Amount** = số tiền DB × 100), **vnp_CurrCode**, **vnp_ResponseCode** và **vnp_TransactionStatus**.
5. Chỉ khi chữ ký và giao dịch hợp lệ, server mới mở transaction DB, khóa booking, kiểm tra lại xung đột ghế rồi cập nhật Payment/Booking/Ticket cùng nhau.
6. Callback lặp cùng mã giao dịch không phát hành vé lần nữa. Nếu VNPay đã thu tiền nhưng ghế không còn khả dụng hoặc phát hiện giao dịch thành công trùng, Payment được đánh dấu **SUCCESS** với **refund_status = 'REQUIRED'** để nhân viên xử lý; booking không được xác nhận thêm lần nữa.

Trạng thái mong đợi:

| Trường hợp | Payment | Booking | Ticket |
|---|---|---|---|
| Chữ ký hợp lệ, giao dịch thành công, ghế còn trống | **SUCCESS** | **CONFIRMED** | **VALID**, có mã vé |
| VNPay báo thất bại/hủy | **FAILED** | **PENDING** | **PENDING** |
| Thanh toán thành công nhưng ghế đã được bán | **SUCCESS**, **refund_status='REQUIRED'** | **CANCELLED** | **CANCELLED** |
| Chữ ký hoặc số tiền sai | Không cập nhật thành công | Không đổi | Không đổi |

## 4. Kiểm thử thủ công

1. **Cấu hình thiếu:** bỏ VNPAY_HASH_SECRET, restart Tomcat, bắt đầu thanh toán. Hệ thống phải báo chưa cấu hình và không xác nhận vé.
2. **Thanh toán thành công:** tạo booking mới, thanh toán trên Sandbox, kiểm tra Payment là SUCCESS, Booking là CONFIRMED, Ticket là VALID và ticket_code đã được tạo.
3. **Hủy/thất bại:** thử hủy hoặc dùng kịch bản thất bại do VNPay Sandbox hỗ trợ. Booking/Ticket không được chuyển sang trạng thái đã xác nhận.
4. **Callback lặp:** gửi lại đúng callback đã ký. Không được tạo thêm mã vé hoặc xử lý giao dịch lần hai.
5. **Chữ ký sai:** sửa vnp_Amount hoặc một tham số callback mà không tạo lại chữ ký. Endpoint phải từ chối xác minh và không cập nhật Booking/Ticket.
6. **Sai số tiền:** gửi callback có chữ ký hợp lệ nhưng số tiền không khớp với Payment. Không xác nhận Booking/Ticket.
7. **Tranh chấp ghế:** tạo hai booking PENDING cho cùng ghế/suất chiếu; chỉ giao dịch được xác minh đầu tiên có thể xác nhận ghế. Giao dịch còn lại cần xử lý hoàn tiền nếu VNPay đã thu tiền.

Các truy vấn đối chiếu nhanh trên SQL Server:

~~~sql
SELECT payment_id, booking_id, merchant_reference, transaction_code,
       amount, status, refund_status, payment_time
FROM dbo.Payment
ORDER BY payment_id DESC;

SELECT booking_id, customer_id, total_amount, status
FROM dbo.Booking
ORDER BY booking_id DESC;

SELECT ticket_id, booking_id, showtime_id, seat_id,
       ticket_code, status
FROM dbo.Ticket
ORDER BY ticket_id DESC;
~~~

## 5. Lưu ý triển khai

- Không xác nhận đơn dựa riêng vào việc người dùng quay về trang kết quả; chỉ dùng dữ liệu đã xác minh chữ ký và đối chiếu DB.
- IPN cần URL công khai để VNPay gọi được. Nếu callback lỗi DB, endpoint trả **RspCode=99** để VNPay có thể gửi lại.
- Hiện tại **refund_status='REQUIRED'** là cờ để nhân viên xử lý thủ công; luồng gọi API hoàn tiền chưa được triển khai trong UC-13.
- Tài liệu VNPay Pay API: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
