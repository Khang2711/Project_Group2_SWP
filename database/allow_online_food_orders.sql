-- Run this once on an existing FCinemaDB database before using online combo checkout.
ALTER TABLE dbo.Food_Order ALTER COLUMN staff_id INT NULL;
GO
