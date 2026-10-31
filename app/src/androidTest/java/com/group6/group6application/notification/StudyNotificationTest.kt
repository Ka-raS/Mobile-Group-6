package com.group6.group6application.notification

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.app.NotificationManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.group6.group6application.dal.AppDB
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.ScheduleStatus
import com.group6.group6application.entities.Student
import com.group6.group6application.entities.StudySchedule
import com.group6.group6application.entities.Subject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudyNotificationTest {
    private lateinit var db: AppDB
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val schedule = StudySchedule(1, 1, 1, "2026-11-01", "10:00", "11:00", "Phòng A")
    private val now = Instant.parse("2026-11-01T02:45:00Z")
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val workTestId = 2_000_000_001

    @Before
    fun setUp(): Unit = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, AppDB::class.java).build()
        db.accountDao().insert(Account(1, "test", "test", Role.STUDENT))
        db.studentDao().insert(Student(1, "TEST01", "Test Student", "test@example.com", "0900000000", 1))
        db.subjectDao().insert(Subject(1, "MOB101", "Lập trình Mobile", "", 3))
        db.studyScheduleDao().insert(schedule)
    }

    @After
    fun tearDown() {
        NotificationScheduler.cancel(context, workTestId).result.get(10, TimeUnit.SECONDS)
        StudyNotifications.dismiss(context, workTestId + 1)
        context.getSharedPreferences("notification-test-session", Context.MODE_PRIVATE).edit().clear().commit()
        db.close()
    }

    private fun input(kind: String = NotificationScheduler.REMINDER): Data = Data.Builder()
        .putInt(NotificationScheduler.KEY_ID, 1)
        .putInt(NotificationScheduler.KEY_STUDENT, 1)
        .putString(NotificationScheduler.KEY_KIND, kind)
        .putLong(NotificationScheduler.KEY_START, Instant.parse("2026-11-01T03:00:00Z").toEpochMilli())
        .build()

    @Test
    fun reminderUsesLatestRoomLocation(): Unit = runBlocking {
        db.studyScheduleDao().update(schedule.copy(location = "Phòng mới"))
        val message = loadStudyNotification(db, input(), now, zone)!!
        assertTrue(message.title.contains("Lập trình Mobile"))
        assertTrue(message.body.contains("Phòng mới"))
    }

    @Test
    fun skipsStaleChangedTimeCancelledDeletedAndOtherStudentRows(): Unit = runBlocking {
        db.studyScheduleDao().update(schedule.copy(startTime = "11:00"))
        assertNull(loadStudyNotification(db, input(), now, zone))
        db.studyScheduleDao().update(schedule.copy(status = ScheduleStatus.CANCELLED))
        assertNull(loadStudyNotification(db, input(), now, zone))
        db.studyScheduleDao().update(schedule)
        val wrongStudent = Data.Builder().putAll(input()).putInt(NotificationScheduler.KEY_STUDENT, 2).build()
        assertNull(loadStudyNotification(db, wrongStudent, now, zone))
        db.studyScheduleDao().delete(schedule)
        assertNull(loadStudyNotification(db, input(), now, zone))
    }

    @Test
    fun cancellationProducesChangeMessageAndDeletionRequiresRowToBeGone(): Unit = runBlocking {
        db.studyScheduleDao().update(schedule.copy(status = ScheduleStatus.CANCELLED))
        assertEquals("Đã hủy lịch học", loadStudyNotification(db, input(NotificationScheduler.CHANGED), now, zone)?.title)
        val deletedInput = Data.Builder().putAll(input(NotificationScheduler.DELETED))
            .putInt("subject_id", 1).putString("study_date", schedule.studyDate)
            .putString("start_time", "10:00").putString("end_time", "11:00").putString("location", "Phòng A").build()
        assertNull(loadStudyNotification(db, deletedInput, now, zone))
        db.studyScheduleDao().delete(schedule)
        assertEquals("Đã xóa lịch học", loadStudyNotification(db, deletedInput, now, zone)?.title)
    }

    @Test
    fun reschedulingReplacesOldWorkAndCancelRemovesPendingReminder() {
        val manager = WorkManager.getInstance(context)
        val future = schedule.copy(id = workTestId, studyDate = LocalDate.now().plusDays(2).toString())
        NotificationScheduler.schedule(context, future).result.get(10, TimeUnit.SECONDS)
        val first = manager.getWorkInfosForUniqueWork(NotificationScheduler.reminderWorkName(workTestId))
            .get(10, TimeUnit.SECONDS).single { !it.state.isFinished }
        NotificationScheduler.schedule(context, future.copy(startTime = "12:00")).result.get(10, TimeUnit.SECONDS)
        val active = manager.getWorkInfosForUniqueWork(NotificationScheduler.reminderWorkName(workTestId))
            .get(10, TimeUnit.SECONDS).filter { !it.state.isFinished }
        assertEquals(1, active.size)
        assertNotEquals(first.id, active.single().id)
        val oldWork = manager.getWorkInfoById(first.id).get(10, TimeUnit.SECONDS)
        assertTrue(oldWork == null || oldWork.state == WorkInfo.State.CANCELLED)
        NotificationScheduler.cancel(context, workTestId).result.get(10, TimeUnit.SECONDS)
        assertTrue(manager.getWorkInfosForUniqueWork(NotificationScheduler.reminderWorkName(workTestId))
            .get(10, TimeUnit.SECONDS).all { it.state.isFinished })
    }

    @Test
    fun sessionGuardRejectsLoggedOutAdminAndDifferentStudent() {
        val isolated = object : ContextWrapper(context) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                context.getSharedPreferences("notification-test-$name", mode)
        }
        val session = SessionManager(isolated)
        session.logout()
        assertTrue(!isCurrentStudent(isolated, 1))
        session.save(Account(1, "test", "test", Role.STUDENT), studentId = 1)
        assertTrue(isCurrentStudent(isolated, 1))
        assertTrue(!isCurrentStudent(isolated, 2))
        session.save(Account(1, "admin", "test", Role.ADMIN), studentId = 1)
        assertTrue(!isCurrentStudent(isolated, 1))
        session.logout()
    }

    @Test
    fun publisherCreatesChannelRespectsPermissionAndClearsNotifications() {
        StudyNotifications.createChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals("Lịch học", manager.getNotificationChannel(StudyNotifications.CHANNEL_ID).name.toString())
        val allowed = StudyNotifications.canNotify(context)
        assertEquals(
            allowed,
            StudyNotifications.show(context, workTestId + 1, StudyNotification("Test nhắc học", "Môn học thử nghiệm"), true),
        )
        StudyNotifications.dismiss(context, workTestId + 1)
        assertTrue(manager.activeNotifications.none { it.id == workTestId + 1 })
    }
}
