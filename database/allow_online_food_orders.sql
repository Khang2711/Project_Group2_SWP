USE FCinemaDB;
GO

/*
    Migration: Support online food orders linked to a booking.
    Run this script once on an existing FCinemaDB database.
*/

-- Allow online orders without a staff member creating them.
ALTER TABLE dbo.Food_Order
ALTER COLUMN staff_id INT NULL;
GO

-- Add booking_id for food orders created during online ticket checkout.
IF COL_LENGTH('dbo.Food_Order', 'booking_id') IS NULL
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD booking_id INT NULL;
END;
GO

-- Add the foreign key only if it does not already exist.
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_FoodOrder_Booking'
      AND parent_object_id = OBJECT_ID('dbo.Food_Order')
)
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD CONSTRAINT FK_FoodOrder_Booking
    FOREIGN KEY (booking_id)
    REFERENCES dbo.Booking(booking_id);
END;
GO

-- Add an index to support lookup by booking_id.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_FoodOrder_BookingId'
      AND object_id = OBJECT_ID('dbo.Food_Order')
)
BEGIN
    CREATE INDEX IX_FoodOrder_BookingId
    ON dbo.Food_Order(booking_id);
END;
GO

-- Verify the resulting columns.
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dbo'
  AND TABLE_NAME = 'Food_Order'
  AND COLUMN_NAME IN ('staff_id', 'booking_id');
GO
