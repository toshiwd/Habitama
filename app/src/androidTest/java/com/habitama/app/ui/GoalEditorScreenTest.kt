package com.habitama.app.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.habitama.app.data.GoalDraft
import com.habitama.app.ui.theme.HabitamaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GoalEditorScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun templatePopulatesFieldsAndCanBeSaved() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            HabitamaTheme {
                GoalEditorScreen(
                    heading = "行動を追加",
                    description = "説明",
                    initialGoal = null,
                    saveLabel = "保存",
                    errorMessage = null,
                    onClearError = {},
                    onBack = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithTag("template_book").performClick()
        composeRule.onNodeWithTag("goal_title").assertTextContains("単語を5個おぼえる")
        composeRule.onNodeWithTag("goal_target").assertTextContains("5")
        composeRule.onNodeWithTag("goal_unit").assertTextContains("個")
        composeRule.onNodeWithTag("save_goal").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals("単語を5個おぼえる", saved?.title)
            assertEquals(5L, saved?.targetValue)
            assertEquals("個", saved?.unit)
        }
    }
}
