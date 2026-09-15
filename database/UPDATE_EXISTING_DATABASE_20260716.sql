USE FCinemaDB;
GO

-- 1. Đổi các tài khoản demo đang dùng mật khẩu 123 thành 123456.
-- Chỉ tác động đến đúng mật khẩu MD5 của chuỗi "123".
UPDATE tblUsers
SET password = 'e10adc3949ba59abbe56e057f20f883e'
WHERE password = '202cb962ac59075b964b07152d234b70';

-- 2. Ghi rõ thành phần của từng combo.
UPDATE tblCombos
SET comboName = N'Combo Solo: 1 bắp vừa + 1 nước vừa', price = 45000
WHERE comboID = 'combo-popcorn';

UPDATE tblCombos
SET comboName = N'Combo Couple: 1 bắp lớn + 2 nước vừa', price = 89000
WHERE comboID = 'combo-couple';

UPDATE tblCombos
SET comboName = N'Combo Family: 2 bắp lớn + 4 nước vừa', price = 149000
WHERE comboID = 'combo-family';
GO

-- Không cần thêm cột hay tạo bảng mới.
-- Các suất chiếu đã qua giờ sẽ được Java tự khóa khi truy cập hệ thống.
