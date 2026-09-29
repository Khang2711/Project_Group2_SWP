/*
    FCinemaDB - SQL Server schema
    Source: latest ERD, DB Diagram, and DB Description supplied by the project team.

    Important booking rule:
    - Creating a PENDING booking/ticket does NOT reserve a seat.
    - Only VALID/USED tickets block a seat for a showtime.
    - Booking_Seat and reservation_expired_at are intentionally NOT used.
*/

IF DB_ID(N'FCinemaDB') IS NULL
BEGIN
    CREATE DATABASE FCinemaDB;
END;
GO

USE FCinemaDB;
GO

/* =========================================================
   01. Role
   ========================================================= */
CREATE TABLE dbo.[Role] (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    description NVARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Role_Status DEFAULT ('ACTIVE'),
    CONSTRAINT UQ_Role_RoleName UNIQUE (role_name)
);
GO

/* =========================================================
   02. Cinema
   ========================================================= */
CREATE TABLE dbo.Cinema (
    cinema_id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_name NVARCHAR(100) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Cinema_Status DEFAULT ('ACTIVE')
);
GO

/* =========================================================
   03. User
   ========================================================= */
CREATE TABLE dbo.[User] (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    role_id INT NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_User_Status DEFAULT ('ACTIVE'),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_User_CreatedAt DEFAULT (SYSDATETIME()),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_User_UpdatedAt DEFAULT (SYSDATETIME()),
    CONSTRAINT UQ_User_Email UNIQUE (email),
    CONSTRAINT FK_User_Role FOREIGN KEY (role_id) REFERENCES dbo.[Role](role_id)
);
GO

/* =========================================================
   04. Customer
   ========================================================= */
CREATE TABLE dbo.Customer (
    customer_id INT PRIMARY KEY,
    CONSTRAINT FK_Customer_User FOREIGN KEY (customer_id)
        REFERENCES dbo.[User](user_id)
);
GO

/* =========================================================
   05. Staff
   ========================================================= */
