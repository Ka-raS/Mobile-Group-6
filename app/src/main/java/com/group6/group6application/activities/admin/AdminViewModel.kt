package com.group6.group6application.activities.admin

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.group6.group6application.dal.AppDB
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Each navigation destination owns an instance; edits survive configuration changes. */
class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(AppDB.get(application))
    var error by mutableStateOf<String?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var loaded by mutableStateOf(false)
        private set
    var saved by mutableStateOf(false)
        private set
    var studentInput by mutableStateOf(StudentInput())
        private set
    var subjectInput by mutableStateOf(SubjectInput())
        private set
    private var initialized = false

    val students = repository.students.catch { failure ->
        if (failure is CancellationException) throw failure
        error = "Unable to load students. Please go back and try again."
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val subjects = repository.subjects.catch { failure ->
        if (failure is CancellationException) throw failure
        error = "Unable to load subjects. Please go back and try again."
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun editStudent(input: StudentInput) { studentInput = input; error = null }
    fun editSubject(input: SubjectInput) { subjectInput = input; error = null }
    fun clearError() { error = null }

    fun loadStudent(id: Int) = initialize {
        if (id != 0) studentInput = repository.loadStudent(id)
    }

    fun loadSubject(id: Int) = initialize {
        if (id != 0) subjectInput = repository.loadSubject(id)
    }

    private fun initialize(load: suspend () -> Unit) {
        if (initialized) return
        initialized = true
        perform { load(); loaded = true }
    }

    fun saveStudent(id: Int) = perform {
        repository.saveStudent(id, studentInput)
        studentInput = studentInput.copy(password = "")
        saved = true
    }

    fun saveSubject(id: Int) = perform { repository.saveSubject(id, subjectInput); saved = true }
    fun deleteStudent(id: Int) = perform { repository.deleteStudent(id) }
    fun deleteSubject(id: Int) = perform { repository.deleteSubject(id) }

    private fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: IllegalArgumentException) {
                error = failure.message
            } catch (_: SQLiteConstraintException) {
                error = "Code or username already exists, or related data has changed."
            } catch (_: Exception) {
                error = "Unable to save changes. Please try again."
            } finally {
                busy = false
            }
        }
    }
}
