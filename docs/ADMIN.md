# Phần người 2 — Admin

## Phạm vi

- Admin Home: mở quản lý sinh viên, quản lý môn học và đăng xuất bằng callback có sẵn (xóa SharedPreferences, quay về Login).
- Sinh viên: danh sách, chi tiết, tìm kiếm mã/tên/email/điện thoại, thêm, sửa, xóa.
- Tạo sinh viên đồng thời tạo Account có role `Student` trong một transaction Room; lỗi ở một bước sẽ rollback cả hai.
- Sửa sinh viên: giữ nguyên `id`, `accountId`; có thể đổi tên đăng nhập/mật khẩu. Để trống mật khẩu khi sửa để giữ mật khẩu cũ; form không đọc mật khẩu cũ ra màn hình.
- Môn học: danh sách, chi tiết, tìm kiếm mã/tên/mô tả, thêm, sửa, xóa.
- Tìm kiếm không phân biệt hoa/thường hoặc dấu tiếng Việt; danh sách cập nhật tự động từ Flow của Room.
- Kiểm tra trường bắt buộc, email, điện thoại 9–15 chữ số (có thể có dấu `+`), mật khẩu tối thiểu 6 ký tự, mã trùng, tên đăng nhập trùng và tín chỉ nguyên dương.
- Mã sinh viên/môn học được trim và chuyển thành chữ hoa. Tên đăng nhập phân biệt hoa/thường như hàm login hiện tại.

## Cấu trúc

- `activities/AdminActivity.kt`: sử dụng nguyên các route được chốt trong `Routes`.
- `activities/admin/AdminHomeScreen.kt`: menu Admin.
- `activities/admin/student/`: danh sách và form sinh viên.
- `activities/admin/subject/`: danh sách và form môn học.
- `activities/admin/AdminComponents.kt`: thành phần danh sách, chi tiết, xác nhận xóa, form dùng chung.
- `activities/admin/AdminViewModel.kt`: trạng thái tải/lưu/lỗi, giữ nội dung form qua xoay màn hình; chặn nhấn lưu nhiều lần.
- `activities/admin/AdminRepository.kt`: kiểm tra dữ liệu và thực hiện giao dịch qua DAO hiện có.

Không thay đổi Entity, DAO, schema hoặc dependency Gradle. Các Preview nằm trong file màn hình tương ứng và không truy cập database.

## Hành vi xóa và tích hợp

Theo khóa ngoại `CASCADE` của bộ khung:

- Xóa sinh viên sẽ xóa Account Student, hồ sơ và lịch học của sinh viên đó.
- Xóa môn học sẽ xóa môn cùng mọi lịch học liên quan, giữ nguyên các sinh viên/tài khoản.
- Giao diện luôn hiển thị cảnh báo cụ thể và yêu cầu xác nhận trước khi xóa.

Người 4 cần bảo đảm worker thông báo bỏ qua lịch đã bị xóa khi đọc lại database, vì Admin có thể xóa lịch gián tiếp qua khóa ngoại. Phần notification hiện tại vẫn là stub của bộ khung; module Admin không thay đổi phần này.

## Demo và kiểm tra thủ công

1. Đăng nhập `admin/admin` trên database mới được seed.
2. Vào **Student Management** → **Add New**. Nhập `SV002`, họ tên, email/điện thoại hợp lệ, username `student2`, password `123456` → **Save**.
3. Tìm bằng mã, tên không dấu, email hoặc số điện thoại; mở **Details** và **Edit**. Xoay máy lúc đang nhập để kiểm tra nội dung form.
4. Thử tạo trùng mã hoặc username; ứng dụng phải báo lỗi và không tạo tài khoản thừa.
5. Sửa tên đăng nhập; để trống mật khẩu → **Save**. **Logout** và đăng nhập bằng tài khoản mới để kiểm tra role Student. Đổi mật khẩu và kiểm tra mật khẩu cũ không còn đăng nhập được.
6. Đăng nhập lại Admin; vào **Subject Management**, thêm môn học, tìm kiếm, xem chi tiết và sửa số tín chỉ. Thử tín chỉ `0`, số âm, số thập phân và mã môn trùng; phải báo lỗi.
7. Nhấn **Delete** → **Cancel** để kiểm tra hủy thao tác, sau đó xác nhận **Delete** với dữ liệu demo. Kiểm tra các bản ghi liên quan theo hành vi CASCADE ở trên.
8. **Logout**; nút Back không quay lại màn Admin. Đóng/mở ứng dụng để kiểm tra dữ liệu Room còn lưu.

## Kiểm thử tự động

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Lệnh thứ hai cần emulator hoặc thiết bị Android đã kết nối. `AdminRepositoryTest` dùng database Room trong RAM, không thay đổi dữ liệu demo của ứng dụng. Các ca kiểm thử bao gồm transaction rollback khi insert hồ sơ thất bại, mã/tài khoản trùng, đổi mật khẩu, sửa/xóa và CASCADE. `AdminScreenTest` kiểm tra menu, tìm kiếm không dấu, mở chi tiết và xác nhận xóa.

Theo quy trình README, commit các file thuộc Admin, test Admin và tài liệu này trên branch cá nhân, sau đó mở PR để người 1 tích hợp. Không đưa những thay đổi sẵn có thuộc Login, Student hoặc cấu hình IDE vào commit Admin.

## Kết quả xác minh ngày 31/10/2026

- `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest`: thành công.
- `lintDebug`: 0 lỗi; 17 cảnh báo ở cấu hình/dependency/resource của bộ khung, không có cảnh báo trong module Admin.
- Chạy riêng package `com.group6.group6application.activities.admin` trên thiết bị thật SM-X110: **12/12 test đạt** (9 test Room, 3 test giao diện).
- Kiểm thử thủ công bằng ADB trên thiết bị đang hiển thị qua scrcpy: đăng nhập Admin, tạo/tìm/xem/sửa sinh viên, chặn username trùng, giữ mật khẩu khi sửa, đăng nhập tài khoản mới vào Student Home, đăng xuất; thêm/tìm/xem/sửa môn học, chặn tín chỉ 0, hủy/xác nhận xóa; kiểm tra dữ liệu và phiên đăng nhập sau khi đóng/mở lại ứng dụng.
- Đã xóa hồ sơ/tài khoản `TEST_ADMIN_1031` và môn `TEST_ADMIN_MOB` tạo riêng cho ca thử. Dữ liệu có sẵn được giữ nguyên.