CREATE TABLE dbo.Staff (
    staff_id INT PRIMARY KEY,
    cinema_id INT NOT NULL,
    CONSTRAINT FK_Staff_User FOREIGN KEY (staff_id)
        REFERENCES dbo.[User](user_id),
    CONSTRAINT FK_Staff_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   06. Room
   ========================================================= */
CREATE TABLE dbo.Room (
    room_id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    room_name NVARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    room_type VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Room_Status DEFAULT ('ACTIVE'),
    CONSTRAINT CK_Room_Capacity CHECK (capacity > 0),
    CONSTRAINT UQ_Room_CinemaRoom UNIQUE (cinema_id, room_name),
    CONSTRAINT FK_Room_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   07. Seat
   ========================================================= */
CREATE TABLE dbo.Seat (
    seat_id INT IDENTITY(1,1) PRIMARY KEY,
    room_id INT NOT NULL,
    seat_row INT NOT NULL,
    seat_number INT NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Seat_Status DEFAULT ('ACTIVE'),
    CONSTRAINT CK_Seat_Row CHECK (seat_row > 0),
    CONSTRAINT CK_Seat_Number CHECK (seat_number > 0),
    CONSTRAINT UQ_Seat_RoomPosition UNIQUE (room_id, seat_row, seat_number),
    CONSTRAINT FK_Seat_Room FOREIGN KEY (room_id)
        REFERENCES dbo.Room(room_id)
);
GO

/* =========================================================
   08. Pricing
   ========================================================= */
CREATE TABLE dbo.Pricing (
    pricing_id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Pricing_Status DEFAULT ('ACTIVE'),
    CONSTRAINT CK_Pricing_Price CHECK (price >= 0),
    CONSTRAINT CK_Pricing_DateRange CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT FK_Pricing_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   09. Movie
   ========================================================= */
CREATE TABLE dbo.Movie (
    movie_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    duration_minutes INT NOT NULL,
    age_rating VARCHAR(20) NULL,
    release_date DATE NULL,
    end_date DATE NULL,
    poster_url VARCHAR(500) NULL,
    trailer_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Movie_Status DEFAULT ('ACTIVE'),
    CONSTRAINT CK_Movie_Duration CHECK (duration_minutes > 0),
    CONSTRAINT CK_Movie_DateRange CHECK (
        release_date IS NULL OR end_date IS NULL OR end_date >= release_date
    )
);
GO

/* =========================================================
   10. Genre
   ========================================================= */
CREATE TABLE dbo.Genre (
    genre_id INT IDENTITY(1,1) PRIMARY KEY,
    genre_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Genre_Status DEFAULT ('ACTIVE'),
    CONSTRAINT UQ_Genre_Name UNIQUE (genre_name)
);
GO

/* =========================================================
   11. Movie_Genre
   ========================================================= */
CREATE TABLE dbo.Movie_Genre (
    movie_id INT NOT NULL,
    genre_id INT NOT NULL,
    CONSTRAINT PK_Movie_Genre PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT FK_MovieGenre_Movie FOREIGN KEY (movie_id)
        REFERENCES dbo.Movie(movie_id),
    CONSTRAINT FK_MovieGenre_Genre FOREIGN KEY (genre_id)
        REFERENCES dbo.Genre(genre_id)
);
GO

/* =========================================================
   12. Cinema_Movie
   ========================================================= */
CREATE TABLE dbo.Cinema_Movie (
    cinema_id INT NOT NULL,
    movie_id INT NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_CinemaMovie_Status DEFAULT ('ACTIVE'),
    CONSTRAINT PK_Cinema_Movie PRIMARY KEY (cinema_id, movie_id),
    CONSTRAINT FK_CinemaMovie_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id),
    CONSTRAINT FK_CinemaMovie_Movie FOREIGN KEY (movie_id)
        REFERENCES dbo.Movie(movie_id)
);
GO

/* =========================================================
   13. Showtime
   ========================================================= */
CREATE TABLE dbo.Showtime (
    showtime_id INT IDENTITY(1,1) PRIMARY KEY,
    movie_id INT NOT NULL,
    room_id INT NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Showtime_Status DEFAULT ('ACTIVE'),
    CONSTRAINT CK_Showtime_TimeRange CHECK (end_time > start_time),
    CONSTRAINT FK_Showtime_Movie FOREIGN KEY (movie_id)
        REFERENCES dbo.Movie(movie_id),
    CONSTRAINT FK_Showtime_Room FOREIGN KEY (room_id)
        REFERENCES dbo.Room(room_id)
);
GO

/* =========================================================
   14. Promotion
   ========================================================= */
CREATE TABLE dbo.Promotion (
    promotion_id INT IDENTITY(1,1) PRIMARY KEY,
    promo_code VARCHAR(50) NOT NULL,
    description NVARCHAR(255) NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_Promotion_MinOrder DEFAULT (0),
    max_discount_amount DECIMAL(12,2) NULL,
    start_date DATETIME2 NOT NULL,
    end_date DATETIME2 NOT NULL,
    usage_limit INT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Promotion_Status DEFAULT ('ACTIVE'),
    CONSTRAINT UQ_Promotion_Code UNIQUE (promo_code),
    CONSTRAINT CK_Promotion_Value CHECK (discount_value >= 0),
    CONSTRAINT CK_Promotion_MinOrder CHECK (min_order_amount >= 0),
    CONSTRAINT CK_Promotion_MaxDiscount CHECK (max_discount_amount IS NULL OR max_discount_amount >= 0),
    CONSTRAINT CK_Promotion_UsageLimit CHECK (usage_limit IS NULL OR usage_limit >= 0),
    CONSTRAINT CK_Promotion_DateRange CHECK (end_date >= start_date),
    CONSTRAINT CK_Promotion_Type CHECK (discount_type IN ('PERCENT', 'FIXED'))
);
GO

/* =========================================================
   15. Booking
   ========================================================= */
CREATE TABLE dbo.Booking (
    booking_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    showtime_id INT NOT NULL,
    promotion_id INT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_Booking_Discount DEFAULT (0),
    total_amount DECIMAL(12,2) NOT NULL,
    booking_time DATETIME2 NOT NULL CONSTRAINT DF_Booking_Time DEFAULT (SYSDATETIME()),
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Booking_Status DEFAULT ('PENDING'),
    CONSTRAINT UQ_Booking_IdShowtime UNIQUE (booking_id, showtime_id),
    CONSTRAINT CK_Booking_Subtotal CHECK (subtotal >= 0),
    CONSTRAINT CK_Booking_Discount CHECK (discount_amount >= 0),
    CONSTRAINT CK_Booking_Total CHECK (total_amount >= 0),
    CONSTRAINT CK_Booking_Status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT FK_Booking_Customer FOREIGN KEY (customer_id)
        REFERENCES dbo.Customer(customer_id),
    CONSTRAINT FK_Booking_Showtime FOREIGN KEY (showtime_id)
        REFERENCES dbo.Showtime(showtime_id),
    CONSTRAINT FK_Booking_Promotion FOREIGN KEY (promotion_id)
        REFERENCES dbo.Promotion(promotion_id)
);
GO

/* =========================================================
   16. Ticket
   ========================================================= */
CREATE TABLE dbo.Ticket (
    ticket_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    ticket_code VARCHAR(100) NULL,
    ticket_price DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Ticket_Status DEFAULT ('PENDING'),
    checked_in_at DATETIME2 NULL,
    CONSTRAINT UQ_Ticket_BookingSeat UNIQUE (booking_id, seat_id),
    CONSTRAINT CK_Ticket_Price CHECK (ticket_price >= 0),
    CONSTRAINT CK_Ticket_Status CHECK (status IN ('PENDING', 'VALID', 'USED', 'CANCELLED')),
    CONSTRAINT FK_Ticket_BookingShowtime FOREIGN KEY (booking_id, showtime_id)
        REFERENCES dbo.Booking(booking_id, showtime_id),
    CONSTRAINT FK_Ticket_Showtime FOREIGN KEY (showtime_id)
        REFERENCES dbo.Showtime(showtime_id),
    CONSTRAINT FK_Ticket_Seat FOREIGN KEY (seat_id)
        REFERENCES dbo.Seat(seat_id)
);
GO

/* SQL Server UNIQUE permits only one NULL, so ticket_code uses a filtered unique index. */
CREATE UNIQUE INDEX UX_Ticket_TicketCode
    ON dbo.Ticket(ticket_code)
    WHERE ticket_code IS NOT NULL;
GO

/*
   Critical seat rule:
   PENDING tickets do not block seats.
   VALID and USED tickets do block a seat for a showtime.
*/
CREATE TRIGGER dbo.TR_Ticket_PreventDuplicateActiveSeat
ON dbo.Ticket
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.Ticket t WITH (UPDLOCK, HOLDLOCK)
          ON t.showtime_id = i.showtime_id
         AND t.seat_id = i.seat_id
         AND t.ticket_id <> i.ticket_id
        WHERE i.status IN ('VALID', 'USED')
          AND t.status IN ('VALID', 'USED')
    )
    BEGIN
        THROW 50001, 'Seat is already confirmed for this showtime.', 1;
    END
END;
GO

/* =========================================================
   17. Payment
   ========================================================= */
CREATE TABLE dbo.Payment (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    merchant_reference VARCHAR(100) NOT NULL,
    transaction_code VARCHAR(100) NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_time DATETIME2 NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Payment_Status DEFAULT ('PENDING'),
    refund_status VARCHAR(20) NULL,
    refund_reference VARCHAR(100) NULL,
    refunded_at DATETIME2 NULL,
    CONSTRAINT UQ_Payment_MerchantReference UNIQUE (merchant_reference),
    CONSTRAINT CK_Payment_Amount CHECK (amount >= 0),
    CONSTRAINT FK_Payment_Booking FOREIGN KEY (booking_id)
        REFERENCES dbo.Booking(booking_id)
);
GO

/* =========================================================
   18. Food_Item
   ========================================================= */
CREATE TABLE dbo.Food_Item (
    food_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    item_name NVARCHAR(100) NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    description NVARCHAR(255) NULL,
    price DECIMAL(12,2) NOT NULL,
    image_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_FoodItem_Status DEFAULT ('ACTIVE'),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_FoodItem_CreatedAt DEFAULT (SYSDATETIME()),
    CONSTRAINT CK_FoodItem_Price CHECK (price >= 0),
    CONSTRAINT FK_FoodItem_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   19. Combo
   ========================================================= */
CREATE TABLE dbo.Combo (
    combo_id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    combo_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    combo_price DECIMAL(12,2) NOT NULL,
    image_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_Combo_Status DEFAULT ('ACTIVE'),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_Combo_CreatedAt DEFAULT (SYSDATETIME()),
    CONSTRAINT CK_Combo_Price CHECK (combo_price >= 0),
    CONSTRAINT FK_Combo_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   20. Combo_Item
   ========================================================= */
CREATE TABLE dbo.Combo_Item (
    combo_id INT NOT NULL,
    food_item_id INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT PK_Combo_Item PRIMARY KEY (combo_id, food_item_id),
    CONSTRAINT CK_ComboItem_Quantity CHECK (quantity > 0),
    CONSTRAINT FK_ComboItem_Combo FOREIGN KEY (combo_id)
        REFERENCES dbo.Combo(combo_id),
    CONSTRAINT FK_ComboItem_FoodItem FOREIGN KEY (food_item_id)
        REFERENCES dbo.Food_Item(food_item_id)
);
GO

/* =========================================================
   21. Food_Order
   ========================================================= */
CREATE TABLE dbo.Food_Order (
    food_order_id INT IDENTITY(1,1) PRIMARY KEY,
    staff_id INT NOT NULL,
    cinema_id INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(50) NULL,
    payment_status VARCHAR(20) NOT NULL CONSTRAINT DF_FoodOrder_PaymentStatus DEFAULT ('PENDING'),
    order_status VARCHAR(20) NOT NULL CONSTRAINT DF_FoodOrder_Status DEFAULT ('PENDING'),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_FoodOrder_CreatedAt DEFAULT (SYSDATETIME()),
    CONSTRAINT CK_FoodOrder_Subtotal CHECK (subtotal >= 0),
    CONSTRAINT CK_FoodOrder_Total CHECK (total_amount >= 0),
    CONSTRAINT FK_FoodOrder_Staff FOREIGN KEY (staff_id)
        REFERENCES dbo.Staff(staff_id),
    CONSTRAINT FK_FoodOrder_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id)
);
GO

/* =========================================================
   22. Food_Order_Item
   ========================================================= */
CREATE TABLE dbo.Food_Order_Item (
    food_order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    food_order_id INT NOT NULL,
    food_item_id INT NULL,
    combo_id INT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT CK_FoodOrderItem_ExactlyOneProduct CHECK (
        (food_item_id IS NOT NULL AND combo_id IS NULL)
        OR
        (food_item_id IS NULL AND combo_id IS NOT NULL)
    ),
    CONSTRAINT CK_FoodOrderItem_Quantity CHECK (quantity > 0),
    CONSTRAINT CK_FoodOrderItem_UnitPrice CHECK (unit_price >= 0),
    CONSTRAINT CK_FoodOrderItem_LineTotal CHECK (line_total >= 0),
    CONSTRAINT FK_FoodOrderItem_Order FOREIGN KEY (food_order_id)
        REFERENCES dbo.Food_Order(food_order_id),
    CONSTRAINT FK_FoodOrderItem_Food FOREIGN KEY (food_item_id)
        REFERENCES dbo.Food_Item(food_item_id),
    CONSTRAINT FK_FoodOrderItem_Combo FOREIGN KEY (combo_id)
        REFERENCES dbo.Combo(combo_id)
);
GO

/* =========================================================
   Supporting indexes
   ========================================================= */
CREATE INDEX IX_User_RoleId ON dbo.[User](role_id);
CREATE INDEX IX_Staff_CinemaId ON dbo.Staff(cinema_id);
CREATE INDEX IX_Room_CinemaId ON dbo.Room(cinema_id);
CREATE INDEX IX_Seat_RoomId ON dbo.Seat(room_id);
CREATE INDEX IX_Pricing_CinemaSeatType ON dbo.Pricing(cinema_id, seat_type);
CREATE INDEX IX_Showtime_RoomStartTime ON dbo.Showtime(room_id, start_time);
CREATE INDEX IX_Showtime_MovieStartTime ON dbo.Showtime(movie_id, start_time);
CREATE INDEX IX_Booking_CustomerId ON dbo.Booking(customer_id);
CREATE INDEX IX_Booking_ShowtimeId ON dbo.Booking(showtime_id);
CREATE INDEX IX_Ticket_ShowtimeSeat ON dbo.Ticket(showtime_id, seat_id);
CREATE INDEX IX_Payment_BookingId ON dbo.Payment(booking_id);
CREATE INDEX IX_FoodItem_CinemaId ON dbo.Food_Item(cinema_id);
CREATE INDEX IX_Combo_CinemaId ON dbo.Combo(cinema_id);
CREATE INDEX IX_FoodOrder_CinemaCreatedAt ON dbo.Food_Order(cinema_id, created_at);
CREATE INDEX IX_FoodOrder_StaffId ON dbo.Food_Order(staff_id);
GO

/* =========================================================
   Business rules intentionally handled by Service/transaction
   =========================================================
   1. Active pricing periods for the same cinema + seat type must not overlap.
   2. Active showtimes in the same room must not overlap.
   3. A Showtime movie must be offered by the room's cinema (Cinema_Movie).
   4. Combo and Combo_Item food products must belong to the same cinema.
   5. Food_Order staff must be assigned to the same cinema as the order.
   6. Food_Order_Item product/combo must belong to the order's cinema.
   7. line_total should equal quantity * unit_price.
   8. Before changing Ticket from PENDING to VALID, payment flow must re-check
      seat availability inside the same database transaction.
*/
GO
