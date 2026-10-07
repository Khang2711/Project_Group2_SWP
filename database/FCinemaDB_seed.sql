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
IF NOT EXISTS (SELECT 1 FROM dbo.[Role]WHERE role_name = 'CINEMA_MANAGER')
    INSERT INTO dbo.[Role](role_name, description, status) VALUES ('CINEMA_MANAGER', N'Cinema Manager', 'ACTIVE');
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
DECLARE @ManagerRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='CINEMA_MANAGER');
DECLARE @StaffRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='STAFF');
DECLARE @CustomerRoleId INT = (SELECT role_id FROM dbo.[Role] WHERE role_name='CUSTOMER');
DECLARE @Cinema1Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Ninh Kiều');
DECLARE @Cinema2Id INT = (SELECT cinema_id FROM dbo.Cinema WHERE cinema_name=N'F-Cinema Cái Răng');
DECLARE @PasswordMD5 VARCHAR(255) = 'e10adc3949ba59abbe56e057f20f883e';

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='admin@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@AdminRoleId,N'FCinema Admin','admin@fcinema.local',@PasswordMD5,'0900000001','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE email='manager@fcinema.local')
    INSERT INTO dbo.[User](role_id,full_name,email,password_hash,phone,status)
    VALUES (@ManagerRoleId,N'FCinema Manager','manager@fcinema.local',@PasswordMD5,'0900000006','ACTIVE');
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
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status)
    VALUES (@Cinema1Id,N'Room 1',10,'2D','ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema1Id AND room_name=N'Room 2')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status)
    VALUES (@Cinema1Id,N'Room 2',10,'2D','ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema2Id AND room_name=N'Room 1')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status)
    VALUES (@Cinema2Id,N'Room 1',10,'2D','ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.Room WHERE cinema_id=@Cinema2Id AND room_name=N'Room 2')
    INSERT INTO dbo.Room(cinema_id,room_name,capacity,room_type,status)
    VALUES (@Cinema2Id,N'Room 2',10,'2D','ACTIVE');
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
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Action')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Action',N'Phim hành động','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Comedy')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Comedy',N'Phim hài','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Animation')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Animation',N'Phim hoạt hình','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Drama')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Drama',N'Phim chính kịch','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Sci-Fi')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Sci-Fi',N'Khoa học viễn tưởng','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Thriller')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Thriller',N'Giật gân, hồi hộp','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Romance')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Romance',N'Tình cảm','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Horror')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Horror',N'Kinh dị','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Family')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Family',N'Gia đình','ACTIVE');
IF NOT EXISTS (SELECT 1 FROM dbo.Genre WHERE genre_name=N'Adventure')
    INSERT INTO dbo.Genre(genre_name,description,status) VALUES(N'Adventure',N'Phiêu lưu','ACTIVE');

DECLARE @SeedMovie TABLE (
    title NVARCHAR(255) PRIMARY KEY,
    description NVARCHAR(MAX),
    duration_minutes INT,
    age_rating VARCHAR(20),
    release_date DATE,
    end_date DATE,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    status VARCHAR(20)
);

INSERT INTO @SeedMovie(
    title,description,duration_minutes,age_rating,
    release_date,end_date,poster_url,trailer_url,status
)
VALUES
(N'The Last Horizon',
 N'Một đội cứu hộ không gian nhận tín hiệu bí ẩn từ vùng biên cuối cùng của hệ Mặt Trời và phải chạy đua với thời gian để đưa phi hành đoàn trở về.',
 125,'T13','2026-09-20','2027-01-31',
 'https://placehold.co/600x900/18181c/ff1f2d?text=The+Last+Horizon',NULL,'ACTIVE'),

