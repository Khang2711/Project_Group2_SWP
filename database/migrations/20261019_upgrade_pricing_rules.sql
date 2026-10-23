USE FCinemaDB;
GO

/* =========================================================
   Upgrade Pricing for real cinema pricing rules

   movie_id NULL       -> applies to all movies
   room_type NULL      -> applies to all room types
   day_type ALL        -> every day
   day_type WEEKDAY    -> Monday - Friday
   day_type WEEKEND    -> Saturday - Sunday
   special_date NULL   -> normal pricing rule
   special_date value  -> special/holiday pricing
   start_time NULL     -> all day
   end_time NULL       -> all day
   ========================================================= */

IF COL_LENGTH('dbo.Pricing', 'movie_id') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD movie_id INT NULL;
END;
GO

IF COL_LENGTH('dbo.Pricing', 'room_type') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD room_type VARCHAR(50) NULL;
END;
GO

IF COL_LENGTH('dbo.Pricing', 'day_type') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD day_type VARCHAR(20)
        NOT NULL
        CONSTRAINT DF_Pricing_DayType
        DEFAULT ('ALL')
        WITH VALUES;
END;
GO

IF COL_LENGTH('dbo.Pricing', 'special_date') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD special_date DATE NULL;
END;
GO

IF COL_LENGTH('dbo.Pricing', 'start_time') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD start_time TIME NULL;
END;
GO

IF COL_LENGTH('dbo.Pricing', 'end_time') IS NULL
BEGIN
    ALTER TABLE dbo.Pricing
    ADD end_time TIME NULL;
END;
GO

/* =========================================================
   FOREIGN KEY
   ========================================================= */

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_Pricing_Movie'
)
BEGIN
    ALTER TABLE dbo.Pricing
    ADD CONSTRAINT FK_Pricing_Movie
        FOREIGN KEY (movie_id)
        REFERENCES dbo.Movie(movie_id);
END;
GO

/* =========================================================
   CHECK: DAY TYPE
   ========================================================= */

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_Pricing_DayType'
)
BEGIN
    ALTER TABLE dbo.Pricing
    ADD CONSTRAINT CK_Pricing_DayType
        CHECK (
            day_type IN (
                'ALL',
                'WEEKDAY',
                'WEEKEND'
            )
        );
END;
GO

/* =========================================================
   CHECK: ROOM TYPE
   ========================================================= */

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_Pricing_RoomType'
)
BEGIN
    ALTER TABLE dbo.Pricing
    ADD CONSTRAINT CK_Pricing_RoomType
        CHECK (
            room_type IS NULL
            OR room_type IN (
                '2D',
                '3D',
                'IMAX'
            )
        );
END;
GO

/* =========================================================
   CHECK: TIME RANGE
   ========================================================= */

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_Pricing_TimeRange'
)
BEGIN
    ALTER TABLE dbo.Pricing
    ADD CONSTRAINT CK_Pricing_TimeRange
        CHECK (
            (
                start_time IS NULL
                AND end_time IS NULL
            )
            OR
            (
                start_time IS NOT NULL
                AND end_time IS NOT NULL
                AND end_time > start_time
            )
        );
END;
GO

/* =========================================================
   INDEX
   ========================================================= */

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_Pricing_RuleLookup'
      AND object_id = OBJECT_ID('dbo.Pricing')
)
BEGIN
    CREATE INDEX IX_Pricing_RuleLookup
        ON dbo.Pricing (
            cinema_id,
            seat_type,
            status,
            start_date
        );
END;
GO
