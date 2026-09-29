# Đối chiếu model với Database Schema trong RDS

Tên cột theo Database Schema đã thống nhất sau khi bỏ cơ chế giữ ghế 10 phút. Khóa ngoại lưu bằng ID để dễ học; không dùng ORM, Lombok hay kế thừa model.

## Role → Role.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| role_id | roleId | int |
| role_name | roleName | String |
| description | description | String |
| status | status | String |

## Cinema → Cinema.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| cinema_id | cinemaId | int |
| cinema_name | cinemaName | String |
| address | address | String |
| phone | phone | String |
| status | status | String |

## User → User.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| user_id | userId | int |
| role_id | roleId | int |
| cinema_id | cinemaId | Integer |
| full_name | fullName | String |
| email | email | String |
| phone | phone | String |
| password_hash | passwordHash | String |
| avatar_url | avatarUrl | String |
| status | status | String |
| created_at | createdAt | Timestamp |
| updated_at | updatedAt | Timestamp |

## Movie → Movie.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| movie_id | movieId | int |
| title | title | String |
| description | description | String |
| duration_minutes | durationMinutes | int |
| age_rating | ageRating | String |
| release_date | releaseDate | Date |
| end_date | endDate | Date |
| poster_url | posterUrl | String |
| status | status | String |
| created_at | createdAt | Timestamp |
| updated_at | updatedAt | Timestamp |

## Cinema_Movie → CinemaMovie.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| cinema_id | cinemaId | int |
| movie_id | movieId | int |
| status | status | String |

## Room → Room.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| room_id | roomId | int |
| cinema_id | cinemaId | int |
| room_name | roomName | String |
| capacity | capacity | int |
| status | status | String |

## Seat → Seat.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| seat_id | seatId | int |
| room_id | roomId | int |
| seat_number | seatNumber | String |
| seat_type | seatType | String |
| status | status | String |

## Pricing → Pricing.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| pricing_id | pricingId | int |
| cinema_id | cinemaId | int |
| seat_type | seatType | String |
| price | price | BigDecimal |
| start_date | startDate | Date |
| end_date | endDate | Date |
| status | status | String |

## Showtime → Showtime.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| showtime_id | showtimeId | int |
| room_id | roomId | int |
| movie_id | movieId | int |
| show_date | showDate | Date |
| start_time | startTime | Time |
| end_time | endTime | Time |
| status | status | String |

## Promotion → Promotion.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| promotion_id | promotionId | int |
| cinema_id | cinemaId | int |
| promo_code | promoCode | String |
| promotion_name | promotionName | String |
| discount_type | discountType | String |
| discount_value | discountValue | BigDecimal |
| start_date | startDate | Date |
| end_date | endDate | Date |
| usage_limit | usageLimit | Integer |
| status | status | String |

## Booking → Booking.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| booking_id | bookingId | int |
| customer_id | customerId | int |
| showtime_id | showtimeId | int |
| promotion_id | promotionId | Integer |
| booking_time | bookingTime | Timestamp |
| subtotal | subtotal | BigDecimal |
| discount_amount | discountAmount | BigDecimal |
| total_amount | totalAmount | BigDecimal |
| status | status | String |

> `reservation_expired_at` và bảng `Booking_Seat` không còn được dùng sau khi nhóm bỏ cơ chế giữ ghế 10 phút.

## Ticket → Ticket.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| ticket_id | ticketId | int |
| booking_id | bookingId | int |
| seat_id | seatId | int |
| ticket_code | ticketCode | String |
| ticket_price | ticketPrice | BigDecimal |
| status | status | String |
| checked_in_at | checkedInAt | Timestamp |

## Payment → Payment.java

| Cột SQL | Thuộc tính Java | Kiểu Java |
|---|---|---|
| payment_id | paymentId | int |
| booking_id | bookingId | int |
| payment_method | paymentMethod | String |
| transaction_code | transactionCode | String |
| amount | amount | BigDecimal |
| payment_time | paymentTime | Timestamp |
| status | status | String |
