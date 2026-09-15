IF DB_ID('FCinemaDB') IS NULL
    CREATE DATABASE FCinemaDB;
GO

USE FCinemaDB;
GO

IF OBJECT_ID('tblTickets') IS NOT NULL DROP TABLE tblTickets;
IF OBJECT_ID('tblOrderCombos') IS NOT NULL DROP TABLE tblOrderCombos;
IF OBJECT_ID('tblOrders') IS NOT NULL DROP TABLE tblOrders;
IF OBJECT_ID('tblCombos') IS NOT NULL DROP TABLE tblCombos;
IF OBJECT_ID('tblShowtimes') IS NOT NULL DROP TABLE tblShowtimes;
IF OBJECT_ID('tblRooms') IS NOT NULL DROP TABLE tblRooms;
IF OBJECT_ID('tblMovies') IS NOT NULL DROP TABLE tblMovies;
IF OBJECT_ID('tblUsers') IS NOT NULL DROP TABLE tblUsers;
GO

CREATE TABLE tblUsers (
    userID NVARCHAR(50) PRIMARY KEY,
    fullName NVARCHAR(100) NOT NULL,
    password VARCHAR(32) NOT NULL,
    roleID VARCHAR(2) NOT NULL
);

CREATE TABLE tblMovies (
    movieID VARCHAR(20) PRIMARY KEY,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000),
    duration INT NOT NULL,
    basePrice FLOAT NOT NULL,
    status BIT NOT NULL,
    imageURL NVARCHAR(1000)
);

CREATE TABLE tblRooms (
    roomID VARCHAR(20) PRIMARY KEY,
    roomName NVARCHAR(100) NOT NULL,
    totalSeats INT NOT NULL,
    status BIT NOT NULL
);

CREATE TABLE tblCombos (
    comboID VARCHAR(20) PRIMARY KEY,
    comboName NVARCHAR(100) NOT NULL,
    price FLOAT NOT NULL,
    status BIT NOT NULL
);

CREATE TABLE tblShowtimes (
    showtimeID VARCHAR(20) PRIMARY KEY,
    movieID VARCHAR(20) NOT NULL,
    roomID VARCHAR(20) NOT NULL,
    showDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    status BIT NOT NULL,
    FOREIGN KEY (movieID) REFERENCES tblMovies(movieID),
    FOREIGN KEY (roomID) REFERENCES tblRooms(roomID)
);

CREATE TABLE tblOrders (
    orderID VARCHAR(30) PRIMARY KEY,
    userID NVARCHAR(50) NOT NULL,
    orderDate DATETIME NOT NULL,
    totalAmount FLOAT NOT NULL,
    status BIT NOT NULL,
    FOREIGN KEY (userID) REFERENCES tblUsers(userID)
);

CREATE TABLE tblOrderCombos (
    orderComboID VARCHAR(40) PRIMARY KEY,
    orderID VARCHAR(30) NOT NULL,
    comboID VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    price FLOAT NOT NULL,
    FOREIGN KEY (orderID) REFERENCES tblOrders(orderID),
    FOREIGN KEY (comboID) REFERENCES tblCombos(comboID)
);

CREATE TABLE tblTickets (
    ticketID VARCHAR(40) PRIMARY KEY,
    orderID VARCHAR(30) NOT NULL,
    showtimeID VARCHAR(20) NOT NULL,
    seatID VARCHAR(10) NOT NULL,
    price FLOAT NOT NULL,
    status BIT NOT NULL,
    FOREIGN KEY (orderID) REFERENCES tblOrders(orderID),
    FOREIGN KEY (showtimeID) REFERENCES tblShowtimes(showtimeID),
    CONSTRAINT UQ_Ticket_Seat UNIQUE(showtimeID, seatID)
);
GO

INSERT INTO tblUsers(userID, fullName, password, roleID) VALUES
('admin', N'Quản trị viên F-Cinema', 'e10adc3949ba59abbe56e057f20f883e', 'AD'),
('khang', N'Lê Hoàng Khang', 'e10adc3949ba59abbe56e057f20f883e', 'US'),
('user01', N'Nguyễn Minh Anh', 'e10adc3949ba59abbe56e057f20f883e', 'US');

INSERT INTO tblRooms(roomID, roomName, totalSeats, status) VALUES
('R01', N'Phòng Cam Premium', 60, 1),
('R02', N'Phòng Blue Dolby', 60, 1),
('R03', N'Phòng Green Couple', 60, 1),
('R04', N'Phòng Maintenance Test', 60, 0);

INSERT INTO tblCombos(comboID, comboName, price, status) VALUES
('combo-popcorn', N'Combo Solo: 1 bắp vừa + 1 nước vừa', 45000, 1),
('combo-couple', N'Combo Couple: 1 bắp lớn + 2 nước vừa', 89000, 1),
('combo-family', N'Combo Family: 2 bắp lớn + 4 nước vừa', 149000, 1);

