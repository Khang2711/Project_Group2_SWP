/*
    FCinemaDB - Basic seed data
    Run AFTER database/FCinemaDB_schema.sql

    Demo login note:
    - Demo users below share the same BCrypt hash commonly used for password: "password".
    - If the project uses a different password encoder, replace password_hash accordingly.

    This script uses IF NOT EXISTS where practical so it can be re-run without
    duplicating most reference/demo data.
*/

USE FCinemaDB;
GO

SET NOCOUNT ON;
GO

/* =========================================================
   01. Roles
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'ADMIN')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('ADMIN', N'System administrator', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'STAFF')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('STAFF', N'Cinema staff member', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'CUSTOMER')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('CUSTOMER', N'Cinema customer', 'ACTIVE');
GO

/* =========================================================
   02. Cinemas
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều')
    INSERT INTO dbo.Cinema(cinema_name, address, phone, status)
    VALUES (N'F-Cinema Ninh Kiều', N'01 Hòa Bình, Ninh Kiều, Cần Thơ', '02923730001', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng')
    INSERT INTO dbo.Cinema(cinema_name, address, phone, status)
    VALUES (N'F-Cinema Cái Răng', N'99 Võ Nguyên Giáp, Cái Răng, Cần Thơ', '02923730002', 'ACTIVE');
GO

/* =========================================================
   03. Users + Customer/Staff profiles
   ========================================================= */
DECLARE @AdminRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'ADMIN');
DECLARE @StaffRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'STAFF');
DECLARE @CustomerRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'CUSTOMER');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');
DECLARE @DemoPasswordHash VARCHAR(255) = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'admin@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@AdminRoleId, N'FCinema Admin', 'admin@fcinema.local', @DemoPasswordHash, '0900000001', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'staff1@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@StaffRoleId, N'Nguyễn Minh Staff', 'staff1@fcinema.local', @DemoPasswordHash, '0900000002', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'staff2@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@StaffRoleId, N'Trần Anh Staff', 'staff2@fcinema.local', @DemoPasswordHash, '0900000003', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'customer1@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@CustomerRoleId, N'Lê Minh Khách', 'customer1@fcinema.local', @DemoPasswordHash, '0900000004', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'customer2@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@CustomerRoleId, N'Phạm Ngọc Khách', 'customer2@fcinema.local', @DemoPasswordHash, '0900000005', 'ACTIVE');

DECLARE @Staff1Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'staff1@fcinema.local');
DECLARE @Staff2Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'staff2@fcinema.local');
DECLARE @Customer1Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'customer1@fcinema.local');
DECLARE @Customer2Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'customer2@fcinema.local');

IF NOT EXISTS (SELECT 1 FROM dbo.Staff WHERE staff_id = @Staff1Id)
    INSERT INTO dbo.Staff(staff_id, cinema_id) VALUES (@Staff1Id, @Cinema1Id);

IF NOT EXISTS (SELECT 1 FROM dbo.Staff WHERE staff_id = @Staff2Id)
    INSERT INTO dbo.Staff(staff_id, cinema_id) VALUES (@Staff2Id, @Cinema2Id);

IF NOT EXISTS (SELECT 1 FROM dbo.Customer WHERE customer_id = @Customer1Id)
    INSERT INTO dbo.Customer(customer_id) VALUES (@Customer1Id);

IF NOT EXISTS (SELECT 1 FROM dbo.Customer WHERE customer_id = @Customer2Id)
    INSERT INTO dbo.Customer(customer_id) VALUES (@Customer2Id);
GO

/* =========================================================
   04. Rooms
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 1')
    INSERT INTO dbo.Room(cinema_id, room_name, capacity, room_type, status)
    VALUES (@Cinema1Id, N'Room 1', 10, '2D', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 2')
    INSERT INTO dbo.Room(cinema_id, room_name, capacity, room_type, status)
    VALUES (@Cinema1Id, N'Room 2', 10, '2D', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id = @Cinema2Id AND room_name = N'Room 1')
    INSERT INTO dbo.Room(cinema_id, room_name, capacity, room_type, status)
    VALUES (@Cinema2Id, N'Room 1', 10, '2D', 'ACTIVE');
GO

/* =========================================================
   05. Seats - 2 rows x 5 seats for each demo room
   ========================================================= */
DECLARE @RoomId INT;
DECLARE @RowNo INT;
DECLARE @SeatNo INT;

