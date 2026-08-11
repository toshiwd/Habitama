package com.habitama.app.ui

import com.habitama.app.data.GoalDraft
import com.habitama.app.data.GrowthType
import com.habitama.app.domain.GoalEvaluationMode

internal data class GoalTemplateCategory(val id: String, val label: String)

internal data class GoalTemplate(
    val draft: GoalDraft,
    val subtitle: String,
    val categoryId: String,
    val recommended: Boolean = false,
)

internal val templateCategories = listOf(
    GoalTemplateCategory("recommended", "おすすめ"),
    GoalTemplateCategory("exercise", "運動"),
    GoalTemplateCategory("health", "健康"),
    GoalTemplateCategory("study", "学び"),
    GoalTemplateCategory("life", "暮らし"),
    GoalTemplateCategory("money", "お金"),
)

internal val goalTemplates = listOf(
    GoalTemplate(GoalDraft("6,000歩あるく", 6_000, "歩", GrowthType.VITALITY, "walk"), "毎日の歩数を積み重ねる", "exercise", true),
    GoalTemplate(GoalDraft("筋トレを20回する", 20, "回", GrowthType.VITALITY, "fitness"), "腕立て・腹筋などの合計", "exercise", true),
    GoalTemplate(GoalDraft("10分ストレッチする", 10, "分", GrowthType.RECOVERY, "stretch"), "からだをゆっくり整える", "exercise"),
    GoalTemplate(GoalDraft("7時間眠る", 7, "時間", GrowthType.RECOVERY, "sleep"), "睡眠時間を記録する", "health", true),
    GoalTemplate(GoalDraft("水を8杯飲む", 8, "杯", GrowthType.RECOVERY, "water"), "水分補給を忘れない", "health"),
    GoalTemplate(
        GoalDraft("間食を200kcal以内にする", 200, "kcal", GrowthType.DISCIPLINE, "food", GoalEvaluationMode.AT_MOST),
        "摂取カロリーの上限を決める",
        "health",
        true,
    ),
    GoalTemplate(GoalDraft("30分勉強する", 30, "分", GrowthType.INTELLIGENCE, "study"), "好きなテーマを学ぶ", "study", true),
    GoalTemplate(GoalDraft("単語を5個おぼえる", 5, "個", GrowthType.INTELLIGENCE, "book"), "小さく学びを積み重ねる", "study"),
    GoalTemplate(GoalDraft("本を10ページ読む", 10, "ページ", GrowthType.INTELLIGENCE, "book"), "読書を毎日の習慣にする", "study"),
    GoalTemplate(GoalDraft("10分片づける", 10, "分", GrowthType.DISCIPLINE, "clean"), "暮らしを少しずつ整える", "life", true),
    GoalTemplate(GoalDraft("5分瞑想する", 5, "分", GrowthType.RECOVERY, "meditate"), "静かな時間をつくる", "life"),
    GoalTemplate(GoalDraft("日記を1回書く", 1, "回", GrowthType.BEAUTY, "journal"), "一日を短く振り返る", "life"),
    GoalTemplate(GoalDraft("1,000円貯金する", 1_000, "円", GrowthType.DISCIPLINE, "saving"), "少額から貯める習慣", "money", true),
    GoalTemplate(
        GoalDraft("1日の支出を3,000円以内にする", 3_000, "円", GrowthType.DISCIPLINE, "wallet", GoalEvaluationMode.AT_MOST),
        "使いすぎをゆるやかに防ぐ",
        "money",
    ),
)

internal val commonGoalUnits = listOf(
    "回", "分", "時間", "歩", "個", "ページ", "kcal", "g", "kg", "ml", "L", "杯", "円", "日",
)

internal fun templatesFor(categoryId: String): List<GoalTemplate> =
    if (categoryId == "recommended") goalTemplates.filter { it.recommended } else goalTemplates.filter { it.categoryId == categoryId }
