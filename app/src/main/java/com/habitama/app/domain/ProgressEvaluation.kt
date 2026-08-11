package com.habitama.app.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

const val MAX_INPUT_VALUE: Long = 999_999_999L

object GoalEvaluationMode {
    const val AT_LEAST = "AT_LEAST"
    const val AT_MOST = "AT_MOST"
    val all = setOf(AT_LEAST, AT_MOST)
}

data class ProgressEvaluation(
    val rawProgress: Double,
    val displayPercentage: Int,
    val evaluationScore: Double,
    val energyEarned: Int,
)

fun evaluateProgress(
    actual: Long,
    target: Long,
    mode: String = GoalEvaluationMode.AT_LEAST,
): ProgressEvaluation {
    require(target in 1..MAX_INPUT_VALUE) { "目標値は1〜${MAX_INPUT_VALUE}で入力してください" }
    require(actual in 0..MAX_INPUT_VALUE) { "実績値は0〜${MAX_INPUT_VALUE}で入力してください" }
    require(mode in GoalEvaluationMode.all) { "評価方法が不正です" }

    val rawProgress: Double
    val evaluationScore: Double
    if (mode == GoalEvaluationMode.AT_MOST) {
        rawProgress = if (actual <= target) 1.0 else target.toDouble() / actual.toDouble()
        evaluationScore = rawProgress
    } else {
        rawProgress = actual.toDouble() / target.toDouble()
        val base = min(rawProgress, 1.0)
        val overBonus = max(0.0, min(rawProgress - 1.0, 0.5)) * 0.4
        evaluationScore = base + overBonus
    }

    return ProgressEvaluation(
        rawProgress = rawProgress,
        displayPercentage = (rawProgress * 100.0).roundToInt(),
        evaluationScore = evaluationScore,
        energyEarned = (10.0 * evaluationScore).roundToInt(),
    )
}
