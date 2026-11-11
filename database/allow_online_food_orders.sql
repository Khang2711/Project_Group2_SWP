
USE FCinemaDB;
GO

-- Cho phép đơn đặt vé online không có nhân viên tạo
ALTER TABLE dbo.Food_Order
ALTER COLUMN staff_id INT NULL;
GO

-- Thêm booking_id nếu database cũ chưa có
IF COL_LENGTH('dbo.Food_Order', 'booking_id') IS NULL
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD booking_id INT NULL;
END;
GO

-- Bổ sung khóa ngoại nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_FoodOrder_Booking'
)
BEGIN
    ALTER TABLE dbo.Food_Order
    ADD CONSTRAINT FK_FoodOrder_Booking
    FOREIGN KEY (booking_id)
    REFERENCES dbo.Booking(booking_id);
END;
GO

-- Bổ sung index nếu chưa có
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

-- Kiểm tra kết quả
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Food_Order'
  AND COLUMN_NAME IN ('staff_id', 'booking_id');
GO
