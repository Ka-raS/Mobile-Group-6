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

class AdminRepository(private val db: AppDB) {
    val students = db.studentDao().getAll()
    val subjects = db.subjectDao().getAll()

    suspend fun loadStudent(id: Int): StudentInput = db.withTransaction {
        val student = requireNotNull(db.studentDao().getById(id)) { "Sinh viên không còn tồn tại." }
        val account = requireNotNull(db.accountDao().getById(student.accountId)) { "Không tìm thấy tài khoản." }
        require(account.role == Role.STUDENT) { "Tài khoản này không có quyền Student." }
        StudentInput(student.studentCode, student.fullname, student.email, student.phone, account.username)
    }

    suspend fun saveStudent(id: Int, input: StudentInput) {
        val code = input.studentCode.trim().uppercase(Locale.ROOT)
        val name = input.fullname.trim()
        val email = input.email.trim()
        val phone = input.phone.trim()
        val username = input.username.trim()
        require(code.isNotEmpty()) { "Vui lòng nhập mã sinh viên." }
        require(name.isNotEmpty()) { "Vui lòng nhập họ tên." }
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Email không hợp lệ." }
        require(phone.matches(Regex("\\+?[0-9]{9,15}"))) { "Số điện thoại phải có 9–15 chữ số, có thể bắt đầu bằng +." }
        require(username.isNotEmpty() && username.none { it.isWhitespace() }) {
            "Tên đăng nhập không được để trống hoặc chứa khoảng trắng."
        }
        require((id != 0 && input.password.isEmpty()) || (input.password.isNotBlank() && input.password.length >= 6)) {
            "Mật khẩu phải có ít nhất 6 ký tự và không chỉ gồm khoảng trắng."
        }
        db.withTransaction {
            val existing = if (id == 0) null else requireNotNull(db.studentDao().getById(id)) {
                "Sinh viên không còn tồn tại."
            }
            require(students.first().none { it.id != id && it.studentCode.equals(code, ignoreCase = true) }) {
                "Mã sinh viên đã tồn tại."
            }
            val usernameOwner = db.accountDao().getByUsername(username)
            require(usernameOwner == null || usernameOwner.id == existing?.accountId) { "Tên đăng nhập đã tồn tại." }
            if (existing == null) {
                val accountId = db.accountDao().insert(Account(username = username, password = input.password, role = Role.STUDENT)).toInt()
                db.studentDao().insert(Student(studentCode = code, fullname = name, email = email, phone = phone, accountId = accountId))
            } else {
                val account = requireNotNull(db.accountDao().getById(existing.accountId)) { "Không tìm thấy tài khoản." }
                require(account.role == Role.STUDENT) { "Không thể sửa tài khoản Admin qua hồ sơ sinh viên." }
                db.accountDao().update(account.copy(username = username, password = input.password.ifEmpty { account.password }))
                db.studentDao().update(existing.copy(studentCode = code, fullname = name, email = email, phone = phone))
            }
        }
    }

    suspend fun deleteStudent(id: Int) = db.withTransaction {
        val student = requireNotNull(db.studentDao().getById(id)) { "Sinh viên không còn tồn tại." }
        val account = requireNotNull(db.accountDao().getById(student.accountId)) { "Không tìm thấy tài khoản." }
        require(account.role == Role.STUDENT) { "Không thể xóa tài khoản Admin qua hồ sơ sinh viên." }
        require(students.first().none { it.id != id && it.accountId == account.id }) {
            "Tài khoản này đang gắn với một hồ sơ sinh viên khác. Vui lòng kiểm tra dữ liệu."
        }
        db.accountDao().delete(account)
    }

    suspend fun loadSubject(id: Int): SubjectInput {
        val subject = requireNotNull(db.subjectDao().getById(id)) { "Môn học không còn tồn tại." }
        return SubjectInput(subject.subjectCode, subject.subjectName, subject.description, subject.credits.toString())
    }

    suspend fun saveSubject(id: Int, input: SubjectInput) {
        val code = input.subjectCode.trim().uppercase(Locale.ROOT)
        val name = input.subjectName.trim()
        val credits = input.credits.trim().toIntOrNull()
        require(code.isNotEmpty()) { "Vui lòng nhập mã môn học." }
        require(name.isNotEmpty()) { "Vui lòng nhập tên môn học." }
        require(credits != null && credits > 0) { "Số tín chỉ phải là số nguyên dương." }
        db.withTransaction {
            require(subjects.first().none { it.id != id && it.subjectCode.equals(code, ignoreCase = true) }) {
                "Mã môn học đã tồn tại."
            }
            val subject = Subject(id, code, name, input.description.trim(), credits)
            if (id == 0) db.subjectDao().insert(subject) else {
                requireNotNull(db.subjectDao().getById(id)) { "Môn học không còn tồn tại." }
                db.subjectDao().update(subject)
            }
        }
    }

    suspend fun deleteSubject(id: Int) = db.withTransaction {
        val subject = requireNotNull(db.subjectDao().getById(id)) { "Môn học không còn tồn tại." }
        db.subjectDao().delete(subject)
    }
}