(N'Robot Adventures',
 N'Chú robot nhỏ bị lạc khỏi phòng thí nghiệm và bắt đầu chuyến phiêu lưu đầy tiếng cười cùng hai người bạn nhí để tìm đường trở về nhà.',
 98,'P','2026-09-15','2027-02-28',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Robot+Adventures',NULL,'ACTIVE'),

(N'Can Tho After Rain',
 N'Sau một cơn mưa lớn ở Cần Thơ, hai người trẻ tình cờ gặp lại nhau và cùng đối diện những lựa chọn giữa tình yêu, gia đình và tương lai.',
 110,'T13','2026-09-25','2027-01-15',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Can+Tho+After+Rain',NULL,'ACTIVE'),

(N'Midnight Signal',
 N'Một kỹ sư trực ca đêm phát hiện tín hiệu radio lặp lại từ một tòa nhà đã bỏ hoang và dần bị cuốn vào bí mật không ai muốn nhắc tới.',
 116,'T16','2026-10-03','2027-01-31',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Midnight+Signal',NULL,'ACTIVE'),

(N'Mekong Rush',
 N'Một nhóm vận chuyển đường sông vô tình nắm giữ bằng chứng quan trọng và phải vượt qua hàng loạt cuộc truy đuổi dọc miền Tây để đưa nó đến nơi an toàn.',
 118,'T13','2026-10-05','2027-02-15',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Mekong+Rush',NULL,'ACTIVE'),

(N'Laughing at 8PM',
 N'Một nhóm bạn quyết định tổ chức đêm diễn hài nghiệp dư lúc 8 giờ tối, nhưng mọi kế hoạch liên tục đi chệch hướng theo cách không ai ngờ tới.',
 102,'P','2026-10-01','2026-12-31',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Laughing+at+8PM',NULL,'ACTIVE'),

(N'The Lantern Keeper',
 N'Người giữ đèn già và cô cháu gái cùng bảo vệ lễ hội truyền thống của thị trấn nhỏ, qua đó hàn gắn những khoảng cách trong gia đình.',
 112,'P','2026-10-10','2027-02-28',
 'https://placehold.co/600x900/18181c/ff1f2d?text=The+Lantern+Keeper',NULL,'ACTIVE'),

(N'Shadow Room',
 N'Một nhóm sinh viên thuê căn phòng giá rẻ cho chuyến đi cuối tuần và phát hiện chiếc bóng trong gương luôn xuất hiện chậm hơn họ một nhịp.',
 107,'T18','2026-10-15','2027-02-28',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Shadow+Room',NULL,'ACTIVE'),

-- Coming soon: giữ status ACTIVE vì trang khách hiện chỉ lấy phim ACTIVE.
-- Các phim này có release_date trong tương lai và chưa seed suất chiếu,
-- nên MovieService sẽ xếp vào nhóm "Sắp chiếu · Chưa mở lịch".
(N'Beyond the Red Moon',
 N'Một đoàn thám hiểm tiến đến mặt trăng đỏ ngoài rìa thiên hà và phát hiện dấu vết của một nền văn minh đã biến mất hàng nghìn năm.',
 132,'T13','2027-02-20','2027-06-30',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Beyond+the+Red+Moon',NULL,'ACTIVE'),

(N'Summer Letter 2027',
 N'Một lá thư bị gửi nhầm đưa hai người xa lạ vào chuỗi cuộc gặp gỡ kéo dài qua nhiều thành phố và một mùa hè đáng nhớ.',
 115,'T13','2027-03-12','2027-07-31',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Summer+Letter+2027',NULL,'ACTIVE'),

(N'Tiny Heroes: City Rescue',
 N'Biệt đội thú cưng tí hon bí mật hợp tác để cứu thành phố sau khi hệ thống giao thông tự động gặp sự cố trong ngày hội lớn.',
 96,'P','2027-04-02','2027-08-31',
 'https://placehold.co/600x900/18181c/ff1f2d?text=Tiny+Heroes+City+Rescue',NULL,'ACTIVE');

UPDATE m
SET m.description = s.description,
    m.duration_minutes = s.duration_minutes,
    m.age_rating = s.age_rating,
    m.release_date = s.release_date,
    m.end_date = s.end_date,
    m.poster_url = s.poster_url,
    m.trailer_url = s.trailer_url,
    m.status = s.status
FROM dbo.Movie m
JOIN @SeedMovie s ON s.title = m.title;

INSERT INTO dbo.Movie(
    title,description,duration_minutes,age_rating,
    release_date,end_date,poster_url,trailer_url,status
)
SELECT s.title,s.description,s.duration_minutes,s.age_rating,
       s.release_date,s.end_date,s.poster_url,s.trailer_url,s.status
FROM @SeedMovie s
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.Movie m WHERE m.title=s.title
);
GO

/* 08. MOVIE_GENRE + CINEMA_MOVIE */
DECLARE @SeedMovieGenre TABLE (
    movie_title NVARCHAR(255),
    genre_name NVARCHAR(100)
);

INSERT INTO @SeedMovieGenre(movie_title,genre_name)
VALUES
(N'The Last Horizon',N'Action'),
(N'The Last Horizon',N'Sci-Fi'),
(N'Robot Adventures',N'Animation'),
(N'Robot Adventures',N'Comedy'),
(N'Robot Adventures',N'Family'),
(N'Can Tho After Rain',N'Drama'),
(N'Can Tho After Rain',N'Romance'),
(N'Midnight Signal',N'Thriller'),
(N'Midnight Signal',N'Sci-Fi'),
(N'Mekong Rush',N'Action'),
(N'Mekong Rush',N'Adventure'),
(N'Laughing at 8PM',N'Comedy'),
(N'The Lantern Keeper',N'Drama'),
(N'The Lantern Keeper',N'Family'),
(N'Shadow Room',N'Horror'),
(N'Shadow Room',N'Thriller'),
(N'Beyond the Red Moon',N'Sci-Fi'),
(N'Beyond the Red Moon',N'Adventure'),
(N'Summer Letter 2027',N'Romance'),
(N'Summer Letter 2027',N'Drama'),
(N'Tiny Heroes: City Rescue',N'Animation'),
(N'Tiny Heroes: City Rescue',N'Family'),
(N'Tiny Heroes: City Rescue',N'Comedy');

INSERT INTO dbo.Movie_Genre(movie_id,genre_id)
SELECT m.movie_id,g.genre_id
FROM @SeedMovieGenre s
JOIN dbo.Movie m ON m.title=s.movie_title
JOIN dbo.Genre g ON g.genre_name=s.genre_name
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.Movie_Genre mg
    WHERE mg.movie_id=m.movie_id
      AND mg.genre_id=g.genre_id
);

DECLARE @SeedMovieTitle TABLE (title NVARCHAR(255) PRIMARY KEY);
INSERT INTO @SeedMovieTitle(title)
VALUES
(N'The Last Horizon'),
(N'Robot Adventures'),
(N'Can Tho After Rain'),
(N'Midnight Signal'),
(N'Mekong Rush'),
(N'Laughing at 8PM'),
(N'The Lantern Keeper'),
(N'Shadow Room'),
(N'Beyond the Red Moon'),
(N'Summer Letter 2027'),
(N'Tiny Heroes: City Rescue');

INSERT INTO dbo.Cinema_Movie(cinema_id,movie_id,status)
SELECT c.cinema_id,m.movie_id,'ACTIVE'
FROM dbo.Cinema c
CROSS JOIN @SeedMovieTitle s
JOIN dbo.Movie m ON m.title=s.title
WHERE c.cinema_name IN (N'F-Cinema Ninh Kiều',N'F-Cinema Cái Răng')
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.Cinema_Movie cm
      WHERE cm.cinema_id=c.cinema_id
        AND cm.movie_id=m.movie_id
  );
GO

/* 09. SHOWTIME */
DECLARE @SeedShowtime TABLE (
    cinema_name NVARCHAR(100),
    room_name NVARCHAR(100),
    movie_title NVARCHAR(255),
    start_time DATETIME2,
    end_time DATETIME2
);

INSERT INTO @SeedShowtime(cinema_name,room_name,movie_title,start_time,end_time)
VALUES
-- 08/10/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'The Last Horizon','2026-10-08T10:00:00','2026-10-08T12:05:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Robot Adventures','2026-10-08T13:30:00','2026-10-08T15:08:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Can Tho After Rain','2026-10-08T17:00:00','2026-10-08T18:50:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Midnight Signal','2026-10-08T20:00:00','2026-10-08T21:56:00'),

-- 10/10/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Mekong Rush','2026-10-10T09:30:00','2026-10-10T11:28:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Laughing at 8PM','2026-10-10T13:00:00','2026-10-10T14:42:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'The Lantern Keeper','2026-10-10T16:30:00','2026-10-10T18:22:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'The Last Horizon','2026-10-10T20:00:00','2026-10-10T22:05:00'),

-- 15/10/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Shadow Room','2026-10-15T10:00:00','2026-10-15T11:47:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Midnight Signal','2026-10-15T13:30:00','2026-10-15T15:26:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Mekong Rush','2026-10-15T17:00:00','2026-10-15T18:58:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Robot Adventures','2026-10-15T20:00:00','2026-10-15T21:38:00'),

-- 20/10/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Can Tho After Rain','2026-10-20T10:00:00','2026-10-20T11:50:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Laughing at 8PM','2026-10-20T13:30:00','2026-10-20T15:12:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'The Lantern Keeper','2026-10-20T17:00:00','2026-10-20T18:52:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Shadow Room','2026-10-20T20:00:00','2026-10-20T21:47:00'),

-- 25/10/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'The Last Horizon','2026-10-25T10:00:00','2026-10-25T12:05:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Robot Adventures','2026-10-25T13:30:00','2026-10-25T15:08:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Midnight Signal','2026-10-25T17:00:00','2026-10-25T18:56:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Mekong Rush','2026-10-25T20:00:00','2026-10-25T21:58:00'),

-- 01/11/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Laughing at 8PM','2026-11-01T10:00:00','2026-11-01T11:42:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'The Lantern Keeper','2026-11-01T13:30:00','2026-11-01T15:22:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Shadow Room','2026-11-01T17:00:00','2026-11-01T18:47:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Can Tho After Rain','2026-11-01T20:00:00','2026-11-01T21:50:00'),

-- 08/11/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Mekong Rush','2026-11-08T10:00:00','2026-11-08T11:58:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'The Last Horizon','2026-11-08T13:30:00','2026-11-08T15:35:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Robot Adventures','2026-11-08T17:00:00','2026-11-08T18:38:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Midnight Signal','2026-11-08T20:00:00','2026-11-08T21:56:00'),

