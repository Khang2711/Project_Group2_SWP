# SWT301 Unit Test – Phần 2A (Vỹ)

## Hai method được kiểm thử

1. `service.PromotionService.calculateDiscount()`
2. `service.CheckInService.validateTicketCode()`

## Cấu hình

- Java 11
- Maven
- JUnit Jupiter 5.10.2
- Maven Surefire 3.2.5
- JaCoCo 0.8.12

## Chạy toàn bộ Unit Test của Vỹ

Từ thư mục gốc project:

```bash
mvn clean test -Dtest=PromotionServiceTest,CheckInServiceTest
```

Hoặc chạy riêng từng class:

```bash
mvn test -Dtest=PromotionServiceTest
mvn test -Dtest=CheckInServiceTest
```

## Xem coverage report

Sau khi lệnh Maven chạy thành công, mở:

```
target/site/jacoco/index.html
```

Trong report:
- **Line coverage**: mức bao phủ câu lệnh/dòng lệnh.
- **Branch coverage**: mức bao phủ các nhánh điều kiện true/false.

Không ghi Pass/Fail hoặc phần trăm coverage vào báo cáo nộp bài trước khi thực sự chạy test trên project.

## Lưu ý về database

`calculateDiscount()` và `validateTicketCode()` không gọi repository trong phần logic đang kiểm thử. `PromotionService` đã được điều chỉnh để chỉ khởi tạo `ShowtimeRepository` khi thật sự cần tra cứu rạp theo suất chiếu; do đó bài test tính giảm giá không cần SQL Server.

