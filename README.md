Project: STUDENT STUDY SCHEDULE MANAGEMENT AND BOOKING APPLICATION
Create a project with the following 4 tables:
Account(id, username, password, role)
Student(id, studentCode, fullname, email, phone, accountId)
Subject(id, subjectCode, subjectName, description, credits)
StudySchedule(id, studentId, subjectId, studyDate, startTime, endTime, location, status)
And required functionalities:
Login: Implement a login function with two roles: Admin and Student, and provide the corresponding functions for each role.
Login Account Storage: Store login account information using SharedPreferences.
Data Storage: Store the tables using Room Database.
Student Management: Allow administrators to add, update, delete, search, and view student information.
Subject Management: Allow administrators to add, update, delete, search, and view subject information.
Study Schedule Management: Allow students to create, update, delete, search, and view their personal study schedules.
Study Schedule Booking: Allow students to select a subject, choose a study date, set the start and end times, specify a study location, and book a study session.
Schedule Conflict Validation: Prevent students from booking overlapping study sessions on the same date and time.
Schedule Status Management: Allow students to view and manage their bookings with statuses such as Pending, Confirmed, and Cancelled.
Calendar View: Display study schedules in a daily, weekly, or monthly calendar view.
Notifications: Notify students of upcoming study sessions and schedule changes.
Version Control and Collaboration: Work together and manage the project code using GitHub or GitLab.
Scoring: The score is based on the time taken to complete the project. The less time a student takes, the higher the score.

---

# Ứng dụng quản lý và đặt lịch học

Kotlin, Jetpack Compose, Room, SharedPreferences, WorkManager. Giao diện tiếng Việt.

## Chạy thử

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Windows dùng `.\gradlew.bat`. Lệnh thứ hai cần máy ảo hoặc điện thoại đang kết nối.

Tài khoản có sẵn (tự seed ở lần đăng nhập đầu tiên):

| Tài khoản | Mật khẩu | Vai trò |
|---|---|---|
| `admin` | `admin` | Admin |
| `student1` | `123456` | Student, hồ sơ SV001 |

Có sẵn 3 môn MOB101, DBI202, PRF192. Đổi Entity mà app crash thì gỡ app hoặc Clear data rồi chạy lại.

## Yêu cầu và nơi cài đặt

| Yêu cầu | Cài đặt |
|---|---|
| 4 bảng | `entities/` (`Account`, `Student`, `Subject`, `StudySchedule`), khóa ngoại `CASCADE` |
| Login 2 role | `activities/LoginActivity.kt`, `AppNavHost` chuyển sang Admin hoặc Student theo role |
| Lưu đăng nhập bằng SharedPreferences | `dal/SessionManager.kt` |
| Lưu dữ liệu bằng Room | `dal/AppDB.kt` và các DAO |
| Quản lý sinh viên | `activities/admin/student/`, thêm sinh viên tạo luôn Account role Student trong một transaction |
| Quản lý môn học | `activities/admin/subject/` |
| Quản lý lịch học cá nhân | `activities/student/schedule/ScheduleListActivity.kt` (xem, tìm, sửa, xóa) |
| Đặt lịch | `ScheduleFormActivity.kt`: chọn môn, ngày, giờ bắt đầu và kết thúc, địa điểm |
| Chặn trùng lịch | `StudyScheduleDao.countOverlap()`, bỏ qua lịch đã hủy |
| Trạng thái Pending, Confirmed, Cancelled | Lọc và đổi trạng thái trong danh sách lịch, hiển thị "Chờ xác nhận", "Đã xác nhận", "Đã hủy" |
| Lịch ngày, tuần, tháng | `activities/calendar/CalendarViewActivity.kt` |
| Thông báo | `notification/`: nhắc trước giờ học 30 phút, báo khi sửa, đổi trạng thái hoặc xóa lịch |
| GitHub | Mỗi người một branch, merge qua PR |

## Cấu trúc project

```
com.group6.group6application
├── activities
│    ├── LoginActivity.kt
│    ├── AdminActivity.kt            graph Admin
│    ├── ScheduleActivity.kt         graph Student
│    ├── CalendarActivity.kt
│    ├── admin/                      AdminHomeActivity, AdminComponents, AdminViewModel, AdminRepository
│    │    ├── student/               StudentListActivity, StudentFormActivity
│    │    └── subject/               SubjectListActivity, SubjectFormActivity
│    ├── student/                    StudentHomeActivity
│    │    └── schedule/              ScheduleListActivity, ScheduleFormActivity, ViewModel, validation
│    ├── calendar/                   CalendarViewActivity, CalendarDates
│    ├── navigation/                 Routes, AppNavHost
│    └── theme/
├── dal                              AppDB, AccountDao, StudentDao, SubjectDao, StudyScheduleDao, SessionManager
├── entities                         Account (kèm Role, ScheduleStatus), Student, Subject, StudySchedule, ScheduleWithSubject
├── notification                     NotificationScheduler, StudyNotificationWorker, StudyNotifications, ReminderTiming, StudentNotificationSetup
└── MainActivity.kt
```

Quy ước:
- Màn hình nằm trong `activities/` và có hậu tố `Activity`.
- Không comment trong code.
- Ngày lưu `"yyyy-MM-dd"`, giờ lưu `"HH:mm"`. Trạng thái lưu `Pending`, `Confirmed`, `Cancelled`, hiển thị qua `ScheduleStatus.label()`.

## Thông báo

- Tạo lịch: `NotificationScheduler.schedule(context, schedule)` sau khi insert, truyền đúng id Room trả về.
- Sửa lịch hoặc đổi trạng thái: `NotificationScheduler.notifyChanged(context, schedule)`.
- Xóa lịch: `NotificationScheduler.notifyDeleted(context, oldSchedule)`.
- Logout hủy hết nhắc. Worker đọc lại Room trước khi gửi nên lịch đã xóa hoặc đã hủy không bị báo nhầm.
- Android 13+ hỏi quyền thông báo ở lần đầu vào màn Student. Nếu từ chối, bật lại trong Cài đặt ứng dụng.

## Kịch bản demo

1. Đăng nhập `admin/admin`. Vào Quản lý sinh viên, thêm `SV002` với tài khoản `student2/123456`. Thử tìm kiếm, xem chi tiết, sửa. Vào Quản lý môn học, thêm, sửa, tìm một môn. Đăng xuất.
2. Đăng nhập `student2/123456`, cho phép thông báo. Đặt một lịch bắt đầu sau 10 phút, sẽ có thông báo nhắc ngay.
3. Đặt một lịch khác trùng giờ để thấy báo lỗi trùng lịch.
4. Trong Lịch học của tôi: tìm kiếm, lọc theo trạng thái, đổi trạng thái, sửa, xóa. Mỗi thao tác sửa, đổi trạng thái, xóa đều có thông báo.
5. Mở Lịch, chuyển Ngày, Tuần, Tháng.
6. Đóng và mở lại app: vẫn đăng nhập và dữ liệu còn nguyên.

## Phân công

| Người | Phần việc |
|---|---|
| Người 1 | Core: entities, DAO, AppDB, SessionManager, Login, Navigation, tích hợp cuối |
| Người 2 | Admin: quản lý sinh viên, môn học (`docs/ADMIN.md`) |
| Người 3 | Student: trang Student, danh sách, đặt lịch, trùng lịch, trạng thái |
| Người 4 | Calendar và Notification |
