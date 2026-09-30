/*
    FCinemaDB - Clean seed data
    Run AFTER: database/FCinemaDB_schema.sql

    Default demo password for all seed users: 123456
    MD5(123456): e10adc3949ba59abbe56e057f20f883e
*/

USE FCinemaDB;
GO
SET NOCOUNT ON;
GO

/* 01. ROLE */
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'ADMIN')
    INSERT INTO dbo.[Role](role_name, description, status) VALUES ('ADMIN', N'System administrator', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'STAFF')
    INSERT INTO dbo.[Role](role_name, description, status) VALUES ('STAFF', N'Cinema staff', 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE role_name = 'CUSTOMER')
    INSERT INTO dbo.[Role](role_name, description, status) VALUES ('CUSTOMER', N'Cinema customer', 'ACTIVE');
GO

/* 02. CINEMA */
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Ninh Kiều')
    INSERT INTO dbo.Cinema(cinema_name,address,phone,status)
    VALUES (N'F-Cinema Ninh Kiều',N'01 Hòa Bình, Ninh Kiều, Cần Thơ','02923730001','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema WHERE cinema_name = N'F-Cinema Cái Răng')
    INSERT INTO dbo.Cinema(cinema_name,address,phone,status)
    VALUES (N'F-Cinema Cái Răng',N'99 Võ Nguyên Giáp, Cái Răng, Cần Thơ','02923730002','ACTIVE');
GO

/* 03. USERS + CUSTOMER/STAFF */
DECLARE @AdminRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='ADMIN');
DECLARE @StaffRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='STAFF');
DECLARE @CustomerRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='CUSTOMER');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Cái Răng');
DECLARE @PasswordMD5 VARCHAR(255) = 'e10adc3949ba59abbe56e057f20f883e';

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='admin@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@AdminRoleId,N'FCinema Admin','admin@fcinema.local',@PasswordMD5,'0900000001','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='staff1@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@StaffRoleId,N'Nguyễn Minh Staff','staff1@fcinema.local',@PasswordMD5,'0900000002','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='staff2@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@StaffRoleId,N'Trần Anh Staff','staff2@fcinema.local',@PasswordMD5,'0900000003','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='customer1@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@CustomerRoleId,N'Lê Minh Khách','customer1@fcinema.local',@PasswordMD5,'0900000004','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='customer2@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@CustomerRoleId,N'Phạm Ngọc Khách','customer2@fcinema.local',@PasswordMD5,'0900000005','ACTIVE');

DECLARE @Staff1Id INT=(SELECT user_id FROM dbo.[User] WHERE email='staff1@fcinema.local');
DECLARE @Staff2Id INT=(SELECT user_id FROM dbo.[User] WHERE email='staff2@fcinema.local');
DECLARE @Customer1Id INT=(SELECT user_id FROM dbo.[User] WHERE email='customer1@fcinema.local');
DECLARE @Customer2Id INT=(SELECT user_id FROM dbo.[User] WHERE email='customer2@fcinema.local');

IF NOT EXISTS (SELECT 1 FROM dbo.Staff WHERE staff_id=@Staff1Id)
    INSERT INTO dbo.Staff(staff_id,cinema_id) VALUES (@Staff1Id,@Cinema1Id);
IF NOT EXISTS (SELECT 1 FROM dbo.Staff WHERE staff_id=@Staff2Id)
    INSERT INTO dbo.Staff(staff_id,cinema_id) VALUES (@Staff2Id,@Cinema2Id);
IF NOT EXISTS (SELECT 1 FROM dbo.Customer WHERE customer_id=@Customer1Id)
    INSERT INTO dbo.Customer(customer_id) VALUES (@Customer1Id);
IF NOT EXISTS (SELECT 1 FROM dbo.Customer WHERE customer_id=@Customer2Id)
    INSERT INTO dbo.Customer(customer_id) VALUES (@Customer2Id);
GO

/* 04. ROOMS */
DECLARE @Cinema1Id INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Cái Răng');
IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema1Id AND room_name=N'Room 1')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status) VALUES (@Cinema1Id,N'Room 1',10,'2D','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema1Id AND room_name=N'Room 2')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status) VALUES (@Cinema1Id,N'Room 2',10,'2D','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema2Id AND room_name=N'Room 1')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status) VALUES (@Cinema2Id,N'Room 1',10,'2D','ACTIVE');
GO

