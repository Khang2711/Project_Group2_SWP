USE FCinemaDB;
GO


IF OBJECT_ID('dbo.Booking', 'U') IS NOT NULL
AND EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.Booking')
      AND name = 'customer_id'
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE dbo.Booking
    ALTER COLUMN customer_id INT NULL;
END;
GO