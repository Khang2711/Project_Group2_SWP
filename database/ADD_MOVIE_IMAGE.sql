USE FCinemaDB;
GO

-- Chạy file này nếu database đã tồn tại và không muốn tạo lại từ đầu.
IF COL_LENGTH('tblMovies', 'imageURL') IS NULL
BEGIN
    ALTER TABLE tblMovies ADD imageURL NVARCHAR(1000) NULL;
END
GO
