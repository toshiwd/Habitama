package com.habitama.app.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.habitama.app.data.GoalDraft
import com.habitama.app.ui.theme.HabitamaTheme
import com.habitama.app.domain.GoalEvaluationMode
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

        composeRule.onNodeWithTag("category_study").performClick()
        composeRule.onNodeWithTag("template_study").performClick()
        composeRule.onNodeWithTag("goal_title").assertTextContains("30分勉強する")
        composeRule.onNodeWithTag("goal_target").assertTextContains("30")
        composeRule.onNodeWithTag("goal_unit").assertTextContains("分")
        composeRule.onNodeWithTag("save_goal").performScrollTo().assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals("30分勉強する", saved?.title)
            assertEquals(30L, saved?.targetValue)
            assertEquals("分", saved?.unit)
        }
    }

    @Test
    fun calorieTemplateSelectsKcalAndAtMostEvaluation() {
        var saved: GoalDraft? = null
        composeRule.setContent {
            HabitamaTheme {
                GoalEditorScreen("行動を追加", "説明", null, "保存", null, {}, {}, { saved = it })
            }
        }

        composeRule.onNodeWithTag("category_health").performClick()
        composeRule.onNodeWithTag("template_food").performClick()
        composeRule.onNodeWithTag("goal_unit").assertTextContains("kcal")
        composeRule.onNodeWithTag("mode_at_most").assertIsSelected()
        composeRule.onNodeWithTag("save_goal").performScrollTo().assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals(200L, saved?.targetValue)
            assertEquals("kcal", saved?.unit)
            assertEquals(GoalEvaluationMode.AT_MOST, saved?.evaluationMode)
        }
    }
}
