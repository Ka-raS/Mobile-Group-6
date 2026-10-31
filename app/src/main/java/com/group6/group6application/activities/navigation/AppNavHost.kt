package com.group6.group6application.activities.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Role
import com.group6.group6application.activities.adminGraph
import com.group6.group6application.activities.LoginActivity
import com.group6.group6application.activities.studentGraph

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val session = SessionManager(LocalContext.current)
    val start = when {
        !session.isLoggedIn -> Routes.LOGIN
        session.role == Role.ADMIN -> Routes.ADMIN_HOME
        else -> Routes.STUDENT_HOME
    }
    val logout: () -> Unit = {
        session.logout()
        navController.navigate(Routes.LOGIN) { popUpTo(0) }
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
