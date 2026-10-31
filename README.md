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

# Phân công công việc (4 người)

Người 1 làm phần nền trước (~30–45 phút), push lên `main`, sau đó 3 người còn lại pull về và làm song song.

## 👤 Người 1 — Core + Login (làm trước)
- Thêm dependencies: Room + KSP, Navigation Compose, WorkManager
- `entities/`: 4 Entity (`Account`, `Student`, `Subject`, `StudySchedule`) và `ScheduleWithSubject` (lịch kèm tên môn)
- `dal/`: `AppDB`, các DAO, `SessionManager`, seed sẵn dữ liệu bằng `AppDB.seedIfEmpty()`
- Viết sẵn query kiểm tra trùng lịch `StudyScheduleDao.countOverlap(studentId, date, start, end, excludeId)`:
  `SELECT COUNT(*) ... WHERE studentId=:sid AND studyDate=:d AND status!='Cancelled' AND startTime < :end AND endTime > :start AND id != :excludeId`
- `SessionManager` lưu account đang đăng nhập bằng SharedPreferences
- `activities/LoginActivity`, sau khi login thì chuyển sang màn Admin hoặc Student theo role
- `NavHost` với tên route chốt sẵn từ đầu để mọi người tự gắn màn của mình vào
- Quản lý repo GitHub (branch protection, review và merge PR), lo phần tích hợp cuối

## Cấu trúc project

```
com.group6.group6application
├── activities
│    ├── LoginActivity.kt
│    ├── AdminActivity.kt       (Người 2)
│    ├── ScheduleActivity.kt    (Người 3)
│    ├── CalendarActivity.kt    (Người 4)
│    ├── navigation/            Routes, AppNavHost, PlaceholderScreen
│    └── theme/
├── dal
│    ├── AppDB.kt
│    ├── AccountDao.kt
│    ├── StudentDao.kt
│    ├── SubjectDao.kt
│    ├── StudyScheduleDao.kt
│    └── SessionManager.kt
├── entities
│    ├── Account.kt              (kèm Role)
│    ├── Student.kt
│    ├── Subject.kt
│    ├── StudySchedule.kt        (kèm ScheduleStatus)
│    └── ScheduleWithSubject.kt
├── notification
│    └── NotificationScheduler.kt   (Người 4)
└── MainActivity.kt
```

- Lấy DB: `AppDB.get(context).studentDao()`
- Lấy người đang đăng nhập: `SessionManager(context).studentId` (Admin là `-1`)
- Tài khoản test: `admin/admin`, `student1/123456` (student SV001), 3 môn MOB101, DBI202, PRF192
- Đổi Entity mà app crash thì gỡ app hoặc Clear data rồi chạy lại

## 👤 Người 2 — Admin
- `activities/admin/student/`: thêm, sửa, xóa, tìm kiếm, xem Student. Khi thêm Student thì tạo luôn Account role Student
- `activities/admin/subject/`: thêm, sửa, xóa, tìm kiếm, xem Subject
- `activities/AdminActivity`: gắn các màn vào graph, màn Admin Home (menu dẫn tới 2 phần trên, có nút logout)

## 👤 Người 3 — Lịch học của Student
- `activities/student/schedule/`: danh sách, tìm kiếm, sửa, xóa lịch cá nhân
- Form đặt lịch: chọn Subject (dropdown), DatePicker, TimePicker bắt đầu và kết thúc, nhập location
- Kiểm tra trùng giờ bằng query của Người 1, nếu trùng thì báo lỗi
- Quản lý trạng thái Pending, Confirmed, Cancelled (lọc theo trạng thái, đổi trạng thái)
- `activities/ScheduleActivity`: gắn các màn vào graph, màn Student Home

## 👤 Người 4 — Calendar + Notification
- `activities/CalendarActivity`: chuyển giữa 3 chế độ xem Ngày, Tuần, Tháng
- `notification/`: tạo notification channel, xin quyền `POST_NOTIFICATIONS`, dùng WorkManager hẹn thông báo trước buổi học (ví dụ trước 30 phút)
- Viết hàm `NotificationScheduler.schedule(schedule)` và `cancel(id)` để Người 3 gọi khi tạo, sửa, hủy lịch (thông báo khi lịch thay đổi)
- Viết README và chuẩn bị demo

## Quy tắc chung
- Mỗi người làm trên branch riêng (`feat/core`, `feat/admin`, `feat/schedule`, `feat/calendar`), merge qua PR vào `main`
- Chỉ Người 1 được sửa Entity, DAO và `build.gradle`. Ai cần thêm thì nhắn Người 1
- Ngày lưu dạng `"yyyy-MM-dd"`, giờ lưu dạng `"HH:mm"` (chuỗi so sánh trực tiếp được, đỡ phải convert)
