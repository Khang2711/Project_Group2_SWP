-- Chỉ chạy trên database mới/trống; không xóa bảng hoặc dữ liệu cũ.
IF DB_ID('FCinemaDB') IS NULL
    CREATE DATABASE FCinemaDB;
GO
USE FCinemaDB;
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

