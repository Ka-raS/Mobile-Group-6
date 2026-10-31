package com.group6.group6application.activities.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Role
import com.group6.group6application.notification.NotificationScheduler
import com.group6.group6application.notification.StudentNotificationSetup
import com.group6.group6application.activities.adminGraph
import com.group6.group6application.activities.LoginActivity
import com.group6.group6application.activities.studentGraph

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val session = remember(context) { SessionManager(context) }
    val start = remember {
        when {
            !session.isLoggedIn -> Routes.LOGIN
            session.role == Role.ADMIN -> Routes.ADMIN_HOME
            else -> Routes.STUDENT_HOME
        }
    }
    val logout: () -> Unit = {
        session.logout()
        NotificationScheduler.cancelAll(context)
        navController.navigate(Routes.LOGIN) { popUpTo(0) }
    }

    val entry by navController.currentBackStackEntryAsState()
    if (entry?.destination?.route?.startsWith("student_") == true &&
        session.isLoggedIn && session.role == Role.STUDENT && session.studentId > 0
    ) {
        StudentNotificationSetup(session.studentId)
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginActivity(onLoggedIn = { role ->
                val home = if (role == Role.ADMIN) Routes.ADMIN_HOME else Routes.STUDENT_HOME
                navController.navigate(home) { popUpTo(0) }
            })
        }
        adminGraph(navController, logout)
        studentGraph(navController, logout)
    }
}
