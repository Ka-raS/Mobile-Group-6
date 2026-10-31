package com.group6.group6application.activities.admin

import android.util.Patterns
import androidx.room.withTransaction
import com.group6.group6application.dal.AppDB
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import com.group6.group6application.entities.Student
import com.group6.group6application.entities.Subject
import kotlinx.coroutines.flow.first
import java.util.Locale

data class StudentInput(
    val studentCode: String = "",
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    val username: String = "",
    val password: String = "",
)

data class SubjectInput(
    val subjectCode: String = "",
    val subjectName: String = "",
    val description: String = "",
    val credits: String = "",
)

/** Admin operations use the shared DAOs without changing the core database contract. */
class AdminRepository(private val db: AppDB) {
    val students = db.studentDao().getAll()
    val subjects = db.subjectDao().getAll()

    suspend fun loadStudent(id: Int): StudentInput = db.withTransaction {
        val student = requireNotNull(db.studentDao().getById(id)) { "Student no longer exists." }
        val account = requireNotNull(db.accountDao().getById(student.accountId)) { "Account not found." }
        require(account.role == Role.STUDENT) { "This account does not have the Student role." }
        StudentInput(student.studentCode, student.fullname, student.email, student.phone, account.username)
    }

    suspend fun saveStudent(id: Int, input: StudentInput) {
        val code = input.studentCode.trim().uppercase(Locale.ROOT)
        val name = input.fullname.trim()
        val email = input.email.trim()
        val phone = input.phone.trim()
        val username = input.username.trim()
        require(code.isNotEmpty()) { "Please enter a student code." }
        require(name.isNotEmpty()) { "Please enter a full name." }
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Invalid email address." }
        require(phone.matches(Regex("\\+?[0-9]{9,15}"))) { "Phone number must be 9–15 digits and may start with +." }
        require(username.isNotEmpty() && username.none { it.isWhitespace() }) {
            "Username cannot be empty or contain spaces."
        }
        require((id != 0 && input.password.isEmpty()) || (input.password.isNotBlank() && input.password.length >= 6)) {
            "Password must be at least 6 characters and not only whitespace."
        }
        db.withTransaction {
            val existing = if (id == 0) null else requireNotNull(db.studentDao().getById(id)) {
                "Student no longer exists."
            }
            require(students.first().none { it.id != id && it.studentCode.equals(code, ignoreCase = true) }) {
                "Student code already exists."
            }
            val usernameOwner = db.accountDao().getByUsername(username)
            require(usernameOwner == null || usernameOwner.id == existing?.accountId) { "Username already exists." }
            if (existing == null) {
                // A failed Student insert rolls back the Account insert as well.
                val accountId = db.accountDao().insert(Account(username = username, password = input.password, role = Role.STUDENT)).toInt()
                db.studentDao().insert(Student(studentCode = code, fullname = name, email = email, phone = phone, accountId = accountId))
            } else {
                val account = requireNotNull(db.accountDao().getById(existing.accountId)) { "Account not found." }
                require(account.role == Role.STUDENT) { "Cannot edit an Admin account through a student profile." }
                db.accountDao().update(account.copy(username = username, password = input.password.ifEmpty { account.password }))
                db.studentDao().update(existing.copy(studentCode = code, fullname = name, email = email, phone = phone))
            }
        }
    }

    suspend fun deleteStudent(id: Int) = db.withTransaction {
        val student = requireNotNull(db.studentDao().getById(id)) { "Student no longer exists." }
        val account = requireNotNull(db.accountDao().getById(student.accountId)) { "Account not found." }
        require(account.role == Role.STUDENT) { "Cannot delete an Admin account through a student profile." }
        require(students.first().none { it.id != id && it.accountId == account.id }) {
            "This account is linked to another student profile. Please check the data."
        }
        // Existing foreign keys cascade to the Student and their StudySchedule rows.
        db.accountDao().delete(account)
    }

    suspend fun loadSubject(id: Int): SubjectInput {
        val subject = requireNotNull(db.subjectDao().getById(id)) { "Subject no longer exists." }
        return SubjectInput(subject.subjectCode, subject.subjectName, subject.description, subject.credits.toString())
    }

    suspend fun saveSubject(id: Int, input: SubjectInput) {
        val code = input.subjectCode.trim().uppercase(Locale.ROOT)
        val name = input.subjectName.trim()
        val credits = input.credits.trim().toIntOrNull()
        require(code.isNotEmpty()) { "Please enter a subject code." }
        require(name.isNotEmpty()) { "Please enter a subject name." }
        require(credits != null && credits > 0) { "Credits must be a positive integer." }
        db.withTransaction {
            require(subjects.first().none { it.id != id && it.subjectCode.equals(code, ignoreCase = true) }) {
                "Subject code already exists."
            }
            val subject = Subject(id, code, name, input.description.trim(), credits)
            if (id == 0) db.subjectDao().insert(subject) else {
                requireNotNull(db.subjectDao().getById(id)) { "Subject no longer exists." }
                db.subjectDao().update(subject)
            }
        }
    }

    suspend fun deleteSubject(id: Int) = db.withTransaction {
        val subject = requireNotNull(db.subjectDao().getById(id)) { "Subject no longer exists." }
        db.subjectDao().delete(subject)
    }
}

