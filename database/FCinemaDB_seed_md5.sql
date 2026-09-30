/*
    FCinemaDB - MD5 password override for demo seed users
    Run AFTER database/FCinemaDB_seed.sql

    Demo plain-text password: password
    MD5(password): 5f4dcc3b5aa765d61d8327deb882cf99

    NOTE: MD5 is used here only because the project currently requires it.
*/

USE FCinemaDB;
GO

DECLARE @DemoPasswordMD5 VARCHAR(255) = '5f4dcc3b5aa765d61d8327deb882cf99';

UPDATE dbo.[User]
SET password_hash = @DemoPasswordMD5,
    updated_at = SYSDATETIME()
WHERE email IN (
    'admin@fcinema.local',
    'staff1@fcinema.local',
    'staff2@fcinema.local',
    'customer1@fcinema.local',
    'customer2@fcinema.local'
);
GO

SELECT user_id, full_name, email, password_hash, status
FROM dbo.[User]
WHERE email IN (
    'admin@fcinema.local',
    'staff1@fcinema.local',
    'staff2@fcinema.local',
    'customer1@fcinema.local',
    'customer2@fcinema.local'
)
ORDER BY user_id;
GO