DECLARE room_cursor CURSOR LOCAL FAST_FORWARD FOR
    SELECT room_id FROM dbo.Room
    WHERE room_name IN (N'Room 1', N'Room 2')
      AND cinema_id IN (
          SELECT cinema_id FROM dbo.Cinema
          WHERE cinema_name IN (N'F-Cinema Ninh Kiều', N'F-Cinema Cái Răng')
      );

OPEN room_cursor;
FETCH NEXT FROM room_cursor INTO @RoomId;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @RowNo = 1;
    WHILE @RowNo <= 2
    BEGIN
        SET @SeatNo = 1;
        WHILE @SeatNo <= 5
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM dbo.Seat
                WHERE room_id = @RoomId AND seat_row = @RowNo AND seat_number = @SeatNo
            )
            BEGIN
                INSERT INTO dbo.Seat(room_id, seat_row, seat_number, seat_type, status)
                VALUES (
                    @RoomId,
                    @RowNo,
                    @SeatNo,
                    CASE WHEN @RowNo = 2 THEN 'VIP' ELSE 'STANDARD' END,
                    'ACTIVE'
                );
            END;
            SET @SeatNo += 1;
        END;
        SET @RowNo += 1;
    END;

    FETCH NEXT FROM room_cursor INTO @RoomId;
END;

CLOSE room_cursor;
DEALLOCATE room_cursor;
GO

/* =========================================================
   06. Pricing
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');

IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id = @Cinema1Id AND seat_type = 'STANDARD' AND start_date = '2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id, seat_type, price, start_date, end_date, status)
    VALUES (@Cinema1Id, 'STANDARD', 80000, '2026-01-01', NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id = @Cinema1Id AND seat_type = 'VIP' AND start_date = '2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id, seat_type, price, start_date, end_date, status)
    VALUES (@Cinema1Id, 'VIP', 100000, '2026-01-01', NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id = @Cinema2Id AND seat_type = 'STANDARD' AND start_date = '2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id, seat_type, price, start_date, end_date, status)
    VALUES (@Cinema2Id, 'STANDARD', 75000, '2026-01-01', NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id = @Cinema2Id AND seat_type = 'VIP' AND start_date = '2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id, seat_type, price, start_date, end_date, status)
    VALUES (@Cinema2Id, 'VIP', 95000, '2026-01-01', NULL, 'ACTIVE');
GO

/* =========================================================
   07. Genres + Movies
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Action')
    INSERT INTO dbo.Genre(genre_name, description, status)
    VALUES (N'Action', N'Action and adventure movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Comedy')
    INSERT INTO dbo.Genre(genre_name, description, status)
    VALUES (N'Comedy', N'Comedy movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Animation')
    INSERT INTO dbo.Genre(genre_name, description, status)
    VALUES (N'Animation', N'Animated movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Drama')
    INSERT INTO dbo.Genre(genre_name, description, status)
    VALUES (N'Drama', N'Drama movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'The Last Horizon')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'The Last Horizon', N'A demo action movie for FCinema testing.', 125, 'T13', '2026-09-20', '2026-12-31', NULL, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'Robot Adventures')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'Robot Adventures', N'A family-friendly animated demo movie.', 98, 'P', '2026-09-15', '2026-12-31', NULL, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'Can Tho After Rain')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'Can Tho After Rain', N'A fictional drama used as sample data.', 110, 'T13', '2026-09-25', '2026-12-31', NULL, NULL, 'ACTIVE');
GO

/* =========================================================
   08. Movie_Genre + Cinema_Movie
   ========================================================= */
DECLARE @Movie1Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'The Last Horizon');
DECLARE @Movie2Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'Robot Adventures');
DECLARE @Movie3Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'Can Tho After Rain');
DECLARE @ActionId INT = (SELECT genre_id FROM dbo.Genre WHERE genre_name = N'Action');
DECLARE @ComedyId INT = (SELECT genre_id FROM dbo.Genre WHERE genre_name = N'Comedy');
DECLARE @AnimationId INT = (SELECT genre_id FROM dbo.Genre WHERE genre_name = N'Animation');
DECLARE @DramaId INT = (SELECT genre_id FROM dbo.Genre WHERE genre_name = N'Drama');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id = @Movie1Id AND genre_id = @ActionId)
    INSERT INTO dbo.Movie_Genre(movie_id, genre_id) VALUES (@Movie1Id, @ActionId);

IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id = @Movie2Id AND genre_id = @AnimationId)
    INSERT INTO dbo.Movie_Genre(movie_id, genre_id) VALUES (@Movie2Id, @AnimationId);

IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id = @Movie2Id AND genre_id = @ComedyId)
    INSERT INTO dbo.Movie_Genre(movie_id, genre_id) VALUES (@Movie2Id, @ComedyId);

IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id = @Movie3Id AND genre_id = @DramaId)
    INSERT INTO dbo.Movie_Genre(movie_id, genre_id) VALUES (@Movie3Id, @DramaId);

IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id = @Cinema1Id AND movie_id = @Movie1Id)
    INSERT INTO dbo.Cinema_Movie(cinema_id, movie_id, status) VALUES (@Cinema1Id, @Movie1Id, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id = @Cinema1Id AND movie_id = @Movie2Id)
    INSERT INTO dbo.Cinema_Movie(cinema_id, movie_id, status) VALUES (@Cinema1Id, @Movie2Id, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id = @Cinema1Id AND movie_id = @Movie3Id)
    INSERT INTO dbo.Cinema_Movie(cinema_id, movie_id, status) VALUES (@Cinema1Id, @Movie3Id, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id = @Cinema2Id AND movie_id = @Movie1Id)
    INSERT INTO dbo.Cinema_Movie(cinema_id, movie_id, status) VALUES (@Cinema2Id, @Movie1Id, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id = @Cinema2Id AND movie_id = @Movie2Id)
    INSERT INTO dbo.Cinema_Movie(cinema_id, movie_id, status) VALUES (@Cinema2Id, @Movie2Id, 'ACTIVE');
GO

/* =========================================================
   09. Showtimes
   ========================================================= */
DECLARE @Movie1Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'The Last Horizon');
DECLARE @Movie2Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'Robot Adventures');
DECLARE @Movie3Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'Can Tho After Rain');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');
DECLARE @C1Room1 INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 1');
DECLARE @C1Room2 INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 2');
DECLARE @C2Room1 INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema2Id AND room_name = N'Room 1');

IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @C1Room1 AND start_time = '2026-10-05T10:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie1Id, @C1Room1, '2026-10-05T10:00:00', '2026-10-05T12:05:00', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @C1Room1 AND start_time = '2026-10-05T14:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie2Id, @C1Room1, '2026-10-05T14:00:00', '2026-10-05T15:38:00', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @C1Room2 AND start_time = '2026-10-05T18:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie3Id, @C1Room2, '2026-10-05T18:00:00', '2026-10-05T19:50:00', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @C2Room1 AND start_time = '2026-10-05T19:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie1Id, @C2Room1, '2026-10-05T19:00:00', '2026-10-05T21:05:00', 'ACTIVE');
GO

/* =========================================================
   10. Promotions
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Promotion WHERE promo_code = 'WELCOME20')
    INSERT INTO dbo.Promotion(
        promo_code, description, discount_type, discount_value,
        min_order_amount, max_discount_amount, start_date, end_date, usage_limit, status
    )
    VALUES (
        'WELCOME20', N'Giảm 20.000đ cho đơn từ 100.000đ', 'FIXED', 20000,
        100000, 20000, '2026-09-01T00:00:00', '2026-12-31T23:59:59', 1000, 'ACTIVE'
    );

IF NOT EXISTS (SELECT 1 FROM dbo.Promotion WHERE promo_code = 'STUDENT10')
    INSERT INTO dbo.Promotion(
        promo_code, description, discount_type, discount_value,
        min_order_amount, max_discount_amount, start_date, end_date, usage_limit, status
    )
    VALUES (
        'STUDENT10', N'Giảm 10% tối đa 30.000đ', 'PERCENT', 10,
        80000, 30000, '2026-09-01T00:00:00', '2026-12-31T23:59:59', 500, 'ACTIVE'
    );
GO

/* =========================================================
   11. Sample confirmed booking + payment + VALID tickets
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Payment WHERE merchant_reference = 'DEMO-PAY-001')
BEGIN
    DECLARE @Customer1Id INT = (
        SELECT u.user_id FROM dbo.[User] u WHERE u.email = 'customer1@fcinema.local'
    );
    DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
    DECLARE @Room1Id INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 1');
    DECLARE @Showtime1Id INT = (
        SELECT showtime_id FROM dbo.Showtime
        WHERE room_id = @Room1Id AND start_time = '2026-10-05T10:00:00'
    );
    DECLARE @PromoId INT = (SELECT promotion_id FROM dbo.Promotion WHERE promo_code = 'WELCOME20');
    DECLARE @SeatA INT = (SELECT seat_id FROM dbo.Seat WHERE room_id = @Room1Id AND seat_row = 1 AND seat_number = 1);
    DECLARE @SeatB INT = (SELECT seat_id FROM dbo.Seat WHERE room_id = @Room1Id AND seat_row = 1 AND seat_number = 2);
    DECLARE @BookingId INT;

    INSERT INTO dbo.Booking(
        customer_id, showtime_id, promotion_id, subtotal, discount_amount,
        total_amount, booking_time, status
    )
    VALUES (
        @Customer1Id, @Showtime1Id, @PromoId, 160000, 20000,
        140000, '2026-09-30T10:00:00', 'CONFIRMED'
    );

    SET @BookingId = SCOPE_IDENTITY();

    INSERT INTO dbo.Ticket(booking_id, showtime_id, seat_id, ticket_code, ticket_price, status)
    VALUES
        (@BookingId, @Showtime1Id, @SeatA, 'DEMO-TICKET-001', 80000, 'VALID'),
        (@BookingId, @Showtime1Id, @SeatB, 'DEMO-TICKET-002', 80000, 'VALID');

    INSERT INTO dbo.Payment(
        booking_id, payment_method, merchant_reference, transaction_code,
        amount, payment_time, status
    )
    VALUES (
        @BookingId, 'VNPAY', 'DEMO-PAY-001', 'VNPAY-DEMO-001',
        140000, '2026-09-30T10:02:00', 'SUCCESS'
    );
END;
GO

/* =========================================================
   12. Sample PENDING booking
   Demonstrates the rule: PENDING ticket does NOT reserve seat.
   ========================================================= */
