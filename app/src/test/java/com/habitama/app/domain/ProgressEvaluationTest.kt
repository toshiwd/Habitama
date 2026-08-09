package com.habitama.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProgressEvaluationTest {
    @Test
    fun zeroProgressProducesNoEnergy() {
        val result = evaluateProgress(actual = 0, target = 6_000)
        assertEquals(0, result.displayPercentage)
        assertEquals(0.0, result.evaluationScore, 0.0001)
        assertEquals(0, result.energyEarned)
    }

    @Test
    fun partialProgressIsPreserved() {
        val result = evaluateProgress(actual = 5_000, target = 6_000)
        assertEquals(83, result.displayPercentage)
        assertEquals(0.8333, result.evaluationScore, 0.0001)
        assertEquals(8, result.energyEarned)
    }

    @Test
    fun targetProducesFullBaseScore() {
        val result = evaluateProgress(actual = 6_000, target = 6_000)
        assertEquals(100, result.displayPercentage)
        assertEquals(1.0, result.evaluationScore, 0.0001)
        assertEquals(10, result.energyEarned)
    }

    @Test
    fun overachievementCapsEvaluationAtOnePointTwo() {
        val capped = evaluateProgress(actual = 9_000, target = 6_000)
        val beyondCap = evaluateProgress(actual = 10_000, target = 6_000)
        assertEquals(150, capped.displayPercentage)
        assertEquals(1.2, capped.evaluationScore, 0.0001)
        assertEquals(167, beyondCap.displayPercentage)
        assertEquals(1.2, beyondCap.evaluationScore, 0.0001)
        assertEquals(12, beyondCap.energyEarned)
    }

    @Test
    fun invalidBoundsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { evaluateProgress(1, 0) }
        assertThrows(IllegalArgumentException::class.java) { evaluateProgress(-1, 1) }
        assertThrows(IllegalArgumentException::class.java) { evaluateProgress(MAX_INPUT_VALUE + 1, 1) }
        assertThrows(IllegalArgumentException::class.java) { evaluateProgress(1, MAX_INPUT_VALUE + 1) }
    }
}
