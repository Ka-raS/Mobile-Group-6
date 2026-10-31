package com.group6.group6application.activities.admin

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.group6.group6application.dal.AppDB
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.StudySchedule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminRepositoryTest {
    private lateinit var db: AppDB
    private lateinit var repository: AdminRepository
    private val student = StudentInput(" sv001 ", " Nguyễn Văn An ", " an@example.com ", "0900000001", " student1 ", "123456")
    private val subject = SubjectInput(" mob101 ", " Lập trình di động ", " Android ", "3")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDB::class.java).build()
        repository = AdminRepository(db)
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun createStudentCreatesWorkingLoginAndLinkedProfile() = runBlocking {
        repository.saveStudent(0, student)
        val account = requireNotNull(db.accountDao().login("student1", "123456"))
        val profile = repository.students.first().single()
        assertEquals(Role.STUDENT, account.role)
        assertEquals(account.id, profile.accountId)
        assertEquals("SV001", profile.studentCode)
        assertEquals("Nguyễn Văn An", profile.fullname)
        assertEquals("an@example.com", profile.email)
        assertEquals("", repository.loadStudent(profile.id).password)
    }

    @Test
    fun duplicateCodeOrUsernameDoesNotCreateOrphanAccount() = runBlocking {
        repository.saveStudent(0, student)
        expectInvalid { repository.saveStudent(0, student.copy(username = "other")) }
        assertNull(db.accountDao().getByUsername("other"))
        expectInvalid { repository.saveStudent(0, student.copy(studentCode = "SV002")) }
        assertEquals(1, repository.students.first().size)
    }

    @Test
    fun failedProfileInsertRollsBackAccountInsert() = runBlocking {
        // Simulate a failure after Account has been inserted, inside the same transaction.
        db.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_student BEFORE INSERT ON Student BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        var failed = false
        try {
            repository.saveStudent(0, student)
        } catch (_: android.database.sqlite.SQLiteException) {
            failed = true
        }
        assertTrue(failed)
        assertNull(db.accountDao().getByUsername("student1"))
        assertTrue(repository.students.first().isEmpty())
    }

    @Test
    fun editKeepsAccountLinkAndSupportsOptionalPasswordChange() = runBlocking {
        repository.saveStudent(0, student)
        val profile = repository.students.first().single()
        val edited = repository.loadStudent(profile.id).copy(fullname = "Nguyễn Văn Bình", username = "renamed")
        repository.saveStudent(profile.id, edited)
        assertNotNull(db.accountDao().login("renamed", "123456"))
        assertNull(db.accountDao().login("student1", "123456"))
        assertEquals(profile.accountId, db.studentDao().getById(profile.id)?.accountId)
        assertEquals("Nguyễn Văn Bình", db.studentDao().getById(profile.id)?.fullname)
        repository.saveStudent(profile.id, edited.copy(password = "newpass"))
        assertNull(db.accountDao().login("renamed", "123456"))
        assertNotNull(db.accountDao().login("renamed", "newpass"))
    }

    @Test
    fun conflictingEditLeavesOriginalAccountAndProfileIntact() = runBlocking {
        repository.saveStudent(0, student)
        repository.saveStudent(0, student.copy(studentCode = "SV002", username = "other"))
        val profile = repository.students.first().first { it.studentCode == "SV001" }
        expectInvalid {
            repository.saveStudent(profile.id, repository.loadStudent(profile.id).copy(username = "other", fullname = "Changed"))
        }
        assertNotNull(db.accountDao().login("student1", "123456"))
        assertEquals("Nguyễn Văn An", db.studentDao().getById(profile.id)?.fullname)
    }

    @Test
    fun invalidFieldsAreRejectedBeforeWriting() = runBlocking {
        listOf(
            student.copy(studentCode = " "), student.copy(fullname = " "),
            student.copy(email = "invalid"), student.copy(phone = "abc"),
            student.copy(username = "two words"), student.copy(password = "123"),
            student.copy(password = "      "),
        ).forEach { input -> expectInvalid { repository.saveStudent(0, input) } }
        listOf("0", "-1", "1.5", "", "2147483648").forEach { credits ->
            expectInvalid { repository.saveSubject(0, subject.copy(credits = credits)) }
        }
        assertTrue(repository.students.first().isEmpty())
        assertTrue(repository.subjects.first().isEmpty())
    }

    @Test
    fun subjectCrudRejectsDuplicateCodeAndMissingRecords() = runBlocking {
        repository.saveSubject(0, subject)
        val saved = repository.subjects.first().single()
        assertEquals("MOB101", saved.subjectCode)
        expectInvalid { repository.saveSubject(0, subject.copy(subjectCode = "MoB101")) }
        repository.saveSubject(saved.id, repository.loadSubject(saved.id).copy(subjectName = "Android nâng cao", credits = "4"))
        assertEquals("Android nâng cao", db.subjectDao().getById(saved.id)?.subjectName)
        assertEquals(4, db.subjectDao().getById(saved.id)?.credits)
        repository.deleteSubject(saved.id)
        assertTrue(repository.subjects.first().isEmpty())
        expectInvalid { repository.saveSubject(saved.id, subject) }
        expectInvalid { repository.loadSubject(saved.id) }
    }

    @Test
    fun deletingStudentRemovesLoginAndSchedulesButKeepsOtherData() = runBlocking {
        val adminId = db.accountDao().insert(Account(username = "admin", password = "admin", role = Role.ADMIN)).toInt()
        repository.saveStudent(0, student)
        repository.saveStudent(0, student.copy(studentCode = "SV002", username = "other"))
        repository.saveSubject(0, subject)
        val profile = repository.students.first().first { it.studentCode == "SV001" }
        val subjectId = repository.subjects.first().single().id
        val scheduleId = db.studyScheduleDao().insert(StudySchedule(
            studentId = profile.id, subjectId = subjectId, studyDate = "2026-11-10",
            startTime = "08:00", endTime = "09:00", location = "A101",
        )).toInt()
        repository.deleteStudent(profile.id)
        assertNull(db.studentDao().getById(profile.id))
        assertNull(db.accountDao().getById(profile.accountId))
        assertNull(db.studyScheduleDao().getById(scheduleId))
        assertNotNull(db.accountDao().getById(adminId))
        assertNotNull(db.accountDao().login("other", "123456"))
        assertNotNull(db.subjectDao().getById(subjectId))
    }

    @Test
    fun deletingSubjectRemovesItsSchedulesAndKeepsStudentLogin() = runBlocking {
        repository.saveStudent(0, student)
        repository.saveSubject(0, subject)
        val profile = repository.students.first().single()
        val subjectId = repository.subjects.first().single().id
        val scheduleId = db.studyScheduleDao().insert(StudySchedule(
            studentId = profile.id, subjectId = subjectId, studyDate = "2026-11-10",
            startTime = "08:00", endTime = "09:00", location = "A101",
        )).toInt()
        repository.deleteSubject(subjectId)
        assertNull(db.studyScheduleDao().getById(scheduleId))
        assertNotNull(db.studentDao().getById(profile.id))
        assertNotNull(db.accountDao().login("student1", "123456"))
    }

    private suspend fun expectInvalid(action: suspend () -> Unit) {
        try {
            action()
            fail("Expected validation failure")
        } catch (_: IllegalArgumentException) {
            // Expected user-facing validation, not a database crash.
        }
    }
}
