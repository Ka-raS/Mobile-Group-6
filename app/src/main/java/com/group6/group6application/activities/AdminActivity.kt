package com.group6.group6application.activities

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.group6.group6application.activities.admin.AdminHomeScreen
import com.group6.group6application.activities.admin.student.StudentFormScreen
import com.group6.group6application.activities.admin.student.StudentListScreen
import com.group6.group6application.activities.admin.subject.SubjectFormScreen
import com.group6.group6application.activities.admin.subject.SubjectListScreen
import com.group6.group6application.activities.navigation.Routes

fun NavGraphBuilder.adminGraph(navController: NavHostController, onLogout: () -> Unit) {
    composable(Routes.ADMIN_HOME) {
        AdminHomeScreen(
            onStudents = { navController.navigate(Routes.ADMIN_STUDENTS) },
            onSubjects = { navController.navigate(Routes.ADMIN_SUBJECTS) },
            onLogout = onLogout,
        )
    }
    composable(Routes.ADMIN_STUDENTS) {
        StudentListScreen(
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(Routes.adminStudentForm()) },
            onEdit = { navController.navigate(Routes.adminStudentForm(it)) },
        )
    }
    composable(Routes.ADMIN_STUDENT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        StudentFormScreen(id = id, onBack = { navController.popBackStack() })
    }
    composable(Routes.ADMIN_SUBJECTS) {
        SubjectListScreen(
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(Routes.adminSubjectForm()) },
            onEdit = { navController.navigate(Routes.adminSubjectForm(it)) },
        )
    }
    composable(Routes.ADMIN_SUBJECT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        SubjectFormScreen(id = id, onBack = { navController.popBackStack() })
    }
}
