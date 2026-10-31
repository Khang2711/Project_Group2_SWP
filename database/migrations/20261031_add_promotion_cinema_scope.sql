/*
    FCinemaDB migration: scope promotions to a cinema branch.
    Run after the base schema has created dbo.Cinema and dbo.Promotion.

    Existing promotion rows are intentionally kept with cinema_id = NULL
    until their owner branch is confirmed. Do not guess a branch for legacy rows.
    New promotions must always be created with a valid cinema_id by the application.
*/
USE FCinemaDB;
GO

/* 1. Add branch ownership column for existing databases. */
IF COL_LENGTH(N'dbo.Promotion', N'cinema_id') IS NULL
BEGIN
    ALTER TABLE dbo.Promotion
    ADD cinema_id INT NULL;
END;
GO

/* 2. Link a promotion to its cinema branch. */
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'FK_Promotion_Cinema'
      AND parent_object_id = OBJECT_ID(N'dbo.Promotion')
)
BEGIN
    ALTER TABLE dbo.Promotion WITH CHECK
    ADD CONSTRAINT FK_Promotion_Cinema
        FOREIGN KEY (cinema_id)
        REFERENCES dbo.Cinema(cinema_id);
END;
GO

/* 3. Remove the previous system-wide unique constraint. */
IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'UQ_Promotion_Code'
      AND parent_object_id = OBJECT_ID(N'dbo.Promotion')
      AND type = N'UQ'
)
BEGIN
    ALTER TABLE dbo.Promotion
    DROP CONSTRAINT UQ_Promotion_Code;
END;
GO

/* 4. Stop if existing branch-owned duplicates would prevent the new index. */
IF EXISTS (
    SELECT 1
    FROM dbo.Promotion
    WHERE cinema_id IS NOT NULL
    GROUP BY cinema_id, promo_code
    HAVING COUNT(*) > 1
)
BEGIN
    ;THROW 51001,
        'Duplicate promo_code exists within the same cinema. Resolve duplicates before creating UX_Promotion_Cinema_Code.',
        1;
END;
GO

/* 5. A promo code must be unique within a cinema, not across all cinemas.
      NULL cinema_id rows are legacy/unassigned and are excluded from this index. */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_Promotion_Cinema_Code'
      AND object_id = OBJECT_ID(N'dbo.Promotion')
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX UX_Promotion_Cinema_Code
        ON dbo.Promotion(cinema_id, promo_code)
        WHERE cinema_id IS NOT NULL;
END;
GO

/* 6. Review legacy rows that still need a confirmed cinema assignment. */
SELECT promotion_id, promo_code, description, status
FROM dbo.Promotion
WHERE cinema_id IS NULL
ORDER BY promotion_id;
GO
