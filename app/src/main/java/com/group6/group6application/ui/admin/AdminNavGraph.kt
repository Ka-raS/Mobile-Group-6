package com.group6.group6application.ui.admin

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.group6.group6application.ui.navigation.PlaceholderScreen
import com.group6.group6application.ui.navigation.Routes

fun NavGraphBuilder.adminGraph(navController: NavHostController, onLogout: () -> Unit) {
    composable(Routes.ADMIN_HOME) {
        PlaceholderScreen(
            "Admin Home",
            "Students" to { navController.navigate(Routes.ADMIN_STUDENTS) },
            "Subjects" to { navController.navigate(Routes.ADMIN_SUBJECTS) },
            "Logout" to onLogout,
        )
    }
    composable(Routes.ADMIN_STUDENTS) {
        PlaceholderScreen("Students", "Add" to { navController.navigate(Routes.adminStudentForm()) })
    }
    composable(Routes.ADMIN_STUDENT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        PlaceholderScreen("Student form (id=$id)", "Back" to { navController.popBackStack() })
    }
    composable(Routes.ADMIN_SUBJECTS) {
        PlaceholderScreen("Subjects", "Add" to { navController.navigate(Routes.adminSubjectForm()) })
    }
    composable(Routes.ADMIN_SUBJECT_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        PlaceholderScreen("Subject form (id=$id)", "Back" to { navController.popBackStack() })
    }
}
