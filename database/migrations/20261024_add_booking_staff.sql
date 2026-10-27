USE FCinemaDB;
GO

-- Vé online và các đơn cũ có thể để staff_id là NULL.
IF COL_LENGTH(N'dbo.Booking', N'staff_id') IS NULL
BEGIN
    ALTER TABLE dbo.Booking
    ADD staff_id INT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'FK_Booking_Staff'
      AND parent_object_id = OBJECT_ID(N'dbo.Booking')
)
BEGIN
    ALTER TABLE dbo.Booking WITH CHECK
    ADD CONSTRAINT FK_Booking_Staff
        FOREIGN KEY (staff_id)
        REFERENCES dbo.Staff(staff_id);
END;
GO