INSERT INTO tblMovies(movieID, title, description, duration, basePrice, status) VALUES
('M001', N'Lật Mặt 8', N'Phim hành động Việt Nam.', 120, 85000, 1),
('M002', N'Mai', N'Phim tâm lý tình cảm.', 131, 90000, 1),
('M003', N'Địa Đạo', N'Phim lịch sử chiến tranh.', 128, 95000, 1),
('M004', N'Inside Out 2', N'Phim hoạt hình gia đình.', 96, 80000, 1),
('M005', N'Dune: Part Two', N'Phim khoa học viễn tưởng.', 166, 110000, 1),
('M006', N'Code Runner', N'Một lập trình viên phát hiện lỗi hệ thống có thể thay đổi cả thành phố.', 118, 90000, 1),
('M007', N'Bếp Nhà Ngoại', N'Hành trình chữa lành qua những món ăn gia đình miền Tây.', 98, 65000, 1),
('M008', N'Vòng Lặp Nửa Đêm', N'Phim trinh thám hồi hộp về một vụ án lặp lại mỗi đêm.', 109, 85000, 1),
('M009', N'Robot Và Biển Xanh', N'Phim phiêu lưu gia đình.', 105, 78000, 1),
('M010', N'Ký Ức Sài Gòn', N'Phim tâm lý xã hội.', 112, 82000, 0),
('M011', N'Bão Trên Đảo Ngọc', N'Phim phiêu lưu sinh tồn.', 115, 88000, 1),
('M012', N'Hành Tinh Cam', N'Phim khoa học viễn tưởng.', 122, 92000, 1),
('M013', N'Chuyến Xe Đêm', N'Phim kinh dị nhẹ.', 101, 76000, 1),
('M014', N'Tuổi Trẻ Rực Rỡ', N'Phim học đường.', 108, 79000, 1),
('M015', N'Đội Điều Tra 404', N'Phim hành động trinh thám.', 117, 87000, 1),
('M016', N'Ngày Xanh Trở Lại', N'Phim gia đình.', 104, 72000, 1);

INSERT INTO tblShowtimes(showtimeID, movieID, roomID, showDate, startTime, endTime, status) VALUES
-- Dùng ngày tương đối để khi chạy database luôn có lịch chiếu trong tương lai.
('ST001', 'M001', 'R01', DATEADD(DAY, 1, CAST(GETDATE() AS date)), '09:00', '11:00', 1),
('ST002', 'M001', 'R02', DATEADD(DAY, 1, CAST(GETDATE() AS date)), '14:00', '16:00', 1),
('ST003', 'M002', 'R03', DATEADD(DAY, 1, CAST(GETDATE() AS date)), '18:30', '20:41', 1),
('ST004', 'M003', 'R01', DATEADD(DAY, 2, CAST(GETDATE() AS date)), '10:00', '12:08', 1),
('ST005', 'M004', 'R02', DATEADD(DAY, 2, CAST(GETDATE() AS date)), '13:30', '15:06', 1),
('ST006', 'M005', 'R03', DATEADD(DAY, 2, CAST(GETDATE() AS date)), '18:00', '20:46', 1),
('ST007', 'M006', 'R01', DATEADD(DAY, 3, CAST(GETDATE() AS date)), '09:30', '11:28', 1),
('ST008', 'M007', 'R02', DATEADD(DAY, 3, CAST(GETDATE() AS date)), '15:00', '16:38', 1),
('ST009', 'M009', 'R03', DATEADD(DAY, 3, CAST(GETDATE() AS date)), '19:30', '21:15', 1),
('ST010', 'M011', 'R01', DATEADD(DAY, 4, CAST(GETDATE() AS date)), '10:30', '12:25', 1),
('ST011', 'M012', 'R02', DATEADD(DAY, 4, CAST(GETDATE() AS date)), '16:00', '18:02', 1),
('ST012', 'M013', 'R03', DATEADD(DAY, 4, CAST(GETDATE() AS date)), '20:00', '21:41', 1);

INSERT INTO tblOrders(orderID, userID, orderDate, totalAmount, status) VALUES
('ODDEMO01', 'khang', GETDATE(), 215000, 1);

INSERT INTO tblOrderCombos(orderComboID, orderID, comboID, quantity, price) VALUES
('OCDEMO0101', 'ODDEMO01', 'combo-popcorn', 1, 45000);

INSERT INTO tblTickets(ticketID, orderID, showtimeID, seatID, price, status) VALUES
('TKDEMO0101', 'ODDEMO01', 'ST001', 'A1', 85000, 1),
('TKDEMO0102', 'ODDEMO01', 'ST001', 'A2', 85000, 1);
GO

-- Tài khoản demo:
-- admin / 123456
-- khang / 123456
-- user01 / 123456
