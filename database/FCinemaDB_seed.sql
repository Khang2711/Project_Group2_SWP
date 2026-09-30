/*
    FCinemaDB - Clean seed data
    Run AFTER: database/FCinemaDB_schema.sql

    Demo password for all seed users: password
    MD5(password): 5f4dcc3b5aa765d61d8327deb882cf99

    This seed is designed for local/demo use and uses IF NOT EXISTS
    where practical so it can be re-run with limited duplication.
*/

USE FCinemaDB;
GO
SET NOCOUNT ON;
GO

/* =========================================================
   01. ROLE
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'ADMIN')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('ADMIN', N'System administrator', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'STAFF')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('STAFF', N'Cinema staff', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'CUSTOMER')
    INSERT INTO dbo.[Role](role_name, description, status)
    VALUES ('CUSTOMER', N'Cinema customer', 'ACTIVE');
GO

/* =========================================================
   02. CINEMA
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều')
    INSERT INTO dbo.Cinema(cinema_name, address, phone, status)
    VALUES (N'F-Cinema Ninh Kiều', N'01 Hòa Bình, Ninh Kiều, Cần Thơ', '02923730001', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng')
    INSERT INTO dbo.Cinema(cinema_name, address, phone, status)
    VALUES (N'F-Cinema Cái Răng', N'99 Võ Nguyên Giáp, Cái Răng, Cần Thơ', '02923730002', 'ACTIVE');
GO

/* =========================================================
   03. USERS + CUSTOMER/STAFF
   ========================================================= */
DECLARE @AdminRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'ADMIN');
DECLARE @StaffRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'STAFF');
DECLARE @CustomerRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name = 'CUSTOMER');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng');
DECLARE @DemoPasswordMD5 VARCHAR(255) = '5f4dcc3b5aa765d61d8327deb882cf99';

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'admin@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@AdminRoleId, N'FCinema Admin', 'admin@fcinema.local', @DemoPasswordMD5, '0900000001', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'staff1@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@StaffRoleId, N'Nguyễn Minh Staff', 'staff1@fcinema.local', @DemoPasswordMD5, '0900000002', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'staff2@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@StaffRoleId, N'Trần Anh Staff', 'staff2@fcinema.local', @DemoPasswordMD5, '0900000003', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'customer1@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@CustomerRoleId, N'Lê Minh Khách', 'customer1@fcinema.local', @DemoPasswordMD5, '0900000004', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email = 'customer2@fcinema.local')
    INSERT INTO dbo.[User](role_id, full_name, email, password_hash, phone, status)
    VALUES (@CustomerRoleId, N'Phạm Ngọc Khách', 'customer2@fcinema.local', @DemoPasswordMD5, '0900000005', 'ACTIVE');

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
   04. ROOMS + SEATS
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

DECLARE @RoomId INT;
DECLARE @RowNo INT;
DECLARE @SeatNo INT;
DECLARE room_cursor CURSOR LOCAL FAST_FORWARD FOR
    SELECT room_id FROM dbo.Room
    WHERE cinema_id IN (@Cinema1Id, @Cinema2Id)
      AND room_name IN (N'Room 1', N'Room 2');
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
                INSERT INTO dbo.Seat(room_id, seat_row, seat_number, seat_type, status)
                VALUES (@RoomId, @RowNo, @SeatNo,
                        CASE WHEN @RowNo = 2 THEN 'VIP' ELSE 'STANDARD' END,
                        'ACTIVE');
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
   05. PRICING
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
   06. GENRES + MOVIES
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Action')
    INSERT INTO dbo.Genre(genre_name, description, status) VALUES (N'Action', N'Action movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Comedy')
    INSERT INTO dbo.Genre(genre_name, description, status) VALUES (N'Comedy', N'Comedy movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Animation')
    INSERT INTO dbo.Genre(genre_name, description, status) VALUES (N'Animation', N'Animated movies', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name = N'Drama')
    INSERT INTO dbo.Genre(genre_name, description, status) VALUES (N'Drama', N'Drama movies', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'The Last Horizon')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'The Last Horizon', N'Demo action movie.', 125, 'T13', '2026-09-20', '2026-12-31', NULL, NULL, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'Robot Adventures')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'Robot Adventures', N'Demo animated movie.', 98, 'P', '2026-09-15', '2026-12-31', NULL, NULL, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title = N'Can Tho After Rain')
    INSERT INTO dbo.Movie(title, description, duration_minutes, age_rating, release_date, end_date, poster_url, trailer_url, status)
    VALUES (N'Can Tho After Rain', N'Demo drama movie.', 110, 'T13', '2026-09-25', '2026-12-31', NULL, NULL, 'ACTIVE');
