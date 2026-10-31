package com.group6.group6application.activities

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.group6.group6application.activities.admin.AdminHomeActivity
import com.group6.group6application.activities.admin.student.StudentFormActivity
import com.group6.group6application.activities.admin.student.StudentListActivity
import com.group6.group6application.activities.admin.subject.SubjectFormActivity
import com.group6.group6application.activities.admin.subject.SubjectListActivity
import com.group6.group6application.activities.navigation.Routes

fun NavGraphBuilder.adminGraph(navController: NavHostController, onLogout: () -> Unit) {
    composable(Routes.ADMIN_HOME) {
        AdminHomeActivity(
            onStudents = { navController.navigate(Routes.ADMIN_STUDENTS) },
            onSubjects = { navController.navigate(Routes.ADMIN_SUBJECTS) },
            onLogout = onLogout,
        )
    }
    composable(Routes.ADMIN_STUDENTS) {
        StudentListActivity(
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(Routes.adminStudentForm()) },
            onEdit = { navController.navigate(Routes.adminStudentForm(it)) },
        )
    }
    composable(Routes.ADMIN_STUDENT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        StudentFormActivity(id = id, onBack = { navController.popBackStack() })
    }
    composable(Routes.ADMIN_SUBJECTS) {
        SubjectListActivity(
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(Routes.adminSubjectForm()) },
            onEdit = { navController.navigate(Routes.adminSubjectForm(it)) },
        )
    }
    composable(Routes.ADMIN_SUBJECT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        SubjectFormActivity(id = id, onBack = { navController.popBackStack() })
    }
}