/* 05. SEATS */
DECLARE @RoomId INT,@RowNo INT,@SeatNo INT;
DECLARE room_cursor CURSOR LOCAL FAST_FORWARD FOR SELECT room_id FROM dbo.Room WHERE room_name IN (N'Room 1',N'Room 2');
OPEN room_cursor;
FETCH NEXT FROM room_cursor INTO @RoomId;
WHILE @@FETCH_STATUS=0
BEGIN
    SET @RowNo=1;
    WHILE @RowNo<=2
    BEGIN
        SET @SeatNo=1;
        WHILE @SeatNo<=5
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.Seat WHERE room_id=@RoomId AND seat_row=@RowNo AND seat_number=@SeatNo)
                INSERT INTO dbo.Seat(room_id,seat_row,seat_number,seat_type,status)
                VALUES (@RoomId,@RowNo,@SeatNo,CASE WHEN @RowNo=2 THEN 'VIP' ELSE 'STANDARD' END,'ACTIVE');
            SET @SeatNo+=1;
        END
        SET @RowNo+=1;
    END
    FETCH NEXT FROM room_cursor INTO @RoomId;
END
CLOSE room_cursor;
DEALLOCATE room_cursor;
GO

/* 06. PRICING */
DECLARE @Cinema1Id INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Cái Răng');
IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id=@Cinema1Id AND seat_type='STANDARD' AND start_date='2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id,seat_type,price,start_date,end_date,status) VALUES (@Cinema1Id,'STANDARD',80000,'2026-01-01',NULL,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id=@Cinema1Id AND seat_type='VIP' AND start_date='2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id,seat_type,price,start_date,end_date,status) VALUES (@Cinema1Id,'VIP',100000,'2026-01-01',NULL,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id=@Cinema2Id AND seat_type='STANDARD' AND start_date='2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id,seat_type,price,start_date,end_date,status) VALUES (@Cinema2Id,'STANDARD',75000,'2026-01-01',NULL,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Pricing WHERE cinema_id=@Cinema2Id AND seat_type='VIP' AND start_date='2026-01-01')
    INSERT INTO dbo.Pricing(cinema_id,seat_type,price,start_date,end_date,status) VALUES (@Cinema2Id,'VIP',95000,'2026-01-01',NULL,'ACTIVE');
GO

/* 07. GENRE + MOVIE */
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Action') INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Action',N'Action movies','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Comedy') INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Comedy',N'Comedy movies','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Animation') INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Animation',N'Animated movies','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Drama') INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Drama',N'Drama movies','ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title=N'The Last Horizon')
    INSERT INTO dbo.Movie(title,description,duration_minutes,age_rating,release_date,end_date,status)
    VALUES(N'The Last Horizon',N'Demo action movie.',125,'T13','2026-09-20','2026-12-31','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title=N'Robot Adventures')
    INSERT INTO dbo.Movie(title,description,duration_minutes,age_rating,release_date,end_date,status)
    VALUES(N'Robot Adventures',N'Demo animated movie.',98,'P','2026-09-15','2026-12-31','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Movie WHERE title=N'Can Tho After Rain')
    INSERT INTO dbo.Movie(title,description,duration_minutes,age_rating,release_date,end_date,status)
    VALUES(N'Can Tho After Rain',N'Demo drama movie.',110,'T13','2026-09-25','2026-12-31','ACTIVE');
GO

/* 08. MOVIE_GENRE + CINEMA_MOVIE */
DECLARE @M1 INT=(SELECT movie_id FROM dbo.Movie WHERE title=N'The Last Horizon');
DECLARE @M2 INT=(SELECT movie_id FROM dbo.Movie WHERE title=N'Robot Adventures');
DECLARE @M3 INT=(SELECT movie_id FROM dbo.Movie WHERE title=N'Can Tho After Rain');
DECLARE @G1 INT=(SELECT genre_id FROM dbo.Genre WHERE genre_name=N'Action');
DECLARE @G2 INT=(SELECT genre_id FROM dbo.Genre WHERE genre_name=N'Animation');
DECLARE @G3 INT=(SELECT genre_id FROM dbo.Genre WHERE genre_name=N'Comedy');
DECLARE @G4 INT=(SELECT genre_id FROM dbo.Genre WHERE genre_name=N'Drama');
DECLARE @C1 INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @C2 INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Cái Răng');
IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id=@M1 AND genre_id=@G1) INSERT INTO dbo.Movie_Genre VALUES(@M1,@G1);
IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id=@M2 AND genre_id=@G2) INSERT INTO dbo.Movie_Genre VALUES(@M2,@G2);
IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id=@M2 AND genre_id=@G3) INSERT INTO dbo.Movie_Genre VALUES(@M2,@G3);
IF NOT EXISTS (SELECT 1 FROM dbo.Movie_Genre WHERE movie_id=@M3 AND genre_id=@G4) INSERT INTO dbo.Movie_Genre VALUES(@M3,@G4);
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id=@C1 AND movie_id=@M1) INSERT INTO dbo.Cinema_Movie VALUES(@C1,@M1,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id=@C1 AND movie_id=@M2) INSERT INTO dbo.Cinema_Movie VALUES(@C1,@M2,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Cinema_Movie WHERE cinema_id=@C2 AND movie_id=@M1) INSERT INTO dbo.Cinema_Movie VALUES(@C2,@M1,'ACTIVE');
GO

