package com.group6.group6application.ui.student

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.group6.group6application.ui.navigation.PlaceholderScreen
import com.group6.group6application.ui.navigation.Routes
import com.group6.group6application.ui.student.calendar.CalendarScreen

// Người 3: replace the PlaceholderScreens with real screens in ui/student/schedule.
// Logged-in student: SessionManager(context).studentId
fun NavGraphBuilder.studentGraph(navController: NavHostController, onLogout: () -> Unit) {
    composable(Routes.STUDENT_HOME) {
        PlaceholderScreen(
            "Student Home",
            "My schedules" to { navController.navigate(Routes.STUDENT_SCHEDULES) },
            "Book a session" to { navController.navigate(Routes.studentScheduleForm()) },
            "Calendar" to { navController.navigate(Routes.STUDENT_CALENDAR) },
            "Logout" to onLogout,
        )
    }
    composable(Routes.STUDENT_SCHEDULES) {
        PlaceholderScreen("My schedules", "Book" to { navController.navigate(Routes.studentScheduleForm()) })
    }
    composable(Routes.STUDENT_SCHEDULE_FORM, listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        PlaceholderScreen("Schedule form (id=$id)", "Back" to { navController.popBackStack() })
    }
    // Người 4 owns CalendarScreen
    composable(Routes.STUDENT_CALENDAR) {
        CalendarScreen(onBack = { navController.popBackStack() })
    }
}