GO

/* =========================================================
   07. MOVIE_GENRE + CINEMA_MOVIE
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
GO

/* =========================================================
   08. SHOWTIMES
   ========================================================= */
DECLARE @Movie1Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'The Last Horizon');
DECLARE @Movie2Id INT = (SELECT movie_id FROM dbo.Movie WHERE title = N'Robot Adventures');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @Room1Id INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 1');
DECLARE @Room2Id INT = (SELECT room_id FROM dbo.Room WHERE cinema_id = @Cinema1Id AND room_name = N'Room 2');

IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @Room1Id AND start_time = '2026-10-01T18:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie1Id, @Room1Id, '2026-10-01T18:00:00', '2026-10-01T20:05:00', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id = @Room2Id AND start_time = '2026-10-01T19:00:00')
    INSERT INTO dbo.Showtime(movie_id, room_id, start_time, end_time, status)
    VALUES (@Movie2Id, @Room2Id, '2026-10-01T19:00:00', '2026-10-01T20:38:00', 'ACTIVE');
GO

/* =========================================================
   09. PROMOTION
   ========================================================= */
IF NOT EXISTS (SELECT 1 FROM dbo.Promotion WHERE promo_code = 'WELCOME10')
    INSERT INTO dbo.Promotion(
        promo_code, description, discount_type, discount_value,
        min_order_amount, max_discount_amount, start_date, end_date,
        usage_limit, status
    )
    VALUES ('WELCOME10', N'Giảm 10% cho khách hàng', 'PERCENT', 10, 100000, 50000,
            '2026-01-01T00:00:00', '2026-12-31T23:59:59', 1000, 'ACTIVE');
GO

/* =========================================================
   10. BOOKING + TICKET + PAYMENT
   ========================================================= */
DECLARE @Customer1Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'customer1@fcinema.local');
DECLARE @ShowtimeId INT = (
    SELECT TOP 1 s.showtime_id
    FROM dbo.Showtime s
    JOIN dbo.Room r ON r.room_id = s.room_id
    JOIN dbo.Cinema c ON c.cinema_id = r.cinema_id
    WHERE c.cinema_name = N'F-Cinema Ninh Kiều'
      AND r.room_name = N'Room 1'
      AND s.start_time = '2026-10-01T18:00:00'
);
DECLARE @PromoId INT = (SELECT promotion_id FROM dbo.Promotion WHERE promo_code = 'WELCOME10');

IF NOT EXISTS (
    SELECT 1 FROM dbo.Booking
    WHERE customer_id = @Customer1Id AND showtime_id = @ShowtimeId AND status = 'CONFIRMED'
)
    INSERT INTO dbo.Booking(customer_id, showtime_id, promotion_id, subtotal, discount_amount, total_amount, status)
    VALUES (@Customer1Id, @ShowtimeId, @PromoId, 180000, 18000, 162000, 'CONFIRMED');

DECLARE @BookingId INT = (
    SELECT TOP 1 booking_id FROM dbo.Booking
    WHERE customer_id = @Customer1Id AND showtime_id = @ShowtimeId AND status = 'CONFIRMED'
    ORDER BY booking_id
);
DECLARE @RoomId INT = (SELECT room_id FROM dbo.Showtime WHERE showtime_id = @ShowtimeId);
DECLARE @Seat1Id INT = (SELECT seat_id FROM dbo.Seat WHERE room_id = @RoomId AND seat_row = 1 AND seat_number = 1);
DECLARE @Seat2Id INT = (SELECT seat_id FROM dbo.Seat WHERE room_id = @RoomId AND seat_row = 2 AND seat_number = 1);

IF NOT EXISTS (SELECT 1 FROM dbo.Ticket WHERE booking_id = @BookingId AND seat_id = @Seat1Id)
    INSERT INTO dbo.Ticket(booking_id, showtime_id, seat_id, ticket_code, ticket_price, status)
    VALUES (@BookingId, @ShowtimeId, @Seat1Id, 'DEMO-TICKET-001', 80000, 'VALID');