/* 09. SHOWTIME */
DECLARE @C1 INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @R1 INT=(SELECT room_id FROM dbo.Room WHERE cinema_id=@C1 AND room_name=N'Room 1');
DECLARE @R2 INT=(SELECT room_id FROM dbo.Room WHERE cinema_id=@C1 AND room_name=N'Room 2');
DECLARE @M1 INT=(SELECT movie_id FROM dbo.Movie WHERE title=N'The Last Horizon');
DECLARE @M2 INT=(SELECT movie_id FROM dbo.Movie WHERE title=N'Robot Adventures');
IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id=@R1 AND start_time='2026-10-01T18:00:00')
    INSERT INTO dbo.Showtime(movie_id,room_id,start_time,end_time,status) VALUES(@M1,@R1,'2026-10-01T18:00:00','2026-10-01T20:05:00','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Showtime WHERE room_id=@R2 AND start_time='2026-10-01T19:00:00')
    INSERT INTO dbo.Showtime(movie_id,room_id,start_time,end_time,status) VALUES(@M2,@R2,'2026-10-01T19:00:00','2026-10-01T20:38:00','ACTIVE');
GO

/* 10. PROMOTION */
IF NOT EXISTS (SELECT 1 FROM dbo.Promotion WHERE promo_code='WELCOME10')
    INSERT INTO dbo.Promotion(promo_code,description,discount_type,discount_value,min_order_amount,max_discount_amount,start_date,end_date,usage_limit,status)
    VALUES('WELCOME10',N'Giảm 10% cho đơn từ 100.000đ','PERCENT',10,100000,50000,'2026-01-01','2026-12-31',1000,'ACTIVE');
GO

/* 11. BOOKING + TICKET + PAYMENT */
DECLARE @CustomerId INT=(SELECT user_id FROM dbo.[User] WHERE email='customer1@fcinema.local');
DECLARE @ShowtimeId INT=(SELECT TOP 1 showtime_id FROM dbo.Showtime ORDER BY showtime_id);
DECLARE @Seat1 INT=(SELECT TOP 1 seat_id FROM dbo.Seat WHERE room_id=(SELECT room_id FROM dbo.Showtime WHERE showtime_id=@ShowtimeId) ORDER BY seat_row,seat_number);
DECLARE @Seat2 INT=(SELECT TOP 1 seat_id FROM dbo.Seat WHERE room_id=(SELECT room_id FROM dbo.Showtime WHERE showtime_id=@ShowtimeId) AND seat_id<>@Seat1 ORDER BY seat_row,seat_number);
DECLARE @PromoId INT=(SELECT promotion_id FROM dbo.Promotion WHERE promo_code='WELCOME10');

IF NOT EXISTS (SELECT 1 FROM dbo.Booking WHERE customer_id=@CustomerId AND showtime_id=@ShowtimeId AND status='CONFIRMED')
BEGIN
    INSERT INTO dbo.Booking(customer_id,showtime_id,promotion_id,subtotal,discount_amount,total_amount,status)
    VALUES(@CustomerId,@ShowtimeId,@PromoId,160000,16000,144000,'CONFIRMED');
    DECLARE @BookingId INT=SCOPE_IDENTITY();
    INSERT INTO dbo.Ticket(booking_id,showtime_id,seat_id,ticket_code,ticket_price,status)
    VALUES(@BookingId,@ShowtimeId,@Seat1,'DEMO-TICKET-001',80000,'VALID'),
          (@BookingId,@ShowtimeId,@Seat2,'DEMO-TICKET-002',80000,'VALID');
    INSERT INTO dbo.Payment(booking_id,payment_method,merchant_reference,transaction_code,amount,payment_time,status)
    VALUES(@BookingId,'VNPAY','SEED-PAY-001','VNP-DEMO-001',144000,SYSDATETIME(),'SUCCESS');
END
GO

/* 12. FOOD + COMBO */
DECLARE @C1 INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id=@C1 AND item_name=N'Bắp rang bơ')
    INSERT INTO dbo.Food_Item(cinema_id,item_name,item_type,description,price,status) VALUES(@C1,N'Bắp rang bơ','FOOD',N'Bắp rang cỡ vừa',45000,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Food_Item WHERE cinema_id=@C1 AND item_name=N'Coca Cola')
    INSERT INTO dbo.Food_Item(cinema_id,item_name,item_type,description,price,status) VALUES(@C1,N'Coca Cola','DRINK',N'Nước ngọt cỡ vừa',30000,'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Combo WHERE cinema_id=@C1 AND combo_name=N'Combo Couple')
    INSERT INTO dbo.Combo(cinema_id,combo_name,description,combo_price,status) VALUES(@C1,N'Combo Couple',N'1 bắp + 2 nước',95000,'ACTIVE');

DECLARE @Food1 INT=(SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id=@C1 AND item_name=N'Bắp rang bơ');
DECLARE @Food2 INT=(SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id=@C1 AND item_name=N'Coca Cola');
DECLARE @ComboId INT=(SELECT combo_id FROM dbo.Combo WHERE cinema_id=@C1 AND combo_name=N'Combo Couple');
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id=@ComboId AND food_item_id=@Food1) INSERT INTO dbo.Combo_Item VALUES(@ComboId,@Food1,1);
IF NOT EXISTS (SELECT 1 FROM dbo.Combo_Item WHERE combo_id=@ComboId AND food_item_id=@Food2) INSERT INTO dbo.Combo_Item VALUES(@ComboId,@Food2,2);
GO

