package com.group6.group6application.activities.navigation

object Routes {
    const val LOGIN = "login"

    const val ADMIN_HOME = "admin_home"
    const val ADMIN_STUDENTS = "admin_students"
    const val ADMIN_STUDENT_FORM = "admin_student_form/{id}"
    const val ADMIN_SUBJECTS = "admin_subjects"
    const val ADMIN_SUBJECT_FORM = "admin_subject_form/{id}"

    const val STUDENT_HOME = "student_home"
    const val STUDENT_SCHEDULES = "student_schedules"
    const val STUDENT_SCHEDULE_FORM = "student_schedule_form/{id}"

    const val STUDENT_CALENDAR = "student_calendar"

    fun adminStudentForm(id: Int = 0) = "admin_student_form/$id"
    fun adminSubjectForm(id: Int = 0) = "admin_subject_form/$id"
    fun studentScheduleForm(id: Int = 0) = "student_schedule_form/$id"
}
