package com.group6.group6application.ui.navigation

/** All routes of the app. Form routes take an id argument: 0 = create new, otherwise edit. */
object Routes {
    const val LOGIN = "login"

    // Người 2 — Admin
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_STUDENTS = "admin_students"
    const val ADMIN_STUDENT_FORM = "admin_student_form/{id}"
    const val ADMIN_SUBJECTS = "admin_subjects"
    const val ADMIN_SUBJECT_FORM = "admin_subject_form/{id}"

    // Người 3 — Student schedule
    const val STUDENT_HOME = "student_home"
    const val STUDENT_SCHEDULES = "student_schedules"
    const val STUDENT_SCHEDULE_FORM = "student_schedule_form/{id}"

    // Người 4 — Calendar
    const val STUDENT_CALENDAR = "student_calendar"

    fun adminStudentForm(id: Int = 0) = "admin_student_form/$id"
    fun adminSubjectForm(id: Int = 0) = "admin_subject_form/$id"
    fun studentScheduleForm(id: Int = 0) = "student_schedule_form/$id"
}
