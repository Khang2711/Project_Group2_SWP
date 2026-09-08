# Hướng dẫn GitHub cho nhóm

## Người tạo repo làm một lần

Giải nén, mở Git Bash trong thư mục FCinema chứa pom.xml và README.md. Trên GitHub tạo repo trống, ví dụ FCinema, chọn Private; chưa thêm README/.gitignore/license. Thay URL bên dưới bằng repo của nhóm.

```bash
git config --global user.name "Ten cua ban"
git config --global user.email "email-cua-ban@example.com"
git init
git branch -M main
git add .
git status
```

Kiểm tra không có db.local.properties, target, WAR hay thông tin kết nối riêng trong danh sách staged. Sau đó:

```bash
git commit -m "Khoi tao du an FCinema"
git remote add origin https://github.com/TEN-TAI-KHOAN/FCinema.git
git push -u origin main
```

Đăng nhập theo yêu cầu của Git. Chủ repo mời thành viên ở Settings → Collaborators; các bạn chấp nhận lời mời. Đưa nội dung thư mục lên Git, không chỉ upload file ZIP.

## Thành viên tải lần đầu

```bash
git clone https://github.com/TEN-TAI-KHOAN/FCinema.git
cd FCinema
```

Làm phần cài database và cấu hình local trong README.md, rồi chạy web.

## Làm một chức năng

Bắt đầu khi phần việc trước đã commit và working tree sạch:

```bash
git switch main
git pull origin main
git switch -c feature/movie-crud
```

Viết code và test localhost. Tiếp theo:

```bash
git add .
git status
git commit -m "Cap nhat CRUD phim"
git push -u origin feature/movie-crud
```

Tạo Pull Request trên GitHub từ nhánh vừa làm vào main. Ghi thay đổi, cách test, SQL cần chạy. Một bạn kiểm tra và merge. Lần sửa tiếp trên cùng nhánh dùng commit và git push; chức năng mới sau khi merge thì tạo nhánh mới từ main đã cập nhật.

## Database khi làm nhóm

Git chỉ chia sẻ file SQL, không đồng bộ database đang chạy. Mỗi người dùng database local riêng. Nếu đổi bảng/cột, thêm script cập nhật có số thứ tự và mô tả, ví dụ database/migrations/03_add_column.sql; commit cùng code. Ghi trong PR thứ tự chạy; mỗi người ghi nhận migration đã chạy, không chạy lặp. Không chạy lại schema/seed để cập nhật database có dữ liệu.

## Khi conflict

Commit phần việc hiện tại trước, ở nhánh chức năng chạy:

```bash
git fetch origin
git merge origin/main
```

Nếu conflict, mở file được Git báo, thống nhất nội dung với bạn cùng sửa; xóa các dấu conflict, giữ code đúng, chạy thử rồi:

```bash
git add .
git commit -m "Giai quyet conflict"
git push
```

Không force push main. Mỗi chức năng có một nhánh; tránh hai người sửa cùng file khi chưa trao đổi.
