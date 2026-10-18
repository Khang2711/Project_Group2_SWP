/*
    Email verification migration for FCinemaDB.
    Existing accounts are marked verified to avoid locking out current users.
    Public registration explicitly inserts email_verified = 0.
*/
IF COL_LENGTH('dbo.[User]', 'email_verified') IS NULL
BEGIN
    ALTER TABLE dbo.[User]
    ADD email_verified BIT NOT NULL
        CONSTRAINT DF_User_EmailVerified DEFAULT (1) WITH VALUES;
END
GO
