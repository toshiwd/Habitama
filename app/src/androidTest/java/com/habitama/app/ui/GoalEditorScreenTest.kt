package com.habitama.app.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.habitama.app.ui.theme.HabitamaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GoalEditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun examplePopulatesFieldsAndCanBeSaved() {
        var savedTitle = ""
        var savedTarget = 0L
        var savedUnit = ""
        composeRule.setContent {
            HabitamaTheme {
                GoalEditorScreen(
                    title = "最初の行動を決めよう",
                    description = "説明",
                    initialGoal = null,
                    pendingLabel = null,
                    errorMessage = null,
                    onClearError = {},
                    onBack = null,
                    onSave = { title, target, unit ->
                        savedTitle = title
                        savedTarget = target
                        savedUnit = unit
                    },
                )
            }
        }

        composeRule.onNodeWithText("20分").performClick()
        composeRule.onNodeWithTag("goal_title").assertTextContains("運動する")
        composeRule.onNodeWithTag("goal_target").assertTextContains("20")
        composeRule.onNodeWithTag("goal_unit").assertTextContains("分")
        composeRule.onNodeWithTag("save_goal").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals("運動する", savedTitle)
            assertEquals(20L, savedTarget)
            assertEquals("分", savedUnit)
        }
    }
}
