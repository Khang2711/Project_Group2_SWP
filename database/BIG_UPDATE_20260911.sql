USE FCinemaDB;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID('tblCinemas') IS NULL CREATE TABLE tblCinemas(cinemaID varchar(20) PRIMARY KEY,cinemaName nvarchar(120) NOT NULL,address nvarchar(250) NOT NULL,phone varchar(20),status bit NOT NULL DEFAULT 1);
IF NOT EXISTS(SELECT 1 FROM tblCinemas WHERE cinemaID='C01') INSERT INTO tblCinemas VALUES('C01',N'F-Cinema Cần Thơ',N'Cần Thơ','',1);
IF COL_LENGTH('tblUsers','status') IS NULL ALTER TABLE tblUsers ADD status bit NOT NULL DEFAULT 1;
IF COL_LENGTH('tblUsers','email') IS NULL ALTER TABLE tblUsers ADD email nvarchar(150) NULL;
IF COL_LENGTH('tblUsers','phone') IS NULL ALTER TABLE tblUsers ADD phone varchar(20) NULL;
IF COL_LENGTH('tblUsers','avatar') IS NULL ALTER TABLE tblUsers ADD avatar nvarchar(500) NULL;
IF COL_LENGTH('tblUsers','birthDate') IS NULL ALTER TABLE tblUsers ADD birthDate date NULL;
IF COL_LENGTH('tblUsers','cinemaID') IS NULL ALTER TABLE tblUsers ADD cinemaID varchar(20) NULL REFERENCES tblCinemas(cinemaID);
IF COL_LENGTH('tblRooms','cinemaID') IS NULL ALTER TABLE tblRooms ADD cinemaID varchar(20) NOT NULL DEFAULT 'C01' REFERENCES tblCinemas(cinemaID);
IF COL_LENGTH('tblMovies','ageRating') IS NULL ALTER TABLE tblMovies ADD ageRating int NOT NULL DEFAULT 0;
IF COL_LENGTH('tblMovies','screeningStart') IS NULL ALTER TABLE tblMovies ADD screeningStart date NULL;
IF COL_LENGTH('tblMovies','screeningEnd') IS NULL ALTER TABLE tblMovies ADD screeningEnd date NULL;
IF COL_LENGTH('tblTickets','checkedInAt') IS NULL ALTER TABLE tblTickets ADD checkedInAt datetime2 NULL;
IF COL_LENGTH('tblTickets','checkedInBy') IS NULL ALTER TABLE tblTickets ADD checkedInBy nvarchar(50) NULL;
IF COL_LENGTH('tblOrders','paymentState') IS NULL ALTER TABLE tblOrders ADD paymentState varchar(20) NOT NULL DEFAULT 'PAID';
IF COL_LENGTH('tblOrders','expiresAt') IS NULL ALTER TABLE tblOrders ADD expiresAt datetime2 NULL;
IF COL_LENGTH('tblOrders','showtimeID') IS NULL ALTER TABLE tblOrders ADD showtimeID varchar(20) NULL;
IF COL_LENGTH('tblOrders','promoCode') IS NULL ALTER TABLE tblOrders ADD promoCode varchar(30) NULL;
IF COL_LENGTH('tblOrders','discount') IS NULL ALTER TABLE tblOrders ADD discount decimal(18,0) NOT NULL DEFAULT 0;
IF OBJECT_ID('tblSeats') IS NULL CREATE TABLE tblSeats(seatKey varchar(40) PRIMARY KEY,roomID varchar(20) NOT NULL REFERENCES tblRooms(roomID),seatID varchar(10) NOT NULL,seatType varchar(10) NOT NULL CHECK(seatType IN('Standard','VIP','Couple')),status bit NOT NULL DEFAULT 1,UNIQUE(roomID,seatID));
IF OBJECT_ID('tblPricing') IS NULL CREATE TABLE tblPricing(pricingID varchar(20) PRIMARY KEY,cinemaID varchar(20) NOT NULL REFERENCES tblCinemas(cinemaID),seatType varchar(10) NOT NULL CHECK(seatType IN('Standard','VIP','Couple')),price decimal(18,0) NOT NULL CHECK(price>0),validFrom date NOT NULL,validTo date NOT NULL,status bit NOT NULL DEFAULT 1,CHECK(validTo>=validFrom));
IF OBJECT_ID('tblPromotions') IS NULL CREATE TABLE tblPromotions(promoCode varchar(30) PRIMARY KEY,cinemaID varchar(20) NOT NULL REFERENCES tblCinemas(cinemaID),promoName nvarchar(120) NOT NULL,discountType varchar(10) NOT NULL CHECK(discountType IN('PERCENT','FIXED')),discountValue decimal(18,0) NOT NULL CHECK(discountValue>0),minAmount decimal(18,0) NOT NULL DEFAULT 0,validFrom date NOT NULL,validTo date NOT NULL,usageLimit int NOT NULL CHECK(usageLimit>0),usedCount int NOT NULL DEFAULT 0,status bit NOT NULL DEFAULT 1,CHECK(validTo>=validFrom),CHECK(discountType<>'PERCENT' OR discountValue<=100));
IF OBJECT_ID('tblReservations') IS NULL CREATE TABLE tblReservations(orderID varchar(30) NOT NULL REFERENCES tblOrders(orderID),showtimeID varchar(20) NOT NULL REFERENCES tblShowtimes(showtimeID),seatID varchar(10) NOT NULL,price decimal(18,0) NOT NULL,PRIMARY KEY(orderID,seatID),UNIQUE(showtimeID,seatID));
IF OBJECT_ID('tblPayments') IS NULL CREATE TABLE tblPayments(paymentID varchar(50) PRIMARY KEY,orderID varchar(30) NOT NULL REFERENCES tblOrders(orderID),amount decimal(18,0) NOT NULL,resultCode varchar(10) NOT NULL,paidAt datetime2 NOT NULL DEFAULT SYSDATETIME());
IF OBJECT_ID('tblPasswordResets') IS NULL CREATE TABLE tblPasswordResets(tokenHash varchar(64) PRIMARY KEY,userID nvarchar(50) NOT NULL REFERENCES tblUsers(userID),expiresAt datetime2 NOT NULL,used bit NOT NULL DEFAULT 0);
IF OBJECT_ID('tblCinemaMovies') IS NULL CREATE TABLE tblCinemaMovies(cinemaID varchar(20) NOT NULL REFERENCES tblCinemas(cinemaID),movieID varchar(20) NOT NULL REFERENCES tblMovies(movieID),PRIMARY KEY(cinemaID,movieID));
ALTER TABLE tblUsers ALTER COLUMN password varchar(255) NOT NULL;
COMMIT;
GO
-- Fill physical seats for existing rooms only; no data reset.
;WITH nums AS(SELECT 1 n UNION ALL SELECT n+1 FROM nums WHERE n<260)
INSERT INTO tblSeats(seatKey,roomID,seatID,seatType,status)
SELECT r.roomID+'-'+CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2)),r.roomID,CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2)),'Standard',1 FROM tblRooms r JOIN nums ON n<=r.totalSeats
WHERE NOT EXISTS(SELECT 1 FROM tblSeats x WHERE x.roomID=r.roomID AND x.seatID=CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2))) OPTION(MAXRECURSION 260);
INSERT INTO tblCinemaMovies SELECT 'C01',movieID FROM tblMovies m WHERE NOT EXISTS(SELECT 1 FROM tblCinemaMovies cm WHERE cm.cinemaID='C01' AND cm.movieID=m.movieID);
GO
-- Legacy AD now means System Administrator ONLY. Create a separate manager through /system/users.
-- No automatic role escalation and no newly seeded passwords.
-- The new rules are enforced for app writes and direct SQL updates.
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='UX_User_Email') CREATE UNIQUE INDEX UX_User_Email ON tblUsers(email) WHERE email IS NOT NULL;
GO
CREATE OR ALTER TRIGGER tr_FCinema_ShowtimeRules ON tblShowtimes AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.showtimeID=i.showtimeID WHERE (i.movieID<>d.movieID OR i.roomID<>d.roomID OR i.showDate<>d.showDate OR i.startTime<>d.startTime OR i.endTime<>d.endTime OR i.status<>d.status) AND (EXISTS(SELECT 1 FROM tblTickets t WHERE t.showtimeID=i.showtimeID AND t.status=1) OR EXISTS(SELECT 1 FROM tblReservations re JOIN tblOrders o ON o.orderID=re.orderID WHERE re.showtimeID=i.showtimeID AND o.expiresAt>SYSDATETIME())))
 THROW 51001,N'Suất chiếu đã có vé hoặc đang giữ ghế; không thể sửa hoặc hủy.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN tblMovies m ON m.movieID=i.movieID JOIN tblRooms r ON r.roomID=i.roomID JOIN tblCinemas c ON c.cinemaID=r.cinemaID WHERE i.status=1 AND (m.status=0 OR r.status=0 OR c.status=0 OR i.endTime<=i.startTime OR DATEDIFF(MINUTE,i.startTime,i.endTime)<>m.duration OR (m.screeningStart IS NOT NULL AND i.showDate<m.screeningStart) OR (m.screeningEnd IS NOT NULL AND i.showDate>m.screeningEnd)))
 THROW 51002,N'Phim, rạp, phòng, thời lượng hoặc kỳ công chiếu không hợp lệ.',1;
 -- Lock each affected room to serialize competing schedule writes.
 DECLARE @lock int;SELECT @lock=COUNT(*) FROM tblRooms WITH(UPDLOCK,HOLDLOCK) WHERE roomID IN(SELECT roomID FROM inserted);
 IF EXISTS(SELECT 1 FROM inserted i JOIN tblShowtimes s ON s.roomID=i.roomID AND s.showtimeID<>i.showtimeID AND s.status=1 WHERE i.status=1 AND
 DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),i.startTime),CAST(i.showDate AS datetime2))<DATEADD(MINUTE,15,DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),s.endTime),CAST(s.showDate AS datetime2))) AND
 DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),s.startTime),CAST(s.showDate AS datetime2))<DATEADD(MINUTE,15,DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00' AS time),i.endTime),CAST(i.showDate AS datetime2))))
 THROW 51003,N'Lịch chiếu bị trùng hoặc chưa đủ 15 phút dọn phòng.',1;
 INSERT INTO tblCinemaMovies(cinemaID,movieID) SELECT DISTINCT r.cinemaID,i.movieID FROM inserted i JOIN tblRooms r ON r.roomID=i.roomID WHERE NOT EXISTS(SELECT 1 FROM tblCinemaMovies cm WHERE cm.cinemaID=r.cinemaID AND cm.movieID=i.movieID);
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_RoomRules ON tblRooms AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted WHERE totalSeats<1 OR totalSeats>260) THROW 51004,N'Phòng cần từ 1 đến 260 ghế.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.roomID=i.roomID WHERE (i.totalSeats<>d.totalSeats OR i.cinemaID<>d.cinemaID OR i.status<>d.status) AND EXISTS(SELECT 1 FROM tblShowtimes s WHERE s.roomID=i.roomID AND s.showDate>=CAST(GETDATE() AS date) AND (EXISTS(SELECT 1 FROM tblTickets t WHERE t.showtimeID=s.showtimeID AND t.status=1) OR EXISTS(SELECT 1 FROM tblReservations re WHERE re.showtimeID=s.showtimeID)))) THROW 51005,N'Phòng có vé hoặc ghế đang giữ; không thể đổi sức chứa, rạp hoặc trạng thái.',1;
 DELETE se FROM tblSeats se JOIN inserted i ON i.roomID=se.roomID WHERE (ASCII(LEFT(se.seatID,1))-65)*10+TRY_CAST(SUBSTRING(se.seatID,2,9) AS int)>i.totalSeats;
 ;WITH nums AS(SELECT 1 n UNION ALL SELECT n+1 FROM nums WHERE n<260)
 INSERT INTO tblSeats(seatKey,roomID,seatID,seatType,status)
 SELECT i.roomID+'-'+CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2)),i.roomID,CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2)),'Standard',1 FROM inserted i JOIN nums ON n<=i.totalSeats WHERE NOT EXISTS(SELECT 1 FROM tblSeats se WHERE se.roomID=i.roomID AND se.seatID=CHAR(65+(n-1)/10)+CAST((n-1)%10+1 AS varchar(2))) OPTION(MAXRECURSION 260);
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_PricingRules ON tblPricing AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 DECLARE @lock int;SELECT @lock=COUNT(*) FROM tblCinemas WITH(UPDLOCK,HOLDLOCK) WHERE cinemaID IN(SELECT cinemaID FROM inserted);
 IF EXISTS(SELECT 1 FROM inserted i JOIN tblPricing p ON p.cinemaID=i.cinemaID AND p.seatType=i.seatType AND p.pricingID<>i.pricingID WHERE i.status=1 AND p.status=1 AND i.validFrom<=p.validTo AND p.validFrom<=i.validTo) THROW 51006,N'Chính sách giá trùng kỳ hiệu lực cho cùng rạp và loại ghế.',1;
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_MovieRules ON tblMovies AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted WHERE duration<=0 OR basePrice<=0 OR ageRating NOT IN(0,13,16,18) OR screeningEnd<screeningStart) THROW 51007,N'Thời lượng, giá, độ tuổi hoặc kỳ công chiếu không hợp lệ.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN tblShowtimes s ON s.movieID=i.movieID WHERE s.status=1 AND s.showDate>=CAST(GETDATE() AS date) AND (i.screeningStart>s.showDate OR i.screeningEnd<s.showDate OR DATEDIFF(MINUTE,s.startTime,s.endTime)<>i.duration)) THROW 51008,N'Thay đổi phim không khớp lịch chiếu đang hoạt động.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.movieID=i.movieID WHERE i.status=0 AND d.status=1 AND EXISTS(SELECT 1 FROM tblShowtimes s JOIN tblTickets t ON t.showtimeID=s.showtimeID WHERE s.movieID=i.movieID AND s.showDate>=CAST(GETDATE() AS date) AND t.status=1)) THROW 51009,N'Phim có vé cho suất chưa kết thúc; không thể ngừng hoạt động.',1;
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_CinemaRules ON tblCinemas AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.cinemaID=i.cinemaID WHERE i.status=0 AND d.status=1 AND EXISTS(SELECT 1 FROM tblRooms r JOIN tblShowtimes s ON s.roomID=r.roomID WHERE r.cinemaID=i.cinemaID AND s.showDate>=CAST(GETDATE() AS date) AND (EXISTS(SELECT 1 FROM tblTickets t WHERE t.showtimeID=s.showtimeID AND t.status=1) OR EXISTS(SELECT 1 FROM tblReservations re WHERE re.showtimeID=s.showtimeID)))) THROW 51010,N'Rạp có vé hoặc ghế đang giữ; không thể ngừng hoạt động.',1;
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_PromoRules ON tblPromotions AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.promoCode=i.promoCode WHERE (i.cinemaID<>d.cinemaID OR i.discountType<>d.discountType OR i.discountValue<>d.discountValue OR i.minAmount<>d.minAmount OR i.validFrom<>d.validFrom OR i.validTo<>d.validTo OR i.usageLimit<>d.usageLimit) AND EXISTS(SELECT 1 FROM tblOrders o WHERE o.promoCode=i.promoCode AND o.paymentState='PENDING' AND o.expiresAt>SYSDATETIME())) THROW 51011,N'Ưu đãi đang áp dụng cho đơn chờ thanh toán. Vui lòng chờ đơn hoàn tất hoặc hết hạn.',1;
END;
GO
CREATE OR ALTER TRIGGER tr_FCinema_SeatRules ON tblSeats AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN tblRooms r ON r.roomID=i.roomID WHERE LEN(i.seatID) NOT BETWEEN 2 AND 3 OR LEFT(i.seatID,1) COLLATE Latin1_General_100_BIN2 NOT BETWEEN 'A' AND 'Z' OR TRY_CAST(SUBSTRING(i.seatID,2,9) AS int) NOT BETWEEN 1 AND 10 OR TRY_CAST(SUBSTRING(i.seatID,2,9) AS int) IS NULL OR (ASCII(LEFT(i.seatID,1))-65)*10+TRY_CAST(SUBSTRING(i.seatID,2,9) AS int)>r.totalSeats) THROW 51012,N'Số ghế phải từ A1 đến Z10 và nằm trong sức chứa phòng.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.seatKey=i.seatKey WHERE (i.roomID<>d.roomID OR i.seatID<>d.seatID OR i.seatType<>d.seatType OR i.status<>d.status) AND EXISTS(SELECT 1 FROM tblShowtimes s WHERE s.roomID=d.roomID AND s.showDate>=CAST(GETDATE() AS date) AND (EXISTS(SELECT 1 FROM tblTickets t WHERE t.showtimeID=s.showtimeID AND t.seatID=d.seatID AND t.status=1) OR EXISTS(SELECT 1 FROM tblReservations re WHERE re.showtimeID=s.showtimeID AND re.seatID=d.seatID)))) THROW 51013,N'Ghế có vé hoặc đang được giữ; không thể thay đổi.',1;
END;
GO
