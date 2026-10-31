package com.group6.group6application.activities

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.group6.group6application.activities.navigation.AppNavHost
import com.group6.group6application.activities.theme.Group6ApplicationTheme
import com.group6.group6application.dal.SessionManager
import com.group6.group6application.entities.Account
import com.group6.group6application.entities.Role
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActivitiesNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val isolatedContext = object : ContextWrapper(appContext) {
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            appContext.getSharedPreferences("activities-navigation-test-$name", mode)
    }

    @Before
    fun setUp() {
        // Keep the real session untouched, suppress permission UI, and query an
        // unused student ID so this navigation test never writes real schedules.
        SessionManager(isolatedContext).save(Account(2_000_000_000, "nav-test", "test", Role.STUDENT), 2_000_000_000)
        isolatedContext.getSharedPreferences("notification-settings", Context.MODE_PRIVATE)
            .edit().putBoolean("permission-requested", true).commit()
    }

    @After
    fun tearDown() {
        SessionManager(isolatedContext).logout()
        isolatedContext.getSharedPreferences("notification-settings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun activitiesGraphOpensImplementedCalendarAndReturnsToStudentHome() {
        compose.setContent {
            val registryOwner = checkNotNull(LocalActivityResultRegistryOwner.current)
            CompositionLocalProvider(
                LocalContext provides isolatedContext,
                LocalActivityResultRegistryOwner provides registryOwner,
            ) {
                Group6ApplicationTheme(dynamicColor = false) { AppNavHost() }
            }
        }
        compose.onNodeWithText("Student Home").assertIsDisplayed()
        compose.onNodeWithText("Calendar").performClick()
        compose.onNodeWithText("Lịch học").assertIsDisplayed()
        compose.onNodeWithText("Ngày").assertIsDisplayed()
        compose.onNodeWithText("Tuần").assertIsDisplayed()
        compose.onNodeWithText("Tháng").assertIsDisplayed()
        compose.onNodeWithText("Quay lại").performClick()
        compose.onNodeWithText("Student Home").assertIsDisplayed()
    }
}