-- 15/11/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'The Lantern Keeper','2026-11-15T10:00:00','2026-11-15T11:52:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Can Tho After Rain','2026-11-15T13:30:00','2026-11-15T15:20:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Laughing at 8PM','2026-11-15T17:00:00','2026-11-15T18:42:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Shadow Room','2026-11-15T20:00:00','2026-11-15T21:47:00'),

-- 05/12/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'The Last Horizon','2026-12-05T10:00:00','2026-12-05T12:05:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Mekong Rush','2026-12-05T13:30:00','2026-12-05T15:28:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Can Tho After Rain','2026-12-05T17:00:00','2026-12-05T18:50:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Robot Adventures','2026-12-05T20:00:00','2026-12-05T21:38:00'),

-- 20/12/2026
(N'F-Cinema Ninh Kiều',N'Room 1',N'Midnight Signal','2026-12-20T10:00:00','2026-12-20T11:56:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Laughing at 8PM','2026-12-20T13:30:00','2026-12-20T15:12:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'The Lantern Keeper','2026-12-20T17:00:00','2026-12-20T18:52:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Shadow Room','2026-12-20T20:00:00','2026-12-20T21:47:00'),

-- 10/01/2027
(N'F-Cinema Ninh Kiều',N'Room 1',N'The Last Horizon','2027-01-10T10:00:00','2027-01-10T12:05:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Robot Adventures','2027-01-10T13:30:00','2027-01-10T15:08:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'Mekong Rush','2027-01-10T17:00:00','2027-01-10T18:58:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'The Lantern Keeper','2027-01-10T20:00:00','2027-01-10T21:52:00'),

-- 07/02/2027
(N'F-Cinema Ninh Kiều',N'Room 1',N'Robot Adventures','2027-02-07T10:00:00','2027-02-07T11:38:00'),
(N'F-Cinema Ninh Kiều',N'Room 2',N'Mekong Rush','2027-02-07T13:30:00','2027-02-07T15:28:00'),
(N'F-Cinema Cái Răng',N'Room 1',N'The Lantern Keeper','2027-02-07T17:00:00','2027-02-07T18:52:00'),
(N'F-Cinema Cái Răng',N'Room 2',N'Shadow Room','2027-02-07T20:00:00','2027-02-07T21:47:00');

INSERT INTO dbo.Showtime(movie_id,room_id,start_time,end_time,status)
SELECT m.movie_id,r.room_id,s.start_time,s.end_time,'ACTIVE'
FROM @SeedShowtime s
JOIN dbo.Cinema c ON c.cinema_name=s.cinema_name
JOIN dbo.Room r
  ON r.cinema_id=c.cinema_id
 AND r.room_name=s.room_name
JOIN dbo.Movie m ON m.title=s.movie_title
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.Showtime st
    WHERE st.room_id=r.room_id
      AND st.start_time=s.start_time
);
GO

