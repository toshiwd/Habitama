package com.habitama.app.ui

import com.habitama.app.domain.GoalEvaluationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalTemplatesTest {
    @Test
    fun everyCategoryHasTemplatesAndCommonUnitsCoverRequestedExamples() {
        templateCategories.filterNot { it.id == "recommended" }.forEach { category ->
            assertTrue("${category.label}にサンプルが必要です", templatesFor(category.id).isNotEmpty())
        }
        assertTrue(commonGoalUnits.containsAll(listOf("回", "分", "時間", "歩", "ページ", "kcal", "円")))
    }

    @Test
    fun calorieLimitAndSavingsTemplatesUseCorrectUnitsAndModes() {
        val calorie = goalTemplates.single { it.draft.icon == "food" }.draft
        val savings = goalTemplates.single { it.draft.icon == "saving" }.draft

        assertEquals("kcal", calorie.unit)
        assertEquals(GoalEvaluationMode.AT_MOST, calorie.evaluationMode)
        assertEquals("円", savings.unit)
        assertEquals(GoalEvaluationMode.AT_LEAST, savings.evaluationMode)
    }
}
