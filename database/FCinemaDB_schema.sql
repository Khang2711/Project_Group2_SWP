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
    booking_id INT NULL,
    CONSTRAINT CK_FoodOrder_Subtotal CHECK (subtotal >= 0),
    CONSTRAINT CK_FoodOrder_Total CHECK (total_amount >= 0),
    CONSTRAINT FK_FoodOrder_Staff FOREIGN KEY (staff_id)
        REFERENCES dbo.Staff(staff_id),
    CONSTRAINT FK_FoodOrder_Cinema FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id),
    CONSTRAINT FK_FoodOrder_Booking FOREIGN KEY (booking_id)
        REFERENCES dbo.Booking(booking_id)
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
CREATE INDEX IX_Pricing_RuleLookup
    ON dbo.Pricing(
        cinema_id,
        seat_type,
        status,
        start_date
    );
CREATE INDEX IX_Showtime_RoomStartTime ON dbo.Showtime(room_id, start_time);
CREATE INDEX IX_Showtime_MovieStartTime ON dbo.Showtime(movie_id, start_time);
CREATE INDEX IX_Booking_CustomerId ON dbo.Booking(customer_id);
CREATE INDEX IX_Booking_StaffId ON dbo.Booking(staff_id);
CREATE INDEX IX_Booking_ShowtimeId ON dbo.Booking(showtime_id);
CREATE INDEX IX_Ticket_ShowtimeSeat ON dbo.Ticket(showtime_id, seat_id);
CREATE INDEX IX_Payment_BookingId ON dbo.Payment(booking_id);
CREATE INDEX IX_FoodItem_CinemaId ON dbo.Food_Item(cinema_id);
CREATE INDEX IX_Combo_CinemaId ON dbo.Combo(cinema_id);
CREATE INDEX IX_FoodOrder_CinemaCreatedAt ON dbo.Food_Order(cinema_id, created_at);
CREATE INDEX IX_FoodOrder_StaffId ON dbo.Food_Order(staff_id);
CREATE INDEX IX_FoodOrder_BookingId ON dbo.Food_Order(booking_id);
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