DECLARE @Customer2Id INT = (
    SELECT u.user_id FROM dbo.[User] u WHERE u.email = 'customer2@fcinema.local'
);
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Room1Id INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 1');
DECLARE @PendingShowtimeId INT = (
    SELECT showtime_id FROM dbo.Showtime
    WHERE room_id = @Room1Id AND start_time = '2026-10-05T14:00:00'
);

IF NOT EXISTS (
    SELECT 1 FROM dbo.Booking
    WHERE customer_id = @Customer2Id
      AND showtime_id = @PendingShowtimeId
      AND status = 'PENDING'
)
BEGIN
    DECLARE @PendingSeatId INT = (
        SELECT seat_id FROM dbo.Seat
        WHERE room_id = @Room1Id AND seat_row = 2 AND seat_number = 1
    );
    DECLARE @PendingBookingId INT;

    INSERT INTO dbo.Booking(
        customer_id, showtime_id, promotion_id, subtotal, discount_amount,
        total_amount, booking_time, status
    )
    VALUES (
        @Customer2Id, @PendingShowtimeId, NULL, 100000, 0,
        100000, '2026-09-30T11:00:00', 'PENDING'
    );

    SET @PendingBookingId = SCOPE_IDENTITY();

    INSERT INTO dbo.Ticket(
        booking_id, showtime_id, seat_id, ticket_code, ticket_price, status
    )
    VALUES (
        @PendingBookingId, @PendingShowtimeId, @PendingSeatId, NULL, 100000, 'PENDING'
    );
END;
GO

