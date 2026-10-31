package com.group6.group6application.activities.admin

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.group6.group6application.activities.theme.Group6ApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminActivityTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun homeOpensBothManagementSectionsAndLogsOut() {
        var action = ""
        compose.setContent {
            Group6ApplicationTheme {
                AdminHomeActivity({ action = "students" }, { action = "subjects" }, { action = "logout" })
            }
        }
        compose.onNodeWithText("Quản lý sinh viên").performClick()
        compose.runOnIdle { assertEquals("students", action) }
        compose.onNodeWithText("Quản lý môn học").performClick()
        compose.runOnIdle { assertEquals("subjects", action) }
        compose.onNodeWithText("Đăng xuất").performClick()
        compose.runOnIdle { assertEquals("logout", action) }
    }

    @Test
    fun listSearchSupportsVietnameseWithoutAccentsAndEmptyResults() {
        showList()
        compose.onNodeWithText("Tìm kiếm").performTextInput("dang")
        compose.onNodeWithText("Đặng Thị Ánh").assertIsDisplayed()
        compose.onNodeWithText("Nguyễn Văn An").assertDoesNotExist()
        compose.onNodeWithText("Tìm kiếm").performTextReplacement("missing")
        compose.onNodeWithText("Không tìm thấy kết quả phù hợp.").assertIsDisplayed()
    }

    @Test
    fun deletionNeedsConfirmationAndDetailsCanOpenEditor() {
        var deleted = 0
        var edited = 0
        showList(onDelete = { deleted = it }, onEdit = { edited = it })
        compose.onAllNodesWithText("Xóa")[0].performClick()
        compose.runOnIdle { assertEquals(0, deleted) }
        compose.onNodeWithText("Hủy").performClick()
        compose.runOnIdle { assertEquals(0, deleted) }
        compose.onAllNodesWithText("Chi tiết")[0].performClick()
        compose.onAllNodesWithText("Sửa").filterToOne(hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle { assertEquals(1, edited) }
        compose.onAllNodesWithText("Xóa")[0].performClick()
        compose.onAllNodesWithText("Xóa").filterToOne(hasAnyAncestor(isDialog())).performClick()
        compose.runOnIdle { assertEquals(1, deleted) }
    }

    private fun showList(onDelete: (Int) -> Unit = {}, onEdit: (Int) -> Unit = {}) {
        compose.setContent {
            var query by remember { mutableStateOf("") }
            Group6ApplicationTheme {
                AdminListContent(
                    title = "Sinh viên", records = listOf(
                        AdminRecord(1, "Nguyễn Văn An", "SV001", listOf("Email" to "an@example.com")),
                        AdminRecord(2, "Đặng Thị Ánh", "SV002", listOf("Email" to "anh@example.com")),
                    ), query = query, onQuery = { query = it }, searchHint = "Mã hoặc họ tên",
                    busy = false, error = null, deleteWarning = "Xóa cả tài khoản và lịch học liên quan.",
                    onBack = {}, onAdd = {}, onEdit = onEdit, onDelete = onDelete,
                )
            }
        }
    }
}