IF NOT EXISTS (SELECT 1 FROM dbo.Ticket WHERE booking_id = @BookingId AND seat_id = @Seat2Id)
    INSERT INTO dbo.Ticket(booking_id, showtime_id, seat_id, ticket_code, ticket_price, status)
    VALUES (@BookingId, @ShowtimeId, @Seat2Id, 'DEMO-TICKET-002', 100000, 'VALID');

IF NOT EXISTS (SELECT 1 FROM dbo.Payment WHERE merchant_reference = 'DEMO-PAY-001')
    INSERT INTO dbo.Payment(
        booking_id, payment_method, merchant_reference, transaction_code,
        amount, payment_time, status
    )
    VALUES (@BookingId, 'VNPAY', 'DEMO-PAY-001', 'VNPAY-DEMO-001', 162000, SYSDATETIME(), 'SUCCESS');
GO

/* =========================================================
   11. FOOD + COMBO
   ========================================================= */
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Bắp rang bơ')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, status)
    VALUES (@Cinema1Id, N'Bắp rang bơ', 'POPCORN', N'Bắp rang bơ size M', 45000, 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Coca Cola')
    INSERT INTO dbo.Food_Item(cinema_id, item_name, item_type, description, price, status)
    VALUES (@Cinema1Id, N'Coca Cola', 'DRINK', N'Nước ngọt size M', 30000, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Combo WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple')
    INSERT INTO dbo.Combo(cinema_id, combo_name, description, combo_price, status)
    VALUES (@Cinema1Id, N'Combo Couple', N'1 bắp + 2 nước', 95000, 'ACTIVE');

DECLARE @PopcornId INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Bắp rang bơ');
DECLARE @CokeId INT = (SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id = @Cinema1Id AND item_name = N'Coca Cola');
DECLARE @ComboId INT = (SELECT combo_id FROM dbo.Combo WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple');

IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @ComboId AND food_item_id = @PopcornId)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@ComboId, @PopcornId, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id = @ComboId AND food_item_id = @CokeId)
    INSERT INTO dbo.Combo_Item(combo_id, food_item_id, quantity) VALUES (@ComboId, @CokeId, 2);
GO

/* =========================================================
   12. FOOD ORDER
   ========================================================= */
DECLARE @Staff1Id INT = (SELECT user_id FROM dbo.[User] WHERE email = 'staff1@fcinema.local');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều');
DECLARE @ComboId INT = (SELECT combo_id FROM dbo.Combo WHERE cinema_id = @Cinema1Id AND combo_name = N'Combo Couple');

IF NOT EXISTS (
    SELECT 1 FROM dbo.Food_Order
    WHERE staff_id = @Staff1Id AND cinema_id = @Cinema1Id AND total_amount = 95000
)
    INSERT INTO dbo.Food_Order(
        staff_id, cinema_id, subtotal, total_amount,
        payment_method, payment_status, order_status
    )
    VALUES (@Staff1Id, @Cinema1Id, 95000, 95000, 'CASH', 'PAID', 'COMPLETED');

DECLARE @FoodOrderId INT = (
    SELECT TOP 1 food_order_id FROM dbo.Food_Order
    WHERE staff_id = @Staff1Id AND cinema_id = @Cinema1Id AND total_amount = 95000
    ORDER BY food_order_id
);

IF NOT EXISTS (SELECT 1 FROM dbo.Food_Order_Item WHERE food_order_id = @FoodOrderId AND combo_id = @ComboId)
    INSERT INTO dbo.Food_Order_Item(food_order_id, food_item_id, combo_id, quantity, unit_price, line_total)
    VALUES (@FoodOrderId, NULL, @ComboId, 1, 95000, 95000);
GO

/* =========================================================
   VERIFY DEMO DATA
   ========================================================= */
SELECT user_id, full_name, email, password_hash, status
FROM dbo.[User]
WHERE email LIKE '%@fcinema.local'
ORDER BY user_id;

SELECT cinema_id, cinema_name, status FROM dbo.Cinema ORDER BY cinema_id;
SELECT movie_id, title, status FROM dbo.Movie ORDER BY movie_id;
SELECT booking_id, customer_id, showtime_id, total_amount, status FROM dbo.Booking ORDER BY booking_id;
SELECT ticket_id, booking_id, showtime_id, seat_id, ticket_code, status FROM dbo.Ticket ORDER BY ticket_id;
GO