/* 13. FOOD ORDER */
DECLARE @StaffId INT=(SELECT user_id FROM dbo.[User] WHERE email='staff1@fcinema.local');
DECLARE @C1 INT=(SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @Food1 INT=(SELECT food_item_id FROM dbo.Food_Item WHERE cinema_id=@C1 AND item_name=N'Bắp rang bơ');
DECLARE @ComboId INT=(SELECT combo_id FROM dbo.Combo WHERE cinema_id=@C1 AND combo_name=N'Combo Couple');
IF NOT EXISTS (SELECT 1 FROM dbo.Food_Order WHERE staff_id=@StaffId AND cinema_id=@C1 AND total_amount=140000)
BEGIN
    INSERT INTO dbo.Food_Order(staff_id,cinema_id,subtotal,total_amount,payment_method,payment_status,order_status)
    VALUES(@StaffId,@C1,140000,140000,'CASH','PAID','COMPLETED');
    DECLARE @FoodOrderId INT=SCOPE_IDENTITY();
    INSERT INTO dbo.Food_Order_Item(food_order_id,food_item_id,combo_id,quantity,unit_price,line_total)
    VALUES(@FoodOrderId,@Food1,NULL,1,45000,45000),(@FoodOrderId,NULL,@ComboId,1,95000,95000);
END
GO

/* QUICK CHECK */
SELECT user_id,full_name,email,password_hash,status FROM dbo.[User] ORDER BY user_id;
SELECT cinema_id,cinema_name,status FROM dbo.Cinema ORDER BY cinema_id;
SELECT movie_id,title,status FROM dbo.Movie ORDER BY movie_id;
GO
