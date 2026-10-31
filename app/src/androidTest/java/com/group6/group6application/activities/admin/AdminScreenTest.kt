package com.group6.group6application.activities.admin

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.group6.group6application.activities.theme.Group6ApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun homeOpensBothManagementSectionsAndLogsOut() {
        var action = ""
        compose.setContent {
            Group6ApplicationTheme {
                AdminHomeScreen({ action = "students" }, { action = "subjects" }, { action = "logout" })
            }
        }
        compose.onNodeWithText("Student Management").performClick()
        compose.runOnIdle { assertEquals("students", action) }
        compose.onNodeWithText("Subject Management").performClick()
        compose.runOnIdle { assertEquals("subjects", action) }
        compose.onNodeWithText("Logout").performClick()
        compose.runOnIdle { assertEquals("logout", action) }
    }

    @Test
    fun listSearchSupportsVietnameseWithoutAccentsAndEmptyResults() {
        showList()
        compose.onNodeWithText("Search").performTextInput("dang")
        compose.onNodeWithText("Đặng Thị Ánh").assertIsDisplayed()
        compose.onNodeWithText("Nguyễn Văn An").assertDoesNotExist()
        compose.onNodeWithText("Search").performTextReplacement("missing")
        compose.onNodeWithText("No matching results found.").assertIsDisplayed()
    }

    @Test
    fun deletionNeedsConfirmationAndDetailsCanOpenEditor() {
        var deleted = 0
        var edited = 0
        showList(onDelete = { deleted = it }, onEdit = { edited = it })
        compose.onAllNodesWithText("Delete")[0].performClick()
        compose.runOnIdle { assertEquals(0, deleted) }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals(0, deleted) }
        compose.onAllNodesWithText("Details")[0].performClick()
        compose.onAllNodesWithText("Edit").filterToOne(hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle { assertEquals(1, edited) }
        compose.onAllNodesWithText("Delete")[0].performClick()
        compose.onAllNodesWithText("Delete").filterToOne(hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle { assertEquals(1, deleted) }
    }

    private fun showList(onDelete: (Int) -> Unit = {}, onEdit: (Int) -> Unit = {}) {
        compose.setContent {
            var query by remember { mutableStateOf("") }
            Group6ApplicationTheme {
                AdminListContent(
                    title = "Students", records = listOf(
                        AdminRecord(1, "Nguyễn Văn An", "SV001", listOf("Email" to "an@example.com")),
                        AdminRecord(2, "Đặng Thị Ánh", "SV002", listOf("Email" to "anh@example.com")),
                    ), query = query, onQuery = { query = it }, searchHint = "Code or name",
                    busy = false, error = null, deleteWarning = "Deletes related account and study schedules.",
                    onBack = {}, onAdd = {}, onEdit = onEdit, onDelete = onDelete,
                )
            }
        }
    }
}