/* =========================================================
   13. Food items
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Bắp rang bơ')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, image_url, status)
    VALUES (@Cinema1Id, N'Bắp rang bơ', 'POPCORN', N'Bắp rang bơ cỡ vừa', 45000, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Coca Cola')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, image_url, status)
    VALUES (@Cinema1Id, N'Coca Cola', 'DRINK', N'Nước ngọt Coca Cola cỡ vừa', 30000, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Nước suối')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, image_url, status)
    VALUES (@Cinema1Id, N'Nước suối', 'DRINK', N'Nước suối 500ml', 20000, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema2Id AND item_name = N'Bắp rang bơ')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, image_url, status)
    VALUES (@Cinema2Id, N'Bắp rang bơ', 'POPCORN', N'Bắp rang bơ cỡ vừa', 45000, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema2Id AND item_name = N'Coca Cola')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, image_url, status)
    VALUES (@Cinema2Id, N'Coca Cola', 'DRINK', N'Nước ngọt Coca Cola cỡ vừa', 30000, NULL, 'ACTIVE');
GO

/* =========================================================
   14. Combos + Combo_Item
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');

IF NOT EXISTS (SELECT 1 FROM dbo.Combo WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple')
    INSERT INTO dbo.Combo(cinema_id, combo_name, description, combo_price, image_url, status)
    VALUES (@Cinema1Id, N'Combo Couple', N'1 bắp + 2 nước ngọt', 95000, NULL, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Combo WHERE cinema_id = @Cinema2Id AND combo_name = N'Combo Couple')
    INSERT INTO dbo.Combo(cinema_id, combo_name, description, combo_price, image_url, status)
    VALUES (@Cinema2Id, N'Combo Couple', N'1 bắp + 2 nước ngọt', 95000, NULL, 'ACTIVE');

DECLARE @Combo1Id INT = (SELECT combo_id FROM dbo.Combo WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple');
DECLARE @Popcorn1Id INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Bắp rang bơ');
DECLARE @Cola1Id INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Coca Cola');
DECLARE @Combo2Id INT = (SELECT combo_id FROM dbo.Combo WHERE cinema_id = @Cinema2Id AND combo_name = N'Combo Couple');
DECLARE @Popcorn2Id INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema2Id AND item_name = N'Bắp rang bơ');
DECLARE @Cola2Id INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema2Id AND item_name = N'Coca Cola');

IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @Combo1Id AND food_item_id = @Popcorn1Id)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@Combo1Id, @Popcorn1Id, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @Combo1Id AND food_item_id = @Cola1Id)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@Combo1Id, @Cola1Id, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @Combo2Id AND food_item_id = @Popcorn2Id)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@Combo2Id, @Popcorn2Id, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @Combo2Id AND food_item_id = @Cola2Id)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@Combo2Id, @Cola2Id, 2);
GO

/* =========================================================
   15. Sample counter food order
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Staff1Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'staff1@fcinema.local');

IF NOT EXISTS (
    SELECT 1 FROM dbo.Food_Order
    WHERE staff_id = @Staff1Id
      AND cinema_id = @Cinema1Id
      AND created_at = '2026-09-30T14:00:00'
)
BEGIN
    DECLARE @FoodOrderId INT;
    DECLARE @PopcornId INT = (
        SELECT food_item_id FROM dbo.Food_Item
        WHERE cinema_id = @Cinema1Id AND item_name = N'Bắp rang bơ'
    );
    DECLARE @ComboId INT = (
        SELECT combo_id FROM dbo.Combo
        WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple'
    );

    INSERT INTO dbo.Food_Order(
        staff_id, cinema_id, subtotal, total_amount,
        payment_method, payment_status, order_status, created_at
    )
    VALUES (
        @Staff1Id, @Cinema1Id, 140000, 140000,
        'CASH', 'PAID', 'COMPLETED', '2026-09-30T14:00:00'
    );

    SET @FoodOrderId = SCOPE_IDENTITY();

    INSERT INTO dbo.Food_Order_Item(
        food_order_id, food_item_id, combo_id, quantity, unit_price, line_total
    )
    VALUES
        (@FoodOrderId, @PopcornId, NULL, 1, 45000, 45000),
        (@FoodOrderId, NULL, @ComboId, 1, 95000, 95000);
END;
GO

/* =========================================================
   Quick verification
   ========================================================= */
PRINT 'FCinemaDB seed completed.';

SELECT 'Role' AS table_name, COUNT(*) AS row_count FROM dbo.[Role]
UNION ALL SELECT 'Cinema', COUNT(*) FROM dbo.Cinema
UNION ALL SELECT 'User', COUNT(*) FROM dbo.[User]
UNION ALL SELECT 'Customer', COUNT(*) FROM dbo.Customer
UNION ALL SELECT 'Staff', COUNT(*) FROM dbo.Staff
UNION ALL SELECT 'Room', COUNT(*) FROM dbo.Room
UNION ALL SELECT 'Seat', COUNT(*) FROM dbo.Seat
UNION ALL SELECT 'Movie', COUNT(*) FROM dbo.Movie
UNION ALL SELECT 'Genre', COUNT(*) FROM dbo.Genre
UNION ALL SELECT 'Showtime', COUNT(*) FROM dbo.Showtime
UNION ALL SELECT 'Booking', COUNT(*) FROM dbo.Booking
UNION ALL SELECT 'Ticket', COUNT(*) FROM dbo.Ticket
UNION ALL SELECT 'Payment', COUNT(*) FROM dbo.Payment
UNION ALL SELECT 'Food_Item', COUNT(*) FROM dbo.Food_Item
UNION ALL SELECT 'Combo', COUNT(*) FROM dbo.Combo
UNION ALL SELECT 'Food_Order', COUNT(*) FROM dbo.Food_Order;
GO