/* 10. PROMOTION */
IF NOT EXISTS (SELECT 1 FROM dbo.Promotion WHERE promo_code='WELCOME10')
    INSERT INTO dbo.Promotion(promo_code,description,discount_type,discount_value,min_order_amount,max_discount_amount,start_date,end_date,usage_limit,status)
    VALUES('WELCOME10',N'Giảm 10% cho đơn từ 100.000đ','PERCENT',10,100000,50000,'2026-01-01','2026-12-31',1000,'ACTIVE');
GO

/* 11. BOOKING + TICKET + PAYMENT */
DECLARE @CustomerId INT=(SELECT user_id FROM dbo.[User] WHERE email='customer1@fcinema.local');
DECLARE @ShowtimeId INT=(
    SELECT TOP 1 showtime_id
    FROM dbo.Showtime
    WHERE start_time > SYSDATETIME()
    ORDER BY start_time
);

IF @ShowtimeId IS NOT NULL
BEGIN
    DECLARE @Seat1 INT=(
        SELECT TOP 1 seat_id
        FROM dbo.Seat
        WHERE room_id=(SELECT room_id FROM dbo.Showtime WHERE showtime_id=@ShowtimeId)
        ORDER BY seat_row,seat_number
    );

    DECLARE @Seat2 INT=(
        SELECT TOP 1 seat_id
        FROM dbo.Seat
        WHERE room_id=(SELECT room_id FROM dbo.Showtime WHERE showtime_id=@ShowtimeId)
          AND seat_id<>@Seat1
        ORDER BY seat_row,seat_number
    );

    DECLARE @PromoId INT=(SELECT promotion_id FROM dbo.Promotion WHERE promo_code='WELCOME10');

    IF NOT EXISTS (
        SELECT 1
        FROM dbo.Booking
        WHERE customer_id=@CustomerId
          AND showtime_id=@ShowtimeId
          AND status='CONFIRMED'
    )
    BEGIN
        INSERT INTO dbo.Booking(
            customer_id,showtime_id,promotion_id,
            subtotal,discount_amount,total_amount,status
        )
        VALUES(@CustomerId,@ShowtimeId,@PromoId,160000,16000,144000,'CONFIRMED');

        DECLARE @BookingId INT=SCOPE_IDENTITY();

        INSERT INTO dbo.Ticket(
            booking_id,showtime_id,seat_id,ticket_code,ticket_price,status
        )
        VALUES
        (@BookingId,@ShowtimeId,@Seat1,CONCAT('DEMO-',@BookingId,'-01'),80000,'VALID'),
        (@BookingId,@ShowtimeId,@Seat2,CONCAT('DEMO-',@BookingId,'-02'),80000,'VALID');

        INSERT INTO dbo.Payment(
            booking_id,payment_method,merchant_reference,
            transaction_code,amount,payment_time,status
        )
        VALUES(
            @BookingId,'VNPAY',
            CONCAT('SEED-PAY-',@BookingId),
            CONCAT('VNP-DEMO-',@BookingId),
            144000,SYSDATETIME(),'SUCCESS'
        );
    END
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
SELECT user_id,full_name,email,password_hash,status
FROM dbo.[User]
ORDER BY user_id;

SELECT cinema_id,cinema_name,status
FROM dbo.Cinema
ORDER BY cinema_id;

SELECT movie_id,title,duration_minutes,age_rating,release_date,end_date,status,
       CASE
           WHEN release_date > CAST(SYSDATETIME() AS DATE)
               THEN 'COMING_SOON'
           ELSE 'NOW_SHOWING'
       END AS display_group
FROM dbo.Movie
ORDER BY release_date,movie_id;

SELECT COUNT(*) AS total_showtimes,
       MIN(start_time) AS first_showtime,
       MAX(start_time) AS last_showtime
FROM dbo.Showtime;

SELECT CAST(start_time AS DATE) AS show_date,
       COUNT(*) AS showtime_count
FROM dbo.Showtime
GROUP BY CAST(start_time AS DATE)
ORDER BY show_date;
GO
