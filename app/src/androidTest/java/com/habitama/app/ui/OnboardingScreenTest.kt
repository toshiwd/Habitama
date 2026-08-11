package com.habitama.app.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.habitama.app.data.GoalDraft
import com.habitama.app.ui.theme.HabitamaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun customGoalCanBeCreatedAndUsedToStart() {
        var saved: List<GoalDraft>? = null
        composeRule.setContent {
            HabitamaTheme {
                OnboardingScreen(
                    errorMessage = null,
                    onClearError = {},
                    onSave = { saved = it },
                )
            }
        }

        composeRule.onNodeWithTag("save_goal").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithTag("create_custom_goal").performScrollTo().performClick()
        composeRule.onNodeWithTag("goal_title").performTextInput("朝に水を飲む")
        composeRule.onNodeWithTag("goal_target").performTextInput("2")
        composeRule.onNodeWithTag("goal_unit").performTextInput("杯")
        composeRule.onNodeWithTag("save_goal").performScrollTo().assertIsEnabled().performClick()

        composeRule.onNodeWithText("朝に水を飲む").assertExists()
        composeRule.onNodeWithTag("save_goal").performScrollTo().assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals(1, saved?.size)
            assertEquals("朝に水を飲む", saved?.single()?.title)
            assertEquals(2L, saved?.single()?.targetValue)
            assertEquals("杯", saved?.single()?.unit)
        }
    }

    @Test
    fun settingsButtonInvokesNavigation() {
        var clicked = false
        composeRule.setContent {
            HabitamaTheme {
                AppHeader(title = "ホーム", onSettings = { clicked = true })
            }
        }

        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.runOnIdle { assertEquals(true, clicked) }
    }
}
