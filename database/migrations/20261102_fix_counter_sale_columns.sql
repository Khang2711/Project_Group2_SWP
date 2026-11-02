/*
    FCinemaDB migration: add counter-sale columns missing from the base schema.
    Safe for existing databases: adds only columns/foreign keys/indexes that are absent.
    Existing rows are preserved; new columns are nullable by design.
*/
USE FCinemaDB;
GO

/* 1. Booking.staff_id: NULL for bookings not created by counter staff. */
IF COL_LENGTH(N'dbo.Booking', N'staff_id') IS NULL
BEGIN
    ALTER TABLE dbo.Booking
    ADD staff_id INT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns pc
      ON pc.object_id = fkc.parent_object_id
     AND pc.column_id = fkc.parent_column_id
    JOIN sys.columns rc
      ON rc.object_id = fkc.referenced_object_id
     AND rc.column_id = fkc.referenced_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.Booking')
      AND pc.name = N'staff_id'
      AND fkc.referenced_object_id = OBJECT_ID(N'dbo.Staff')
      AND rc.name = N'staff_id'
)
BEGIN
    ALTER TABLE dbo.Booking
    ADD CONSTRAINT FK_Booking_Staff
        FOREIGN KEY (staff_id) REFERENCES dbo.Staff(staff_id);
END;
GO

/* 2. Food_Order.booking_id: NULL for standalone food/combo orders. */
IF COL_LENGTH(N'dbo.Food_Order', N'booking_id') IS NULL
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD booking_id INT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns pc
      ON pc.object_id = fkc.parent_object_id
     AND pc.column_id = fkc.parent_column_id
    JOIN sys.columns rc
      ON rc.object_id = fkc.referenced_object_id
     AND rc.column_id = fkc.referenced_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.Food_Order')
      AND pc.name = N'booking_id'
      AND fkc.referenced_object_id = OBJECT_ID(N'dbo.Booking')
      AND rc.name = N'booking_id'
)
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD CONSTRAINT FK_FoodOrder_Booking
        FOREIGN KEY (booking_id) REFERENCES dbo.Booking(booking_id);
END;
GO

/* 3. Supporting indexes. */
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Booking')
      AND name = N'IX_Booking_StaffId'
)
BEGIN
    CREATE INDEX IX_Booking_StaffId ON dbo.Booking(staff_id);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.Food_Order')
      AND name = N'IX_FoodOrder_BookingId'
)
BEGIN
    CREATE INDEX IX_FoodOrder_BookingId ON dbo.Food_Order(booking_id);
END;
GO

/* 4. Verify resulting columns. */
SELECT N'Booking' AS table_name, N'staff_id' AS column_name,
       COL_LENGTH(N'dbo.Booking', N'staff_id') AS column_length
UNION ALL
SELECT N'Food_Order', N'booking_id',
       COL_LENGTH(N'dbo.Food_Order', N'booking_id');
GO
